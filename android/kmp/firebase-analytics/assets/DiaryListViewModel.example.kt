// ViewModel Example - Systematic Tracking Pattern
// Path: composeApp/src/commonMain/kotlin/features/diary/DiaryListViewModel.kt

package com.example.digitaldiary.features.diary

import com.example.digitaldiary.analytics.AnalyticsLogger
import com.example.digitaldiary.analytics.AnalyticsEvents
import kotlinx.coroutines.launch
import androidx.lifecycle.ViewModel

class DiaryListViewModel(
    private val repository: DiaryRepository,
    private val analytics: AnalyticsLogger
) : ViewModel() {
    
    private val screenStartTime = System.currentTimeMillis()
    private var actionCount = 0

    init {
        // Log screen view on enter
        analytics.logEvent(
            AnalyticsEvents.SCREEN_VIEWED,
            mapOf(
                AnalyticsEvents.Params.SCREEN_NAME to "diary_list",
                AnalyticsEvents.Params.SOURCE to "home_nav" // or deep_link, etc.
            )
        )
        
        loadEntries()
    }

    fun onViewEntry(entryId: String, entryAgeDays: Int) {
        actionCount++
        analytics.logEvent(
            AnalyticsEvents.ENTRY_VIEWED,
            mapOf(
                AnalyticsEvents.Params.ENTRY_ID to entryId,
                AnalyticsEvents.Params.ENTRY_AGE_DAYS to entryAgeDays
            )
        )
    }

    fun onCreateNewEntry() {
        actionCount++
        analytics.logEvent(
            AnalyticsEvents.ENTRY_EDIT_STARTED,
            mapOf(
                AnalyticsEvents.Params.ENTRY_ID to "new"
            )
        )
    }

    fun onDeleteEntry(entryId: String) {
        actionCount++
        viewModelScope.launch {
            try {
                repository.deleteEntry(entryId)
                analytics.logEvent(
                    AnalyticsEvents.ENTRY_DELETED,
                    mapOf(
                        AnalyticsEvents.Params.ENTRY_ID to entryId
                    )
                )
            } catch (e: Exception) {
                analytics.logEvent(
                    AnalyticsEvents.SYNC_FAILED,
                    mapOf(
                        AnalyticsEvents.Params.ERROR_CODE to e.javaClass.simpleName,
                        AnalyticsEvents.Params.ENTRY_ID to entryId
                    )
                )
            }
        }
    }

    fun onShareEntry(entryId: String, shareMethod: String) {
        actionCount++
        analytics.logEvent(
            AnalyticsEvents.ENTRY_SHARED,
            mapOf(
                AnalyticsEvents.Params.ENTRY_ID to entryId,
                AnalyticsEvents.Params.SHARE_METHOD to shareMethod
            )
        )
    }

    fun onSearch(query: String, searchType: String) {
        actionCount++
        analytics.logEvent(
            AnalyticsEvents.SEARCH_INITIATED,
            mapOf(
                AnalyticsEvents.Params.SEARCH_TYPE to searchType
            )
        )
    }

    fun onFilterApplied(filterType: String, filterValue: String) {
        actionCount++
        analytics.logEvent(
            AnalyticsEvents.FILTER_APPLIED,
            mapOf(
                AnalyticsEvents.Params.FILTER_TYPE to filterType,
                AnalyticsEvents.Params.FILTER_VALUE to filterValue
            )
        )
    }

    // Called when user navigates away without action
    fun onScreenAbandoned() {
        val timeSpentMs = System.currentTimeMillis() - screenStartTime
        
        // Only log abandonment if minimal action
        if (actionCount < 1) {
            analytics.logEvent(
                AnalyticsEvents.SCREEN_ABANDONED,
                mapOf(
                    AnalyticsEvents.Params.SCREEN_NAME to "diary_list",
                    AnalyticsEvents.Params.TIME_SPENT_MS to timeSpentMs,
                    AnalyticsEvents.Params.ACTION_COUNT to actionCount
                )
            )
        }
    }

    private fun loadEntries() {
        val startTime = System.currentTimeMillis()
        viewModelScope.launch {
            try {
                val entries = repository.getEntries()
                val duration = System.currentTimeMillis() - startTime
                
                analytics.logEvent(
                    AnalyticsEvents.FEATURE_PERFORMANCE,
                    mapOf(
                        AnalyticsEvents.Params.FEATURE_NAME to "diary_list_load",
                        AnalyticsEvents.Params.DURATION_MS to duration
                    )
                )
            } catch (e: Exception) {
                analytics.logEvent(
                    AnalyticsEvents.SYNC_FAILED,
                    mapOf(
                        AnalyticsEvents.Params.ERROR_CODE to e.javaClass.simpleName
                    )
                )
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        onScreenAbandoned() // Log abandonment if screen cleared without explicit action
    }
}
