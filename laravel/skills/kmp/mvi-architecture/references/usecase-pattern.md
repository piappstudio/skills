# UseCase Pattern & Business Logic Extraction

## What is a UseCase?

A UseCase represents **one single business operation**. It encapsulates business rules, data fetching, transformation, and error handling for that specific operation.

**Key Characteristics:**
- Single responsibility (one operation)
- Suspend function (async-first)
- Injected dependencies
- Pure logic (no UI knowledge)
- Highly testable
- Reusable across features

## UseCase Structure

```kotlin
class GetUserUseCase(
    private val userRepository: UserRepository,
    private val logger: Logger                    // Injected dependencies
) {
    suspend operator fun invoke(id: String): Result<User> {
        return try {
            logger.d("Fetching user with ID: $id")
            val user = userRepository.getUser(id)
            Result.Success(user)
        } catch (e: NetworkException) {
            logger.e("Network error", e)
            Result.Error(e)
        } catch (e: Exception) {
            logger.e("Unknown error", e)
            Result.Error(e)
        }
    }
}
```

**Components:**
1. **Constructor**: Declare all dependencies
2. **invoke()**: Implements suspend function (operator overload recommended)
3. **Error Handling**: Wrap in try/catch or return Result
4. **Logging**: Log important operations
5. **Type Safety**: Strong return types

## UseCase Patterns

### Pattern 1: Simple Data Fetching

```kotlin
class GetUserUseCase(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(userId: String): Result<User> {
        return try {
            val user = userRepository.getUser(userId)
            Result.Success(user)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}

// Usage in ViewModel
private suspend fun loadUser(id: String) {
    val result = getUserUseCase(id)
    _uiState.update {
        when (result) {
            is Result.Success -> it.copy(user = result.data, isLoading = false)
            is Result.Error -> it.copy(error = result.exception.message, isLoading = false)
        }
    }
}
```

### Pattern 2: Data Transformation

```kotlin
class GetUserProfileUseCase(
    private val userRepository: UserRepository,
    private val prefRepository: PreferenceRepository
) {
    suspend operator fun invoke(userId: String): Result<UserProfile> {
        return try {
            val user = userRepository.getUser(userId)
            val preferences = prefRepository.getUserPreferences(userId)
            
            // Transform and combine data
            val profile = UserProfile(
                id = user.id,
                name = user.name,
                isDarkModeEnabled = preferences.isDarkMode,
                language = preferences.language
            )
            
            Result.Success(profile)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}
```

### Pattern 3: Action with Validation

```kotlin
class DeleteUserUseCase(
    private val userRepository: UserRepository,
    private val validator: UserValidator
) {
    suspend operator fun invoke(userId: String): Result<Unit> {
        return try {
            // Validate
            if (!validator.canDeleteUser(userId)) {
                return Result.Error(ValidationException("Cannot delete this user"))
            }
            
            // Delete
            userRepository.deleteUser(userId)
            
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}
```

### Pattern 4: Complex Business Logic

```kotlin
class UpdateUserAndSyncUseCase(
    private val userRepository: UserRepository,
    private val syncService: SyncService,
    private val logger: Logger
) {
    suspend operator fun invoke(user: User): Result<User> {
        return try {
            // Step 1: Update locally
            logger.d("Updating user: ${user.id}")
            val updated = userRepository.updateUser(user)
            
            // Step 2: Sync to server
            logger.d("Syncing user to server")
            syncService.syncUser(updated)
            
            // Step 3: Refresh from server
            logger.d("Fetching latest state")
            val freshUser = userRepository.getUser(user.id)
            
            Result.Success(freshUser)
        } catch (e: NetworkException) {
            logger.e("Sync failed, local update kept", e)
            // Partial success - local updated, sync failed
            Result.Error(e)
        } catch (e: Exception) {
            logger.e("Update failed", e)
            Result.Error(e)
        }
    }
}
```

### Pattern 5: Search/Filter with Debounce

```kotlin
class SearchUsersUseCase(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(query: String, limit: Int = 10): Result<List<User>> {
        return try {
            when {
                query.isEmpty() -> Result.Success(emptyList())
                query.length < 2 -> Result.Success(emptyList()) // Min 2 chars
                else -> {
                    val results = userRepository.searchUsers(query, limit)
                    Result.Success(results)
                }
            }
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}

// In ViewModel - handle debouncing
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

private suspend fun search(query: String) {
    val result = searchUsersUseCase(query)
    _uiState.update {
        when (result) {
            is Result.Success -> it.copy(searchResults = result.data)
            is Result.Error -> it.copy(error = result.exception.message)
        }
    }
}
```

### Pattern 6: Conditional Execution

```kotlin
class GetCachedUserUseCase(
    private val userRepository: UserRepository,
    private val cacheManager: CacheManager
) {
    suspend operator fun invoke(
        userId: String,
        forceRefresh: Boolean = false
    ): Result<User> {
        return try {
            // Check cache if not forcing refresh
            if (!forceRefresh) {
                val cached = cacheManager.getUser(userId)
                if (cached != null) {
                    return Result.Success(cached)
                }
            }
            
            // Fetch from repository
            val user = userRepository.getUser(userId)
            
            // Cache result
            cacheManager.saveUser(user)
            
            Result.Success(user)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}
```

## Building UseCase Chains

### Combining Multiple UseCases

```kotlin
class AuthenticateAndFetchUserUseCase(
    private val loginUseCase: LoginUseCase,
    private val getUserUseCase: GetUserUseCase
) {
    suspend operator fun invoke(
        email: String,
        password: String
    ): Result<User> {
        return try {
            // Step 1: Authenticate
            val authResult = loginUseCase(email, password)
            val userId = when (authResult) {
                is Result.Success -> authResult.data.id
                is Result.Error -> return authResult
            }
            
            // Step 2: Fetch user details
            getUserUseCase(userId)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}
```

## Testing UseCases

### Unit Test Example

```kotlin
@Test
fun testGetUserUseCase_Success() = runTest {
    // Arrange
    val repository = mockk<UserRepository>()
    val expectedUser = User(id = "1", name = "Alice")
    coEvery { repository.getUser("1") } returns expectedUser
    
    val useCase = GetUserUseCase(repository)
    
    // Act
    val result = useCase("1")
    
    // Assert
    assertIs<Result.Success<User>>(result)
    assertEquals(expectedUser, (result as Result.Success).data)
    coVerify { repository.getUser("1") }
}

@Test
fun testGetUserUseCase_Error() = runTest {
    // Arrange
    val repository = mockk<UserRepository>()
    val exception = Exception("Network error")
    coEvery { repository.getUser("1") } throws exception
    
    val useCase = GetUserUseCase(repository)
    
    // Act
    val result = useCase("1")
    
    // Assert
    assertIs<Result.Error>(result)
    assertEquals(exception, (result as Result.Error).exception)
}
```

## Dependency Injection Setup (Koin)

### Koin Module

```kotlin
val useCaseModule = module {
    // Factory creates new instance each time
    factory { GetUserUseCase(userRepository = get()) }
    factory { DeleteUserUseCase(userRepository = get()) }
    factory { SearchUsersUseCase(userRepository = get()) }
}
```

### Inject into ViewModel

```kotlin
class UserViewModel(
    private val getUserUseCase: GetUserUseCase,
    private val deleteUserUseCase: DeleteUserUseCase,
    private val searchUsersUseCase: SearchUsersUseCase
) : ViewModel() {
    // ... ViewModel implementation
}

// In PresentationModule
val presentationModule = module {
    viewModel { UserViewModel(
        getUserUseCase = get(),
        deleteUserUseCase = get(),
        searchUsersUseCase = get()
    ) }
}
```

### Usage in Activity/Fragment/Composable

**Fragment**
```kotlin
class UserFragment : Fragment() {
    private val viewModel: UserViewModel by viewModel()
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Use viewModel
    }
}
```

**Composable**
```kotlin
@Composable
fun UserScreen() {
    val viewModel = koinViewModel<UserViewModel>()
    // Use viewModel
}
```

## Anti-patterns to Avoid

### ❌ Don't: UseCase depends on ViewModel

```kotlin
// WRONG - circular dependency
class GetUserUseCase(private val viewModel: UserViewModel) { }
```

### ❌ Don't: UseCase has side effects

```kotlin
// WRONG - UseCase should be pure
class GetUserUseCase {
    operator fun invoke(id: String): Result<User> {
        val user = repository.getUser(id)
        // Clear cache? Emit analytics? Do it elsewhere
        analyticsService.track("user_fetched")
        return Result.Success(user)
    }
}
```

### ❌ Don't: UseCase knows about UI

```kotlin
// WRONG - UseCase shouldn't reference UI
class GetUserUseCase {
    operator fun invoke(id: String): Result<User> {
        return try {
            repository.getUser(id)
        } catch (e: Exception) {
            showErrorDialog(e) // NO!
        }
    }
}
```

### ✅ Do: UseCase returns Result, let ViewModel handle effects

```kotlin
class GetUserUseCase(private val repository: UserRepository) {
    suspend operator fun invoke(id: String): Result<User> {
        return try {
            Result.Success(repository.getUser(id))
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}

// ViewModel handles the result and side effects
viewModelScope.launch {
    val result = getUserUseCase(id)
    when (result) {
        is Result.Success -> updateState(result.data)
        is Result.Error -> emitErrorSideEffect(result.exception)
    }
}
```
