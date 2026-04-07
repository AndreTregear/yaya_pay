package com.yayapay.engine.ui.screens.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.yayapay.engine.data.model.PaymentIntentStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onNavigateToLogs: () -> Unit,
    onNavigateToSettings: () -> Unit,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val intentCount by viewModel.todayIntentCount.collectAsState(initial = 0)
    val succeededCount by viewModel.todaySucceededCount.collectAsState(initial = 0)
    val revenue by viewModel.todayRevenue.collectAsState(initial = 0L)
    val notifCount by viewModel.todayNotificationCount.collectAsState(initial = 0)
    val recentIntents by viewModel.recentIntents.collectAsState(initial = emptyList())

    val serverRunning = viewModel.embeddedServer.isRunning()
    val tunnelConnected = viewModel.relayClient.isConnected

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("YayaPay") },
                actions = {
                    IconButton(onClick = onNavigateToLogs) {
                        Icon(Icons.AutoMirrored.Filled.List, "Logs")
                    }
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, "Settings")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Spacer(Modifier.height(4.dp))
                // Server status
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (serverRunning) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = if (serverRunning) Color(0xFF4CAF50) else Color(0xFFF44336),
                            modifier = Modifier.size(24.dp)
                        )
                        Column {
                            Text(
                                if (serverRunning) "Server Running" else "Server Stopped",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                "Port ${viewModel.embeddedServer.getPort()} | Tunnel: ${if (tunnelConnected) "Connected" else "Off"}",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }

            // Stats cards
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard("Intents", "$intentCount", Modifier.weight(1f))
                    StatCard("Confirmed", "$succeededCount", Modifier.weight(1f))
                }
            }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard("Revenue", "$revenue", Modifier.weight(1f))
                    StatCard("Notifications", "$notifCount", Modifier.weight(1f))
                }
            }

            // Recent transactions
            item {
                Text("Recent", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 8.dp))
            }
            if (recentIntents.isEmpty()) {
                item {
                    Text("No transactions yet", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                }
            }
            items(recentIntents) { intent ->
                Card(Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.padding(12.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(intent.walletType.displayName, style = MaterialTheme.typography.bodyMedium)
                            Text(intent.id.take(20) + "...", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("${intent.amount}", style = MaterialTheme.typography.bodyMedium)
                            Text(
                                intent.status.name.lowercase(),
                                style = MaterialTheme.typography.labelSmall,
                                color = when (intent.status) {
                                    PaymentIntentStatus.SUCCEEDED -> Color(0xFF4CAF50)
                                    PaymentIntentStatus.EXPIRED, PaymentIntentStatus.FAILED -> Color(0xFFF44336)
                                    PaymentIntentStatus.CANCELED -> Color(0xFFFF9800)
                                    else -> Color.Gray
                                }
                            )
                        }
                    }
                }
            }
            item { Spacer(Modifier.height(16.dp)) }
        }
    }
}

@Composable
private fun StatCard(label: String, value: String, modifier: Modifier = Modifier) {
    Card(modifier) {
        Column(Modifier.padding(16.dp)) {
            Text(value, style = MaterialTheme.typography.headlineSmall)
            Text(label, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        }
    }
}
