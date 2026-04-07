package com.yayapay.engine.server

import android.content.pm.PackageManager
import com.yayapay.engine.data.local.db.WebhookEndpointDao
import com.yayapay.engine.engine.PaymentIntentRepository
import com.yayapay.engine.server.auth.ApiKeyAuth
import com.yayapay.engine.server.auth.RateLimiter
import com.yayapay.engine.server.dto.errorResponse
import com.yayapay.engine.server.routes.healthRoutes
import com.yayapay.engine.server.routes.paymentIntentRoutes
import com.yayapay.engine.server.routes.walletRoutes
import com.yayapay.engine.server.routes.webhookRoutes
import com.yayapay.engine.util.IdGenerator
import com.yayapay.engine.wallet.WalletRegistry
import com.yayapay.engine.webhook.WebhookDispatcher
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.createRouteScopedPlugin
import io.ktor.server.application.install
import io.ktor.server.cio.CIO
import io.ktor.server.cio.CIOApplicationEngine
import io.ktor.server.engine.EmbeddedServer as KtorEmbeddedServer
import io.ktor.server.engine.embeddedServer
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.cors.routing.CORS
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.request.header
import io.ktor.server.response.respond
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EmbeddedServer @Inject constructor(
    private val apiKeyAuth: ApiKeyAuth,
    private val rateLimiter: RateLimiter,
    private val intentRepo: PaymentIntentRepository,
    private val walletRegistry: WalletRegistry,
    private val webhookDispatcher: WebhookDispatcher,
    private val webhookEndpointDao: WebhookEndpointDao,
    private val apiKeyDao: com.yayapay.engine.data.local.db.ApiKeyDao,
    private val idGenerator: IdGenerator
) {
    private var server: KtorEmbeddedServer<CIOApplicationEngine, CIOApplicationEngine.Configuration>? = null
    private var startTime: Long = 0
    private var port: Int = 8080
    var packageManager: PackageManager? = null

    fun start(port: Int = 8080) {
        if (server != null) return
        this.port = port
        this.startTime = System.currentTimeMillis()

        val apiKeyAuthRef = apiKeyAuth
        val rateLimiterRef = rateLimiter

        val ApiKeyAuthPlugin = createRouteScopedPlugin("ApiKeyAuth") {
            onCall { call ->
                val bearer = call.request.header("Authorization")?.removePrefix("Bearer ")
                if (bearer == null || !apiKeyAuthRef.validate(bearer)) {
                    call.respond(HttpStatusCode.Unauthorized, errorResponse("Invalid API key", "authentication_error"))
                }
                if (bearer != null && !rateLimiterRef.allowRequest(bearer)) {
                    call.respond(HttpStatusCode.TooManyRequests, errorResponse("Rate limit exceeded", "rate_limit_error"))
                }
            }
        }

        server = embeddedServer(CIO, port = port, host = "0.0.0.0") {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    encodeDefaults = true
                })
            }
            install(StatusPages) {
                exception<Throwable> { call, cause ->
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        errorResponse(cause.message ?: "Internal server error", "api_error")
                    )
                }
            }
            install(CORS) {
                anyHost()
                allowMethod(HttpMethod.Get)
                allowMethod(HttpMethod.Post)
                allowMethod(HttpMethod.Delete)
                allowHeader(HttpHeaders.Authorization)
                allowHeader(HttpHeaders.ContentType)
            }

            routing {
                // Health + bootstrap - no auth required
                healthRoutes(walletRegistry, this@EmbeddedServer.port, startTime, apiKeyDao, idGenerator)

                // All /v1 routes require API key
                route("/v1") {
                    install(ApiKeyAuthPlugin)
                    paymentIntentRoutes(intentRepo, walletRegistry, webhookDispatcher, idGenerator)
                    webhookRoutes(webhookEndpointDao, idGenerator)
                    walletRoutes(walletRegistry, packageManager)
                }
            }
        }.start(wait = false)
    }

    fun stop() {
        server?.stop(1000, 5000)
        server = null
    }

    fun isRunning(): Boolean = server != null
    fun getPort(): Int = port
}
