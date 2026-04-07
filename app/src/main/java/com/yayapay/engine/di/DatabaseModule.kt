package com.yayapay.engine.di

import android.content.Context
import androidx.room.Room
import com.yayapay.engine.data.local.db.ApiKeyDao
import com.yayapay.engine.data.local.db.PaymentIntentDao
import com.yayapay.engine.data.local.db.WalletNotificationDao
import com.yayapay.engine.data.local.db.WebhookDeliveryDao
import com.yayapay.engine.data.local.db.WebhookEndpointDao
import com.yayapay.engine.data.local.db.YayaPayDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): YayaPayDatabase =
        Room.databaseBuilder(context, YayaPayDatabase::class.java, "yayapay.db")
            .build()

    @Provides
    fun providePaymentIntentDao(db: YayaPayDatabase): PaymentIntentDao = db.paymentIntentDao()

    @Provides
    fun provideWalletNotificationDao(db: YayaPayDatabase): WalletNotificationDao = db.walletNotificationDao()

    @Provides
    fun provideWebhookEndpointDao(db: YayaPayDatabase): WebhookEndpointDao = db.webhookEndpointDao()

    @Provides
    fun provideWebhookDeliveryDao(db: YayaPayDatabase): WebhookDeliveryDao = db.webhookDeliveryDao()

    @Provides
    fun provideApiKeyDao(db: YayaPayDatabase): ApiKeyDao = db.apiKeyDao()
}
