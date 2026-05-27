# MVI Folder Structure & Organization

## Standard Project Layout

```
app/
├── src/main/java/
│   └── com/digitaldiary/
│       ├── core/                          # Shared across features
│       │   ├── di/                        # Dependency injection
│       │   ├── ui/                        # Common UI components
│       │   └── util/                      # Utilities
│       │
│       └── feature/
│           ├── user/                      # Feature module
│           │   ├── data/
│           │   │   ├── repository/
│           │   │   │   └── UserRepository.kt
│           │   │   └── model/
│           │   │       └── UserDTO.kt
│           │   │
│           │   ├── domain/
│           │   │   ├── model/
│           │   │   │   └── User.kt
│           │   │   └── usecase/
│           │   │       ├── GetUserUseCase.kt
│           │   │       ├── DeleteUserUseCase.kt
│           │   │       └── SearchUsersUseCase.kt
│           │   │
│           │   ├── presentation/
│           │   │   ├── state/
│           │   │   │   └── UserUiState.kt
│           │   │   ├── intent/
│           │   │   │   └── UserIntent.kt
│           │   │   ├── side_effect/
│           │   │   │   └── UserSideEffect.kt
│           │   │   └── viewmodel/
│           │   │       └── UserViewModel.kt
│           │   │
│           │   └── ui/
│           │       ├── UserScreen.kt
│           │       └── UserListItem.kt
│           │
│           ├── auth/                     # Another feature
│           │   ├── data/
│           │   ├── domain/
│           │   ├── presentation/
│           │   │   ├── state/
│           │   │   ├── intent/
│           │   │   ├── side_effect/
│           │   │   └── viewmodel/
│           │   └── ui/
│           │
│           └── diary/                    # Another feature
│               └── ... (same structure)
```

## Per-Feature Structure Explained

### `/data/`
**Purpose**: Data access layer (remote, local, repository implementations)

```
user/data/
├── repository/
│   └── UserRepositoryImpl.kt          # Repository implementation
├── datasource/
│   ├── remote/
│   │   └── UserRemoteDataSource.kt   # API calls
│   └── local/
│       └── UserLocalDataSource.kt    # Database
└── model/
    ├── UserDTO.kt                    # Network models
    └── UserDbEntity.kt               # Database models
```

**Key Points:**
- Contains network/database models (DTO, Entity)
- Implements repository interfaces
- No UI or business logic here
- Can be tested separately

### `/domain/`
**Purpose**: Pure business logic, independent of frameworks

```
user/domain/
├── model/
│   └── User.kt                       # Domain models (pure data)
├── repository/
│   └── UserRepository.kt             # Repository interface (contract)
└── usecase/
    ├── GetUserUseCase.kt
    ├── DeleteUserUseCase.kt
    ├── SearchUsersUseCase.kt
    └── ValidateUserUseCase.kt
```

**Key Points:**
- No Android framework dependencies
- Pure Kotlin, highly testable
- Domain entities should be simple data classes
- UseCase = one business operation
- Repository is interface only (implementation in data/)

### `/presentation/`
**Purpose**: UI state management (MVI pattern)

```
user/presentation/
├── state/
│   └── UserUiState.kt                # UI state snapshot
├── intent/
│   └── UserIntent.kt                 # User actions
├── side_effect/
│   └── UserSideEffect.kt             # One-time events
└── viewmodel/
    └── UserViewModel.kt              # MVI orchestrator
```

**Key Points:**
- State = immutable data class
- Intent = sealed class with all actions
- SideEffect = sealed class for navigation, toasts, etc
- ViewModel orchestrates everything

### `/ui/`
**Purpose**: UI composables/fragments

```
user/ui/
├── UserScreen.kt                     # Main screen (Compose)
├── UserListItem.kt                   # Reusable item component
└── UserDetailScreen.kt               # Detail screen
```

**Key Points:**
- Only presentation code (Compose/XML + Fragment logic)
- Collect state flows
- Send intents on user interaction
- Handle side effects (navigation, snackbars)

## File Naming Conventions

| Component | File Name | Example |
|-----------|-----------|---------|
| UiState | `{Feature}UiState.kt` | `UserUiState.kt` |
| Intent | `{Feature}Intent.kt` | `UserIntent.kt` |
| SideEffect | `{Feature}SideEffect.kt` | `UserSideEffect.kt` |
| ViewModel | `{Feature}ViewModel.kt` | `UserViewModel.kt` |
| UseCase | `{Action}UseCase.kt` | `GetUserUseCase.kt` |
| Repository | `{Entity}Repository.kt` | `UserRepository.kt` |
| DataSource | `{Entity}{Source}DataSource.kt` | `UserRemoteDataSource.kt` |

## Dependency Injection Structure (Koin)

```
core/di/

├── AppModule.kt               # Root DI module
├── RepositoryModule.kt        # Repository bindings
├── DataSourceModule.kt        # Data source bindings
└── UseCaseModule.kt           # UseCase bindings
    └── PresentationModule.kt  # ViewModel bindings
```

### Koin Setup Example

**AppModule.kt** (combines all modules)
```kotlin
object AppModule {
    fun create() = listOf(
        dataSourceModule,
        repositoryModule,
        useCaseModule,
        presentationModule
    )
}
```

**RepositoryModule.kt**
```kotlin
val repositoryModule = module {
    single<UserRepository> {
        UserRepositoryImpl(
            remoteDataSource = get(),
            localDataSource = get()
        )
    }
}
```

**UseCaseModule.kt**
```kotlin
val useCaseModule = module {
    factory { GetUserUseCase(repo = get()) }
    factory { DeleteUserUseCase(repo = get()) }
}
```

**PresentationModule.kt**
```kotlin
val presentationModule = module {
    viewModel { UserViewModel(
        useCase1 = get(),
        useCase2 = get()
    ) }
}
```

**MainActivity.kt or Application.kt**
```kotlin
class App : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@App)
            modules(AppModule.create())
        }
    }
}
```

## Multi-Feature Navigation

```
navigation/
├── NavGraph.kt                       # Root navigation
├── userNavigation.kt                 # User feature routes
├── authNavigation.kt                 # Auth feature routes
└── diaryNavigation.kt                # Diary feature routes

// In NavGraph.kt
NavHost(navController, startDestination = "auth") {
    authNavigation(navController)
    userNavigation(navController)
    diaryNavigation(navController)
}
```

## Module Dependencies

**Recommended dependency order:**

```
ui/ (Compose/XML)
  ↓
presentation/ (state, intent, viewmodel, side_effect)
  ↓
domain/ (usecase, repository interface, models)
  ↓
data/ (repository implementation, datasource)
  ↓
core/ (di, utils, common ui)
```

**Key Rule**: Lower layers don't depend on upper layers
- data/ doesn't know about presentation/ or ui/
- domain/ doesn't depend on data/ (only interfaces)
- presentation/ depends on domain/ usecases

## Gradle Module Structure (Advanced)

```gradle
settings.gradle.kts

include(":app")
include(":core:common")
include(":core:di")
include(":feature:user")
include(":feature:auth")
include(":feature:diary")

// Each feature is a separate module
// app/ depends on feature modules
// feature modules depend on core
```

## Testing Structure

```
user/
├── src/main/...          # Source code
├── src/test/kotlin/      # Unit tests (JVM)
│   └── com/digitaldiary/feature/user/
│       ├── domain/
│       │   └── usecase/
│       │       └── GetUserUseCaseTest.kt
│       ├── presentation/
│       │   └── viewmodel/
│       │       └── UserViewModelTest.kt
│       └── data/
│           └── repository/
│               └── UserRepositoryTest.kt
│
└── src/androidTest/...    # Instrumented tests (Android)
    └── com/digitaldiary/feature/user/
        └── ui/
            └── UserScreenTest.kt
```
