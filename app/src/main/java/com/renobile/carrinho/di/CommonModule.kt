package com.renobile.carrinho.di

import com.renobile.carrinho.util.Prefs
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import org.koin.dsl.module

val commonModule = module {
    single { Prefs }
    single<CoroutineDispatcher> { Dispatchers.IO }
}
