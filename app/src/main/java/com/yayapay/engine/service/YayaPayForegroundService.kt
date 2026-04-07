package com.yayapay.engine.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.yayapay.engine.R
import com.yayapay.engine.engine.IntentExpirationManager
import com.yayapay.engine.server.EmbeddedServer
import com.yayapay.engine.tunnel.RelayClient
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import javax.inject.Inject

@AndroidEntryPoint(Service::class)
class YayaPayForegroundService : Hilt_YayaPayForegroundService() {

    @Inject lateinit var embeddedServer: EmbeddedServer
    @Inject lateinit var relayClient: RelayClient
    @Inject lateinit var expirationManager: IntentExpirationManager

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onCreate() {
        super.onCreate()

        val channel = NotificationChannel(
            CHANNEL_ID,
            getString(R.string.notification_channel_service),
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = getString(R.string.notification_channel_service_desc)
        }
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)

        val notification = Notification.Builder(this, CHANNEL_ID)
            .setContentTitle("YayaPay")
            .setContentText(getString(R.string.notification_service_running, SERVER_PORT))
            .setSmallIcon(android.R.drawable.ic_menu_manage)
            .setOngoing(true)
            .build()

        startForeground(NOTIFICATION_ID, notification)

        embeddedServer.packageManager = packageManager
        embeddedServer.start(SERVER_PORT)
        expirationManager.start(serviceScope)
    }

    override fun onDestroy() {
        super.onDestroy()
        expirationManager.stop()
        relayClient.disconnect()
        embeddedServer.stop()
        serviceScope.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        const val CHANNEL_ID = "yayapay_service"
        const val NOTIFICATION_ID = 1
        const val SERVER_PORT = 8080
    }
}
