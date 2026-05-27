# MVI Implementation Checklist

Use this checklist to verify your MVI implementation is complete and follows best practices.

## Folder Structure ✓

- [ ] `/domain/usecase/` folder created with UseCase classes
- [ ] `/domain/model/` folder contains domain entities
- [ ] `/domain/repository/` has repository interfaces (contracts)
- [ ] `/presentation/state/` contains UiState data class
- [ ] `/presentation/intent/` contains Intent sealed class
- [ ] `/presentation/side_effect/` contains SideEffect sealed class
- [ ] `/presentation/viewmodel/` contains MVI ViewModel
- [ ] `/ui/` contains Composables/Fragments

## UseCase Implementation ✓

- [ ] Each UseCase has single responsibility (one use case = one operation)
- [ ] UseCase has suspend `invoke()` function (operator overload)
- [ ] All dependencies injected in constructor
- [ ] Error handling with try/catch or Result wrapper
- [ ] No UI framework dependencies in UseCases
- [ ] UseCases are testable without mocking framework
- [ ] Return type is strong (Result<T> or specific type)
- [ ] Logging added for important operations

## UiState ✓

- [ ] Is a data class (immutable)
- [ ] All UI-needed properties included
- [ ] Proper defaults provided
- [ ] No mutable collections
- [ ] Never null for optional fields (use nullable wrapper or default)
- [ ] Can be serialized (if state persistence needed)
- [ ] Single source of truth for UI rendering

## Intent ✓

- [ ] Is a sealed class
- [ ] One subclass per user action
- [ ] Parameters capture action details
- [ ] No side effects in construction
- [ ] All intents handled in ViewModel
- [ ] Intent names are action-oriented (verb + noun)
- [ ] No UI logic in intent creation

## SideEffect ✓

- [ ] Is a sealed class
- [ ] For navigation, toasts, dialogs, errors (not state)
- [ ] One-time consumption (not stored in state)
- [ ] Immutable data classes
- [ ] Proper typing (NavigateTo, ShowMessage, etc)
- [ ] ViewModel emits, UI collects
- [ ] No blocking operations in effect handling

## ViewModel ✓

- [ ] Injects UseCases (not repositories)
- [ ] Has single `handleIntent(Intent)` public entry point
- [ ] Exposes immutable StateFlow for UiState
- [ ] Exposes Flow for SideEffect (via Channel)
- [ ] Private suspend functions for intent handling
- [ ] Updates state using `.update { }` pattern
- [ ] Emits side effects via channel
- [ ] Uses viewModelScope for coroutines
- [ ] No direct state mutation, only via copy()
- [ ] Initializes with default/loading state

## State Management ✓

- [ ] All state updates use `.update { it.copy(...) }`
- [ ] Never mutates state directly
- [ ] Loading state managed (UiState contains isLoading flag)
- [ ] Error state managed (UiState contains error field)
- [ ] Cleared error properly ("clear error" intent)
- [ ] State immutability enforced
- [ ] No state leakage between features

## Intent Handling ✓

- [ ] All intent types handled in `when` expression
- [ ] No missing intent cases (sealed class exhaustiveness)
- [ ] Each intent triggers appropriate state change
- [ ] Intent processing is deterministic
- [ ] No race conditions in concurrent intents
- [ ] Debouncing/throttling applied where needed (search, etc)
- [ ] Proper scope for long operations (viewModelScope)

## Side Effect Handling ✓

- [ ] ViewModel emits effects for one-time events
- [ ] UI collects effects in LaunchedEffect
- [ ] Effects consumed efficiently (no duplication)
- [ ] Navigation emitted as side effect
- [ ] Messages/toasts emitted as side effect
- [ ] Dialogs triggered via side effect
- [ ] No state stored for one-time events

## UI Integration ✓

- [ ] Composable collects `uiState` with `collectAsState()`
- [ ] Composable collects `sideEffect` with `LaunchedEffect`
- [ ] All user interactions emit intents
- [ ] No direct ViewModel method calls (only intents)
- [ ] UI renders based on state, not ViewModel
- [ ] Side effects handled appropriately (toast, navigate, dialog)
- [ ] No loading state flash (proper state transitions)

## Dependency Injection (Koin) ✓

- [ ] UseCase module created with factory { }
- [ ] Repository module created with single { }
- [ ] ViewModel module created with viewModel { }
- [ ] All modules combined in AppModule
- [ ] startKoin called in Application.onCreate()
- [ ] Viewmodels injected with by viewModel() or koinViewModel()
- [ ] No manual instantiation of ViewModels
- [ ] Appropriate scopes: single (singleton), factory (new instance), viewModel

## Error Handling ✓

- [ ] All UseCase exceptions caught
- [ ] Result wrapper used consistently
- [ ] ViewModel handles both Success and Error results
- [ ] Error stored in UiState for UI display
- [ ] Critical errors emitted as SideEffect
- [ ] Network errors handled gracefully
- [ ] Validation errors clear and user-friendly
- [ ] Stack traces logged (not shown to user)

## Testing ✓

- [ ] UseCase unit tests with mocked repository
- [ ] ViewModel tests with mocked UseCases
- [ ] Integration tests for repository + UseCase
- [ ] UI tests for Composable integration
- [ ] Tests verify state transitions
- [ ] Tests verify side effect emission
- [ ] Tests use runTest for coroutines
- [ ] Mock objects used appropriately (mockk or Mockito)

## Performance ✓

- [ ] No unnecessary state updates
- [ ] State flows use distinct() where appropriate
- [ ] No memory leaks from coroutines
- [ ] Lazy loading implemented where possible
- [ ] Pagination handled if needed
- [ ] Image loading optimized
- [ ] Database queries optimized

## Code Quality ✓

- [ ] Clear naming: UserUiState, UserIntent, UserSideEffect, etc
- [ ] KDoc comments on public functions
- [ ] No commented-out code
- [ ] Consistent formatting
- [ ] No duplicate code
- [ ] No over-engineering
- [ ] Readability prioritized

## Documentation ✓

- [ ] Feature documented in README
- [ ] MVI pattern explained in code comments
- [ ] UseCase purpose documented
- [ ] Complex state transitions explained
- [ ] Integration points documented
- [ ] Testing strategy documented

## Gradual Migration ✓ (if refactoring existing code)

- [ ] UseCases extracted first
- [ ] State/Intent/SideEffect added without breaking ViewModel
- [ ] UI gradually migrated to use intents
- [ ] Old ViewModel methods can coexist during transition
- [ ] Tests added for new MVI parts
- [ ] Old code removed once UI migrated
