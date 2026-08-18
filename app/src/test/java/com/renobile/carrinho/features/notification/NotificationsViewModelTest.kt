package com.renobile.carrinho.features.notification

import com.renobile.carrinho.network.NotificationApiService
import com.renobile.carrinho.network.models.NotificationModel
import com.renobile.carrinho.network.models.NotificationResponse
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class NotificationsViewModelTest {

    private val apiService = mockk<NotificationApiService>()
    private val dispatcher = UnconfinedTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `given success response, when init, then notifications are loaded`() = runTest {
        val notifications = listOf(
            NotificationModel("1", "Oferta", "Texto", "2026-08-08", "img.png"),
        )
        coEvery { apiService.getNotifications() } returns
            NotificationResponse(true, null, notifications)

        val vm = NotificationsViewModel(apiService)

        assertEquals(notifications, vm.uiState.value.notifications)
        assertTrue(!vm.uiState.value.isLoading)
        assertEquals(null, vm.uiState.value.error)
    }

    @Test
    fun `given failure response, when init, then error is set`() = runTest {
        coEvery { apiService.getNotifications() } returns
            NotificationResponse(false, "erro", null)

        val vm = NotificationsViewModel(apiService)

        assertEquals("erro", vm.uiState.value.error)
        assertTrue(vm.uiState.value.notifications.isEmpty())
    }

    @Test
    fun `given exception, when init, then error is set`() = runTest {
        coEvery { apiService.getNotifications() } throws RuntimeException("boom")

        val vm = NotificationsViewModel(apiService)

        assertEquals("boom", vm.uiState.value.error)
    }
}
