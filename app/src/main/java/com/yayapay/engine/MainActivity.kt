package com.yayapay.engine

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.yayapay.engine.data.local.db.ApiKeyDao
import com.yayapay.engine.service.YayaPayForegroundService
import com.yayapay.engine.ui.navigation.YayaPayNavGraph
import com.yayapay.engine.ui.theme.YayaPayTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

@AndroidEntryPoint(ComponentActivity::class)
class MainActivity : Hilt_MainActivity() {

    @Inject lateinit var apiKeyDao: ApiKeyDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            YayaPayTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    var isSetupComplete by remember { mutableStateOf<Boolean?>(null) }

                    LaunchedEffect(Unit) {
                        // Always start the server — health endpoint works without API key
                        startForegroundService(Intent(this@MainActivity, YayaPayForegroundService::class.java))
                        try {
                            val hasKeys = withContext(Dispatchers.IO) { apiKeyDao.countActive() > 0 }
                            Log.d("YayaPay", "Setup check: hasKeys=$hasKeys")
                            isSetupComplete = hasKeys
                        } catch (e: Exception) {
                            Log.e("YayaPay", "Setup check failed", e)
                            isSetupComplete = false
                        }
                    }

                    isSetupComplete?.let { complete ->
                        YayaPayNavGraph(
                            navController = navController,
                            isSetupComplete = complete
                        )
                    }
                }
            }
        }
    }
}
