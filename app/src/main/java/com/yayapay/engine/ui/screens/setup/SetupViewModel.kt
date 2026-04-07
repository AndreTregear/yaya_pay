package com.yayapay.engine.ui.screens.setup

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

data class SetupUiState(
    val step: Int = 0,
    val generatedApiKey: String? = null,
    val isComplete: Boolean = false
)

@HiltViewModel
class SetupViewModel @Inject constructor(
    private val apiKeyDao: ApiKeyDao,
    private val idGenerator: IdGenerator
) : ViewModel() {

    private val _state = MutableStateFlow(SetupUiState())
    val state: StateFlow<SetupUiState> = _state

    fun nextStep() {
        _state.value = _state.value.copy(step = _state.value.step + 1)
    }

    fun generateApiKey() {
        viewModelScope.launch {
            val rawKey = idGenerator.apiKeySecret()
            val hash = ApiKeyAuth.hashKey(rawKey)
            val entity = ApiKeyEntity(
                id = idGenerator.apiKeyId(),
                name = "Default",
                keyHash = hash,
                keyPrefix = rawKey.take(12) + "..."
            )
            apiKeyDao.insert(entity)
            _state.value = _state.value.copy(generatedApiKey = rawKey)
        }
    }

    fun completeSetup() {
        _state.value = _state.value.copy(isComplete = true)
    }
}
