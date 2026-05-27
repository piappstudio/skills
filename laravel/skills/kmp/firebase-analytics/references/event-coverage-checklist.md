# Event Coverage Checklist

Use this screen-by-screen checklist to ensure comprehensive event tracking without missed points.

## Checklist Template

For each screen/feature in your app, verify:

```
Screen: [Screen Name]
Purpose: [What user does here]

Navigation:
  [ ] Screen entered → event_<screen_name>_viewed (include source screen)
  [ ] User leaves → event_<screen_name>_abandoned (include time spent, action count)
  [ ] Deep link lands here → logged with event_deep_link_opened

Primary Actions:
  [ ] Main CTA (button tap, form submit, etc.)
  [ ] Secondary CTAs
  [ ] Each maps to event_<action>_completed + parameters

Error States:
  [ ] Network error → event_<action>_failed (error_code, error_message_hash)
  [ ] Validation error → event_<action>_failed (field_name, rule_violated)
  [ ] User cancels → event_<action>_cancelled
  [ ] Timeout/retry → event_<action>_retried (attempt_count)

State Changes:
  [ ] Toggle/selection changes → event_<setting>_changed
  [ ] Pagination/infinite scroll → event_list_pagination (page_num, direction)
  [ ] Filter/sort applied → event_filter_applied (filter_name, value)

Performance:
  [ ] Screen load time measured → event_feature_performance (duration_ms)
  [ ] Slow operations logged → event_sync_started / _completed or _failed
  [ ] Images/content load tracked → event_content_loaded (asset_type, duration_ms)

First-Time Experience:
  [ ] First screen shown → event_onboarding_step (step_number, step_name)
  [ ] Onboarding completed → event_onboarding_completed
  [ ] Tutorial viewed → event_tutorial_viewed (tutorial_id)

User Engagement:
  [ ] Favorite/bookmark toggle → event_entry_starred / event_entry_unstarred
  [ ] Share initiated → event_entry_shared (share_method: copy, email, social)
  [ ] Review/rating shown → event_feature_review_shown
  [ ] Help/support accessed → event_help_tapped (topic_name)
```

---

## Digital Diary Screens - Coverage Tracking

### 1. Splash / Onboarding

```
Screen: Splash / App Launch
Purpose: Initial app load, routing to auth or home

Navigation:
  [x] app_launched (measured from intent/cold start)
  [x] event_screen_viewed (screen_name: splash)
  [x] Navigate to login or home (after auth check)

Engagement:
  [ ] Onboarding shown (first-time users) → event_onboarding_step
  [ ] Skip onboarding → event_onboarding_skipped
  [ ] Onboarding completed → event_onboarding_completed
```

### 2. Authentication (Login/Signup)

```
Screen: Login / Authentication
Purpose: User login or account creation

Actions:
  [x] event_login_attempt (login_method: email, google, apple)
  [x] event_login_success (duration_ms, login_method)
  [x] event_login_failed (error_code: invalid_credentials, network_error, etc.)
  [ ] event_forgot_password_clicked
  [ ] event_signup_initiated
  [ ] event_signup_completed (duration_ms)

Error Handling:
  [x] Invalid credentials → event_login_failed (error_code: invalid_credentials)
  [x] Network error → event_login_failed (error_code: network_error)
  [ ] Rate limit → event_login_failed (error_code: rate_limited, retry_after_seconds)
```

### 3. Home / Diary List

```
Screen: Home / Entries List
Purpose: Browse and manage diary entries

Navigation:
  [x] event_screen_viewed (source: auth, deep_link, or previous screen)
  [x] event_screen_abandoned (if user exits without action, time_spent_ms, action_count)

Primary Actions:
  [ ] Tap entry to view → event_entry_viewed (entry_id, entry_age_days)
  [ ] Create new entry → event_entry_edit_started (entry_id: null)
  [ ] Long-press entry (options) → event_entry_options_shown (entry_id)
  [ ] Delete entry → event_entry_deleted (entry_id)
  [ ] Share entry → event_entry_shared (entry_id, share_method: copy, email, message)
  [ ] Star/favorite toggle → event_entry_starred (entry_id, action: star or unstar)

Search & Filter:
  [ ] Search initiated → event_search_initiated (search_type: full_text or tag_search)
  [ ] Search results shown → event_search_completed (search_type, result_count, duration_ms)
  [ ] Filter applied (date range, tag) → event_filter_applied (filter_type: date_range or tag, filter_value)
  [ ] Sort changed → event_sort_changed (sort_by: newest, oldest, a_z)

Pagination:
  [ ] Scroll load more → event_list_pagination (direction: down, page_num)

Performance:
  [ ] List loaded → event_feature_performance (feature_name: home_list_load, duration_ms)
  [ ] Sync status → event_sync_completed (entries_synced, duration_ms)
```

### 4. Entry Detail / Edit

```
Screen: Diary Entry Viewer & Editor
Purpose: Read and edit individual diary entries

Navigation:
  [x] event_entry_viewed (entry_id, entry_age_days)
  [ ] event_screen_abandoned (if user exits without save, time_spent_ms)

Actions:
  [ ] Edit tapped → event_entry_edit_started (entry_id)
  [ ] Save entry → event_entry_edit_saved (entry_id, char_count, duration_ms, tags_added_count)
  [ ] Cancel edit → event_entry_edit_cancelled (entry_id, unsaved_changes: true/false)
  [ ] Delete entry → event_entry_deleted (entry_id)
  [ ] Share from detail → event_entry_shared (entry_id, share_method)
  [ ] Add/remove tags → event_entry_tag_changed (entry_id, action: added or removed, tag_name)

Offline:
  [ ] Save offline → event_offline_save_recorded (entry_id)
  [ ] Sync when online → event_sync_completed (entries_synced: 1)

Performance:
  [ ] Entry load time → event_feature_performance (feature_name: entry_detail_load, duration_ms)
```

### 5. Search & Filter

```
Screen: Search / Filter UI
Purpose: Query and filter entries

Actions:
  [ ] Search box tapped → event_search_initiated (search_type: full_text)
  [ ] Tag filter tapped → event_filter_applied (filter_type: tag, filter_value)
  [ ] Date range selected → event_filter_applied (filter_type: date_range, date_start, date_end)
  [ ] Results shown → event_search_completed (search_type, result_count, duration_ms)
  [ ] Filter cleared → event_filter_cleared (filter_type)

Performance:
  [ ] Search API call time → event_feature_performance (feature_name: search_api, duration_ms)
```

### 6. Settings

```
Screen: User Settings
Purpose: Manage preferences, account, sync

Navigation:
  [ ] event_settings_opened
  [ ] event_screen_abandoned

Settings Changes:
  [ ] Notification toggle → event_notification_setting_changed (setting_name: push_notifications, new_value: true/false)
  [ ] Theme changed → event_theme_changed (new_theme: light, dark, auto)
  [ ] Privacy/export setting → event_privacy_setting_changed (setting_name, new_value)
  [ ] Backup enabled/disabled → event_backup_setting_changed (action: enabled or disabled)

Account:
  [ ] View profile → event_settings_section_viewed (section_name: profile)
  [ ] Edit profile → event_profile_edit_started
  [ ] Profile saved → event_profile_saved (fields_changed_count)
  [ ] Logout → event_logout (session_duration_ms)

Legal:
  [ ] Privacy policy viewed → event_privacy_policy_viewed
  [ ] Terms viewed → event_terms_of_service_viewed
  [ ] Support contacted → event_support_contacted (support_type, topic)
```

### 7. Premium / Subscription

```
Screen: Premium Upsell / Subscription
Purpose: Premium features and upgrades

Navigation:
  [ ] event_premium_viewed (ui_section: home_banner, settings_upsell, feature_gate)

Actions:
  [ ] Premium purchase initiated → event_premium_purchase_initiated (subscription_type: monthly or annual, price)
  [ ] Purchase completed → event_premium_purchase_completed (subscription_type, price, duration_days)
  [ ] Purchase failed → event_premium_purchase_failed (error_code, error_message_hash)
  [ ] Subscription cancelled → event_subscription_cancelled (reason)
  [ ] Restore purchase → event_subscription_restored (subscription_type)
```

### 8. Sync & Backup

```
Screen: Sync / Backup UI
Purpose: Cloud sync and backup management

Actions:
  [ ] Manual sync triggered → event_sync_started (entry_count)
  [ ] Sync completed → event_sync_completed (duration_ms, entries_synced, conflicts_resolved: count)
  [ ] Sync failed → event_sync_failed (error_code: auth_error, network_error, quota_exceeded, duration_ms, retry_auto: true/false)
  [ ] Offline mode toggled → event_offline_mode_enabled or event_offline_mode_disabled
  [ ] Restore from backup → event_data_restored (source: cloud or local, entries_restored: count, duration_ms)
```

### 9. Notifications

```
Screen/Flow: Push Notifications
Purpose: Notification delivery and user interaction

Actions:
  [ ] Notification delivered → event_notification_delivered (notification_type: daily_reminder or feature_update, delivered_time)
  [ ] Notification tapped → event_notification_tapped (notification_type, deep_link_target)
  [ ] Notification dismissed → event_notification_dismissed (notification_type)
```

### 10. Deep Links / External Entry

```
Cross-App: Deep Link Handling
Purpose: Cross-app navigation and sharing

Actions:
  [ ] Deep link opened → event_deep_link_opened (deep_link: entry_id or url, source: share, notification, sms)
  [ ] Shared link (export) → event_entry_shared (share_method: link, share_platform: message or email)
```

---

## Completion Criteria

When you've gone through all screens:

- [ ] Every screen has `event_<screen>_viewed` at entry
- [ ] Primary CTAs each have mapped events
- [ ] All error paths emit event_<action>_failed
- [ ] All major settings have event_<setting>_changed
- [ ] Performance metrics for slow operations (sync, search, load)
- [ ] First-time experiences logged separately
- [ ] All parameters use consistent tag names (from [event-tags.md](./event-tags.md))
- [ ] No hardcoded event strings—all use `AnalyticsEvents.kt` constants
- [ ] No PII in any parameters
- [ ] Firebase Analytics Console shows all events appearing in real-time (with test events)

---

## Quick Missing Event Audit

**Run this audit if you suspect gaps:**

1. [ ] Search "event_" in codebase—count occurrences (less than 50? probably missing events)
2. [ ] Check error handling—is every catch block logging an event?
3. [ ] Check screen transitions—does every screen log `_viewed`?
4. [ ] Check abandonment—can you detect when users quit a feature without action?
5. [ ] Check settings—is every toggle/preference change tracked?
