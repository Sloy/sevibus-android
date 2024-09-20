package com.sloy.sevibus.infrastructure.location

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Looper
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Granularity
import com.google.android.gms.location.LastLocationRequest
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.Priority
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FusedLocationService(
    private val context: Context,
    private val client: FusedLocationProviderClient
) : LocationService {

    override fun requestLocationUpdates(): Flow<Location> = callbackFlow {
        if (!context.hasLocationPermission()) {
            return@callbackFlow
        }
        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.locations.lastOrNull()?.let {
                    trySend(it)
                }
            }
        }
        client.requestLocationUpdates(
            LocationRequest.Builder(5_000)
                .setWaitForAccurateLocation(false)
                .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
                .build(),
            locationCallback,
            Looper.getMainLooper() //TODO main or myLooper?
        )
        awaitClose {
            client.removeLocationUpdates(locationCallback)
        }
    }

    override suspend fun obtainCurrentLocation(): Location? {
        if (!context.hasLocationPermission()) {
            return null
        }
        return client.getLastLocation(LastLocationRequest.Builder().setGranularity(Granularity.GRANULARITY_FINE).build()).await()
    }
}

fun Context.hasLocationPermission(): Boolean {
    return ContextCompat.checkSelfPermission(
        this,
        Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
}
