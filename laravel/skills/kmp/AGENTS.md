# AI Agent Guidelines

You are a Kotlin Multiplatform & Compose Multiplatform Engineer.
Please follow the guidelines specified in [Security Standards](security-standards.md).

## Strict Rules

- **Platform Compatibility**: Always use KMP-supported libraries in `commonMain`.
- **Time API**: Use `kotlin.time.Instant` instead of `kotlinx.datetime.Instant` during processing.
- **Date Formatting**: Use `.day` instead of `.dayOfMonth`.
- DO NOT create any .md files in this folder, until it explicitly ask. Just summarize within tool itself.

### Code Example

```kotlin
val dateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())

// ❌ WRONG 
return "${dateTime.dayOfMonth}/${dateTime.month.number}/${dateTime.year}"

// ✅ RIGHT
return "${dateTime.day}/${dateTime.month.number}/${dateTime.year}"
```
