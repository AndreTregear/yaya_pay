package com.yayapay.engine.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.yayapay.engine.data.model.ApiKeyEntity
import com.yayapay.engine.data.model.PaymentIntentEntity
import com.yayapay.engine.data.model.WalletNotificationEntity
import com.yayapay.engine.data.model.WebhookDeliveryEntity
import com.yayapay.engine.data.model.WebhookEndpointEntity

@Database(
    entities = [
        PaymentIntentEntity::class,
        WalletNotificationEntity::class,
        WebhookEndpointEntity::class,
        WebhookDeliveryEntity::class,
        ApiKeyEntity::class
    ],
    version = 1,
    exportSchema = true
)
abstract class YayaPayDatabase : RoomDatabase() {
    abstract fun paymentIntentDao(): PaymentIntentDao
    abstract fun walletNotificationDao(): WalletNotificationDao
    abstract fun webhookEndpointDao(): WebhookEndpointDao
    abstract fun webhookDeliveryDao(): WebhookDeliveryDao
    abstract fun apiKeyDao(): ApiKeyDao
}
