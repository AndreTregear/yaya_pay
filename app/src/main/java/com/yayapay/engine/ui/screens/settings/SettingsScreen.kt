package com.yayapay.engine.ui.screens.settings

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yayapay.engine.data.local.db.ApiKeyDao
import com.yayapay.engine.data.model.ApiKeyEntity
import com.yayapay.engine.server.auth.ApiKeyAuth
import com.yayapay.engine.util.IdGenerator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val apiKeyDao: ApiKeyDao,
    private val idGenerator: IdGenerator
) : ViewModel() {

    val apiKeys = apiKeyDao.getAll()

    private val _newKey = MutableStateFlow<String?>(null)
    val newKey: StateFlow<String?> = _newKey

    fun createApiKey(name: String) {
        viewModelScope.launch {
            val rawKey = idGenerator.apiKeySecret()
            val hash = ApiKeyAuth.hashKey(rawKey)
            apiKeyDao.insert(ApiKeyEntity(
                id = idGenerator.apiKeyId(),
                name = name,
                keyHash = hash,
                keyPrefix = rawKey.take(12) + "..."
            ))
            _newKey.value = rawKey
        }
    }

    fun revokeKey(id: String) {
        viewModelScope.launch { apiKeyDao.revoke(id) }
    }

    fun clearNewKey() { _newKey.value = null }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val apiKeys by viewModel.apiKeys.collectAsState(initial = emptyList())
    val newKey by viewModel.newKey.collectAsState()
    val context = LocalContext.current
    var keyName by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
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
            item {
                Text("API Keys", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
            }

            // Show newly created key
            newKey?.let { key ->
                item {
                    Card(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(12.dp)) {
                            Text("New API Key (save now!):", style = MaterialTheme.typography.labelMedium)
                            Text(key, fontFamily = FontFamily.Monospace, style = MaterialTheme.typography.bodySmall)
                            Spacer(Modifier.height(8.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedButton(onClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    clipboard.setPrimaryClip(ClipData.newPlainText("API Key", key))
                                }) {
                                    Text("Copy")
                                }
                                OutlinedButton(onClick = { viewModel.clearNewKey() }) {
                                    Text("Dismiss")
                                }
                            }
                        }
                    }
                }
            }

            item {
                Button(onClick = { viewModel.createApiKey("API Key ${apiKeys.size + 1}") }) {
                    Text("Create New Key")
                }
                Spacer(Modifier.height(8.dp))
            }

            items(apiKeys) { key ->
                Card(Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.padding(12.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(key.name, style = MaterialTheme.typography.bodyMedium)
                            Text(key.keyPrefix, fontFamily = FontFamily.Monospace, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                            Text(
                                if (key.active) "Active" else "Revoked",
                                color = if (key.active) Color(0xFF4CAF50) else Color(0xFFF44336),
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                        if (key.active) {
                            OutlinedButton(onClick = { viewModel.revokeKey(key.id) }) {
                                Text("Revoke")
                            }
                        }
                    }
                }
            }
        }
    }
}
