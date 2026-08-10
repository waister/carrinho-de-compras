package com.renobile.carrinho.features.notification.detail

import com.renobile.carrinho.network.NotificationApiService
import com.renobile.carrinho.network.models.NotificationModel
import com.renobile.carrinho.network.models.NotificationResponse
import com.renobile.carrinho.util.PREF_NOTIFICATION_JSON
import com.renobile.carrinho.util.Prefs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
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
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
@OptIn(ExperimentalCoroutinesApi::class)
class NotificationDetailsViewModelTest {

    private val apiService = mockk<NotificationApiService>()
    private val dispatcher = UnconfinedTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(dispatcher)
        mockkObject(Prefs)
        every { Prefs.getValue<String>(any(), any()) } answers { secondArg() }
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkObject(Prefs)
    }

    private fun newViewModel() = NotificationDetailsViewModel(apiService)

    @Test
    fun `given cached json, when init, then loads notification from cache without network`() = runTest {
        every { Prefs.getValue(PREF_NOTIFICATION_JSON + "10", "") } returns
            """{"title":"Oferta","body":"Texto","date":"2026-08-08","image":"img.png"}"""

        val vm = newViewModel()
        vm.init("10")

        val notification = vm.uiState.value.notification
        assertEquals("10", notification?.id)
        assertEquals("Oferta", notification?.title)
        assertEquals("Texto", notification?.body)
        assertEquals("2026-08-08", notification?.date)
        assertEquals("img.png", notification?.image)
        coVerify(exactly = 0) { apiService.getNotificationDetail(any()) }
    }

    @Test
    fun `given no cache and success response, when init, then loads notification from api`() = runTest {
        coEvery { apiService.getNotificationDetail("10") } returns NotificationResponse(
            true, null, listOf(NotificationModel("10", "Oferta", "Texto", "2026-08-08", "img.png"))
        )

        val vm = newViewModel()
        vm.init("10")

        assertEquals("Oferta", vm.uiState.value.notification?.title)
        assertEquals(null, vm.uiState.value.error)
    }

    @Test
    fun `given no cache and failure response, when init, then error is set`() = runTest {
        coEvery { apiService.getNotificationDetail("10") } returns
            NotificationResponse(false, "not found", null)

        val vm = newViewModel()
        vm.init("10")

        assertNull(vm.uiState.value.notification)
        assertEquals("not found", vm.uiState.value.error)
    }

    @Test
    fun `given no cache and exception, when init, then error is set`() = runTest {
        coEvery { apiService.getNotificationDetail("10") } throws RuntimeException("boom")

        val vm = newViewModel()
        vm.init("10")

        assertEquals("boom", vm.uiState.value.error)
    }
}
