package com.yayapay.engine.ui.screens.logs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
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
import com.yayapay.engine.data.local.db.PaymentIntentDao
import com.yayapay.engine.data.model.PaymentIntentEntity
import com.yayapay.engine.data.model.PaymentIntentStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import androidx.lifecycle.ViewModel

@HiltViewModel
class LogViewModel @Inject constructor(
    intentDao: PaymentIntentDao
) : ViewModel() {
    val recentIntents = intentDao.recentIntents(50)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogScreen(
    onBack: () -> Unit,
    viewModel: LogViewModel = hiltViewModel()
) {
    val intents by viewModel.recentIntents.collectAsState(initial = emptyList())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Transaction Log") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
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
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(intents) { intent ->
                IntentCard(intent)
            }
        }
    }
}

@Composable
private fun IntentCard(intent: PaymentIntentEntity) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(intent.walletType.displayName, style = MaterialTheme.typography.titleSmall)
                Text(
                    intent.status.name.lowercase(),
                    color = when (intent.status) {
                        PaymentIntentStatus.SUCCEEDED -> Color(0xFF4CAF50)
                        PaymentIntentStatus.EXPIRED, PaymentIntentStatus.FAILED -> Color(0xFFF44336)
                        else -> Color.Gray
                    },
                    style = MaterialTheme.typography.labelMedium
                )
            }
            Text("${intent.currency.symbol} ${intent.currency.toDisplayAmount(intent.amount)}", style = MaterialTheme.typography.bodyLarge)
            Text(intent.id, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            intent.senderName?.let {
                Text("From: $it", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
