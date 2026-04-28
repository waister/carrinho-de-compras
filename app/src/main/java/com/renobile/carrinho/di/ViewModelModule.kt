package com.renobile.carrinho.di

import com.renobile.carrinho.MainViewModel
import com.renobile.carrinho.features.cart.CartViewModel
import com.renobile.carrinho.features.cart.detail.CartDetailsViewModel
import com.renobile.carrinho.features.cart.history.CartsHistoryViewModel
import com.renobile.carrinho.features.comparator.ComparatorViewModel
import com.renobile.carrinho.features.list.ListViewModel
import com.renobile.carrinho.features.list.detail.ListDetailsViewModel
import com.renobile.carrinho.features.list.history.ListsHistoryViewModel
import com.renobile.carrinho.features.notification.NotificationsViewModel
import com.renobile.carrinho.features.notification.detail.NotificationDetailsViewModel
import com.renobile.carrinho.features.removeads.RemoveAdsViewModel
import com.renobile.carrinho.features.start.StartViewModel
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.dsl.module

val viewModelModule = module {
    viewModelOf(::MainViewModel)
    viewModelOf(::CartViewModel)
    viewModelOf(::CartDetailsViewModel)
    viewModelOf(::CartsHistoryViewModel)
    viewModelOf(::ComparatorViewModel)
    viewModelOf(::ListViewModel)
    viewModelOf(::ListDetailsViewModel)
    viewModelOf(::ListsHistoryViewModel)
    viewModelOf(::NotificationsViewModel)
    viewModelOf(::NotificationDetailsViewModel)
    viewModelOf(::StartViewModel)
    viewModelOf(::RemoveAdsViewModel)
}
