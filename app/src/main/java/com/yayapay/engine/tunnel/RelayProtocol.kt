package com.yayapay.engine.tunnel

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class RelayRequest(
    val requestId: String,
    val method: String,
    val path: String,
    val headers: Map<String, String> = emptyMap(),
    val body: String? = null
)

@Serializable
data class RelayResponse(
    val requestId: String,
    val statusCode: Int,
    val headers: Map<String, String> = emptyMap(),
    val body: String? = null
)

object RelayProtocol {
    private val json = Json { ignoreUnknownKeys = true }

    fun decodeRequest(text: String): RelayRequest = json.decodeFromString(text)
    fun encodeResponse(response: RelayResponse): String = json.encodeToString(response)
}
