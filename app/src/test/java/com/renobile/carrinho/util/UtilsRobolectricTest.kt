package com.renobile.carrinho.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.json.JSONObject
import java.util.Locale

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class UtilsRobolectricTest {

    @Test
    fun `getApiImage should build full url for uploads path`() {
        val result = "/uploads/products/img.png".getApiImage()
        assertEquals("https://maggapps.com/uploads/products/img.png", result)
    }

    @Test
    fun `isValidUrl should validate urls`() {
        assertTrue("https://maggapps.com".isValidUrl())
        assertFalse("not a url".isValidUrl())
        assertFalse((null as String?).isValidUrl())
    }

    @Test
    fun `getValidJSONObject should parse valid json and return null otherwise`() {
        val obj = """{"title":"Oferta","body":"Texto"}""".getValidJSONObject()
        assertEquals("Oferta", obj?.getStringVal("title"))
        assertEquals("Texto", obj?.getStringVal("body"))
        assertEquals(null, "invalid json".getValidJSONObject())
        assertEquals(null, (null as String?).getValidJSONObject())
    }

    @Test
    fun `getStringVal should return default when tag is missing`() {
        val obj = JSONObject("""{"title":"X"}""")
        assertEquals("", obj.getStringVal("missing"))
        assertEquals("fallback", obj.getStringVal("missing", "fallback"))
    }

    @Test
    fun `formatDatetime should parse api datetime or return empty`() {
        Locale.setDefault(Locale.forLanguageTag("pt-BR"))
        assert("2026-08-08 10:30:00".formatDatetime().isNotEmpty())
        assertEquals("", (null as String?).formatDatetime())
        assertEquals("", "not-a-date".formatDatetime())
    }

    @Test
    fun `fromHtml should convert html to spanned text`() {
        val spanned = "Texto <b>negrito</b>".fromHtml()
        assertTrue(spanned.toString().contains("Texto"))
        assertTrue(spanned.toString().contains("negrito"))
    }
}
