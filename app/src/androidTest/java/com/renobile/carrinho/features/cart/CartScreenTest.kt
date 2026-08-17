package com.renobile.carrinho.features.cart

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.renobile.carrinho.R
import com.renobile.carrinho.ui.theme.MyAppTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CartScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `givenEmptyCartState_whenScreenIsDisplayed_thenAddProductButtonIsShown`() {
        composeTestRule.setContent {
            MyAppTheme {
                CartScreen(
                    state = CartState(),
                    actions = CartActions()
                )
            }
        }

        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val addProductLabel = context.getString(R.string.add_product)

        composeTestRule.onNodeWithContentDescription(addProductLabel).assertIsDisplayed()
    }
}
