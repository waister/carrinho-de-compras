package com.renobile.carrinho

import com.renobile.carrinho.network.ConfigResponse
import com.renobile.carrinho.repositories.ConfigRepository
import com.renobile.carrinho.util.PREF_FCM_TOKEN
import com.renobile.carrinho.util.Prefs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.Runs
import io.mockk.unmockkObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModelTest {

    private val configRepository = mockk<ConfigRepository>(relaxed = true)
    private val dispatcher = UnconfinedTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(dispatcher)
        mockkObject(Prefs)
        every { Prefs.getValue(any(), any<Any>()) } answers { secondArg() }
        every { Prefs.putValue(any(), any<Any>()) } just Runs
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkObject(Prefs)
    }

    private fun newViewModel() = MainViewModel(configRepository)

    @Test
    fun `given no fcm token, when checkVersion, then does not call repository`() = runTest {
        val vm = newViewModel()

        vm.checkVersion()

        coVerify(exactly = 0) { configRepository.identify(any()) }
    }

    @Test
    fun `given token and version below minimum, when checkVersion, then version update needed`() = runTest {
        every { Prefs.getValue(PREF_FCM_TOKEN, "") } returns "fcm-token"
        coEvery { configRepository.identify("fcm-token") } returns
            Result.success(ConfigResponse(true, 0, Int.MAX_VALUE, null))

        val vm = newViewModel()
        vm.checkVersion()

        assertEquals(VersionUpdate.Needed, vm.uiState.value.versionUpdate)
        coVerify(exactly = 1) { configRepository.saveConfig(any()) }
    }

    @Test
    fun `given token and version below last, when checkVersion, then version update available`() = runTest {
        every { Prefs.getValue(PREF_FCM_TOKEN, "") } returns "fcm-token"
        coEvery { configRepository.identify("fcm-token") } returns
            Result.success(ConfigResponse(true, 100, 27, null))

        val vm = newViewModel()
        vm.checkVersion()

        assertEquals(VersionUpdate.Available, vm.uiState.value.versionUpdate)
    }

    @Test
    fun `given identify failure, when checkVersion, then version update stays null`() = runTest {
        every { Prefs.getValue(PREF_FCM_TOKEN, "") } returns "fcm-token"
        coEvery { configRepository.identify("fcm-token") } returns Result.failure(RuntimeException("boom"))

        val vm = newViewModel()
        vm.checkVersion()

        assertNull(vm.uiState.value.versionUpdate)
        coVerify(exactly = 0) { configRepository.saveConfig(any()) }
    }

    @Test
    fun `given visible flag, when setBottomBarVisible, then updates state`() {
        val vm = newViewModel()

        vm.setBottomBarVisible(true)

        assertEquals(true, vm.uiState.value.isBottomBarVisible)
    }

    @Test
    fun `given visible flag, when setBarsVisible, then updates state`() {
        val vm = newViewModel()

        vm.setBarsVisible(false)

        assertEquals(false, vm.uiState.value.areBarsVisible)
    }

    @Test
    fun `when onVersionUpdateHandled, then version update is cleared`() = runTest {
        every { Prefs.getValue(PREF_FCM_TOKEN, "") } returns "fcm-token"
        coEvery { configRepository.identify("fcm-token") } returns
            Result.success(ConfigResponse(true, 0, Int.MAX_VALUE, null))
        val vm = newViewModel()
        vm.checkVersion()
        assertEquals(VersionUpdate.Needed, vm.uiState.value.versionUpdate)

        vm.onVersionUpdateHandled()

        assertNull(vm.uiState.value.versionUpdate)
    }
}
