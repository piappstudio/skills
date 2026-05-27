# KMP Patterns & Best Practices

## expect/actual Pattern

### ✅ Do: Proper expect/actual Structure

**commonMain** (Shared code)
```kotlin
// Declare interface in common source
expect class PlatformInfo {
    fun getPlatformName(): String
    fun getDeviceId(): String
}

// Use in common code
val info = PlatformInfo()
println(info.getPlatformName())
```

**androidMain**
```kotlin
actual class PlatformInfo {
    actual fun getPlatformName(): String = "Android"
    
    actual fun getDeviceId(): String {
        return Settings.Secure.getString(
            contentResolver,
            Settings.Secure.ANDROID_ID
        )
    }
}
```

**iosMain**
```kotlin
actual class PlatformInfo {
    actual fun getPlatformName(): String = "iOS"
    
    actual fun getDeviceId(): String {
        return UIDevice.current.identifierForVendor?.uuidString ?: ""
    }
}
```

### ❌ Don't

```kotlin
// Avoid platform logic in commonMain
expect fun getPlatformName(): String

// Avoid complex platform API leakage
expect class ComplexPlatformAPI { ... } // Too specific

// Avoid incomplete expect/actual pairs
expect fun platformSpecificFunction() // Might be missing in one platform
```

## Module Organization

### ✅ Do

it is veruy important `open class`

```
composeApp/
├── src/
│   ├── commonMain/
│   │   ├── kotlin/
│   │   │   └── com/app/
│   │   │       ├── data/
│   │   │       │   ├── repository/
│   │   │       │   └── model/
│   │   │       ├── domain/
│   │   │       │   └── usecase/
│   │   │       ├── presentation/
│   │   │       │   └── viewmodel/
│   │   │       └── util/
│   │   └── resources/
│   ├── androidMain/
│   │   ├── kotlin/com/app/
│   │   │   ├── ui/
│   │   │   └── shared/
│   │   └── res/
│   ├── iosMain/
│   │   └── kotlin/com/app/
│   │       └── shared/
```

### Key Principles
- **commonMain**: Domain logic, interfaces, shared utilities
- **platformMain**: Platform implementations, UI frameworks, native APIs
- **Share aggressively**: Move code to commonMain if it works across platforms
- **Minimize platform-specific**: Only platform dependencies go to androidMain/iosMain

## Expect/Actual for Different Scenarios

### 1. Single Interface, Multiple Implementations
```kotlin
// commonMain
expect fun getCurrentTimeMillis(): Long

// androidMain
actual fun getCurrentTimeMillis(): Long = System.currentTimeMillis()

// iosMain
actual fun getCurrentTimeMillis(): Long = NSDate().timeIntervalSince1970.toLong() * 1000
```

### 2. Platform-Specific Classes
```kotlin
// commonMain
expect class Log {
    companion object {
        fun d(tag: String, msg: String)
    }
}

// androidMain
actual class Log {
    actual companion object {
        actual fun d(tag: String, msg: String) {
            android.util.Log.d(tag, msg)
        }
    }
}
```

### 3. Optional Platform Features
```kotlin
// commonMain
expect fun isBiometricAvailable(): Boolean

// androidMain
actual fun isBiometricAvailable(): Boolean {
    return BiometricManager.from(context).canAuthenticate() == BIOMETRIC_SUCCESS
}

// iosMain
actual fun isBiometricAvailable(): Boolean {
    val context = LAContext()
    var error: NSError? = null
    return context.canEvaluatePolicy(LAPolicy.deviceOwnerAuthenticationWithBiometrics, error)
}
```

## Resource Sharing

### ✅ Do: Share Strings in commonMain

```kotlin
// commonMain/kotlin/com/app/util/Strings.kt
object Strings {
    const val WELCOME = "Welcome to MyApp"
    const val ERROR_NETWORK = "Network error occurred"
}
```

```kotlin
// commonMain code
println(Strings.WELCOME)
```

### ✅ Do: Platform-Specific Strings

```kotlin
// androidMain/res/values/strings.xml
<string name="app_title">MyApp</string>

// iosMain resources handled differently
```

## Dependency Injection in KMP

### ✅ Do
```kotlin
// commonMain interface
interface UserRepository {
    suspend fun getUser(id: String): Result<User>
}

// Platform implementations
actual class UserRepositoryImpl : UserRepository { ... }

// Provide via DI
expect fun createUserRepository(): UserRepository
```

## State Management

### ✅ Do: Use Expect/Actual for Platform Dispatchers
```kotlin
// commonMain
expect val Dispatchers.IO: CoroutineDispatcher

// androidMain
actual val Dispatchers.IO: CoroutineDispatcher 
    get() = Dispatchers.IO

// iosMain
actual val Dispatchers.IO: CoroutineDispatcher
    get() = Dispatchers.Default // iOS doesn't distinguish IO
```
