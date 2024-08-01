package com.renobile.carrinho.domain

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class Product : RealmObject() {

    @PrimaryKey
    var id: Long = 0
    var cartId: Long = 0
    var listId: Long = 0
    var name: String = ""
    var quantity: Double = 0.0
    var price: Double = 0.0

}
