package com.yayapay.engine.server.routes

import com.yayapay.engine.data.local.db.ApiKeyDao
import com.yayapay.engine.data.model.ApiKeyEntity
import com.yayapay.engine.server.auth.ApiKeyAuth
import com.yayapay.engine.server.dto.HealthResponse
import com.yayapay.engine.util.IdGenerator
import com.yayapay.engine.wallet.WalletRegistry
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import java.net.NetworkInterface

fun Route.healthRoutes(
    walletRegistry: WalletRegistry,
    serverPort: Int,
    startTime: Long,
    apiKeyDao: ApiKeyDao? = null,
    idGenerator: IdGenerator? = null
) {
    get("/health") {
        call.respond(HealthResponse(
            version = "0.1.0",
            uptime = System.currentTimeMillis() - startTime,
            serverPort = serverPort,
            activeWallets = walletRegistry.getAllProviders().size,
            localIp = getLocalIpAddress()
        ))
    }

    // Bootstrap endpoint: create first API key (only works if no keys exist)
    if (apiKeyDao != null && idGenerator != null) {
        post("/bootstrap") {
            val count = apiKeyDao.countActive()
            if (count > 0) {
                call.respond(mapOf("error" to "API keys already exist. Use the app to manage keys."))
                return@post
            }
            val rawKey = idGenerator.apiKeySecret()
            val hash = ApiKeyAuth.hashKey(rawKey)
            apiKeyDao.insert(ApiKeyEntity(
                id = idGenerator.apiKeyId(),
                name = "Bootstrap Key",
                keyHash = hash,
                keyPrefix = rawKey.take(12) + "..."
            ))
            call.respond(mapOf(
                "apiKey" to rawKey,
                "message" to "Save this key! It will not be shown again."
            ))
        }
    }
}

private fun getLocalIpAddress(): String? {
    return try {
        NetworkInterface.getNetworkInterfaces()?.asSequence()
            ?.flatMap { it.inetAddresses.asSequence() }
            ?.filter { !it.isLoopbackAddress && it is java.net.Inet4Address }
            ?.firstOrNull()?.hostAddress
    } catch (_: Exception) {
        null
    }
}
