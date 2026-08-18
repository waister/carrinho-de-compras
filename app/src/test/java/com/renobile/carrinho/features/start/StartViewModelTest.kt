package com.renobile.carrinho.features.start

import android.util.Log
import app.cash.turbine.test
import com.renobile.carrinho.network.AppConfigs
import com.renobile.carrinho.network.ConfigApiService
import com.renobile.carrinho.network.ConfigResponse
import com.renobile.carrinho.util.PREF_ADMOB_ID
import com.renobile.carrinho.util.PREF_APP_NAME
import com.renobile.carrinho.util.PREF_DEVICE_ID
import com.renobile.carrinho.util.PREF_FCM_TOKEN
import com.renobile.carrinho.util.PREF_SHARE_LINK
import com.renobile.carrinho.util.Prefs
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.mockk.unmockkObject
import io.mockk.unmockkStatic
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class StartViewModelTest {

    private val apiService = mockk<ConfigApiService>()

    @Before
    fun setup() {
        mockkObject(Prefs)
        every { Prefs.getValue<String>(any(), any()) } answers { secondArg() }
        every { Prefs.putValue(any(), any<Any>()) } just Runs
        mockkStatic(Log::class)
        every { Log.i(any(), any()) } returns 0
        every { Log.e(any(), any()) } returns 0
    }

    @After
    fun tearDown() {
        unmockkObject(Prefs)
        unmockkStatic(Log::class)
        Dispatchers.resetMain()
    }

    private fun newViewModel() = StartViewModel(apiService)

    @Test
    fun `given saved device id and empty admob id, when start, then identifies and persists configs`() = runTest {
        Dispatchers.setMain(UnconfinedTestDispatcher(testScheduler))
        every { Prefs.getValue(PREF_DEVICE_ID, "") } returns "device-1-v3"
        every { Prefs.getValue(PREF_FCM_TOKEN, "") } returns "fcm-token"
        val configs = AppConfigs(
            storeLink = "https://store.example.com",
            appName = "MeuApp",
            admobId = "admob-id",
            admobAdMainId = "main",
            admobInterstitialId = "inter",
            admobRemoveAdsId = "remove",
            admobOpenAppId = "open",
            planVideoDuration = 1000L,
        )
        coEvery { apiService.identify("fcm-token") } returns ConfigResponse(true, 5, 3, configs)

        val vm = newViewModel()
        vm.events.test {
            vm.start()

            assertEquals(StartEvents.NavigateToMain, awaitItem())
        }

        verify(exactly = 1) { Prefs.putValue(PREF_SHARE_LINK, "https://store.example.com") }
        verify(exactly = 1) { Prefs.putValue(PREF_APP_NAME, "MeuApp") }
        verify(exactly = 1) { Prefs.putValue(PREF_ADMOB_ID, "admob-id") }
    }

    @Test
    fun `given identify failure, when start, then still navigates to main`() = runTest {
        Dispatchers.setMain(UnconfinedTestDispatcher(testScheduler))
        every { Prefs.getValue(PREF_DEVICE_ID, "") } returns "device-1-v3"
        coEvery { apiService.identify(any()) } throws RuntimeException("boom")

        val vm = newViewModel()
        vm.events.test {
            vm.start()

            assertEquals(StartEvents.NavigateToMain, awaitItem())
        }
    }

    @Test
    fun `given admob id present, when start, then navigates to main without identifying`() = runTest {
        Dispatchers.setMain(UnconfinedTestDispatcher(testScheduler))
        every { Prefs.getValue(PREF_DEVICE_ID, "") } returns "device-1-v3"
        every { Prefs.getValue(PREF_ADMOB_ID, "") } returns "admob-id"

        val vm = newViewModel()
        vm.events.test {
            vm.start()
            advanceTimeBy(300)
            advanceUntilIdle()

            assertEquals(StartEvents.NavigateToMain, awaitItem())
        }
        coVerify(exactly = 0) { apiService.identify(any()) }
    }
}
