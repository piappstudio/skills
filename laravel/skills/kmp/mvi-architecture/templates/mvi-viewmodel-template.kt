package com.digitaldiary.feature.yourfeature.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.digitaldiary.feature.yourfeature.domain.usecase.*
import com.digitaldiary.feature.yourfeature.presentation.intent.YourIntent
import com.digitaldiary.feature.yourfeature.presentation.side_effect.YourSideEffect
import com.digitaldiary.feature.yourfeature.presentation.state.YourUiState
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.GlobalContext.get

/**
 * MVI ViewModel using Koin for dependency injection
 * 
 * Usage in Koin module:
 * viewModel { YourViewModel(get(), get()) }
 */
class YourViewModel(
    private val useCase1: UseCase1,
    private val useCase2: UseCase2,
    // Add other injected UseCases here
) : ViewModel() {

    // ============ STATE MANAGEMENT ============
    
    private val _uiState = MutableStateFlow(YourUiState())
    val uiState: StateFlow<YourUiState> = _uiState.asStateFlow()

    // ============ SIDE EFFECTS ============
    
    private val _sideEffect = Channel<YourSideEffect>(Channel.BUFFERED)
    val sideEffect: Flow<YourSideEffect> = _sideEffect.receiveAsFlow()

    // ============ LIFECYCLE ============
    
    init {
        // Load initial data if needed
        handleIntent(YourIntent.LoadData)
    }

    // ============ PUBLIC API ============
    
    fun handleIntent(intent: YourIntent) {
        viewModelScope.launch {
            when (intent) {
                YourIntent.LoadData -> loadData()
                is YourIntent.Action1 -> action1(intent)
                is YourIntent.Action2 -> action2(intent.param)
                YourIntent.ClearError -> clearError()
                // Add other intent cases here
            }
        }
    }

    // ============ INTENT HANDLERS ============
    
    private suspend fun loadData() {
        // Set loading state
        _uiState.update { it.copy(isLoading = true, error = null) }
        
        // Call UseCase
        val result = useCase1()
        
        // Update state based on result
        _uiState.update {
            when (result) {
                is Result.Success -> it.copy(
                    isLoading = false,
                    data = result.data,
                    error = null
                )
                is Result.Error -> it.copy(
                    isLoading = false,
                    data = null,
                    error = result.exception.message ?: "Unknown error"
                )
            }
        }
    }
    
    private suspend fun action1(intent: YourIntent.Action1) {
        val result = useCase2(intent.param)
        
        when (result) {
            is Result.Success -> {
                // Update state
                _uiState.update { it.copy(/* ... */) }
                
                // Optionally emit side effect
                _sideEffect.send(YourSideEffect.ShowMessage("Success"))
            }
            is Result.Error -> {
                // Emit error side effect
                _sideEffect.send(YourSideEffect.ShowError(result.exception.message ?: "Error"))
                
                // Or update state with error
                _uiState.update { it.copy(error = result.exception.message) }
            }
        }
    }
    
    private suspend fun action2(param: String) {
        // Implementation
    }
    
    private fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
