package io.github.thibaultbee.srtdroid

import io.github.thibaultbee.srtdroid.models.Socket
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.CoroutineContext

class CoroutineSocket(override val coroutineContext: CoroutineContext) : CoroutineScope {
    private val socket = Socket()

    suspend fun connect(ip: String, port: Int) = suspendCancellableCoroutine { continuation ->
        try {
            socket.connect(ip, port)
            continuation.resumeWith(Result.success(null))
        } catch (e: Exception) {
            continuation.resumeWith(Result.failure(e))
        }
    }

    suspend fun send(message: String) = suspendCancellableCoroutine { continuation ->
        try {
            socket.send(message)
            continuation.resumeWith(Result.success(null))
        } catch (e: Exception) {
            continuation.resumeWith(Result.failure(e))
        }
    }
}