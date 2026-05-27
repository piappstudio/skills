// Common Analytics Logger Interface - Place in composeApp/src/commonMain/kotlin
// Path: composeApp/src/commonMain/kotlin/analytics/FirebaseAnalytics.kt

expect class AnalyticsLogger {
    fun logEvent(name: String, params: Map<String, Any> = emptyMap())
    fun logEvent(name: String, params: Pair<String, Any>)
    fun setUserProperty(name: String, value: String)
    fun setUserId(userId: String)
}

object AnalyticsEvents {
    // Authentication
    const val LOGIN_ATTEMPT = "event_login_attempt"
    const val LOGIN_SUCCESS = "event_login_success"
    const val LOGIN_FAILED = "event_login_failed"
    const val SIGNUP_INITIATED = "event_signup_initiated"
    const val SIGNUP_COMPLETED = "event_signup_completed"
    const val LOGOUT = "event_logout"
    const val FORGOT_PASSWORD = "event_forgot_password_clicked"

    // Navigation & Screens
    const val SCREEN_VIEWED = "event_screen_viewed"
    const val SCREEN_ABANDONED = "event_screen_abandoned"
    const val DEEP_LINK_OPENED = "event_deep_link_opened"

    // Diary Entries
    const val ENTRY_VIEWED = "event_entry_viewed"
    const val ENTRY_EDIT_STARTED = "event_entry_edit_started"
    const val ENTRY_EDIT_SAVED = "event_entry_edit_saved"
    const val ENTRY_EDIT_CANCELLED = "event_entry_edit_cancelled"
    const val ENTRY_DELETED = "event_entry_deleted"
    const val ENTRY_SHARED = "event_entry_shared"
    const val ENTRY_STARRED = "event_entry_starred"

    // Search & Filter
    const val SEARCH_INITIATED = "event_search_initiated"
    const val SEARCH_COMPLETED = "event_search_completed"
    const val FILTER_APPLIED = "event_filter_applied"

    // Sync & Offline
    const val SYNC_STARTED = "event_sync_started"
    const val SYNC_COMPLETED = "event_sync_completed"
    const val SYNC_FAILED = "event_sync_failed"
    const val OFFLINE_MODE_ENABLED = "event_offline_mode_enabled"
    const val DATA_RESTORED = "event_data_restored"

    // Premium
    const val PREMIUM_VIEWED = "event_premium_viewed"
    const val PREMIUM_PURCHASE_INITIATED = "event_premium_purchase_initiated"
    const val PREMIUM_PURCHASE_COMPLETED = "event_premium_purchase_completed"
    const val PREMIUM_PURCHASE_FAILED = "event_premium_purchase_failed"

    // Settings
    const val SETTINGS_OPENED = "event_settings_opened"
    const val NOTIFICATION_SETTING_CHANGED = "event_notification_setting_changed"
    const val THEME_CHANGED = "event_theme_changed"

    // Performance & Errors
    const val FEATURE_PERFORMANCE = "event_feature_performance"
    const val API_CALL_FAILED = "event_api_call_failed"

    // Parameter Names
    object Params {
        // Global
        const val PLATFORM = "platform"
        const val SESSION_ID = "session_id"
        const val USER_ID = "user_id"
        const val TIMESTAMP = "timestamp"
        const val APP_VERSION = "app_version"

        // Auth & User
        const val LOGIN_METHOD = "login_method"
        const val EMAIL_DOMAIN = "email_domain"
        const val ERROR_CODE = "error_code"
        const val DURATION_MS = "duration_ms"

        // Entry
        const val ENTRY_ID = "entry_id"
        const val ENTRY_AGE_DAYS = "entry_age_days"
        const val CHAR_COUNT = "char_count"
        const val SHARE_METHOD = "share_method"
        const val UNSAVED_CHANGES = "unsaved_changes"

        // Search
        const val SEARCH_TYPE = "search_type"
        const val RESULT_COUNT = "result_count"
        const val FILTER_TYPE = "filter_type"
        const val FILTER_VALUE = "filter_value"

        // Sync
        const val ENTRY_COUNT = "entry_count"
        const val ENTRIES_SYNCED = "entries_synced"
        const val RETRY_COUNT = "retry_count"
        const val SOURCE = "source"

        // Screen
        const val SCREEN_NAME = "screen_name"
        const val TIME_SPENT_MS = "time_spent_ms"
        const val ACTION_COUNT = "action_count"

        // Performance
        const val FEATURE_NAME = "feature_name"

        // Subscriptions
        const val SUBSCRIPTION_TYPE = "subscription_type"
        const val PRICE = "price"

        // Settings
        const val SETTING_NAME = "setting_name"
        const val NEW_VALUE = "new_value"
    }
}

// Platform detection helper
expect val currentPlatform: String
