# Coding Standards & Conventions

## File Organization

### ✅ Do

```kotlin
// 1. File-level comments/documentation (optional)
/**
 * User authentication logic for the app.
 */

// 2. Package declaration
package com.digitalDiary.auth

// 3. Imports (grouped and sorted)
import android.content.Context
import androidx.lifecycle.ViewModel
import com.digitalDiary.data.UserRepository
import com.digitalDiary.util.Logger
import kotlinx.coroutines.launch

// 4. Type aliases (if any)
typealias OnUserUpdate = (User) -> Unit

// 5. Top-level declarations
data class User(val id: String, val email: String)

// 6. Extension functions
fun User.displayName(): String = email.substringBefore("@")

// 7. Classes/interfaces
class AuthViewModel(private val repository: UserRepository) : ViewModel() { ... }
```

## Naming Conventions

### Classes & Interfaces
```kotlin
// ✅ Do
class UserViewModel
interface UserRepository
object DateUtils
enum class NotificationType
data class UserProfile(val id: String)
sealed class Result<T>

// ❌ Don't
class user_view_model
class IUserRepository // Don't prefix with I
```

### Functions & Parameters
```kotlin
// ✅ Do
fun getUserById(id: String): User?
fun onUserClicked(user: User)
fun isValidEmail(email: String): Boolean
private fun initializeDatabase()

// ❌ Don't
fun get_user_by_id(id: String)
fun getUserbyid(id: String)
fun uId() // Ambiguous abbreviation
```

### Constants
```kotlin
// ✅ Do
companion object {
    const val MAX_RETRY_COUNT = 3
    const val DATABASE_NAME = "users.db"
    private const val TAG = "UserViewModel"
}

// ❌ Don't
const val maxRetryCount = 3 // Variable naming
val MAX_RETRIES: Int = 3 // Should use const
```

### Variables
```kotlin
// ✅ Do
val userName: String = "Alice"
private var isLoading = false
val currentUser: User? = null

// ❌ Don't
val UserName = "Alice" // Class naming for var
var user_name = "Alice" // Snake case
var u: String? = null // Abbreviations
```

## Visibility Modifiers

### ✅ Do: Explicit Visibility

```kotlin
class UserViewModel : ViewModel() {
    // Public API
    val users: StateFlow<List<User>> = _users
    
    // Protected for subclasses
    protected fun onCleared() { ... }
    
    // Internal to module
    internal fun resetCache() { ... }
    
    // Private implementation
    private val _users = MutableStateFlow<List<User>>()
    private fun fetchUsersFromNetwork() { ... }
}

// File-level private
private const val TAG = "UserViewModel"
private fun logUsage() { ... }
```

### ❌ Don't

```kotlin
class UserViewModel {
    public val users // Redundant
    fun onCleared() // Should be private or protected
}
```

## Type Safety

### ✅ Do

```kotlin
// Use non-nullable by default
val name: String = "Alice"

// Nullable only when needed
val email: String? = user.email

// Generic bounds for type safety
fun <T : Comparable<T>> sort(items: List<T>): List<T> = items.sorted()

// Sealed types for exhaustiveness
sealed class UserStatus {
    object Active : UserStatus()
    object Inactive : UserStatus()
}
```

### ❌ Don't

```kotlin
// Avoid Any type
fun process(obj: Any) { ... }

// Avoid unbounded generics
fun <T> convert(item: T): String = item.toString()

// Avoid casting when possible
val user = obj as? User ?: return // Use when instead
```

## Comments & Documentation

### ✅ Do: KDoc for Public APIs

```kotlin
/**
 * Authenticates a user with the provided credentials.
 *
 * @param email The user's email address.
 * @param password The user's password.
 * @return A [Result] containing the authenticated [User] or an error.
 * @throws IllegalArgumentException if email or password is empty.
 *
 * @sample com.digitalDiary.auth.sampleUserAuthentication
 */
fun authenticate(email: String, password: String): Result<User>

/** Returns the user's display name. */
fun User.displayName(): String = name.ifEmpty { email }
```

### ❌ Don't

```kotlin
// Avoid obvious comments
fun getUserById(id: String): User? {
    // Get user from repo
    return repository.getUser(id)
}

// Avoid commented-out code
// val oldUser = repository.getOldUser()
// repository.deleteOldUser(oldUser)
```

## Formatting

### ✅ Do: Line Length & Continuation

```kotlin
// Keep lines under 120 characters
viewModelScope.launch {
    val users = repository.fetchUsers()
    _state.value = ViewState.Success(users)
}

// Break long function signatures
fun processUserList(
    users: List<User>,
    filter: (User) -> Boolean,
    transform: (User) -> String
): List<String> = users
    .filter(filter)
    .map(transform)

// Align with parameters or expressions
val result = someFunction(
    param1 = "value1",
    param2 = "value2"
)

val computed = longExpression
    .filter { it > 0 }
    .map { it * 2 }
    .sorted()
```

### ❌ Don't

```kotlin
// Avoid overly long lines
fun processUserList(users: List<User>, filter: (User) -> Boolean, transform: (User) -> String): List<String> = users.filter(filter).map(transform)

// Avoid inconsistent indentation
fun getData() {
val x = 1
  val y = 2
return x + y
}
```

## Lambda & Function Reference Style

### ✅ Do

```kotlin
// Simple lambdas: inline them
users.filter { it.isActive }

// Multi-line: use block
users.forEach { user ->
    println(user.name)
    logActivity(user)
}

// Function reference when available
users.map(User::displayName)

// Trailing lambda syntax
items.filter { it > 0 }
    .map { it * 2 }
    .forEach(::println)
```

### ❌ Don't

```kotlin
// Avoid explicit parameter names for single parameter
users.filter { user -> user.isActive }

// Avoid unnecessary function references
users.map { it.id } // Use lambda instead of User::getId
```

## Error Handling

### ✅ Do

```kotlin
// Sealed ResultError for domain errors
sealed class ResultError {
    data class NetworkError(val message: String) : ResultError()
    data class ValidationError(val field: String) : ResultError()
}

// Typed results
typealias UserResult = Result<User, ResultError>

// Explicit error messages
require(email.isNotEmpty()) { "Email cannot be empty" }
requireNotNull(user) { "User not found" }
```

### ❌ Don't

```kotlin
// Avoid generic exceptions
throw Exception("Something went wrong")

// Avoid swallowing exceptions
try {
    loadData()
} catch (e: Exception) {
    // Ignore
}
```

## Performance

### ✅ Do

```kotlin
// Use sequences for large collections
list.asSequence()
    .filter { it > 10 }
    .map { it * 2 }
    .toList()

// Lazy initialization
val expensiveResource: Resource by lazy { initResource() }

// Batch database operations
repository.insertUsers(users) // Not in a loop
```

### ❌ Don't

```kotlin
// Avoid repeated calculations
for (user in users) {
    val expensive = calculateExpensively() // Called every iteration
}

// Avoid creating collections in hot paths
fun format(items: List<String>): String {
    val temp = mutableListOf<String>() // Created unnecessarily
    return items.joinToString()
}
```
