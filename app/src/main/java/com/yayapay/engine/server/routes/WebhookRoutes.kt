package com.yayapay.engine.server.routes

import com.yayapay.engine.data.local.db.WebhookEndpointDao
import com.yayapay.engine.data.model.WebhookEndpointEntity
import com.yayapay.engine.server.dto.CreateWebhookRequest
import com.yayapay.engine.server.dto.errorResponse
import com.yayapay.engine.server.dto.toResponse
import com.yayapay.engine.util.IdGenerator
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post

fun Route.webhookRoutes(
    webhookEndpointDao: WebhookEndpointDao,
    idGenerator: IdGenerator
) {
    post("/webhook_endpoints") {
        val req = call.receive<CreateWebhookRequest>()

        if (req.url.isBlank()) {
            call.respond(HttpStatusCode.BadRequest, errorResponse("URL is required"))
            return@post
        }
        if (req.enabledEvents.isEmpty()) {
            call.respond(HttpStatusCode.BadRequest, errorResponse("At least one event type is required"))
            return@post
        }

        val secret = idGenerator.webhookSecret()
        val endpoint = WebhookEndpointEntity(
            id = idGenerator.webhookEndpointId(),
            url = req.url,
            secret = secret,
            enabledEvents = req.enabledEvents.joinToString(","),
            description = req.description
        )

        webhookEndpointDao.insert(endpoint)
        call.respond(HttpStatusCode.Created, endpoint.toResponse(includeSecret = true))
    }

    get("/webhook_endpoints") {
        val endpoints = webhookEndpointDao.getAll()
        call.respond(endpoints.map { it.toResponse() })
    }

    delete("/webhook_endpoints/{id}") {
        val id = call.parameters["id"] ?: run {
            call.respond(HttpStatusCode.BadRequest, errorResponse("Missing id"))
            return@delete
        }
        webhookEndpointDao.delete(id)
        call.respond(HttpStatusCode.OK, mapOf("deleted" to true, "id" to id))
    }
}
