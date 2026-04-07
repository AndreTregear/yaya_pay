package com.yayapay.engine.data.local.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.yayapay.engine.data.model.DeliveryStatus
import com.yayapay.engine.data.model.WebhookDeliveryEntity

@Dao
interface WebhookDeliveryDao {

    @Insert
    suspend fun insert(delivery: WebhookDeliveryEntity)

    @Query("SELECT * FROM webhook_deliveries WHERE status = 'PENDING' AND (nextRetryAt IS NULL OR nextRetryAt <= :now) ORDER BY createdAt ASC LIMIT 50")
    suspend fun getPendingDeliveries(now: Long): List<WebhookDeliveryEntity>

    @Query("UPDATE webhook_deliveries SET status = 'DELIVERED', httpStatusCode = :statusCode, deliveredAt = :deliveredAt, attemptCount = attemptCount + 1 WHERE id = :id")
    suspend fun markDelivered(id: String, statusCode: Int, deliveredAt: Long)

    @Query("UPDATE webhook_deliveries SET status = 'FAILED', httpStatusCode = :statusCode, responseBody = :responseBody, attemptCount = :attempt, nextRetryAt = :nextRetryAt WHERE id = :id")
    suspend fun scheduleRetry(id: String, attempt: Int, nextRetryAt: Long, statusCode: Int?, responseBody: String?)

    @Query("UPDATE webhook_deliveries SET status = 'EXHAUSTED', httpStatusCode = :statusCode, responseBody = :responseBody, attemptCount = :attempt WHERE id = :id")
    suspend fun markExhausted(id: String, attempt: Int, statusCode: Int?, responseBody: String?)
}
