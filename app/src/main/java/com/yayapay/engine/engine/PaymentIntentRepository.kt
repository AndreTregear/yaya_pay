package com.yayapay.engine.engine

import com.yayapay.engine.data.local.db.PaymentIntentDao
import com.yayapay.engine.data.model.PaymentIntentEntity
import com.yayapay.engine.data.model.PaymentIntentStatus
import com.yayapay.engine.data.model.WalletType
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PaymentIntentRepository @Inject constructor(
    private val intentDao: PaymentIntentDao
) {
    suspend fun create(intent: PaymentIntentEntity) = intentDao.insert(intent)

    suspend fun getById(id: String): PaymentIntentEntity? = intentDao.getById(id)

    suspend fun getByIdempotencyKey(key: String): PaymentIntentEntity? = intentDao.getByIdempotencyKey(key)

    suspend fun list(
        status: PaymentIntentStatus? = null,
        walletType: WalletType? = null,
        limit: Int = 25,
        offset: Int = 0
    ): List<PaymentIntentEntity> = when {
        status != null && walletType != null -> intentDao.getByStatusAndWallet(status, walletType, limit, offset)
        status != null -> intentDao.getByStatus(status, limit, offset)
        walletType != null -> intentDao.getByWallet(walletType, limit, offset)
        else -> intentDao.getAll(limit, offset)
    }

    suspend fun cancel(id: String): PaymentIntentEntity? {
        val intent = intentDao.getById(id) ?: return null
        if (intent.status != PaymentIntentStatus.CREATED && intent.status != PaymentIntentStatus.PENDING) {
            return null
        }
        val now = System.currentTimeMillis()
        intentDao.updateStatus(id, PaymentIntentStatus.CANCELED, now, canceledAt = now)
        return intentDao.getById(id)
    }
}
