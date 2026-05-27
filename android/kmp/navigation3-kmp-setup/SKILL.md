---
name: navigation3-kmp-setup
description: Provides guidelines for configuring and implementing Jetpack Navigation 3 in Kotlin Multiplatform (KMP) projects. This skill covers route serialization, backstack management, NavDisplay setup, and nested navigation patterns suitable for Compose Multiplatform.
---

# Jetpack Navigation 3 in KMP Setup Guide

Use this skill to implement or refactor navigation in a Kotlin Multiplatform project using Navigation 3. This approach leverages Kotlin Serialization for type-safe routing and is designed for Compose Multiplatform.

## 1. Dependency Configuration

Ensure the following dependencies are added to your `commonMain` source set in `build.gradle.kts`:

```kotlin
sourceSets {
    commonMain.dependencies {
        implementation("org.jetbrains.androidx.navigation:navigation3-ui:2.8.0-alpha10") // Use latest version
        implementation("org.jetbrains.androidx.lifecycle:lifecycle-viewmodel-navigation3:2.8.0-alpha10")
        implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.1")
    }
}
```

## 2. Defining Type-Safe Routes

Define your routes as `@Serializable` objects or data classes implementing `NavKey`.

```kotlin
@Serializable
sealed interface PiRoute : NavKey {
    @Serializable
    data object Home : PiRoute
    
    @Serializable
    data class Details(val id: Long) : PiRoute
    
    sealed interface BottomBar : PiRoute {
        @Serializable
        data object Profile : BottomBar
    }
}
```

## 3. Serialization Configuration

Navigation 3 requires a `SavedStateConfiguration` to handle polymorphic serialization of your `NavKey` implementations.

```kotlin
val navConfig = SavedStateConfiguration {
    serializersModule = SerializersModule {
        polymorphic(NavKey::class) {
            subclass(PiRoute.Home::class, PiRoute.Home.serializer())
            subclass(PiRoute.Details::class, PiRoute.Details.serializer())
            // Register all subclasses
        }
    }
}
```

## 4. Implementing the Navigation Root

Use `rememberNavBackStack` to manage the state and `NavDisplay` to render the screens.

```kotlin
@Composable
fun NavigationRoot() {
    val backStack = rememberNavBackStack(navConfig, PiRoute.Home)
    
    NavDisplay(
        backStack = backStack,
        entryProvider = entryProvider {
            entry<PiRoute.Home> {
                HomeScreen(onNavigateDetails = { id ->
                    backStack.add(PiRoute.Details(id))
                })
            }
            entry<PiRoute.Details> { route ->
                DetailsScreen(id = route.id, onBack = {
                    backStack.removeLast()
                })
            }
        }
    )
}
```

## 5. Nested Navigation (e.g., BottomBar)

For nested navigation, create a separate `rememberNavBackStack` and pass the parent backstack if cross-navigation is needed.

```kotlin
@Composable
fun BottomNavGraph(parentBackStack: NavBackStack<NavKey>) {
    val bottomBackStack = rememberNavBackStack(bottomConfig, PiRoute.BottomBar.Profile)
    
    Scaffold(
        bottomBar = { PiBottomBar(bottomBackStack) }
    ) { innerPadding ->
        NavDisplay(
            backStack = bottomBackStack,
            entryProvider = entryProvider {
                entry<PiRoute.BottomBar.Profile> {
                    ProfileScreen(onFullDetails = { id ->
                        parentBackStack.add(PiRoute.Details(id))
                    })
                }
            }
        )
    }
}
```

## 6. Integration with ViewModels (Koin)

When using Koin, you can provide ViewModels scoped to the navigation entry or use `koinViewModel()` directly within the `entry` block.

```kotlin
entry<PiRoute.Home> {
    val viewModel: HomeViewModel = koinViewModel()
    HomeScreen(viewModel)
}
```

## Best Practices

- **Polymorphism:** Always register all route subclasses in the `SerializersModule`.
- **Backstack Management:** Use `backStack.add()`, `backStack.removeLast()`, and `backStack.clear()` to manipulate history.
- **Composition Local:** Consider providing the backstack via `CompositionLocal` if deep nesting makes passing it cumbersome.
- **Deep Links:** Navigation 3 handles deep links by restoring the backstack from the `SavedState`. Ensure your data classes are concise.
