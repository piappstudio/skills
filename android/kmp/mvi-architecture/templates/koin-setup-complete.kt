// ============================================================
// Complete Koin Setup for MVI Architecture
// ============================================================

// Step 1: Create DI Modules
// File: core/di/AppModule.kt

package com.digitaldiary.core.di

import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.dsl.module

// ============================================================
// DATA LAYER
// ============================================================

val dataSourceModule = module {
    // Remote data sources
    single { UserRemoteDataSource(get()) }  // retrofit instance from context
    single { DiaryRemoteDataSource(get()) }
    
    // Local data sources
    single { UserLocalDataSource(get()) }   // database instance
    single { DiaryLocalDataSource(get()) }
}

val repositoryModule = module {
    // Repositories - typically singleton
    single<UserRepository> {
        UserRepositoryImpl(
            remoteDataSource = get(),
            localDataSource = get()
        )
    }
    
    single<DiaryRepository> {
        DiaryRepositoryImpl(
            remoteDataSource = get(),
            localDataSource = get()
        )
    }
}

// ============================================================
// DOMAIN LAYER (UseCases)
// ============================================================

val useCaseModule = module {
    // User UseCases - factory creates new instance each time
    factory { GetUserUseCase(repository = get()) }
    factory { GetUsersUseCase(repository = get()) }
    factory { DeleteUserUseCase(repository = get()) }
    factory { UpdateUserUseCase(repository = get()) }
    factory { CreateUserUseCase(repository = get()) }
    factory { SearchUsersUseCase(repository = get()) }
    
    // Diary UseCases
    factory { GetDiaryEntriesUseCase(repository = get()) }
    factory { CreateDiaryEntryUseCase(repository = get()) }
    factory { DeleteDiaryEntryUseCase(repository = get()) }
}

// ============================================================
// PRESENTATION LAYER (ViewModels)
// ============================================================

val presentationModule = module {
    // ViewModels - use viewModel scope (lifecycle-aware)
    viewModel { UserViewModel(
        getUserUseCase = get(),
        getUsersUseCase = get(),
        deleteUserUseCase = get(),
        updateUserUseCase = get(),
        createUserUseCase = get()
    ) }
    
    viewModel { UserDetailViewModel(
        getUserUseCase = get(),
        updateUserUseCase = get(),
        deleteUserUseCase = get()
    ) }
    
    viewModel { DiaryListViewModel(
        getDiaryEntriesUseCase = get(),
        createDiaryEntryUseCase = get(),
        deleteDiaryEntryUseCase = get()
    ) }
    
    viewModel { DiaryDetailViewModel(
        getDiaryEntryUseCase = get(),
        deleteDiaryEntryUseCase = get()
    ) }
}

// ============================================================
// APP MODULE - Combines all
// ============================================================

object AppModule {
    fun create() = listOf(
        dataSourceModule,
        repositoryModule,
        useCaseModule,
        presentationModule
    )
}


// ============================================================
// Step 2: Initialize Koin in Application
// File: DigitalDiaryApp.kt
// ============================================================

package com.digitaldiary

import android.app.Application
import com.digitaldiary.core.di.AppModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

class DigitalDiaryApp : Application() {
    override fun onCreate() {
        super.onCreate()
        
        // Start Koin DI
        startKoin {
            // Log Koin events
            androidLogger(Level.DEBUG)
            
            // Android context available for injection
            androidContext(this@DigitalDiaryApp)
            
            // Load all modules
            modules(AppModule.create())
        }
    }
}


// ============================================================
// Step 3: Update AndroidManifest.xml
// ============================================================

/*
<manifest ...>
    <application
        android:name=".DigitalDiaryApp"
        ...>
        <!-- rest of config -->
    </application>
</manifest>
*/


// ============================================================
// Step 4: Use in Fragments
// ============================================================

package com.digitaldiary.feature.user.ui

import androidx.fragment.app.Fragment
import com.digitaldiary.feature.user.presentation.viewmodel.UserViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class UserFragment : Fragment() {
    
    // Lazy inject ViewModel - gets lifecycle scope automatically
    private val viewModel: UserViewModel by viewModel()
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Collect state
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    render(state)
                }
            }
        }
        
        // Handle side effects
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.sideEffect.collect { effect ->
                    handleEffect(effect)
                }
            }
        }
    }
    
    private fun render(state: UserUiState) {
        // Update UI based on state
    }
    
    private suspend fun handleEffect(effect: UserSideEffect) {
        when (effect) {
            is UserSideEffect.ShowMessage -> showToast(effect.message)
            is UserSideEffect.NavigateToDetail -> navigate(effect.userId)
            else -> {}
        }
    }
}


// ============================================================
// Step 5: Use in Compose
// ============================================================

package com.digitaldiary.feature.user.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import com.digitaldiary.feature.user.presentation.viewmodel.UserViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun UserScreen(
    viewModel: UserViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    // Handle side effects
    LaunchedEffect(Unit) {
        viewModel.sideEffect.collect { effect ->
            when (effect) {
                is UserSideEffect.ShowMessage -> 
                    showToast(effect.message)
                is UserSideEffect.NavigateToDetail -> 
                    navController.navigate("detail/${effect.userId}")
                else -> {}
            }
        }
    }
    
    // Render UI based on state
    when {
        uiState.isLoading -> LoadingScreen()
        uiState.error != null -> ErrorScreen(uiState.error)
        else -> UserListContent(
            users = uiState.users,
            onUserClick = { user ->
                viewModel.handleIntent(UserIntent.SelectUser(user.id))
            }
        )
    }
}


// ============================================================
// Step 6: Gradle Dependencies (build.gradle.kts)
// ============================================================

/*
dependencies {
    // Koin
    implementation("io.insert-koin:koin-android:3.5.0")
    implementation("io.insert-koin:koin-androidx-viewmodel:3.5.0")
    implementation("io.insert-koin:koin-androidx-compose:3.5.0")
    
    // For Compose
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.6.0")
    
    // Testing
    testImplementation("io.insert-koin:koin-test:3.5.0")
}
*/


// ============================================================
// Step 7: Testing with Koin
// ============================================================

package com.digitaldiary.feature.user.presentation.viewmodel

import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import kotlin.test.assertEquals

class UserViewModelTest {
    
    @Before
    fun setup() {
        // Start a test Koin context
        startKoin {
            modules(testModule)
        }
    }
    
    private val testModule = module {
        // Mock UseCases
        factory {
            mockk<GetUsersUseCase>().apply {
                coEvery { this@apply.invoke() } returns 
                    Result.Success(listOf(User("1", "Alice")))
            }
        }
        
        // ViewModel using mocked UseCases
        factory { GetUsersUseCase(repository = get()) }
    }
    
    @Test
    fun testLoadUsers() = runTest {
        val useCase = mockk<GetUsersUseCase>()
        coEvery { useCase() } returns Result.Success(listOf(User("1", "Alice")))
        
        val viewModel = UserViewModel(useCase, mockk())
        
        // Test state changes
        assertEquals(false, viewModel.uiState.value.isLoading)
    }
    
    @After
    fun tearDown() {
        stopKoin()
    }
}
