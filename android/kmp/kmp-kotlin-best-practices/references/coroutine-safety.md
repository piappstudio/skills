# Coroutine Safety & Best Practices

## Scope Management

### ✅ Do: Use Proper Scopes

```kotlin
// ViewModel with lifecycleScope
class UserViewModel : ViewModel() {
    fun loadUser(id: String) {
        viewModelScope.launch {
            try {
                val user = repository.getUser(id)
                _state.value = ViewState.Success(user)
            } catch (e: Exception) {
                _state.value = ViewState.Error(e)
            }
        }
    }
}

// Composable with LaunchedEffect
@Composable
fun UserScreen() {
    LaunchedEffect(Unit) {
        viewModel.loadUser()
    }
}
```

### ❌ Don't

```kotlin
// Don't use GlobalScope
GlobalScope.launch {
    val user = repository.getUser(id)
}

// Don't launch in init block
init {
    launch { loadData() } // No scope!
}

// Don't ignore cancellation
launch {
    while (true) {
        delay(1000)
        updateUI()
    }
}
```

## Exception Handling

### ✅ Do: Handle Exceptions Properly

```kotlin
// Try/catch in launch
viewModelScope.launch {
    try {
        val data = fetchData()
        _state.value = Success(data)
    } catch (e: IOException) {
        _state.value = Error("Network error")
    } catch (e: Exception) {
        _state.value = Error("Unknown error")
    }
}

// CoroutineExceptionHandler
val exceptionHandler = CoroutineExceptionHandler { _, exception ->
    println("Caught $exception")
}

viewModelScope.launch(exceptionHandler) {
    val data = fetchData()
}

// Async with await
viewModelScope.launch {
    try {
        val user = async { repository.getUser(id) }.await()
        _state.value = Success(user)
    } catch (e: Exception) {
        _state.value = Error(e.message)
    }
}
```

### ❌ Don't

```kotlin
// Don't ignore exceptions
launch {
    fetchData() // Exception silently lost
}

// Don't catch Throwable (unless you know why)
try {
    fetchData()
} catch (e: Throwable) { // Too broad
    ...
}

// Don't suppress cancellation
try {
    delay(1000)
} catch (e: CancellationException) {
    // Never suppress!
    throw e
}
```

## Cancellation

### ✅ Do: Respect Cancellation

```kotlin
// Check cancellation cooperatively
suspend fun processLargeDataset(items: List<Item>) {
    for (item in items) {
        ensureActive() // Throw if cancelled
        processItem(item)
    }
}

// Work with cancellation token
viewModelScope.launch {
    try {
        val job = launch {
            while (true) {
                delay(1000)
                updateUI()
            }
        }
        
        // Cancel after 5 seconds
        delay(5000)
        job.cancel()
    } catch (e: CancellationException) {
        // Cleanup if needed
    }
}

// Use withTimeoutOrNull
viewModelScope.launch {
    val result = withTimeoutOrNull(5000) {
        fetchData()
    } ?: run {
        _state.value = Error("Timeout")
    }
}
```

### ❌ Don't

```kotlin
// Don't ignore cancellation checks
launch {
    for (i in 1..1000000) {
        heavyComputation() // Will run after scope destroyed
    }
}

// Don't swallow CancellationException
try {
    delay(1000)
} catch (e: Exception) {
    // This catches CancellationException!
    log(e)
}

// Don't use try-finally for cancellation
launch {
    try {
        delay(1000)
    } finally {
        // This runs even if cancelled
        cleanup()
    }
}
```

## Job Management

### ✅ Do

```kotlin
// Check job state before operations
if (!viewModelScope.coroutineContext[Job]?.isActive == true) {
    return // Scope already cancelled
}

// Wait for job completion
viewModelScope.launch {
    val job = launch { fetchData() }
    job.join() // Wait for completion
}

// Handle job lifecycle
viewModelScope.launch {
    val job = coroutineContext[Job]!!
    job.invokeOnCompletion { exception ->
        if (exception == null) {
            println("Completed successfully")
        }
    }
}
```

### ❌ Don't

```kotlin
// Don't ignore job cancellation
val job = launch { fetchData() }
job.cancel()
result.value = job.getCompleted() // Throws!
```

## Flow

### ✅ Do: Proper Flow Usage

```kotlin
// Share flow with limited subscribers
class UserRepository {
    private val _users = MutableStateFlow<List<User>>(emptyList())
    val users: StateFlow<List<User>> = _users.asStateFlow()
}

// Collect with lifecycle-aware scope
@Composable
fun UserList() {
    val users by viewModel.users.collectAsState()
    LazyColumn {
        items(users) { user -> UserItem(user) }
    }
}

// Handle backpressure
viewModelScope.launch {
    repository.userEvents
        .buffer(capacity = Channel.UNLIMITED)
        .collect { event ->
            handleUserEvent(event)
        }
}
```

### ❌ Don't

```kotlin
// Don't use StateFlow without collect
val value = stateFlow.value // Stale data

// Don't launch collectors without scope
stateFlow.collect { value -> // No scope!
    updateUI(value)
}

// Don't ignore backpressure
repository.largeFlow.collect {
    delay(100) // Consumer slower than producer
}
```

## Structured Concurrency

### ✅ Do

```kotlin
// Parent waits for children
class MyViewModel : ViewModel() {
    fun loadMultipleUsers(ids: List<String>) {
        viewModelScope.launch {
            val users = ids.map { id ->
                async { repository.getUser(id) }
            }.awaitAll() // Wait for all
            
            _state.value = Success(users)
        }
    }
}

// Supervisor job for partial failures
val supervisor = SupervisorJob()
val scope = CoroutineScope(supervisor + Dispatchers.Main)

scope.launch {
    try {
        val job1 = launch { task1() }
        val job2 = launch { task2() } // Continues even if job1 fails
    } finally {
        supervisor.cancelChildren()
    }
}
```

### ❌ Don't

```kotlin
// Don't abandon coroutines
val job = launch { fetchData() }
// Scope destroyed without waiting

// Don't mix scopes
GlobalScope.launch {
    viewModelScope.launch { // Contradictory scopes
        fetchData()
    }
}
```

## Checklist

- [ ] All long-running operations use appropriate scope (viewModelScope, launchEffect, etc.)
- [ ] Exceptions are caught and handled appropriately
- [ ] CancellationException is not suppressed
- [ ] Job/Deferred completion is awaited when necessary
- [ ] Flow collectors respect backpressure
- [ ] StateFlow vs SharedFlow used correctly
- [ ] No GlobalScope usage in production code
- [ ] Timeouts are set for long operations
- [ ] Resources are cleaned up in finally or scope cleanup
