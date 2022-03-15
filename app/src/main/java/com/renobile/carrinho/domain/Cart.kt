package com.renobile.carrinho.domain

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class Cart : RealmObject() {

    @PrimaryKey
    var id: Long = 0
    var name: String = ""
    var dateOpen: Long = 0
    var dateClose: Long = 0
    var products: Int = 0
    var units: Int = 0
    var valueTotal: Double = 0.0
    var keywords: String = ""

}