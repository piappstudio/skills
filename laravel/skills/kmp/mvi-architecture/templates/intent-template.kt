package com.digitaldiary.feature.yourfeature.presentation.intent

/**
 * User intents for [YourFeature]
 * One sealed subclass per user action
 */
sealed class YourIntent {
    // Data loading
    object LoadData : YourIntent()
    object RefreshData : YourIntent()
    
    // Actions
    data class Action1(val param: String) : YourIntent()
    data class Action2(val id: String) : YourIntent()
    
    // Selection
    data class SelectItem(val id: String) : YourIntent()
    object ClearSelection : YourIntent()
    
    // Search/Filter
    data class SearchQueryChanged(val query: String) : YourIntent()
    object ClearSearch : YourIntent()
    
    // Error handling
    object ClearError : YourIntent()
}
