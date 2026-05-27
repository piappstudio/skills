---
name: mvi-architecture
description: 'Convert Android ViewModels to MVI (Model-View-Intent) architecture. Use when: migrating to MVI, implementing new MVI screens, separating UI state/intents/side effects, extracting business logic into UseCases.'
argument-hint: 'Specify the ViewModel/feature name to convert (e.g., "UserViewModel", "AuthScreen")'
---

# MVI (Model-View-Intent) Architecture

A comprehensive guide to converting Android ViewModels to MVI architecture with organized state management, intent handling, and UseCase-driven business logic.

## What is MVI?

MVI decouples concerns into:
- **Model**: Immutable UI state objects (UiState)
- **View**: UI that renders state and sends intents
- **Intent**: User actions and events (Intent)
- **Side Effect**: One-time events (SideEffect)
- **UseCase**: Business logic layer extracting heavy operations

## Project Structure

```
feature/
├── data/                       # Network, DB, repositories
├── domain/
│   ├── usecase/               # Business logic extracted here
│   │   ├── GetUserUseCase.kt
│   │   └── ValidateEmailUseCase.kt
│   └── model/                 # Domain models
├── presentation/
│   ├── state/                 # UiState definitions
│   │   └── UserUiState.kt
│   ├── intent/                # User intents/actions
│   │   └── UserIntent.kt
│   ├── side_effect/           # One-time events
│   │   └── UserSideEffect.kt
│   └── viewmodel/             # MVI-based ViewModel
│       └── UserViewModel.kt
└── ui/                        # Composables/Fragments
    └── UserScreen.kt
```

## Key Components

| Component | Purpose | Location |
|-----------|---------|----------|
| **UiState** | Immutable state snapshot | `/state/` |
| **Intent** | User actions to process | `/intent/` |
| **SideEffect** | One-time events (snackbar, nav) | `/side_effect/` |
| **UseCase** | Business logic, isolated testable | `/domain/usecase/` |
| **ViewModel** | Orchestrates UiState, Intent, SideEffect | `/presentation/viewmodel/` |

## When to Use

✅ Complex screens with multiple state transitions  
✅ Heavy business logic to isolate and test  
✅ Multiple UI components reacting to shared state  
✅ Need for undo/redo or time-travel debugging  
✅ Following modern Android architecture patterns  

## Quick Start Workflow

### Phase 1: Define State Schema
1. Create `UiState` data class in `/state/`
2. Include loading, success, error, idle states
3. Keep all UI data needed in one object

### Phase 2: Define Intents
1. Create sealed class for `Intent` in `/intent/`
2. One subclass per user action
3. Include necessary parameters

### Phase 3: Extract BusinessLogic → UseCases
1. Move complex operations to `/domain/usecase/`
2. One UseCase per business operation
3. Make them injectable, testable

### Phase 4: Implement ViewModel
1. Setup MVI ViewModel scaffolding
2. Handle intents → update state
3. Emit side effects for one-time events
4. Wire UseCases for business logic

### Phase 5: Connect UI
1. Collect state flows in UI
2. Send intents from UI events
3. Handle side effects (navigation, snackbars)

## Supported DI Frameworks

- **Koin** (Recommended) - Lightweight, Kotlin-first, minimal boilerplate
- **Hilt** - Android-focused, compile-time safety, Dagger-based
- **Manual DI** - Full control, no framework overhead

All examples use **Koin** by default. See references for framework-specific setup.

## Reference Documents

- **[MVI Fundamentals](references/mvi-fundamentals.md)**: Core concepts and patterns
- **[Folder Structure](references/folder-structure.md)**: Detailed directory organization (Koin examples)
- **[UseCase Pattern](references/usecase-pattern.md)**: Extracting and structuring UseCases
- **[Conversion Guide](references/conversion-guide.md)**: Step-by-step migration from ViewModel
- **[MVI Architecture Checklist](checklists/mvi-checklist.md)**: Verification points

## Templates

Quick-start templates for common patterns:
- [Complete MVI ViewModel](templates/mvi-viewmodel-template.kt)
- [UiState Example](templates/uistate-template.kt)
- [Intent Sealed Class](templates/intent-template.kt)
- [SideEffect Sealed Class](templates/sideeffect-template.kt)
- [UseCase Template](templates/usecase-template.kt)

## Common Questions

**Q: How do I handle navigation?**  
A: Use `SideEffect` for navigation events. Emit `SideEffect.NavigateTo(route)` and handle in UI.

**Q: Where does repository logic go?**  
A: In `UseCase`. Repository injected into UseCase, which orchestrates data fetching and transformation.

**Q: How do I test this?**  
A: Mock UseCase in ViewModel tests. Test UseCase logic in integration tests with mock repository.

**Q: Can I use this with Compose?**  
A: Yes! Collect `uiState` and `sideEffect` flows in Compose and emit intents on user interaction.

**Q: Should every screen use MVI?**  
A: No. Use for complex screens. Simple screens can use basic MVVM.

## Benefits

✨ **Testable**: UseCases isolated, ViewModel logic predictable  
✨ **Scalable**: Clear separation of concerns  
✨ **Debuggable**: All state changes traceable  
✨ **Maintainable**: Clear data flow (state → intent → effect)  
✨ **Predictable**: Immutable state, functional updates  
