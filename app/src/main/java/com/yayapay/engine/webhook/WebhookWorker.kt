package com.yayapay.engine.webhook

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.yayapay.engine.data.local.db.WebhookDeliveryDao
import com.yayapay.engine.data.local.db.WebhookEndpointDao
import com.yayapay.engine.data.model.WebhookDeliveryEntity
import com.yayapay.engine.data.model.WebhookEndpointEntity
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit
import kotlin.math.pow

@HiltWorker
class WebhookWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val webhookDeliveryDao: WebhookDeliveryDao,
    private val webhookEndpointDao: WebhookEndpointDao,
    private val signatureUtil: WebhookSignatureUtil
) : CoroutineWorker(context, params) {

    companion object {
        private const val TAG = "WebhookWorker"
    }

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    override suspend fun doWork(): Result {
        val pending = webhookDeliveryDao.getPendingDeliveries(System.currentTimeMillis())
        for (delivery in pending) {
            val endpoint = webhookEndpointDao.getById(delivery.webhookEndpointId) ?: continue
            deliverWebhook(delivery, endpoint)
        }
        return Result.success()
    }

    private suspend fun deliverWebhook(delivery: WebhookDeliveryEntity, endpoint: WebhookEndpointEntity) {
        val signature = signatureUtil.sign(delivery.payload, endpoint.secret)
        val request = Request.Builder()
            .url(endpoint.url)
            .header("Content-Type", "application/json")
            .header("YayaPay-Signature", signature)
            .header("YayaPay-Event", delivery.eventType)
            .post(delivery.payload.toRequestBody("application/json".toMediaType()))
            .build()

        try {
            val response = httpClient.newCall(request).execute()
            response.use {
                if (it.isSuccessful) {
                    webhookDeliveryDao.markDelivered(delivery.id, it.code, System.currentTimeMillis())
                    Log.d(TAG, "Webhook delivered: ${delivery.id}")
                } else {
                    scheduleRetry(delivery, it.code, it.body?.string()?.take(500))
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Webhook delivery failed: ${delivery.id}", e)
            scheduleRetry(delivery, null, e.message?.take(500))
        }
    }

    private suspend fun scheduleRetry(delivery: WebhookDeliveryEntity, statusCode: Int?, responseBody: String?) {
        val attempt = delivery.attemptCount + 1
        if (attempt >= delivery.maxAttempts) {
            webhookDeliveryDao.markExhausted(delivery.id, attempt, statusCode, responseBody)
            Log.w(TAG, "Webhook exhausted: ${delivery.id}")
        } else {
            // Exponential backoff: 5s, 25s, 125s, 625s
            val delayMs = (5_000L * 5.0.pow(attempt - 1)).toLong()
            webhookDeliveryDao.scheduleRetry(
                delivery.id, attempt,
                System.currentTimeMillis() + delayMs,
                statusCode, responseBody
            )
        }
    }
}
