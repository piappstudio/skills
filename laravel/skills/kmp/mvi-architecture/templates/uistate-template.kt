package com.digitaldiary.feature.yourfeature.presentation.state

import com.digitaldiary.feature.yourfeature.domain.model.YourDataModel

/**
 * Immutable UI state for [YourFeature]
 */
data class YourUiState(
    // Loading state
    val isLoading: Boolean = false,
    
    // Data state
    val data: YourDataModel? = null,
    val items: List<YourDataModel> = emptyList(),
    
    // UI state
    val selectedId: String? = null,
    val searchQuery: String = "",
    
    // Error state
    val error: String? = null,
    
    // Derived properties
    val isEmpty: Boolean = items.isEmpty(),
    val hasError: Boolean = error != null,
) {
    val isIdle: Boolean = !isLoading && !hasError && isEmpty
}
