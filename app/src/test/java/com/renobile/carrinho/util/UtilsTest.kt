package com.renobile.carrinho.util

import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Locale

class UtilsTest {

    @Test
    fun `getStringValid should return empty string for special null cases`() {
        assertEquals("", (null as String?).getStringValid())
        assertEquals("", "null".getStringValid())
        assertEquals("", "[null]".getStringValid())
        assertEquals("Valid", "Valid".getStringValid())
    }

    @Test
    fun `stringToInt should return only digits as int`() {
        assertEquals(123, "123".stringToInt())
        assertEquals(123, "abc123def".stringToInt())
        assertEquals(0, "abc".stringToInt())
        assertEquals(0, (null as String?).stringToInt())
    }

    @Test
    fun `Double formatPrice should format correctly`() {
        // Set locale to ensure consistent testing
        Locale.setDefault(Locale.forLanguageTag("pt-BR"))
        val price = 1234.56
        // Result depends on locale, in pt-BR it's "R$ 1.234,56" (or similar depending on Java version)
        // Since it's hard to match exact characters (like non-breaking space), we check if it contains the numbers
        val formatted = price.formatPrice()
        assert(formatted.contains("1.234,56"))
    }

    @Test
    fun `Double addPluralCharacter should return 's' for values other than singular range`() {
        assertEquals("", 1.0.addPluralCharacter())
        assertEquals("s", 2.0.addPluralCharacter())
        assertEquals("s", 0.0.addPluralCharacter())
        assertEquals("", 0.5.addPluralCharacter()) // isSingular returns true for 0.5
    }
    
    @Test
    fun `Int addPluralCharacter should return 's' for values other than 1`() {
        assertEquals("", 1.addPluralCharacter())
        assertEquals("s", 2.addPluralCharacter())
        assertEquals("s", 0.addPluralCharacter())
    }
}
