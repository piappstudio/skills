# Kotlin Idioms & Conventions

## Null Safety

### ✅ Do
```kotlin
// Use nullable types explicitly
val name: String? = null

// Use let for safe access
user?.let { println(it.name) }

// Use Elvis operator
val displayName = user?.name ?: "Unknown"

// Use require/check for validation
require(age >= 0) { "Age must be non-negative" }
check(initialized) { "Must initialize before use" }
```

### ❌ Don't
```kotlin
// Avoid non-nullable assertions unless guaranteed safe
val name = user!!.name // Can throw NPE

// Avoid multiple null checks
if (user != null && user.name != null) { ... }
```

## Scoping & Method Chaining

### ✅ Do
```kotlin
// Use apply for object initialization
val user = User().apply {
    name = "Alice"
    age = 30
}

// Use run for complex operations
val result = user.run {
    validate()
    process()
}

// Use also for side effects
val user = loadUser().also {
    println("Loaded: ${it.name}")
}

// Use with for multiple operations on same object
with(user) {
    println(name)
    println(age)
}
```

### ❌ Don't
```kotlin
// Avoid repetition
user.name = "Alice"
user.age = 30
println(user.name)
```

## Collections

### ✅ Do
```kotlin
// Use immutable collections by default
val items: List<String> = listOf("a", "b")
val map: Map<String, Int> = mapOf("x" to 1)

// Use sequences for large collections
list.asSequence()
    .filter { it > 10 }
    .map { it * 2 }
    .toList()

// Use destructuring
val (x, y) = Pair(1, 2)
```

### ❌ Don't
```kotlin
// Avoid mutable collections unless necessary
val items = mutableListOf<String>()

// Avoid chaining operations on large lists without sequences
list.filter { it > 10 }.map { it * 2 }
```

## Extension Functions

### ✅ Do
```kotlin
// Use extensions for utility operations
fun String.toTitleCase(): String = 
    split(" ").joinToString(" ") { it.capitalize() }

// Use receiver for clear context
fun <T> List<T>.second(): T = this[1]

// Use infix for DSL-like syntax (sparingly)
infix fun <T> T.applyTo(block: (T) -> Unit) {
    block(this)
}
```

### ❌ Don't
```kotlin
// Avoid utility objects when extensions work better
object StringUtils {
    fun toTitleCase(s: String) = ...
}
```

## Delegation

### ✅ Do
```kotlin
// Use delegation by keyword
class Child : Parent by ParentImpl()

// Use lazy for expensive initialization
val database: Database by lazy { 
    initializeDatabase() 
}

// Use observable for property changes
var name: String by Delegates.observable("") { _, old, new ->
    println("Changed from $old to $new")
}
```

## Data Classes

### ✅ Do
```kotlin
// Use data class for value objects
data class User(val id: Int, val name: String)

// Use copy for modifications
val newUser = user.copy(name = "Bob")

// Use destructuring in for loops
for ((id, name) in users) {
    println("$id: $name")
}
```

### ❌ Don't
```kotlin
// Avoid regular classes for simple data
class User(val id: Int, val name: String) {
    override fun equals(other: Any?): Boolean { ... }
    override fun hashCode(): Int { ... }
    override fun toString(): String { ... }
}
```

## Sealed Classes for Type Safety

### ✅ Do
```kotlin
sealed class Result<T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error<T>(val exception: Exception) : Result<T>()
    class Loading<T> : Result<T>()
}

// Type-safe exhaustive when
fun <T> handle(result: Result<T>) {
    when (result) {
        is Result.Success -> println(result.data)
        is Result.Error -> println(result.exception)
        is Result.Loading -> println("Loading...")
    }
}
```

## Scope Functions Quick Reference

| Function | Purpose | Return |
|----------|---------|--------|
| `let` | Execute block if not null | Block result |
| `run` | Complex operations on object | Block result |
| `apply` | Initialize/configure object | Object itself |
| `also` | Side effects (e.g., logging) | Object itself |
| `with` | Multiple operations on same object | Block result |
