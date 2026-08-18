package com.renobile.carrinho.network

import kotlinx.coroutines.test.runTest
import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import okio.Buffer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class VersionApiServiceTest {

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
    fun `given valid response, when checkVersion, then parses config response`() = runTest {
        val json = """
            {
              "success": true,
              "version_last": 7,
              "version_min": 4,
              "configs": {
                "store_link": "https://store.example.com",
                "app_name": "MeuApp",
                "admob_id": "ca-app-pub-123",
                "admob_ad_main_id": "admain",
                "admob_interstitial_id": "interstitial",
                "admob_remove_ads_id": "removeads",
                "admob_open_app_id": "openapp",
                "plan_video_duration": 2000
              }
            }
        """.trimIndent()
        server.enqueue(MockResponse.Builder().code(200).body(Buffer().writeUtf8(json)).build())

        val response = createService().checkVersion("token")

        assertTrue(response.success)
        assertEquals(7, response.versionLast)
        assertEquals(4, response.versionMin)
        assertEquals("https://store.example.com", response.configs?.storeLink)
        assertEquals("MeuApp", response.configs?.appName)
        assertEquals("ca-app-pub-123", response.configs?.admobId)
        assertEquals(2000L, response.configs?.planVideoDuration)
    }

    @Test
    fun `given response without configs, when checkVersion, then configs is null`() = runTest {
        server.enqueue(
            MockResponse.Builder().code(200).body(Buffer().writeUtf8("""{"success":false,"version_last":1,"version_min":1,"configs":null}""")).build(),
        )

        val response = createService().checkVersion("token")

        assertTrue(!response.success)
        assertNull(response.configs)
    }

    @Test
    fun `given request, when checkVersion, then sends token query parameter`() = runTest {
        server.enqueue(
            MockResponse.Builder().code(200).body(Buffer().writeUtf8("""{"success":true,"version_last":1,"version_min":1,"configs":null}""")).build(),
        )

        createService().checkVersion("myToken")

        val request = server.takeRequest()
        assertEquals("/identify", request.url.encodedPath)
        assertEquals("token=myToken", request.url.query)
    }

    private fun createService(): VersionApiService =
        Retrofit.Builder()
            .baseUrl(server.url("/"))
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(VersionApiService::class.java)
}
