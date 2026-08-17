package com.renobile.carrinho.features.comparator

import com.renobile.carrinho.util.PREF_PRICE_FIRST
import com.renobile.carrinho.util.PREF_PRICE_SECOND
import com.renobile.carrinho.util.PREF_SIZE_FIRST
import com.renobile.carrinho.util.PREF_SIZE_SECOND
import com.renobile.carrinho.util.Prefs
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockkObject
import io.mockk.unmockkObject
import io.mockk.verify
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ComparatorViewModelTest {

    @Before
    fun setup() {
        mockkObject(Prefs)
        every { Prefs.getValue<String>(any(), any()) } answers { secondArg() }
        every { Prefs.putValue(any(), any<Any>()) } just Runs
    }

    @After
    fun tearDown() {
        unmockkObject(Prefs)
    }

    private fun newViewModel() = ComparatorViewModel(RuntimeEnvironment.getApplication())

    @Test
    fun `given empty prefs, when init, then state starts empty without result`() {
        val vm = newViewModel()

        val state = vm.uiState.value
        assertEquals("", state.priceFirst)
        assertEquals("", state.sizeFirst)
        assertEquals("", state.priceSecond)
        assertEquals("", state.sizeSecond)
        assertFalse(state.showResult)
    }

    @Test
    fun `given saved prefs, when init, then restores previous values`() {
        every { Prefs.getValue(PREF_PRICE_FIRST, "") } returns "10,00"
        every { Prefs.getValue(PREF_SIZE_FIRST, "") } returns "2"
        every { Prefs.getValue(PREF_PRICE_SECOND, "") } returns "20,00"
        every { Prefs.getValue(PREF_SIZE_SECOND, "") } returns "2"

        val vm = newViewModel()

        assertEquals("10,00", vm.uiState.value.priceFirst)
        assertEquals("2", vm.uiState.value.sizeFirst)
        assertEquals("20,00", vm.uiState.value.priceSecond)
        assertEquals("2", vm.uiState.value.sizeSecond)
        assertTrue(vm.uiState.value.showResult)
    }

    @Test
    fun `given price change, when onPriceFirstChanged, then updates state and hides result`() {
        val vm = newViewModel()

        vm.onPriceFirstChanged("R$ 10,00")

        assertEquals("R$ 10,00", vm.uiState.value.priceFirst)
        assertFalse(vm.uiState.value.showResult)
    }

    @Test
    fun `given full values, when calculate, then computes result`() {
        val vm = newViewModel()
        vm.onPriceFirstChanged("10,00")
        vm.onSizeFirstChanged("2")
        vm.onPriceSecondChanged("20,00")
        vm.onSizeSecondChanged("2")

        vm.calculate()

        val state = vm.uiState.value
        assertTrue(state.showResult)
        assertNotNull(state.resultFirst)
        assertNotNull(state.resultSecond)
        assertNotNull(state.resultPercentage)
    }

    @Test
    fun `given equal values, when calculate, then result percentage is not null`() {
        val vm = newViewModel()
        vm.onPriceFirstChanged("10,00")
        vm.onSizeFirstChanged("2")
        vm.onPriceSecondChanged("10,00")
        vm.onSizeSecondChanged("2")

        vm.calculate()

        assertTrue(vm.uiState.value.showResult)
        assertNotNull(vm.uiState.value.resultPercentage)
    }

    @Test
    fun `given missing size, when calculate, then does not show result`() {
        val vm = newViewModel()
        vm.onPriceFirstChanged("10,00")

        vm.calculate()

        assertFalse(vm.uiState.value.showResult)
    }

    @Test
    fun `when clear, then resets fields and persists empty values`() {
        val vm = newViewModel()
        vm.onPriceFirstChanged("10,00")
        vm.onSizeFirstChanged("2")

        vm.clear()

        val state = vm.uiState.value
        assertEquals("", state.priceFirst)
        assertEquals("", state.sizeFirst)
        assertEquals("", state.priceSecond)
        assertEquals("", state.sizeSecond)
        assertFalse(state.showResult)
        verify(exactly = 4) { Prefs.putValue(any(), any<Any>()) }
    }
}
