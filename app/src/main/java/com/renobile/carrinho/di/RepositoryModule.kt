package com.renobile.carrinho.di

import com.renobile.carrinho.repositories.CartRepository
import com.renobile.carrinho.repositories.CartRepositoryImpl
import com.renobile.carrinho.repositories.ConfigRepository
import com.renobile.carrinho.repositories.ConfigRepositoryImpl
import com.renobile.carrinho.repositories.ProductRepository
import com.renobile.carrinho.repositories.ProductRepositoryImpl
import com.renobile.carrinho.repositories.PurchaseListRepository
import com.renobile.carrinho.repositories.PurchaseListRepositoryImpl
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val repositoryModule = module {
    singleOf(::ConfigRepositoryImpl) bind ConfigRepository::class
    singleOf(::CartRepositoryImpl) bind CartRepository::class
    singleOf(::ProductRepositoryImpl) bind ProductRepository::class
    singleOf(::PurchaseListRepositoryImpl) bind PurchaseListRepository::class
}
