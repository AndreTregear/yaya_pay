package com.yayapay.engine.engine

import android.util.Log
import com.yayapay.engine.data.local.db.PaymentIntentDao
import com.yayapay.engine.data.model.PaymentIntentStatus
import com.yayapay.engine.data.model.WebhookEventType
import com.yayapay.engine.webhook.WebhookDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class IntentExpirationManager @Inject constructor(
    private val intentDao: PaymentIntentDao,
    private val webhookDispatcher: WebhookDispatcher
) {
    companion object {
        private const val TAG = "IntentExpiration"
        private const val CHECK_INTERVAL_MS = 30_000L
    }

    private var job: Job? = null

    fun start(scope: CoroutineScope) {
        job = scope.launch {
            while (isActive) {
                try {
                    expireOverdueIntents()
                } catch (e: Exception) {
                    Log.e(TAG, "Error expiring intents", e)
                }
                delay(CHECK_INTERVAL_MS)
            }
        }
    }

    private suspend fun expireOverdueIntents() {
        val now = System.currentTimeMillis()
        val expired = intentDao.getExpiredIntents(now)
        for (intent in expired) {
            intentDao.updateStatus(intent.id, PaymentIntentStatus.EXPIRED, now)
            webhookDispatcher.dispatch(
                WebhookEventType.PAYMENT_INTENT_EXPIRED,
                intent.copy(status = PaymentIntentStatus.EXPIRED, updatedAt = now)
            )
            Log.d(TAG, "Expired intent: ${intent.id}")
        }
    }

    fun stop() {
        job?.cancel()
    }
}
