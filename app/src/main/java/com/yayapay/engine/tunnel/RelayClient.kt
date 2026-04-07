package com.yayapay.engine.tunnel

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RelayClient @Inject constructor() {

    companion object {
        private const val TAG = "RelayClient"
        private const val MAX_RECONNECT_DELAY_MS = 60_000L
    }

    private var webSocket: WebSocket? = null
    private var reconnectAttempts = 0
    private var scope: CoroutineScope? = null
    private var localPort: Int = 8080
    var isConnected: Boolean = false
        private set

    private val client = OkHttpClient.Builder()
        .readTimeout(0, TimeUnit.SECONDS)
        .pingInterval(30, TimeUnit.SECONDS)
        .build()

    private val localClient = OkHttpClient.Builder()
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    fun connect(relayUrl: String, deviceId: String, apiKey: String, port: Int, scope: CoroutineScope) {
        if (relayUrl.isBlank()) return
        this.scope = scope
        this.localPort = port

        val request = Request.Builder()
            .url("$relayUrl/tunnel/$deviceId")
            .header("Authorization", "Bearer $apiKey")
            .build()

        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                isConnected = true
                reconnectAttempts = 0
                Log.i(TAG, "Connected to relay: $relayUrl")
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                scope.launch(Dispatchers.IO) {
                    handleRelayRequest(webSocket, text)
                }
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                isConnected = false
                Log.e(TAG, "Relay connection failed", t)
                scheduleReconnect(relayUrl, deviceId, apiKey, port)
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                isConnected = false
                Log.i(TAG, "Relay disconnected: $reason")
            }
        })
    }

    fun disconnect() {
        webSocket?.close(1000, "Client disconnect")
        webSocket = null
        isConnected = false
    }

    private suspend fun handleRelayRequest(ws: WebSocket, text: String) {
        try {
            val relayReq = RelayProtocol.decodeRequest(text)
            val localUrl = "http://127.0.0.1:$localPort${relayReq.path}"

            val requestBuilder = Request.Builder().url(localUrl)
            relayReq.headers.forEach { (k, v) -> requestBuilder.header(k, v) }

            when (relayReq.method.uppercase()) {
                "GET" -> requestBuilder.get()
                "POST" -> requestBuilder.post(
                    (relayReq.body ?: "").toRequestBody("application/json".toMediaType())
                )
                "DELETE" -> requestBuilder.delete(
                    relayReq.body?.toRequestBody("application/json".toMediaType())
                )
            }

            val response = localClient.newCall(requestBuilder.build()).execute()
            response.use {
                val relayResp = RelayResponse(
                    requestId = relayReq.requestId,
                    statusCode = it.code,
                    body = it.body?.string()
                )
                ws.send(RelayProtocol.encodeResponse(relayResp))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error handling relay request", e)
        }
    }

    private fun scheduleReconnect(relayUrl: String, deviceId: String, apiKey: String, port: Int) {
        val delayMs = minOf(
            1000L * (1L shl reconnectAttempts.coerceAtMost(6)),
            MAX_RECONNECT_DELAY_MS
        )
        reconnectAttempts++
        scope?.launch {
            delay(delayMs)
            connect(relayUrl, deviceId, apiKey, port, this)
        }
    }
}
