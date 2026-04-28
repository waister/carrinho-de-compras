package com.renobile.carrinho.di

import com.renobile.carrinho.util.Prefs
import org.koin.dsl.module

val commonModule = module {
    single { Prefs }
}
