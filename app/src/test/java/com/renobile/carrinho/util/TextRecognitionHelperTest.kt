package com.renobile.carrinho.util

import org.junit.Assert.assertEquals
import org.junit.Test

class TextRecognitionHelperTest {

    @Test
    fun `given raw alexa shopping list screenshot text, when parsed, then returns clean shopping items`() {
        val rawAlexaText = """
            09:41
            100%
            Lista de compras
            Adicionar item
            Banana prata
            Leite integral 2 caixas
            Pão de forma
            Sabonete Dove
            Concluído
            Maçã fuji
        """.trimIndent()

        val items = TextRecognitionHelper.parseRawTextToShoppingItems(rawAlexaText)

        assertEquals(
            listOf(
                "Banana prata",
                "Leite integral 2 caixas",
                "Pão de forma",
                "Sabonete Dove",
            ),
            items,
        )
    }

    @Test
    fun `given lines with checkboxes and bullets, when parsed, then prefixes are stripped`() {
        val rawText = """
            [ ] Arroz 5kg
            [x] Feijão carioca
            • Azeite de oliva
            - Café moído
            * Detergente
            1. Papel toalha
            O Manteiga
        """.trimIndent()

        val items = TextRecognitionHelper.parseRawTextToShoppingItems(rawText)

        assertEquals(
            listOf(
                "Arroz 5kg",
                "Feijão carioca",
                "Azeite de oliva",
                "Café moído",
                "Detergente",
                "Papel toalha",
                "Manteiga",
            ),
            items,
        )
    }

    @Test
    fun `given empty or noise text, when parsed, then returns empty list`() {
        val rawText = """
            12:30
            5G
            98%
            Lista de compras
            Adicionar à lista
            Itens
            Alexa
            Amazon
        """.trimIndent()

        val items = TextRecognitionHelper.parseRawTextToShoppingItems(rawText)

        assertEquals(emptyList<String>(), items)
    }
}
