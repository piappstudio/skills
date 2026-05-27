# Code Review Checklist

Use this for quick audits. For detailed analysis, reference the full documentation in `/references/`.

## Kotlin Idioms ✓

- [ ] No non-nullable force unwrap (`.!!`) unless absolutely safe
- [ ] Nullable types use `?.`, `?:`, or `.let` appropriately
- [ ] Collections use immutable types (`List`, `Map`, `Set`) by default
- [ ] Extension functions used instead of utility objects
- [ ] Scope functions (`apply`, `run`, `let`, `with`) used correctly
- [ ] Data classes for value objects instead of regular classes

## KMP Patterns ✓

- [ ] expect/actual signatures match precisely across platforms
- [ ] Platform-specific logic not in `commonMain`
- [ ] expect classes only where platform-specific implementation needed
- [ ] String/resource constants in `commonMain` when possible
- [ ] Module organization follows standard structure (data, domain, presentation)
- [ ] No unnecessary public APIs exposed from expect/actual

## Coroutine Safety ✓

- [ ] No `GlobalScope` usage
- [ ] Proper scope used (`viewModelScope`, `launchEffect`, etc.)
- [ ] All long-running operations launched in coroutines
- [ ] `CancellationException` not suppressed
- [ ] Exception handling covers all paths (try/catch or CoroutineExceptionHandler)
- [ ] `delay()` used instead of `Thread.sleep()`
- [ ] Job/Deferred results awaited when necessary
- [ ] Timeouts set for network/long operations
- [ ] Flow backpressure considered
- [ ] No blocking operations inside coroutines

## Code Standards ✓

### Naming
- [ ] Classes/Interfaces: `PascalCase`
- [ ] Functions/Variables: `camelCase`
- [ ] Constants: `UPPER_SNAKE_CASE`
- [ ] Private members respect convention
- [ ] Names are descriptive (no single letters except type parameters)
- [ ] Boolean properties prefixed with `is` or `has`

### Visibility
- [ ] Public APIs documented with KDoc
- [ ] Visibility modifiers explicit for public APIs
- [ ] Internal/Private used to hide implementation details
- [ ] No `public` modifier (it's default)

### Type Safety
- [ ] Non-nullable by default, nullable only when needed
- [ ] Generics bounded appropriately
- [ ] No unchecked casts
- [ ] Sealed types for enum-like behavior
- [ ] `Any` type avoided when possible

## Anti-patterns ✓

- [ ] No GlobalScope launch
- [ ] No launch in init without scope
- [ ] No commented-out code
- [ ] No mutable collections exposed
- [ ] No string concatenation in loops
- [ ] No overcomplication (KISS principle)
- [ ] No platform-specific logic in commonMain
- [ ] No mismatched expect/actual signatures

## Documentation ✓

- [ ] Public functions have KDoc
- [ ] Complex logic has comments explaining "why" not "what"
- [ ] expect functions document expected behavior across platforms
- [ ] No obvious comments ("// increment counter")

## Performance ✓

- [ ] Large collections use `asSequence()` before chaining operations
- [ ] No repeated expensive calculations
- [ ] Collections not created unnecessarily in hot paths
- [ ] Lazy initialization used for expensive resources
- [ ] Batch database operations (no loops)

## Testing Readiness ✓

- [ ] Dependencies injectable (not hidden in objects)
- [ ] Pure functions where possible
- [ ] Side effects isolated
- [ ] No testing of private functions
