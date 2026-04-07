package com.yayapay.engine.ui.screens.dashboard

import androidx.lifecycle.ViewModel
import com.yayapay.engine.data.local.db.PaymentIntentDao
import com.yayapay.engine.data.local.db.WalletNotificationDao
import com.yayapay.engine.data.model.PaymentIntentEntity
import com.yayapay.engine.server.EmbeddedServer
import com.yayapay.engine.tunnel.RelayClient
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val intentDao: PaymentIntentDao,
    private val notificationDao: WalletNotificationDao,
    val embeddedServer: EmbeddedServer,
    val relayClient: RelayClient
) : ViewModel() {

    private val todayStart: Long
        get() {
            val cal = Calendar.getInstance()
            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)
            return cal.timeInMillis
        }

    val todayIntentCount: Flow<Int> = intentDao.countSince(todayStart)
    val todaySucceededCount: Flow<Int> = intentDao.countSucceededSince(todayStart)
    val todayRevenue: Flow<Long> = intentDao.sumSucceededSince(todayStart)
    val todayNotificationCount: Flow<Int> = notificationDao.countSince(todayStart)
    val recentIntents: Flow<List<PaymentIntentEntity>> = intentDao.recentIntents(5)
}
