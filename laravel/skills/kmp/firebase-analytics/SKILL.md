---
name: firebase-analytics-kmp
description: 'Systematic Firebase Analytics event tracking for Kotlin Multiplatform. Use when: designing event schemas, implementing user event tracking, ensuring consistency across platforms (Android/iOS), validating event coverage, avoiding missed tracking points, establishing naming conventions.'
argument-hint: 'Describe your event tracking task: schema design, implementation, coverage audit, or best practices'
---

# Firebase Analytics for Kotlin Multiplatform

Expert guidance for systematic, comprehensive user event tracking across Android and iOS using Firebase Analytics in KMP projects.

## When to Use

- **Design Phase**: Define event schemas and tag naming conventions before implementation
- **Implementation**: Add event tracking to screens and features systematically
- **Audit**: Verify event coverage and identify missed tracking points
- **Standards**: Establish team conventions for consistency across platforms

## Core Principles

1. **Consistency**: Same event names and parameters across all platforms (Android/iOS)
2. **Completeness**: Systematic coverage—no missed tracking points
3. **Clarity**: Self-documenting event names and parameter tags
4. **Efficiency**: Reusable tracker patterns to reduce boilerplate

## Event Design Workflow

### 1. Define Event Schema

Before implementation, catalog all events your app should track:

```
Screen/Feature → User Actions → Event Name + Parameters

Example: LoginScreen
├── User enters email → event_login_attempt (email_domain)
├── User taps "forgot password" → event_forgot_password_clicked
└── User completes verification → event_login_success (login_method)
```

**Naming Convention**: `event_<action>_<context>`
- All lowercase, snake_case
- Examples: `event_diary_entry_saved`, `event_authentication_failed`, `event_premium_upgrade_viewed`

### 2. Document Tag Names

Create a **centralized tag reference** (`.github/skills/firebase-analytics/references/event-tags.md`):

```
Global Tags:
- user_id: Unique user identifier (track across sessions)
- session_id: Session identifier
- platform: android | ios | web

Screen/Feature Tags:
- diary_entry_id: ID of diary entry (event_diary_entry_saved)
- error_code: Error type for failures (event_sync_failed)
- duration_ms: How long an action took (event_export_completed)
```

**Tag Rules**:
- All lowercase snake_case
- Max 64 characters
- Reuse tags across events (e.g., `duration_ms`, `error_code`)
- Document units for time/count params

### 3. Event Tracking Checklist

For each screen/feature, verify these tracking points. Use this [event tracking checklist](./references/event-coverage-checklist.md):

- [ ] Screen/Feature loaded → `event_<screen>_viewed`
- [ ] Primary user action → `event_<action>_completed`
- [ ] Error/failure state → `event_<action>_failed`
- [ ] User abandonment (navigate away) → `event_<screen>_abandoned`
- [ ] Settings/preference changes → `event_<setting>_changed`
- [ ] Deep link/external entry → `event_deep_link_opened`

## Implementation Patterns

### Pattern 1: Common Event Logger

Define a shared interface in `commonMain`:

```kotlin
// composeApp/src/commonMain/kotlin/analytics/FirebaseAnalytics.kt
expect class AnalyticsLogger {
    fun logEvent(name: String, params: Map<String, Any> = emptyMap())
    fun setUserProperty(name: String, value: String)
}

object AnalyticsEvents {
    const val DIARY_ENTRY_SAVED = "event_diary_entry_saved"
    const val LOGIN_ATTEMPT = "event_login_attempt"
    // ... all events
    
    object Params {
        const val ENTRY_ID = "diary_entry_id"
        const val LOGIN_METHOD = "login_method"
        const val DURATION_MS = "duration_ms"
    }
}
```

### Pattern 2: Platform Implementations

**Android** (`androidMain`):
```kotlin
actual class AnalyticsLogger {
    private val firebase = Firebase.analytics
    
    actual fun logEvent(name: String, params: Map<String, Any>) {
        val bundle = Bundle().apply {
            params.forEach { (k, v) ->
                when (v) {
                    is String -> putString(k, v)
                    is Int -> putInt(k, v)
                    is Long -> putLong(k, v)
                    is Double -> putDouble(k, v)
                    is Boolean -> putBoolean(k, v)
                }
            }
        }
        firebase.logEvent(name, bundle)
    }
}
```

**iOS** (`iosMain`):
```kotlin
actual class AnalyticsLogger {
    actual fun logEvent(name: String, params: Map<String, Any>) {
        Analytics.logEvent(withName: name, parameters: params)
    }
}
```

### Pattern 3: Systematic Tracking in Screens

Use ViewModel/StateHolder to centralize analytics:

```kotlin
class DiaryScreenViewModel(
    private val analytics: AnalyticsLogger
) {
    init {
        analytics.logEvent(
            AnalyticsEvents.DIARY_VIEWED,
            mapOf("platform" to currentPlatform)
        )
    }
    
    fun onSaveEntry(entry: DiaryEntry) {
        try {
            repository.saveEntry(entry)
            analytics.logEvent(AnalyticsEvents.DIARY_ENTRY_SAVED, mapOf(
                AnalyticsEvents.Params.ENTRY_ID to entry.id,
                AnalyticsEvents.Params.DURATION_MS to (System.currentTimeMillis() - startTime)
            ))
        } catch (e: Exception) {
            analytics.logEvent(AnalyticsEvents.DIARY_ENTRY_SAVE_FAILED, mapOf(
                "error_code" to e.javaClass.simpleName
            ))
            throw e
        }
    }
}
```

## Coverage Validation

### Audit Checklist

Run through this before shipping analytics-related features:

1. **Event Naming**: All events follow `event_<action>_<context>` convention
2. **Parameter Consistency**: Same parameters use same tag names across all events
3. **Platform Parity**: Event coverage identical on Android and iOS (use shared VM)
4. **Error Tracking**: All error paths logged with error_code/reason
5. **Duplicate Prevention**: No missed events, but no redundant tracking
6. **User Privacy**: No PII in parameters (use ID references, not emails/names)

### Common Missed Events

- ❌ User navigates away (abandonment signals)
- ❌ Feature first time seen (onboarding tracking)
- ❌ Deep link/external navigation origin
- ❌ Settings/preference changes
- ❌ Error states and exception details
- ❌ Performance metrics (load time, sync duration)

## Best Practices

### Do's
✅ Log user actions at appropriate granularity (not too frequent, not too sparse)
✅ Include sufficient context in parameters (but avoid high-cardinality data)
✅ Test events locally before releasing (Firebase Console preview)
✅ Document event meanings in team wiki/comments
✅ Review analytics quarterly to identify gaps

### Don'ts
❌ Track PII (emails, names, phone numbers) in event parameters
❌ Log every keystroke (collect app engagement metrics separately)
❌ Hardcode event strings (use constants object like `AnalyticsEvents`)
❌ Skip error event logging (these are critical for debugging)
❌ Mix event logging with business logic (inject logger dependency)

## Next Steps

1. Start with [event-tags reference](./references/event-tags.md)—list all events and parameters for your app
2. Use [event-coverage-checklist](./references/event-coverage-checklist.md)—verify every screen
3. Create `AnalyticsLogger` expect/actual in your KMP project
4. Implement patterns for each screen/feature systematically
5. Validate in Firebase Analytics Console (real-time dashboard)

## Troubleshooting

**Events not showing in Firebase Console?**
- Check event names match exactly (case-sensitive)
- Verify parameters are serializable (no custom objects)
- Ensure `setUserId()` or `setUserProperty()` called before events

**Missing events in production?**
- Audit implementation against checklist
- Check for swallowed exceptions (error events not logged)
- Validate SDK integration on each platform

---

**Learn More**: [Firebase Analytics Best Practices](https://firebase.google.com/docs/analytics/events) | [KMP Expect/Actual](https://kotlinlang.org/docs/multiplatform-expect-actual.html)
