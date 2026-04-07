package com.yayapay.engine.data.local.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.yayapay.engine.data.model.WalletNotificationEntity
import com.yayapay.engine.data.model.WalletType
import kotlinx.coroutines.flow.Flow

@Dao
interface WalletNotificationDao {

    @Insert
    suspend fun insert(notification: WalletNotificationEntity): Long

    @Query("SELECT * FROM wallet_notifications WHERE id = :id")
    suspend fun getById(id: Long): WalletNotificationEntity?

    @Query("SELECT COUNT(*) FROM wallet_notifications WHERE notificationHash = :hash AND capturedAt >= :since")
    suspend fun countByHashSince(hash: String, since: Long): Int

    @Query("""
        SELECT COUNT(*) FROM wallet_notifications
        WHERE senderName = :senderName AND amount = :amount AND walletType = :walletType AND capturedAt >= :since
    """)
    suspend fun countBySenderAmountWalletSince(senderName: String, amount: Long, walletType: WalletType, since: Long): Int

    @Query("UPDATE wallet_notifications SET matched = 1, matchedIntentId = :intentId WHERE id = :id")
    suspend fun markMatched(id: Long, intentId: String)

    @Query("SELECT * FROM wallet_notifications WHERE matched = 0 ORDER BY capturedAt DESC LIMIT :limit")
    suspend fun getUnmatched(limit: Int = 50): List<WalletNotificationEntity>

    @Query("SELECT * FROM wallet_notifications ORDER BY capturedAt DESC LIMIT :limit OFFSET :offset")
    suspend fun getAll(limit: Int = 25, offset: Int = 0): List<WalletNotificationEntity>

    @Query("SELECT COUNT(*) FROM wallet_notifications WHERE capturedAt >= :since")
    fun countSince(since: Long): Flow<Int>

    @Query("SELECT * FROM wallet_notifications ORDER BY capturedAt DESC LIMIT :limit")
    fun recentNotifications(limit: Int = 10): Flow<List<WalletNotificationEntity>>
}
