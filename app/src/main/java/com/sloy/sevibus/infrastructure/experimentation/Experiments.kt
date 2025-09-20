package com.sloy.sevibus.infrastructure.experimentation

import android.app.Application
import android.content.Context
import com.sloy.sevibus.infrastructure.BuildVariant
import com.sloy.sevibus.infrastructure.SevLogger
import com.sloy.sevibus.infrastructure.session.SessionService
import com.statsig.androidsdk.DynamicConfig
import com.statsig.androidsdk.EvaluationDetails
import com.statsig.androidsdk.EvaluationReason
import com.statsig.androidsdk.InitializationDetails
import com.statsig.androidsdk.Statsig
import com.statsig.androidsdk.StatsigOptions
import com.statsig.androidsdk.StatsigUser
import com.statsig.androidsdk.Tier
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class Experiments(private val context: Context, private val sessionService: SessionService) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val statsig = CompletableDeferred<Statsig?>()

    init {
        initSdk()
        monitorUserSession()
    }

    private fun initSdk() {
        scope.launch {
            val result: InitializationDetails? = Statsig.initialize(
                context.applicationContext as Application,
                if (BuildVariant.isRelease()) PROD_PUBLIC_KEY else DEV_PUBLIC_KEY,
                StatsigUser(sessionService.getCurrentUser()?.id),
                StatsigOptions().apply {
                    setTier(if (BuildVariant.isRelease()) Tier.PRODUCTION else Tier.DEVELOPMENT)
                }
            )
            when (result?.success) {
                true -> statsig.complete(Statsig)
                false -> {
                    SevLogger.logW(msg = "Statsig initialization failed: ${result.failureDetails}")
                    statsig.complete(null)
                }

                null -> statsig.complete(null)
            }
        }
    }

    suspend fun isFeatureEnabled(flag: FeatureFlag): Boolean {
        return statsig.await()?.checkGate(flag.flagName)?.also {
            SevLogger.logD("Statsig value for flag $flag=$it")
        } ?: false
    }

    suspend fun getExperiment(experiment: Experiment): ExperimentResult {
        val result: DynamicConfig = statsig.await()?.getExperiment(experiment.experimentName) ?: DynamicConfig(
            experiment.experimentName,
            EvaluationDetails(EvaluationReason.Error, lcut = 0)
        )
        return result.toExperimentResult().also {
            SevLogger.logD("Statsig value for experiment $experiment=$it")
        }

    }

    private fun monitorUserSession() {
        sessionService.observeCurrentUser()
            .distinctUntilChanged()
            .onEach { user ->
                statsig.await()?.updateUser(StatsigUser(user?.id))
                SevLogger.logD("Statsig user updated to ${user?.id}")
            }
            .launchIn(scope)
    }

    private companion object {
        const val DEV_PUBLIC_KEY = "client-6YHj5a1NAL1cWWUCebD9V0bZ9SGNjZl1V0IFhvuRnDl"
        const val PROD_PUBLIC_KEY = "client-qk2wMPLOG0EfGJ9Edmi9KbcX8NuFbf3hq2GlikUlSk4"
    }
}