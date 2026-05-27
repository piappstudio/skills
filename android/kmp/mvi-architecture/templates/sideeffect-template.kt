package com.digitaldiary.feature.yourfeature.presentation.side_effect

/**
 * One-time side effects for [YourFeature]
 * Emitted by ViewModel, consumed by UI
 */
sealed class YourSideEffect {
    // Navigation
    data class NavigateToDetail(val id: String) : YourSideEffect()
    object NavigateBack : YourSideEffect()
    object NavigateToHome : YourSideEffect()
    
    // Messaging
    data class ShowMessage(val message: String) : YourSideEffect()
    data class ShowError(val message: String) : YourSideEffect()
    
    // Dialogs
    object ShowDeleteConfirmation : YourSideEffect()
    data class ShowConfirmDialog(val title: String, val message: String) : YourSideEffect()
    
    // Other
    object ClearFocus : YourSideEffect()
}
