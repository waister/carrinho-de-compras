package com.renobile.carrinho.di

import com.renobile.carrinho.network.ConfigApiService
import com.renobile.carrinho.network.NotificationApiService
import com.renobile.carrinho.network.VersionApiService
import com.renobile.carrinho.util.APP_HOST
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
            .build()
    }

    single {
        Retrofit.Builder()
            .baseUrl(APP_HOST)
            .client(get())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    single { get<Retrofit>().create(VersionApiService::class.java) }
    single { get<Retrofit>().create(ConfigApiService::class.java) }
    single { get<Retrofit>().create(NotificationApiService::class.java) }
}
