# Step-by-Step Conversion Guide: ViewModel → MVI

## Overview

Converting an existing ViewModel to MVI architecture takes 5 phases. Each phase can be done incrementally.

## Before We Start - Example ViewModel

```kotlin
class UserViewModel(private val repository: UserRepository) : ViewModel() {
    
    private val _users = MutableStateFlow<List<User>>(emptyList())
    val users: StateFlow<List<User>> = _users.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    fun loadUsers() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val users = repository.getUsers()
                _users.value = users
                _error.value = null
            } catch (e: Exception) {
                _error.value = e.message
                _users.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun deleteUser(userId: String) {
        viewModelScope.launch {
            try {
                repository.deleteUser(userId)
                val updated = repository.getUsers()
                _users.value = updated
            } catch (e: Exception) {
                _error.value = "Failed to delete user"
            }
        }
    }
}
```

---

## Phase 1: Create Domain Folder & Extract Business Logic to UseCases

**Goal**: Move heavy operations from ViewModel to testable UseCases

### Step 1.1: Create UseCase Files

**File**: `domain/usecase/GetUsersUseCase.kt`
```kotlin
class GetUsersUseCase(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(): Result<List<User>> {
        return try {
            val users = userRepository.getUsers()
            Result.Success(users)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}
```

**File**: `domain/usecase/DeleteUserUseCase.kt`
```kotlin
class DeleteUserUseCase(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(userId: String): Result<List<User>> {
        return try {
            userRepository.deleteUser(userId)
            val updatedUsers = userRepository.getUsers()
            Result.Success(updatedUsers)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}
```

### Step 1.2: Create Result Wrapper

**File**: `domain/util/Result.kt`
```kotlin
sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val exception: Exception) : Result<Nothing>()
}
```

### Step 1.3: Test Your UseCases

```kotlin
@Test
fun testGetUsersUseCase() = runTest {
    val repo = mockk<UserRepository>()
    val users = listOf(User("1", "Alice"), User("2", "Bob"))
    coEvery { repo.getUsers() } returns users
    
    val useCase = GetUsersUseCase(repo)
    val result = useCase()
    
    assertIs<Result.Success>(result)
    assertEquals(users, (result as Result.Success).data)
}
```

---

## Phase 2: Create MVI Classes (State, Intent, SideEffect)

**Goal**: Define immutable state, all user actions, and one-time events

### Step 2.1: Create UiState

**File**: `presentation/state/UserUiState.kt`
```kotlin
data class UserUiState(
    val isLoading: Boolean = false,
    val users: List<User> = emptyList(),
    val error: String? = null,
    val selectedUserId: String? = null
) {
    val isEmpty: Boolean = users.isEmpty()
}
```

### Step 2.2: Create Intent

**File**: `presentation/intent/UserIntent.kt`
```kotlin
sealed class UserIntent {
    object LoadUsers : UserIntent()
    data class DeleteUser(val userId: String) : UserIntent()
    object RefreshUsers : UserIntent()
    object ClearError : UserIntent()
    data class SelectUser(val userId: String) : UserIntent()
}
```

### Step 2.3: Create SideEffect

**File**: `presentation/side_effect/UserSideEffect.kt`
```kotlin
sealed class UserSideEffect {
    data class ShowMessage(val message: String) : UserSideEffect()
    data class NavigateToDetail(val userId: String) : UserSideEffect()
    object NavigateBack : UserSideEffect()
    data class ShowError(val message: String) : UserSideEffect()
}
```

---

## Phase 3: Refactor ViewModel to MVI

**Goal**: Introduce state/intent/effect channels and wire UseCases

### Step 3.1: Setup MVI ViewModel Structure

**File**: `presentation/viewmodel/UserViewModel.kt`
```kotlin
class UserViewModel(
    private val getUsersUseCase: GetUsersUseCase,
    private val deleteUserUseCase: DeleteUserUseCase
) : ViewModel() {
    
    // State management
    private val _uiState = MutableStateFlow(UserUiState())
    val uiState: StateFlow<UserUiState> = _uiState.asStateFlow()
    
    // Side effects
    private val _sideEffect = Channel<UserSideEffect>(Channel.BUFFERED)
    val sideEffect: Flow<UserSideEffect> = _sideEffect.receiveAsFlow()
    
    // Public intent handler
    fun handleIntent(intent: UserIntent) {
        viewModelScope.launch {
            when (intent) {
                UserIntent.LoadUsers -> loadUsers()
                UserIntent.RefreshUsers -> loadUsers()
                is UserIntent.DeleteUser -> deleteUser(intent.userId)
                UserIntent.ClearError -> clearError()
                is UserIntent.SelectUser -> selectUser(intent.userId)
            }
        }
    }
    
    init {
        handleIntent(UserIntent.LoadUsers)
    }
    
    private suspend fun loadUsers() {
        // Update state to loading
        _uiState.update { it.copy(isLoading = true, error = null) }
        
        // Call UseCase
        val result = getUsersUseCase()
        
        // Update state based on result
        _uiState.update {
            when (result) {
                is Result.Success -> it.copy(
                    isLoading = false,
                    users = result.data,
                    error = null
                )
                is Result.Error -> it.copy(
                    isLoading = false,
                    error = result.exception.message ?: "Unknown error",
                    users = emptyList()
                )
            }
        }
    }
    
    private suspend fun deleteUser(userId: String) {
        val result = deleteUserUseCase(userId)
        
        when (result) {
            is Result.Success -> {
                _uiState.update { it.copy(users = result.data) }
                _sideEffect.send(UserSideEffect.ShowMessage("User deleted"))
            }
            is Result.Error -> {
                _sideEffect.send(UserSideEffect.ShowError("Failed to delete user"))
            }
        }
    }
    
    private fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
    
    private fun selectUser(userId: String) {
        _uiState.update { it.copy(selectedUserId = userId) }
        viewModelScope.launch {
            _sideEffect.send(UserSideEffect.NavigateToDetail(userId))
        }
    }
}
```

### Step 3.2: Setup Koin DI

Create Koin modules for dependencies:

**UseCaseModule.kt**
```kotlin
val useCaseModule = module {
    factory { GetUsersUseCase(repository = get()) }
    factory { DeleteUserUseCase(repository = get()) }
}
```

**PresentationModule.kt**
```kotlin
val presentationModule = module {
    viewModel { UserViewModel(
        getUsersUseCase = get(),
        deleteUserUseCase = get()
    ) }
}
```

**App.kt or MainActivity.kt**
```kotlin
class DigitalDiaryApp : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@DigitalDiaryApp)
            modules(
                useCaseModule,
                repositoryModule,
                presentationModule
            )
        }
    }
}
```

---

## Phase 4: Update UI to Use Intent-Based API

**Goal**: Replace direct method calls with intent-based communication

### Before (Old ViewModel)
```kotlin
@Composable
fun UserScreen(viewModel: UserViewModel) {
    val users by viewModel.users.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    
    LazyColumn {
        items(users) { user ->
            UserItem(
                user = user,
                onDelete = { viewModel.deleteUser(user.id) } // Direct call
            )
        }
    }
}
```

### After (MVI ViewModel)
```kotlin
@Composable
fun UserScreen(viewModel: UserViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    
    // Handle side effects
    LaunchedEffect(Unit) {
        viewModel.sideEffect.collect { effect ->
            when (effect) {
                is UserSideEffect.ShowMessage -> {
                    Toast.makeText(context, effect.message, Toast.LENGTH_SHORT).show()
                }
                is UserSideEffect.NavigateToDetail -> {
                    navController.navigate("detail/${effect.userId}")
                }
                is UserSideEffect.ShowError -> {
                    Snackbar.make(view, effect.message, Snackbar.LENGTH_LONG).show()
                }
                else -> {}
            }
        }
    }
    
    LazyColumn {
        items(uiState.users, key = { it.id }) { user ->
            UserItem(
                user = user,
                isLoading = uiState.isLoading,
                onDelete = { 
                    viewModel.handleIntent(UserIntent.DeleteUser(user.id))
                }
            )
        }
    }
    
    if (uiState.error != null) {
        ErrorDialog(
            message = uiState.error!!,
            onDismiss = {
                viewModel.handleIntent(UserIntent.ClearError)
            }
        )
    }
}
```

---

## Phase 5: Testing

**Goal**: Unit test UseCases and ViewModel separately

### Test UseCases

```kotlin
class GetUsersUseCaseTest {
    
    @Test
    fun testGetUsers_success() = runTest {
        val repo = mockk<UserRepository>()
        val users = listOf(User("1", "Alice"))
        coEvery { repo.getUsers() } returns users
        
        val useCase = GetUsersUseCase(repo)
        val result = useCase()
        
        assertIs<Result.Success<List<User>>>(result)
    }
}
```

### Test ViewModel

```kotlin
class UserViewModelTest {
    
    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()
    
    @Test
    fun testLoadUsers_emitsCorrectState() = runTest {
        val useCase = mockk<GetUsersUseCase>()
        val users = listOf(User("1", "Alice"))
        coEvery { useCase() } returns Result.Success(users)
        
        val viewModel = UserViewModel(useCase, mockk())
        
        advanceUntilIdle()
        
        val state = viewModel.uiState.value
        assertEquals(users, state.users)
        assertEquals(false, state.isLoading)
        assertEquals(null, state.error)
    }
    
    @Test
    fun testDeleteUser_sendsCorrectSideEffect() = runTest {
        val deleteUseCase = mockk<DeleteUserUseCase>()
        val users = emptyList<User>()
        coEvery { deleteUseCase(any()) } returns Result.Success(users)
        
        val viewModel = UserViewModel(mockk(), deleteUseCase)
        
        viewModel.handleIntent(UserIntent.DeleteUser("123"))
        advanceUntilIdle()
        
        val effect = viewModel.sideEffect.first()
        assertIs<UserSideEffect.ShowMessage>(effect)
    }
}
```

---

## Incremental Migration Strategy

If you have multiple ViewModels, convert them incrementally:

1. **Week 1**: Phases 1-2 for module A (UseCases + State/Intent/SideEffect)
2. **Week 2**: Phase 3-4 for module A (ViewModel refactor + UI update)
3. **Week 3**: Phase 5 for module A (Tests)
4. **Week 4+**: Repeat for modules B, C, etc.

**Benefits of incremental approach:**
- Reduced risk
- Easier debugging
- Team learns gradually
- Can ship improvements without full refactor
