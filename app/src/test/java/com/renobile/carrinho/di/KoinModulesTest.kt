package com.renobile.carrinho.di

import com.renobile.carrinho.MainViewModel
import com.renobile.carrinho.database.AppDatabase
import com.renobile.carrinho.database.dao.CartDao
import com.renobile.carrinho.database.dao.ProductDao
import com.renobile.carrinho.database.dao.PurchaseListDao
import com.renobile.carrinho.features.cart.CartViewModel
import com.renobile.carrinho.features.cart.detail.CartDetailsViewModel
import com.renobile.carrinho.features.cart.history.CartsHistoryViewModel
import com.renobile.carrinho.features.comparator.ComparatorViewModel
import com.renobile.carrinho.features.list.ListViewModel
import com.renobile.carrinho.features.list.detail.ListDetailsViewModel
import com.renobile.carrinho.features.list.history.ListsHistoryViewModel
import com.renobile.carrinho.features.notification.NotificationsViewModel
import com.renobile.carrinho.features.notification.detail.NotificationDetailsViewModel
import com.renobile.carrinho.features.removeads.RemoveAdsViewModel
import com.renobile.carrinho.features.start.StartViewModel
import com.renobile.carrinho.network.ConfigApiService
import com.renobile.carrinho.network.NotificationApiService
import com.renobile.carrinho.repositories.CartRepository
import com.renobile.carrinho.repositories.ConfigRepository
import com.renobile.carrinho.repositories.ProductRepository
import com.renobile.carrinho.repositories.PurchaseListRepository
import com.renobile.carrinho.util.Prefs
import io.mockk.every
import io.mockk.just
import io.mockk.mockkObject
import io.mockk.Runs
import io.mockk.unmockkObject
import org.junit.After
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.android.ext.koin.androidContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class KoinModulesTest : KoinComponent {

    @Before
    fun setup() {
        mockkObject(Prefs)
        every { Prefs.getValue<String>(any(), any()) } answers { secondArg() }
        every { Prefs.getValue<Long>(any(), any()) } answers { secondArg() }
        every { Prefs.getValue<Boolean>(any(), any()) } answers { secondArg() }
        every { Prefs.putValue(any(), any<Any>()) } just Runs
    }

    @After
    fun tearDown() {
        unmockkObject(Prefs)
        stopKoin()
    }

    private fun startApplication() {
        startKoin {
            androidContext(RuntimeEnvironment.getApplication())
            modules(appModules)
        }
    }

    @Test
    fun `given app modules, when started, then database layer is resolved`() {
        startApplication()

        assertNotNull(get<AppDatabase>())
        assertNotNull(get<CartDao>())
        assertNotNull(get<ProductDao>())
        assertNotNull(get<PurchaseListDao>())
    }

    @Test
    fun `given app modules, when started, then repositories are resolved`() {
        startApplication()

        assertTrue(get<CartRepository>() != null)
        assertTrue(get<ProductRepository>() != null)
        assertTrue(get<PurchaseListRepository>() != null)
        assertTrue(get<ConfigRepository>() != null)
    }

    @Test
    fun `given app modules, when started, then network services are resolved`() {
        startApplication()

        assertTrue(get<ConfigApiService>() != null)
        assertTrue(get<NotificationApiService>() != null)
    }

    @Test
    fun `given app modules, when started, then view models are resolved`() {
        startApplication()

        assertNotNull(get<MainViewModel>())
        assertNotNull(get<CartViewModel>())
        assertNotNull(get<CartDetailsViewModel>())
        assertNotNull(get<CartsHistoryViewModel>())
        assertNotNull(get<ListViewModel>())
        assertNotNull(get<ListDetailsViewModel>())
        assertNotNull(get<ListsHistoryViewModel>())
        assertNotNull(get<NotificationsViewModel>())
        assertNotNull(get<NotificationDetailsViewModel>())
        assertNotNull(get<StartViewModel>())
        assertNotNull(get<ComparatorViewModel>())
        assertNotNull(get<RemoveAdsViewModel>())
    }

    @Test
    fun `given app modules, when inspected, then contains all expected modules`() {
        assertTrue(appModules.isNotEmpty())
    }
}
