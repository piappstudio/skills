---
name: kmp-kotlin-best-practices
description: 'Review, audit, and improve Kotlin Multiplatform (KMP) code. Use when: implementing KMP features, refactoring Kotlin code, checking coroutine usage, ensuring coding standards, optimizing for expect/actual patterns, managing platform-specific code.'
argument-hint: 'Specify target files, modules, or areas to review (e.g., "coroutine management", "expect/actual pattern", "naming conventions")'
---

# KMP & Kotlin Best Practices

A comprehensive skill for ensuring high-quality, maintainable Kotlin code in Kotlin Multiplatform projects. Covers best practices for common-source code, platform-specific implementations, coroutines, and coding standards.

## When to Use



- **Writing new KMP features**: Ensure proper use of expect/actual, platform APIs, and shared logic
- **Code review**: Audit for coroutine safety, resource leaks, threading issues
- **Refactoring**: Migrate code to idiomatic Kotlin and KMP patterns
- **Coroutine checks**: Validate proper scope management, exception handling, cancellation
- **Naming & style**: Enforce project coding standards and Kotlin conventions
- **Performance**: Identify inefficiencies in common-source and platform-specific code

## Quick Start

Choose the focus area:

| Area | Focus |
|------|-------|
| [Kotlin Idioms](./references/kotlin-idioms.md) | Naming, nullability, scoping, extension functions |
| [KMP Patterns](./references/kmp-patterns.md) | expect/actual, platform APIs, commonMain structure |
| [Coroutine Safety](./references/coroutine-safety.md) | Scope management, exception handling, cancellation |
| [Code Standards](./references/code-standards.md) | Project conventions, style guide, anti-patterns |

## Audit Procedure

### Phase 1: Scope & Gather
1. Identify target file(s) or module (e.g., `composeApp/src/commonMain/`)
2. Determine focus area from the Quick Start table above
3. Load the relevant reference document

### Phase 2: Analysis
1. Read target source files line-by-line
2. Cross-reference against best practices checklist
3. Identify deviations, risks, and improvement opportunities
4. Document findings with code snippets

### Phase 3: Review & Recommend
1. Summarize findings by category (critical, important, nice-to-have)
2. Provide code examples for each recommendation
3. Suggest refactored versions where applicable
4. Prioritize by impact and effort

### Phase 4: Implement & Verify
1. Apply recommended changes one at a time
2. Run validation (compilation, type-checking)
3. Test coroutine scenarios if changes touch concurrency
4. Document rationale for changes

## Common Audits

### Coroutine Safety Audit
**Goal**: Ensure proper scope, exception handling, and cancellation.

```
1. Check all ViewModelScope, lifecycleScope usage
2. Verify try/catch or CoroutineExceptionHandler coverage
3. Ensure cancellation propagates correctly
4. Validate Job and Deferred handling
```

### expect/actual Pattern Audit
**Goal**: Verify platform implementations match interface and handle edge cases.

```
1. Confirm expect declarations in commonMain
2. Check actual implementations match signature
3. Validate platform-specific behavior is necessary
4. Ensure no logic duplication across platforms
5. Check error handling consistency
```

### Naming & Style Audit
**Goal**: Ensure code follows Kotlin conventions and project standards.

```
1. Classes: PascalCase (public APIs)
2. Functions/Variables: camelCase
3. Constants (companion object, top-level): UPPER_SNAKE_CASE
4. Private members: _camelCase or camelCase (convention)
5. Extension functions: verb + context
6. Type parameters: Single letter (T, R, K, V)
```

## Reference Documents

- **[Kotlin Idioms](./references/kotlin-idioms.md)**: Null safety, scoping, extension functions, delegation
- **[KMP Patterns](./references/kmp-patterns.md)**: expect/actual, tree structure, platform APIs
- **[Coroutine Safety](./references/coroutine-safety.md)**: Scopes, exception handling, cancellation
- **[Code Standards](./references/code-standards.md)**: Project conventions, style guide
- **[Anti-patterns](./references/anti-patterns.md)**: Common pitfalls and how to avoid them

## Quick Checklist

Use this [checklist](./checklists/code-review-checklist.md) for fast reviews.
