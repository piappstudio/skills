# Firebase Analytics Event Tags Reference

Centralized catalog of all event names and parameters for the Digital Diary app.

## Global Parameters (Include with All Events)

| Tag | Type | Description | Example |
|-----|------|-------------|---------|
| `platform` | string | android \| ios | "android" |
| `session_id` | string | Unique session identifier | "uuid-..." |
| `user_id` | string | Anonymous user identifier | "user-123" |
| `timestamp` | long | Event timestamp (ms) | 1649275200000 |
| `app_version` | string | App version | "1.2.3" |

## Authentication Events

| Event | Parameters | Purpose |
|-------|-----------|---------|
| `event_login_attempt` | `login_method` (string), `email_domain` (string) | Track login flow initiation |
| `event_login_success` | `login_method` (string), `duration_ms` (long) | Successful login completion |
| `event_login_failed` | `error_code` (string), `login_method` (string) | Login failure (log error type) |
| `event_forgot_password_clicked` | none | Password reset flow initiation |
| `event_verification_code_sent` | `delivery_method` (email\|sms) | OTP/verification codes |
| `event_signup_initiated` | `signup_source` (string) | Account creation start |
| `event_signup_completed` | `duration_ms` (long) | Account creation finish |
| `event_logout` | `session_duration_ms` (long) | User logout event |

## Diary Entry Events

| Event | Parameters | Purpose |
|-------|-----------|---------|
| `event_entry_viewed` | `entry_id` (string), `entry_age_days` (int) | Entry display |
| `event_entry_edit_started` | `entry_id` (string) | Edit mode opened |
| `event_entry_edit_saved` | `entry_id` (string), `duration_ms` (long), `char_count` (int) | Entry saved |
| `event_entry_edit_cancelled` | `entry_id` (string), `unsaved_changes` (boolean) | Edit abandoned |
| `event_entry_deleted` | `entry_id` (string) | Permanent deletion |
| `event_entry_shared` | `entry_id` (string), `share_method` (string) | Share action |
| `event_entry_starred` | `entry_id` (string), `action` (star\|unstar) | Favorite toggle |

## Navigation Events

| Event | Parameters | Purpose |
|-------|-----------|---------|
| `event_screen_viewed` | `screen_name` (string), `source` (string) | Screen transition |
| `event_screen_abandoned` | `screen_name` (string), `time_spent_ms` (long) | User exits without action |
| `event_deep_link_opened` | `deep_link` (string), `source` (string) | External/notification tap |

## Search & Filter Events

| Event | Parameters | Purpose |
|-------|-----------|---------|
| `event_search_initiated` | `search_type` (full_text\|tag\|date) | Search started |
| `event_search_completed` | `search_type` (string), `result_count` (int), `duration_ms` (long) | Search results shown |
| `event_filter_applied` | `filter_type` (string), `filter_value` (string) | Filter/sort applied |

## Sync & Offline Events

| Event | Parameters | Purpose |
|-------|-----------|---------|
| `event_sync_started` | `entry_count` (int) | Sync initiation |
| `event_sync_completed` | `duration_ms` (long), `entries_synced` (int) | Sync success |
| `event_sync_failed` | `error_code` (string), `retry_count` (int) | Sync failure |
| `event_offline_mode_enabled` | none | Offline mode activated |
| `event_data_restored` | `source` (backup\|cloud) | Data recovery |

## Premium/Subscription Events

| Event | Parameters | Purpose |
|-------|-----------|---------|
| `event_premium_viewed` | `ui_section` (string) | Premium screen shown |
| `event_premium_purchase_initiated` | `subscription_type` (string) | Subscription start |
| `event_premium_purchase_completed` | `subscription_type` (string), `price` (string) | Purchase success |
| `event_premium_purchase_failed` | `error_code` (string) | Purchase failure |
| `event_subscription_cancelled` | `reason` (string) | Cancellation with reason |

## Settings & Preferences Events

| Event | Parameters | Purpose |
|-------|-----------|---------|
| `event_settings_opened` | none | Settings screen viewed |
| `event_notification_setting_changed` | `setting_name` (string), `new_value` (boolean) | Notification pref toggled |
| `event_theme_changed` | `new_theme` (light\|dark\|auto) | Theme preference changed |
| `event_privacy_policy_viewed` | none | Legal doc viewed |

## Performance & Error Events

| Event | Parameters | Purpose |
|-------|-----------|---------|
| `event_app_crashed` | `error_code` (string), `stack_trace_hash` (string) | App crash (Firebase auto-logs too) |
| `event_api_call_failed` | `endpoint` (string), `error_code` (int) | Network error |
| `event_feature_performance` | `feature_name` (string), `duration_ms` (long) | Feature load time |

## How to Add New Events

1. **Identify the action**: User taps, navigates, changes setting, etc.
2. **Name it**: `event_<action>_<context>`
3. **Add minimal parameters**: Only include data needed for analysis
4. **Update this file**: Add to appropriate category
5. **Add to `AnalyticsEvents.kt`**: Create constant for code reuse
6. **Document valid values**: Use enums (star\|unstar, not freeform strings) where possible

## Parameter Types

- `string`: Text values, IDs
- `int`: Counts, ages, positions
- `long`: Timestamps, durations (in milliseconds)
- `boolean`: Yes/no states
- `double`: Decimals, percentages

**Rules**:
- All uppercase, lowercase snake_case in code: `DIARY_ENTRY_SAVED`, `duration_ms`
- Max parameter cardinality: Avoid millions of unique values (use categories, not free IDs)
- **No PII**: No emails, phone numbers, full names in parameters
