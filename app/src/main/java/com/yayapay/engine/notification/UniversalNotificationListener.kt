package com.yayapay.engine.notification

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint(NotificationListenerService::class)
class UniversalNotificationListener : Hilt_UniversalNotificationListener() {

    @Inject lateinit var notificationRouter: NotificationRouter

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        if (sbn == null) return
        serviceScope.launch {
            try {
                notificationRouter.route(sbn)
            } catch (e: Exception) {
                android.util.Log.e("NotificationListener", "Error routing notification", e)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }
}
