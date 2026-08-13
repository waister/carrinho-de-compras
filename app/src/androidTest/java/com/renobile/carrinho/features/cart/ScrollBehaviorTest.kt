package com.renobile.carrinho.features.cart

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.renobile.carrinho.database.entities.CartEntity
import com.renobile.carrinho.database.entities.ProductEntity
import com.renobile.carrinho.ui.theme.MyAppTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ScrollBehaviorTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testScrollingBehavior() {
        val products12 = (1..12).map { 
            ProductEntity(it.toLong(), 1, 0, "Product $it", 1.0, 10.0) 
        }
        val products8 = (1..8).map { 
            ProductEntity(it.toLong(), 1, 0, "Product $it", 1.0, 10.0) 
        }

        var cartState by mutableStateOf(CartState(
            cart = CartEntity(1, "Test Cart 12", System.currentTimeMillis(), 0, 12, 12.0, 120.0, ""),
            products = products12.reversed() // Reverse to have Product 12 at top
        ))
        
        var areBarsVisible by mutableStateOf(true)

        composeTestRule.setContent {
            MyAppTheme {
                CartScreen(
                    state = cartState,
                    actions = CartActions(
                        onScroll = { areBarsVisible = it }
                    ),
                    areBarsVisible = areBarsVisible
                )
            }
        }

        // 1. Verify bars are visible initially with 12 items
        composeTestRule.onNodeWithText("Carrinho: Test Cart 12").assertIsDisplayed()

        // 2. Scroll down to hide. Swipe on a visible item (Product 12 is first)
        composeTestRule.onNodeWithText("Product 12").performTouchInput {
            swipeUp()
        }
        
        // 3. Bars should hide. 
        // We use waitUntil because of AnimatedVisibility
        composeTestRule.waitUntil(5000) {
            composeTestRule.onAllNodesWithText("Carrinho: Test Cart 12").fetchSemanticsNodes().isEmpty()
        }

        // 4. Scroll up to show.
        composeTestRule.onNode(hasScrollAction()).performTouchInput {
            swipeDown()
        }
        // Wait for bars to show
        composeTestRule.waitUntil(5000) {
            composeTestRule.onAllNodesWithText("Carrinho: Test Cart 12").fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithText("Carrinho: Test Cart 12").assertIsDisplayed()

        // 5. Switch to 8 items
        cartState = CartState(
            cart = CartEntity(1, "Test Cart 8", System.currentTimeMillis(), 0, 8, 8.0, 80.0, ""),
            products = products8.reversed()
        )
        areBarsVisible = true 

        // 6. Verify bars visible
        composeTestRule.onNodeWithText("Carrinho: Test Cart 8").assertIsDisplayed()

        // 7. Scroll down
        composeTestRule.onNodeWithText("Product 8").performTouchInput {
            swipeUp()
        }

        // 8. With 8 items, bars should NOT hide. Give it a moment to NOT hide.
        composeTestRule.mainClock.advanceTimeBy(1000)
        composeTestRule.onNodeWithText("Carrinho: Test Cart 8").assertIsDisplayed()
    }
}
