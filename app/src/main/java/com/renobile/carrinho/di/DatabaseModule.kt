package com.renobile.carrinho.di

import com.renobile.carrinho.database.AppDatabase
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val databaseModule = module {
    single { AppDatabase.getDatabase(androidContext()) }
    single { get<AppDatabase>().cartDao() }
    single { get<AppDatabase>().productDao() }
    single { get<AppDatabase>().purchaseListDao() }
}
