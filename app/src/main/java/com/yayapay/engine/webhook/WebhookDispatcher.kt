package com.yayapay.engine.webhook

import com.yayapay.engine.data.local.db.WebhookDeliveryDao
import com.yayapay.engine.data.local.db.WebhookEndpointDao
import com.yayapay.engine.data.model.PaymentIntentEntity
import com.yayapay.engine.data.model.WebhookDeliveryEntity
import com.yayapay.engine.data.model.WebhookEventType
import com.yayapay.engine.server.dto.WebhookEvent
import com.yayapay.engine.server.dto.toResponse
import com.yayapay.engine.util.IdGenerator
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WebhookDispatcher @Inject constructor(
    private val webhookEndpointDao: WebhookEndpointDao,
    private val webhookDeliveryDao: WebhookDeliveryDao,
    private val idGenerator: IdGenerator,
    private val workManager: WorkManager
) {
    private val json = Json { encodeDefaults = true }

    suspend fun dispatch(eventType: WebhookEventType, intent: PaymentIntentEntity) {
        val endpoints = webhookEndpointDao.getActiveEndpoints()
        val event = WebhookEvent(
            id = idGenerator.eventId(),
            type = eventType.value,
            createdAt = System.currentTimeMillis(),
            data = intent.toResponse()
        )
        val payload = json.encodeToString(event)

        for (endpoint in endpoints) {
            val enabledEvents = endpoint.enabledEvents.split(",")
            if (eventType.value !in enabledEvents && "*" !in enabledEvents) continue

            val delivery = WebhookDeliveryEntity(
                id = idGenerator.webhookDeliveryId(),
                webhookEndpointId = endpoint.id,
                eventType = eventType.value,
                payload = payload,
                nextRetryAt = System.currentTimeMillis()
            )
            webhookDeliveryDao.insert(delivery)
        }

        scheduleDelivery()
    }

    private fun scheduleDelivery() {
        val request = OneTimeWorkRequestBuilder<WebhookWorker>()
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .build()
        workManager.enqueueUniqueWork("webhook_delivery", ExistingWorkPolicy.REPLACE, request)
    }
}
