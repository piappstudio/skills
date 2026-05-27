# MVI Architecture Fundamentals

## The MVI Cycle

```
User Action (Intent) 
    ↓
ViewModel processes Intent
    ↓
Calls UseCase (business logic)
    ↓
UseCase returns data/result
    ↓
ViewModel updates UiState
    ↓
ViewModel emits SideEffect (if needed)
    ↓
UI observes state & renders
    ↓
UI observes effect & reacts (navigate, toast, etc)
    ↓
User interacts again...
```

## Core Concepts

### 1. UiState - Immutable State

Everything the UI needs to render in one snapshot.

```kotlin
data class UserUiState(
    val isLoading: Boolean = false,
    val user: User? = null,
    val error: String? = null,
    val isEmpty: Boolean = true,
    val selectedUserId: String? = null
)
```

**Key Principles:**
- Immutable (data class)
- Defaults provided
- All fields UI-relevant
- Can be serialized (for state persistence)
- Single source of truth

### 2. Intent - User Actions

Sealed class representing all user interactions.

```kotlin
sealed class UserIntent {
    data class LoadUser(val id: String) : UserIntent()
    data class DeleteUser(val id: String) : UserIntent()
    object RefreshUsers : UserIntent()
    object ClearError : UserIntent()
}
```

**Key Principles:**
- One intent per action
- Sealed class for exhaustiveness
- Include action parameters
- Immutable
- No side effects in creation

### 3. SideEffect - One-Time Events

Events that happen once, not captured in state.

```kotlin
sealed class UserSideEffect {
    data class ShowToast(val message: String) : UserSideEffect()
    data class NavigateToDetail(val userId: String) : UserSideEffect()
    object ShowDeleteConfirm : UserSideEffect()
    data class ShowError(val exception: Throwable) : UserSideEffect()
}
```

**Key Principles:**
- Not part of state
- Consumed once
- Emit for navigation, toasts, dialogs
- Never lost if not collected

### 4. UseCase - Business Logic

Isolated, testable unit of business logic.

```kotlin
class GetUserUseCase(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(id: String): Result<User> {
        return try {
            val user = userRepository.getUser(id)
            Result.Success(user)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}
```

**Key Principles:**
- Single responsibility
- Suspend function for async
- Throws or returns Result
- Injectable dependencies
- Highly testable
- No ViewModel dependencies

### 5. ViewModel - Orchestrator

Processes intents, manages state, emits effects.

```kotlin
class UserViewModel(
    private val getUserUseCase: GetUserUseCase,
    private val deleteUserUseCase: DeleteUserUseCase
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(UserUiState())
    val uiState: StateFlow<UserUiState> = _uiState
    
    private val _sideEffect = Channel<UserSideEffect>()
    val sideEffect = _sideEffect.receiveAsFlow()
    
    fun handleIntent(intent: UserIntent) {
        viewModelScope.launch {
            when (intent) {
                is UserIntent.LoadUser -> loadUser(intent.id)
                is UserIntent.DeleteUser -> deleteUser(intent.id)
                // ...
            }
        }
    }
    
    private suspend fun loadUser(id: String) {
        _uiState.update { it.copy(isLoading = true) }
        
        val result = getUserUseCase(id)
        _uiState.update {
            when (result) {
                is Result.Success -> it.copy(
                    isLoading = false,
                    user = result.data
                )
                is Result.Error -> it.copy(
                    isLoading = false,
                    error = result.exception.message
                )
            }
        }
    }
}
```

## State Management Patterns

### Initial State
```kotlin
val initialState = UserUiState(
    isLoading = false,
    users = emptyList(),
    selectedUser = null,
    error = null
)
```

### State Transitions
```kotlin
// Loading state
_uiState.value = uiState.value.copy(isLoading = true, error = null)

// Success state
_uiState.value = uiState.value.copy(
    isLoading = false, 
    users = users,
    error = null
)

// Error state
_uiState.value = uiState.value.copy(
    isLoading = false,
    error = exception.message
)
```

### State Immutability
```kotlin
// ✅ CORRECT - Create new object with copy()
state = state.copy(selectedUser = user)

// ❌ WRONG - Mutating existing state
state.selectedUser = user
```

## Intent Processing Patterns

### Sequential Intent Handling
```kotlin
fun handleIntent(intent: UserIntent) {
    viewModelScope.launch {
        when (intent) {
            is UserIntent.LoadUser -> {
                _uiState.update { it.copy(isLoading = true) }
                val result = useCase(intent.id)
                _uiState.update { /* update with result */ }
            }
            is UserIntent.ClearError -> {
                _uiState.update { it.copy(error = null) }
            }
        }
    }
}
```

### Debouncing Search Intent
```kotlin
private val searchQuery = MutableStateFlow("")

init {
    viewModelScope.launch {
        searchQuery
            .debounce(300)
            .distinctUntilChanged()
            .collect { query ->
                handleIntent(UserIntent.Search(query))
            }
    }
}

fun onSearchQueryChanged(query: String) {
    searchQuery.value = query
}
```

## SideEffect Patterns

### Navigation SideEffect
```kotlin
sealed class UserSideEffect {
    data class NavigateToDetail(val userId: String) : UserSideEffect()
    object NavigateBack : UserSideEffect()
}

// In ViewModel
_sideEffect.send(UserSideEffect.NavigateToDetail(userId))

// In UI (Compose)
LaunchedEffect(Unit) {
    viewModel.sideEffect.collect { effect ->
        when (effect) {
            is UserSideEffect.NavigateToDetail -> 
                navController.navigate("detail/${effect.userId}")
            UserSideEffect.NavigateBack -> 
                navController.popBackStack()
        }
    }
}
```

### Toast SideEffect
```kotlin
sealed class UserSideEffect {
    data class ShowMessage(val message: String) : UserSideEffect()
}

// In ViewModel
_sideEffect.send(UserSideEffect.ShowMessage("User deleted"))

// In UI (Fragment or Compose)
lifecycleScope.launch {
    viewModel.sideEffect.collect { effect ->
        when (effect) {
            is UserSideEffect.ShowMessage -> 
                Toast.makeText(context, effect.message, Toast.LENGTH_SHORT).show()
        }
    }
}
```

## Error Handling in MVI

### Approach 1: Store in State
```kotlin
data class UserUiState(
    val isLoading: Boolean = false,
    val user: User? = null,
    val error: String? = null
)

// Clear error intent
is UserIntent.ClearError -> {
    _uiState.update { it.copy(error = null) }
}
```

### Approach 2: Side Effect
```kotlin
sealed class UserSideEffect {
    data class ShowError(val message: String) : UserSideEffect()
}

// Show error and auto-dismiss
_sideEffect.send(UserSideEffect.ShowError(exception.message ?: "Unknown error"))
```

### Approach 3: Both (Recommended)
```kotlin
// Store in state for UI display
_uiState.update { it.copy(error = exception.message) }

// Also emit as side effect for special handling
_sideEffect.send(UserSideEffect.ErrorOccurred(exception))
```

## Testing MVI

### ViewModel Testing
```kotlin
@Test
fun testLoadUserIntent() = runTest {
    val mockUseCase = mockk<GetUserUseCase>()
    val testUser = User(id = "1", name = "Alice")
    coEvery { mockUseCase(any()) } returns Result.Success(testUser)
    
    val viewModel = UserViewModel(mockUseCase)
    
    // Act
    viewModel.handleIntent(UserIntent.LoadUser("1"))
    advanceUntilIdle()
    
    // Assert
    val state = viewModel.uiState.value
    assert(state.user == testUser)
    assert(!state.isLoading)
}
```

### UseCase Testing
```kotlin
@Test
fun testGetUserUseCase() = runTest {
    val repository = mockk<UserRepository>()
    val user = User(id = "1", name = "Bob")
    coEvery { repository.getUser("1") } returns user
    
    val useCase = GetUserUseCase(repository)
    val result = useCase("1")
    
    assert(result is Result.Success)
    assert((result as Result.Success).data == user)
}
```
