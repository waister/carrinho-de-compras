package com.renobile.carrinho.network

import kotlinx.coroutines.test.runTest
import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import okio.Buffer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class NotificationApiServiceTest {

    private lateinit var server: MockWebServer

    @Before
    fun setup() {
        server = MockWebServer()
        server.start()
    }

    @After
    fun teardown() {
        server.close()
    }

    @Test
    fun `given valid response, when getNotifications, then parses notifications`() = runTest {
        val json = """
            {
              "success": true,
              "message": null,
              "notifications": [
                { "id": "1", "title": "Oferta", "body": "Corra", "date": "2026-08-08", "image": "img1.png" },
                { "id": "2", "title": "Novidade", "body": "Chegou", "date": "2026-08-09", "image": null }
              ]
            }
        """.trimIndent()
        server.enqueue(MockResponse.Builder().code(200).body(Buffer().writeUtf8(json)).build())

        val response = createService().getNotifications()

        assertTrue(response.success)
        assertEquals(2, response.notifications?.size)
        assertEquals("Oferta", response.notifications?.first()?.title)
        assertEquals("Chegou", response.notifications?.get(1)?.body)
        assertEquals(null, response.notifications?.get(1)?.image)
    }

    @Test
    fun `given valid response, when getNotificationDetail, then parses notification`() = runTest {
        val json = """
            {
              "success": true,
              "message": null,
              "notifications": [
                { "id": "10", "title": "Detalhe", "body": "Conteudo", "date": "2026-08-08", "image": "img.png" }
              ]
            }
        """.trimIndent()
        server.enqueue(MockResponse.Builder().code(200).body(Buffer().writeUtf8(json)).build())

        val response = createService().getNotificationDetail("10")

        assertEquals("10", response.notifications?.first()?.id)
        assertEquals("Detalhe", response.notifications?.first()?.title)
    }

    @Test
    fun `given response without notifications, when getNotifications, then notifications is null`() = runTest {
        server.enqueue(
            MockResponse.Builder().code(200).body(Buffer().writeUtf8("""{"success":false,"message":"erro","notifications":null}""")).build()
        )

        val response = createService().getNotifications()

        assertEquals(null, response.notifications)
    }

    private fun createService(): NotificationApiService =
        Retrofit.Builder()
            .baseUrl(server.url("/"))
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(NotificationApiService::class.java)
}
