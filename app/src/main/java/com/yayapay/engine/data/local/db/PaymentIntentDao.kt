package com.yayapay.engine.data.local.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.yayapay.engine.data.model.Currency
import com.yayapay.engine.data.model.PaymentIntentEntity
import com.yayapay.engine.data.model.PaymentIntentStatus
import com.yayapay.engine.data.model.WalletType
import kotlinx.coroutines.flow.Flow

@Dao
interface PaymentIntentDao {

    @Insert
    suspend fun insert(intent: PaymentIntentEntity)

    @Update
    suspend fun update(intent: PaymentIntentEntity)

    @Query("SELECT * FROM payment_intents WHERE id = :id")
    suspend fun getById(id: String): PaymentIntentEntity?

    @Query("SELECT * FROM payment_intents ORDER BY createdAt DESC LIMIT :limit OFFSET :offset")
    suspend fun getAll(limit: Int = 25, offset: Int = 0): List<PaymentIntentEntity>

    @Query("SELECT * FROM payment_intents WHERE status = :status ORDER BY createdAt DESC LIMIT :limit OFFSET :offset")
    suspend fun getByStatus(status: PaymentIntentStatus, limit: Int = 25, offset: Int = 0): List<PaymentIntentEntity>

    @Query("SELECT * FROM payment_intents WHERE walletType = :walletType ORDER BY createdAt DESC LIMIT :limit OFFSET :offset")
    suspend fun getByWallet(walletType: WalletType, limit: Int = 25, offset: Int = 0): List<PaymentIntentEntity>

    @Query("SELECT * FROM payment_intents WHERE status = :status AND walletType = :walletType ORDER BY createdAt DESC LIMIT :limit OFFSET :offset")
    suspend fun getByStatusAndWallet(status: PaymentIntentStatus, walletType: WalletType, limit: Int = 25, offset: Int = 0): List<PaymentIntentEntity>

    @Query("""
        SELECT * FROM payment_intents
        WHERE walletType = :walletType
        AND amount = :amount
        AND status IN ('CREATED', 'PENDING')
        AND expiresAt > :now
        ORDER BY createdAt ASC
    """)
    suspend fun findMatchingIntents(walletType: WalletType, amount: Long, now: Long): List<PaymentIntentEntity>

    @Query("""
        UPDATE payment_intents
        SET status = 'SUCCEEDED',
            matchedNotificationId = :notificationId,
            senderName = :senderName,
            succeededAt = :succeededAt,
            updatedAt = :succeededAt
        WHERE id = :id
    """)
    suspend fun confirmIntent(id: String, notificationId: Long, senderName: String?, succeededAt: Long)

    @Query("UPDATE payment_intents SET status = :status, updatedAt = :now, canceledAt = :canceledAt WHERE id = :id")
    suspend fun updateStatus(id: String, status: PaymentIntentStatus, now: Long = System.currentTimeMillis(), canceledAt: Long? = null)

    @Query("SELECT * FROM payment_intents WHERE status IN ('CREATED', 'PENDING') AND expiresAt <= :now")
    suspend fun getExpiredIntents(now: Long): List<PaymentIntentEntity>

    @Query("SELECT * FROM payment_intents WHERE idempotencyKey = :key")
    suspend fun getByIdempotencyKey(key: String): PaymentIntentEntity?

    // Dashboard aggregates
    @Query("SELECT COUNT(*) FROM payment_intents WHERE createdAt >= :since")
    fun countSince(since: Long): Flow<Int>

    @Query("SELECT COUNT(*) FROM payment_intents WHERE status = 'SUCCEEDED' AND succeededAt >= :since")
    fun countSucceededSince(since: Long): Flow<Int>

    @Query("SELECT COALESCE(SUM(amount), 0) FROM payment_intents WHERE status = 'SUCCEEDED' AND succeededAt >= :since")
    fun sumSucceededSince(since: Long): Flow<Long>

    @Query("SELECT * FROM payment_intents ORDER BY createdAt DESC LIMIT :limit")
    fun recentIntents(limit: Int = 10): Flow<List<PaymentIntentEntity>>
}
