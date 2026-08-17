package com.renobile.carrinho.repositories

import android.util.Log
import com.renobile.carrinho.network.AppConfigs
import com.renobile.carrinho.network.ConfigApiService
import com.renobile.carrinho.network.ConfigResponse
import com.renobile.carrinho.util.PREF_ADMOB_AD_MAIN_ID
import com.renobile.carrinho.util.PREF_ADMOB_ID
import com.renobile.carrinho.util.PREF_ADMOB_INTERSTITIAL_ID
import com.renobile.carrinho.util.PREF_ADMOB_OPEN_APP_ID
import com.renobile.carrinho.util.PREF_ADMOB_REMOVE_ADS_ID
import com.renobile.carrinho.util.PREF_APP_NAME
import com.renobile.carrinho.util.PREF_PLAN_VIDEO_DURATION
import com.renobile.carrinho.util.PREF_SHARE_LINK
import com.renobile.carrinho.util.Prefs
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.mockk.unmockkObject
import io.mockk.unmockkStatic
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ConfigRepositoryImplTest {

    private val apiService = mockk<ConfigApiService>()
    private val repository = ConfigRepositoryImpl(apiService)

    @Before
    fun setup() {
        mockkStatic(Log::class)
        every { Log.i(any(), any()) } returns 0
        every { Log.e(any(), any()) } returns 0
    }

    @After
    fun tearDown() {
        unmockkStatic(Log::class)
    }

    @Test
    fun `given identify success, when identify, then returns success with response`() = runTest {
        val response = ConfigResponse(true, 5, 3, null)
        coEvery { apiService.identify("token") } returns response

        val result = repository.identify("token")

        assertTrue(result.isSuccess)
        assertEquals(response, result.getOrNull())
    }

    @Test
    fun `given identify failure, when identify, then returns failure`() = runTest {
        coEvery { apiService.identify("token") } throws RuntimeException("network error")

        val result = repository.identify("token")

        assertTrue(result.isFailure)
    }

    @Test
    fun `given success response with configs, when saveConfig, then persists values`() {
        mockkObject(Prefs)
        every { Prefs.putValue(any(), any<Any>()) } just Runs

        val configs = AppConfigs(
            storeLink = "https://link.com",
            appName = "App",
            admobId = "admob",
            admobAdMainId = "main",
            admobInterstitialId = "inter",
            admobRemoveAdsId = "remove",
            admobOpenAppId = "open",
            planVideoDuration = 1000L
        )
        repository.saveConfig(ConfigResponse(true, 5, 3, configs))

        verify(exactly = 1) { Prefs.putValue(PREF_SHARE_LINK, "https://link.com") }
        verify(exactly = 1) { Prefs.putValue(PREF_APP_NAME, "App") }
        verify(exactly = 1) { Prefs.putValue(PREF_ADMOB_ID, "admob") }
        verify(exactly = 1) { Prefs.putValue(PREF_ADMOB_AD_MAIN_ID, "main") }
        verify(exactly = 1) { Prefs.putValue(PREF_ADMOB_INTERSTITIAL_ID, "inter") }
        verify(exactly = 1) { Prefs.putValue(PREF_ADMOB_REMOVE_ADS_ID, "remove") }
        verify(exactly = 1) { Prefs.putValue(PREF_ADMOB_OPEN_APP_ID, "open") }
        verify(exactly = 1) { Prefs.putValue(PREF_PLAN_VIDEO_DURATION, 1000L) }

        unmockkObject(Prefs)
    }

    @Test
    fun `given failure response, when saveConfig, then persists nothing`() {
        mockkObject(Prefs)
        every { Prefs.putValue(any(), any<Any>()) } just Runs

        repository.saveConfig(ConfigResponse(false, 5, 3, null))

        verify(exactly = 0) { Prefs.putValue(any(), any<Any>()) }

        unmockkObject(Prefs)
    }
}
