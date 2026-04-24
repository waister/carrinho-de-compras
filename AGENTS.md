# Agent Guide: KaBuM! Android App

This document provides essential information for LLM agents working on the KaBuM! Android application codebase.

## 1. Project Overview

This is the Android application for KaBuM!, a large e-commerce company in Brazil. The application is a multi-flavor app (`prod` and `stg`) built with modern Android technologies.

**Key Technologies:**

*   **Languages:** Kotlin
*   **UI:** AndroidX, Jetpack Compose, Material Design
*   **Architecture:** Clean Architecture with MVVM
*   **Networking:** Retrofit, Ktor
*   **Dependency Injection:** Koin
*   **Image Loading:** Glide, Coil
*   **Async:** Kotlin Coroutines
*   **Analytics:** Firebase Analytics, AppsFlyer, Insider
*   **Crash Reporting:** Firebase Crashlytics
*   **Testing:** JUnit, MockK, Robolectric, Turbine

## 2. Project Structure

The application follows a Single-Activity and MVVM architecture. The key source directories are organized into modules:

*   **`data`**: Contains data sources and repositories.
*   **`models`**: Contains data models.
*   **`di`**: Responsible for dependency injection using Koin.
*   **`features`**: Contains the different features of the application.
*   **`ui`**: Contains base shared UI components and theme configuration.
*   **`events`**: Contains a generic `AnalyticsEvents` class that centralizes all SDKs for logging events.
*   **`utils`**: Contains utility classes.

## 3. Creating a New Feature

New features are self-contained modules located under `app/src/main/java/br/com/kabum/webviewapp/features/`.

Follow this structure for a new feature (e.g., `my_feature`):

*   **`features/my_feature/MyFeatureFragment.kt`**: The `Fragment` that hosts the Compose UI. It injects the `ViewModel` using `by viewModel()`.
*   **`features/my_feature/MyFeatureViewModel.kt`**: The `ViewModel` responsible for the feature's logic and state.
*   **`features/my_feature/MyFeatureState.kt`**: Contains data classes and sealed interfaces representing the UI state.
*   **`features/my_feature/MyFeatureScreen.kt`**: The main `@Composable` that builds the UI for the feature.
*   **`features/my_feature/components/` (optional)**: A directory for smaller, reusable or feature-specific `@Composables`.
*   **`features/my_feature/usecases/` (optional)**: A directory for use cases that abstract complex business logic, keeping the `ViewModel` lean.

**UI Composition**
- Break UI into small, reusable Composables.
- Feature-specific components go inside `components/`.
- Shared components go in `ui/components/`.

**Use Cases**
- Use cases SHOULD be created when:
    - Business logic is reused
    - Logic is complex
    - The logic benefits from being isolated and independently testable
- Simple orchestration or direct repository calls MAY remain in the ViewModel

**Testing**
- Use `runTest` from `kotlinx-coroutines-test` for asynchronous code.
- Use `Turbine` for testing `Flow/StateFlow`.
- Mock dependencies using `mockk`.
- Test names should follow the pattern: `given [context], when [action], then [expected result]`.

**Analytics**
- Do not call Firebase/AppsFlyer directly in ViewModels or Fragments.
- Always use the `AnalyticsEvents` class (injected via Koin) to log events.

###  Key Principles
- State Hoisting to ViewModel: All state that affects behavior must live in the ViewModel's state
- ViewModel enforces Unidirectional Data Flow (UDF) with State/Action/Event pattern
- Actions must be implemented as `data class FeatureActions` that agregate the lambdas of all actions
- ViewModel state should be exposed as a single `StateFlow`.
- Reactivity using Kotlin Flows in ViewModel with `stateIn()` and other operators like map, transform, etc
- Always use `CoroutineExceptionHandlers` when launching new coroutines
- Always catch exceptions in ViewModel StateFlows with the `catchAndRecord{}` extension function
- Repository functions that return values must return Result<T> or custom sealed result types

## 4. Dependency Injection (Koin)

The project uses Koin for dependency injection. Dependencies are organized into several modules, each responsible for a specific layer of the application. These modules are located in `app/src/main/java/br/com/kabum/webviewapp/di/`.

*   **`CommonModule.kt`**: Contains general application-wide dependencies like analytics, utility classes, and various delegates.
*   **`LocalModule.kt`**: Declares dependencies related to local data storage (e.g., DataStores, SharedPreferences).
*   **`RepositoryModule.kt`**: Defines repository implementations, providing data access logic.
*   **`ServiceModule.kt`**: Configures and provides network service interfaces (e.g., Retrofit services).
*   **`UseCaseModule.kt`**: Declares application-specific business logic encapsulated in use case classes.
*   **`ViewModelModule.kt`**: Provides `ViewModel` instances for various features.

**To add a new dependency:**

1.  Identify the appropriate Koin module for your dependency.
2.  Add your dependency declaration using one of the following methods and the `bind` keyword if implementing an interface:
    ```kotlin
    // Declares a singleton instance that is created once and reused throughout the application.
    single { MySingletonClass(get()) }
    single<MyRepository> { MyRepositoryImpl(get()) }
    singleOf(::MyRepositoryImpl) bind MyRepository::class

    // Declares a new instance every time it is injected.
    // Suitable for dependencies that should not be shared or have mutable state.
    factory { MyInstanceClass(get()) }
    factory<MyUseCase> { MyUseCaseImpl(get()) }
    factoryOf(::MyUseCaseImpl) bind MyUseCase::class

    // Declares an Android ViewModel
    // It functions like a factory but integrates with the ViewModel lifecycle.
    viewModelOf(::MyNewViewModel)
    ```

> **Important!**
>
> Instances that need a CoroutineScope tied to the ViewModel lifecycle should be declared inside the `viewModelScope {}` block.

## 5. Build & Test Commands

The project has two main variants (flavors): `stg` and `prod`.

```bash
# Build all variants
./gradlew assemble

# Build a specific variant (e.g., Staging Debug)
./gradlew assembleStgDebug

# Run unit tests for a specific variant
./gradlew app:testStgDebugUnitTest -Ptest

# Check code quality with ktlint
./gradlew ktlintCheck

# Format code with ktlint
./gradlew ktlintFormat

# Run ktlint on a specific file
./gradlew ktlintCheck -PinternalKtlintGitFilter="path/to/file.kt"
```

## 6. Development Conventions

*   **Code Style:** `ktlint` is used to enforce a consistent style. Run `./gradlew ktlintCheck` before submitting changes.
*   **Branching:** Create branches from `main`. All pull requests target `main`.
*   **Commits:** Follow the [Conventional Commits](https://www.conventionalcommits.org/en/v1.0.0/) specification.
*   **Secrets:** Secrets are managed in a `keystore.properties` file in the root directory. This file is not version-controlled. The developer should have already provided this file.
*   **Dependencies:** All dependencies are managed in `gradle/libs.versions.toml`. Use aliases (e.g., `libs.my.dependency`) in `build.gradle.kts` files. Do not use hardcoded dependency strings.
