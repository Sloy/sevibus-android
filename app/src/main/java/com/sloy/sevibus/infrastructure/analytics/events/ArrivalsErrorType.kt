package com.sloy.sevibus.infrastructure.analytics.events

import com.sloy.sevibus.infrastructure.analytics.events.Events.ArrivalsFailed.ErrorType
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException

fun Throwable.toArrivalsErrorType(): ErrorType = when (this) {
    is SocketTimeoutException -> ErrorType.TIMEOUT
    is IOException -> ErrorType.NETWORK
    is HttpException -> ErrorType.SERVER
    else -> ErrorType.UNKNOWN
}
