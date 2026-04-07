package com.yayapay.engine.ui.screens.setup

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun SetupScreen(
    onSetupComplete: () -> Unit,
    viewModel: SetupViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(state.isComplete) {
        if (state.isComplete) onSetupComplete()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("YayaPay Setup", style = MaterialTheme.typography.headlineLarge)
        Spacer(Modifier.height(8.dp))
        Text("Step ${state.step + 1} of 3", style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(32.dp))

        when (state.step) {
            0 -> {
                Text("Grant Notification Access", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
                Text("YayaPay needs to read wallet notifications to detect payments.")
                Spacer(Modifier.height(16.dp))
                Button(onClick = {
                    context.startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
                }) {
                    Text("Open Settings")
                }
                Spacer(Modifier.height(8.dp))
                OutlinedButton(onClick = { viewModel.nextStep() }) {
                    Text("I've granted access")
                }
            }

            1 -> {
                Text("Generate API Key", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
                Text("Create your first API key to authenticate requests.")
                Spacer(Modifier.height(16.dp))

                if (state.generatedApiKey != null) {
                    Card(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(16.dp)) {
                            Text("Your API Key (save it now!):", style = MaterialTheme.typography.labelMedium)
                            Spacer(Modifier.height(8.dp))
                            Text(
                                state.generatedApiKey!!,
                                fontFamily = FontFamily.Monospace,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    OutlinedButton(onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText("API Key", state.generatedApiKey))
                    }) {
                        Text("Copy to Clipboard")
                    }
                    Spacer(Modifier.height(16.dp))
                    Button(onClick = { viewModel.nextStep() }) {
                        Text("Next")
                    }
                } else {
                    Button(onClick = { viewModel.generateApiKey() }) {
                        Text("Generate API Key")
                    }
                }
            }

            2 -> {
                Text("Ready!", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
                Text("YayaPay will start the payment server and begin listening for wallet notifications.")
                Spacer(Modifier.height(16.dp))
                Button(onClick = { viewModel.completeSetup() }) {
                    Text("Start YayaPay")
                }
            }
        }
    }
}
