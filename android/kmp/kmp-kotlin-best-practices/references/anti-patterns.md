# Anti-patterns to Avoid

## Coroutine Anti-patterns

### 1. GlobalScope Launch
```kotlin
// ❌ NEVER DO THIS
GlobalScope.launch {
    fetchData()
}

// ✅ DO THIS
viewModelScope.launch {
    fetchData()
}
```
**Why**: GlobalScope ignores lifecycle. Coroutine continues even after scope is destroyed, causing memory leaks and crashes.

### 2. Launch in init Block Without Scope
```kotlin
// ❌ WRONG
class UserViewModel : ViewModel() {
    init {
        launch { loadUser() } // No scope!
    }
}

// ✅ CORRECT
class UserViewModel : ViewModel() {
    init {
        viewModelScope.launch { loadUser() }
    }
}
```

### 3. Ignoring CancellationException
```kotlin
// ❌ WRONG
launch {
    try {
        delay(1000)
    } catch (e: Exception) {
        log(e) // Catches CancellationException!
    }
}

// ✅ CORRECT
launch {
    try {
        delay(1000)
    } catch (e: CancellationException) {
        throw e // Re-throw
    } catch (e: Exception) {
        log(e)
    }
}
```

### 4. Blocking Inside Coroutines
```kotlin
// ❌ WRONG
launch {
    val data = Thread.sleep(5000) // Blocks entire thread!
    updateUI()
}

// ✅ CORRECT
launch {
    delay(5000) // Suspends, releases thread
    updateUI()
}
```

### 5. Fire-and-forget Coroutines
```kotlin
// ❌ WRONG
launch {
    val response = api.getUser(id)
    // Response ignored!
}

// ✅ CORRECT
launch {
    val response = api.getUser(id)
    _state.value = response
}
```

## Null Safety Anti-patterns

### 1. Double Negation
```kotlin
// ❌ WRONG
if (user != null && user.email != null) {
    println(user.email)
}

// ✅ CORRECT
user?.email?.let { println(it) }
```

### 2. Unnecessary Force Unwrap
```kotlin
// ❌ WRONG
val name = user!!.name // Can throw NPE

// ✅ CORRECT
val name = user?.name
if (name != null) {
    println(name)
}
```

### 3. Unchecked Type Casting
```kotlin
// ❌ WRONG
val user = obj as User // Can throw ClassCastException

// ✅ CORRECT
val user = obj as? User ?: return
```

## Collection Anti-patterns

### 1. Mutating Collections Externally
```kotlin
// ❌ WRONG
val users = mutableListOf<User>()

class Repository {
    fun getUsers() = users // Exposed!
}

// Later
val userList = repository.getUsers()
userList.add(User(...)) // Caller modifies internal state!

// ✅ CORRECT
val _users = mutableListOf<User>()

class Repository {
    fun getUsers() = _users.toList() // Immutable copy
}
```

### 2. Creating New Collections in Loop
```kotlin
// ❌ WRONG
val results = mutableListOf<String>()
for (item in items) {
    val temp = mutableListOf<String>() // Created every iteration!
    temp.add(item.name)
    results.addAll(temp)
}

// ✅ CORRECT
val results = items.map { it.name }
```

### 3. Not Using Sequences for Large Lists
```kotlin
// ❌ WRONG - Creates intermediate lists
list.filter { it > 10 }     // New list created here
    .map { it * 2 }         // Another new list
    .sorted()               // Another new list

// ✅ CORRECT - Lazy evaluation
list.asSequence()
    .filter { it > 10 }
    .map { it * 2 }
    .sorted()
    .toList()
```

## KMP expect/actual Anti-patterns

### 1. Platform Logic in Common Code
```kotlin
// ❌ WRONG - commonMain
fun getPlatformInfo(): String {
    return if (isAndroid()) {
        // Android-specific
    } else {
        // iOS-specific
    }
}

// ✅ CORRECT - Use expect/actual
expect fun getPlatformInfo(): String
// Implement in androidMain and iosMain
```

### 2. Over-sharing Code
```kotlin
// ❌ WRONG - Forcing common code where platform logic is needed
// commonMain
expect class DatabaseHelper {
    fun setupDatabase() // Very different per platform
}

// ✅ CORRECT - Platform-specific where needed
// androidMain
class AndroidDatabaseHelper { ... }

// iosMain
class IOSDatabaseHelper { ... }

// commonMain provides interface
interface DatabaseHelper { ... }
```

### 3. Mismatched Signatures
```kotlin
// ❌ WRONG
// commonMain
expect fun processImage(bitmap: Bitmap): ByteArray

// androidMain
actual fun processImage(bitmap: Bitmap): ByteArray { ... }

// iosMain
actual fun processImage(image: UIImage): ByteArray { ... } // Different type!

// ✅ CORRECT - Use common types
expect suspend fun processImage(imageBytes: ByteArray): ByteArray
// Implement on each platform with platform-specific conversion
```

## Resource Leak Anti-patterns

### 1. Not Closing Resources
```kotlin
// ❌ WRONG
val file = File("data.txt")
val reader = FileReader(file)
reader.readLine() // If this throws, reader never closed

// ✅ CORRECT
File("data.txt").bufferedReader().use { reader ->
    reader.readLine()
} // Auto-closed via use block
```

### 2. Long-lived References
```kotlin
// ❌ WRONG - ViewModel holds Activity reference
class DataViewModel(val activity: Activity) : ViewModel() {
    // Activity never released even if ViewModel outlives it
}

// ✅ CORRECT - Use requireActivity(), requireContext() in fragments
class DataViewModel : ViewModel() {
    fun showDialog() {
        // Get context when needed, don't hold references
    }
}
```

## String/Object Anti-patterns

### 1. String Concatenation in Loop
```kotlin
// ❌ WRONG - Creates new String object each iteration
var result = ""
for (item in items) {
    result += item // String immutable, new object created!
}

// ✅ CORRECT
val result = items.joinToString("\n")
// Or StringBuilder for complex logic
```

### 2. Excessive Object Creation
```kotlin
// ❌ WRONG
data class Point(val x: Int, val y: Int)

fun calculateDistance() {
    val points = (1..1000000).map { Point(it, it * 2) } // Huge memory!
}

// ✅ CORRECT
fun calculateDistance() {
    (1..1000000).asSequence()
        .map { Point(it, it * 2) }
        .forEach { calculateFor(it) }
}
```

## Testing Anti-patterns

### 1. Testing Implementation Details
```kotlin
// ❌ WRONG - Testing private function
private fun calculateAge(birthYear: Int): Int {
    return 2024 - birthYear
}

// Test couples to implementation
fun testCalculateAge() {
    val age = obj.calculateAge(2000)
}

// ✅ CORRECT - Test public API
fun getUserAge(): Int = calculateAge(user.birthYear)

fun testGetUserAge() {
    `given`(user.birthYear).willReturn(2000)
    `when`(obj.getUserAge()).thenReturn(24)
}
```

### 2. Flaky Async Tests
```kotlin
// ❌ WRONG - Race condition
launch {
    val user = getUser()
    _user.value = user
}
// Test immediately checks value - might be null!
assert(user.value != null)

// ✅ CORRECT - Wait for coroutine
runTest {
    launch { getUser() }
    advanceUntilIdle()
    assert(user.value != null)
}
```
