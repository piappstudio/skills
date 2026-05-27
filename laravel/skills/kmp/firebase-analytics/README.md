# Firebase Analytics for KMP - Skill Overview

This skill provides a **systematic, comprehensive approach** to Firebase Analytics event tracking in Kotlin Multiplatform projects—specifically designed to ensure no key events are missed and maintain consistency across platforms (Android/iOS).

## Quick Start (5 minutes)

1. **Run the event coverage audit**: Use [event-coverage-checklist.md](./references/event-coverage-checklist.md) to identify what events exist and spot gaps.

2. **Review your event schema**: Check [event-tags.md](./references/event-tags.md) for standardized event names and parameter tags.

3. **Copy the templates**: Use the code samples in [assets/](./assets/) as boilerplate:
   - `AnalyticsEvents.kt` - Centralized event & parameter constants
   - `FirebaseAnalytics.android.kt` - Android platform implementation
   - `FirebaseAnalytics.ios.kt` - iOS platform implementation
   - `DiaryListViewModel.example.kt` - Screen implementation pattern

4. **Implement systematically**: Follow the [event design workflow](./SKILL.md#event-design-workflow) in SKILL.md.

## File Structure

```
.github/skills/firebase-analytics/
├── SKILL.md                          # Main guide (read this first)
├── README.md                         # This file
├── references/
│   ├── event-tags.md                 # Event catalog (all events + params)
│   └── event-coverage-checklist.md   # Screen-by-screen audit checklist
└── assets/
    ├── AnalyticsEvents.kt            # Shared constants
    ├── FirebaseAnalytics.android.kt  # Android expect/actual
    ├── FirebaseAnalytics.ios.kt      # iOS expect/actual
    └── DiaryListViewModel.example.kt # Implementation pattern
```

## Key Principles

✅ **Consistency**: Same event names and parameters across Android/iOS (use expect/actual pattern)
✅ **Completeness**: Systematic checklist to catch all tracking points (no missed events)
✅ **Clarity**: Self-documenting event names and parameter tags
✅ **Efficiency**: Reusable patterns (copy-paste ready templates)

## When to Use This Skill

- **Starting fresh**: Design your event schema before writing code
- **Adding features**: Verify event coverage for new screens using the checklist
- **Refactoring**: Audit existing analytics for gaps or inconsistencies
- **Code reviews**: Use as a checklist for Firebase analytics PRs

## Common Workflows

### Workflow 1: Design Events for a New Screen

1. Open [event-tags.md](./references/event-tags.md)
2. Add your new events to the appropriate category
3. Reference the checklist in [event-coverage-checklist.md](./references/event-coverage-checklist.md) for required tracking points
4. Add constants to `AnalyticsEvents.kt`
5. Implement using the ViewModel pattern from `DiaryListViewModel.example.kt`

### Workflow 2: Audit Existing Analytics

1. Go through [event-coverage-checklist.md](./references/event-coverage-checklist.md) screen by screen
2. Mark ✅ for events you've already implemented
3. Identify gaps (missing navigation logs, error tracking, performance metrics)
4. Add missing events to your codebase
5. Validate in Firebase Analytics Console (real-time event preview)

### Workflow 3: Fix Inconsistent Event Naming

1. Grep your codebase for `logEvent(` calls
2. Compare against canonical event names in `AnalyticsEvents.kt`
3. Replace all hardcoded strings with constants (e.g., `"event_login_success"` → `AnalyticsEvents.LOGIN_SUCCESS`)
4. Test in Firebase Analytics Console to ensure events still appear

## Troubleshooting

**Q: Events not appearing in Firebase Console?**
- [ ] Event name matches exactly (case-sensitive: `event_login_success`, not `event_loginSuccess`)
- [ ] Parameters are serializable (strings, numbers, booleans only—no custom objects)
- [ ] Called `setUserId()` or `setUserProperty()` before logging events
- [ ] Wait 15-30 minutes for Firebase Console to process

**Q: Too many duplicate events?**
- [ ] Audit `logEvent()` calls—you may be logging the same event in multiple places
- [ ] Check for redundant tracking (e.g., logging both `_viewed` and `_loaded` for same screen)
- [ ] Use referrer parameters to distinguish duplicate events (e.g., `screen_viewed` with different `source`)

**Q: Unsure if we're missing events?**
- [ ] Run the audit in [event-coverage-checklist.md](./references/event-coverage-checklist.md) line-by-line
- [ ] Count total event constants: <50? Probably missing major category of events
- [ ] Check `catch` blocks: every failure should emit an error event

## Next Steps

1. **Get started**: Read [SKILL.md](./SKILL.md) top to bottom
2. **Plan your events**: Use [event-tags.md](./references/event-tags.md) to document what you're tracking
3. **Audit coverage**: Work through [event-coverage-checklist.md](./references/event-coverage-checklist.md)
4. **Implement patterns**: Copy code from [assets/](./assets/) and adapt to your screens
5. **Validate**: Check Firebase Analytics Console in real-time

---

**Related Skills**: 
- [kmp-kotlin-best-practices](../) - Kotlin Multiplatform patterns
- [mvi-architecture](../) - State management for analytics-heavy screens

**Official Docs**:
- Firebase Analytics: https://firebase.google.com/docs/analytics
- KMP Multiplatform: https://kotlinlang.org/docs/multiplatform.html
- KMP Expect/Actual: https://kotlinlang.org/docs/multiplatform-expect-actual.html
