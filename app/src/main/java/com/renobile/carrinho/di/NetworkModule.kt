package com.renobile.carrinho.di

import com.renobile.carrinho.BuildConfig
import com.renobile.carrinho.network.ConfigApiService
import com.renobile.carrinho.network.NotificationApiService
import com.renobile.carrinho.util.API_ANDROID
import com.renobile.carrinho.util.API_DEBUG
import com.renobile.carrinho.util.API_IDENTIFIER
import com.renobile.carrinho.util.API_PLATFORM
import com.renobile.carrinho.util.API_V
import com.renobile.carrinho.util.API_VERSION
import com.renobile.carrinho.util.APP_HOST
import com.renobile.carrinho.util.PREF_DEVICE_ID
import com.renobile.carrinho.util.Prefs
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

val networkModule = module {
    single {
        val interceptor = HttpLoggingInterceptor()
        interceptor.level = HttpLoggingInterceptor.Level.BODY

        OkHttpClient.Builder()
            .addInterceptor(interceptor)
            .addInterceptor { chain ->
                val original = chain.request()
                val originalHttpUrl = original.url

                val url = originalHttpUrl.newBuilder()
                    .addQueryParameter(API_IDENTIFIER, Prefs.getValue(PREF_DEVICE_ID, ""))
                    .addQueryParameter(API_VERSION, BuildConfig.VERSION_CODE.toString())
                    .addQueryParameter(API_PLATFORM, API_ANDROID)
                    .addQueryParameter(API_DEBUG, if (BuildConfig.DEBUG) "1" else "0")
                    .addQueryParameter(API_V, "8")
                    .build()

                val requestBuilder = original.newBuilder().url(url)
                val request = requestBuilder.build()
                chain.proceed(request)
            }
            .build()
    }

    single {
        val baseUrl = "${APP_HOST}api/${BuildConfig.API_APP_NAME}/"
        Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(get())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    single { get<Retrofit>().create(ConfigApiService::class.java) }
    single { get<Retrofit>().create(NotificationApiService::class.java) }
}
