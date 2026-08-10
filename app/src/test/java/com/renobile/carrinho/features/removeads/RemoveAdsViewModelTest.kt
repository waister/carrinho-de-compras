package com.renobile.carrinho.features.removeads

import com.google.android.gms.ads.MobileAds
import com.renobile.carrinho.util.PREF_ADMOB_REMOVE_ADS_ID
import com.renobile.carrinho.util.PREF_HAVE_PLAN
import com.renobile.carrinho.util.Prefs
import io.mockk.every
import io.mockk.just
import io.mockk.mockkObject
import io.mockk.Runs
import io.mockk.mockkStatic
import io.mockk.unmockkObject
import io.mockk.verify
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class RemoveAdsViewModelTest {

    @Before
    fun setup() {
        mockkObject(Prefs)
        every { Prefs.getValue<String>(any(), any()) } answers { secondArg() }
        every { Prefs.getValue<Boolean>(any(), any()) } answers { secondArg() }
        every { Prefs.getValue<Long>(any(), any()) } answers { secondArg() }
        mockkStatic(MobileAds::class)
        every { MobileAds.initialize(any(), any()) } just Runs
    }

    @After
    fun tearDown() {
        unmockkObject(Prefs)
    }

    private fun newViewModel() = RemoveAdsViewModel(RuntimeEnvironment.getApplication())

    @Test
    fun `given no plan, when init, then description is loaded`() {
        val vm = newViewModel()

        assertFalse(vm.uiState.value.haveVideoPlan)
        assertTrue(vm.uiState.value.description.isNotEmpty())
    }

    @Test
    fun `given active plan, when loadAd, then returns early without initializing ads`() {
        every { Prefs.getValue(PREF_HAVE_PLAN, false) } returns true
        val vm = newViewModel()

        vm.loadAd()

        verify(exactly = 0) { MobileAds.initialize(any(), any()) }
        assertFalse(vm.uiState.value.isLoading)
    }

    @Test
    fun `given valid ad unit id, when loadAd, then starts loading ad`() {
        every { Prefs.getValue(PREF_ADMOB_REMOVE_ADS_ID, "") } returns "ca-app-pub-123"
        val vm = newViewModel()

        vm.loadAd()

        verify(exactly = 1) { MobileAds.initialize(any(), any()) }
        assertTrue(vm.uiState.value.isLoading)
    }

    @Test
    fun `when dismissRestartDialog, then dialog is hidden`() {
        val vm = newViewModel()

        vm.dismissRestartDialog()

        assertFalse(vm.uiState.value.showRestartDialog)
    }
}
