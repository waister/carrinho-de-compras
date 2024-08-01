package com.renobile.carrinho.domain

import io.realm.DynamicRealm
import io.realm.FieldAttribute
import io.realm.RealmMigration


class RealmMigration : RealmMigration {
    override fun migrate(realm: DynamicRealm, mOldVersion: Long, mNewVersion: Long) {
        var oldVersion = mOldVersion
        val schema = realm.schema

        println("Realm migration mOldVersion: $mOldVersion")
        println("Realm migration mNewVersion: $mNewVersion")

        if (oldVersion == 0L) {
            schema.get("Cart")
                ?.addField("keywords", String::class.java, FieldAttribute.REQUIRED)
            oldVersion++
        }

        if (oldVersion == 1L) {
            // Change "Cart.units" type from Int to Double
            schema.get("Cart")
                ?.addField("units_tmp", Double::class.java)
                ?.transform { obj ->
                    val units = obj.getInt("units")
                    obj.setDouble("units_tmp", units.toDouble())
                }
                ?.removeField("units")
                ?.renameField("units_tmp", "units")

            // Change "Product.quantity" type from Int to Double
            schema.get("Product")
                ?.addField("quantity_tmp", Double::class.java)
                ?.transform { obj ->
                    obj.setDouble("quantity_tmp", obj.getInt("quantity").toDouble())
                }
                ?.removeField("quantity")
                ?.renameField("quantity_tmp", "quantity")

            // Change "PurchaseList.units" type from Int to Double
            schema.get("PurchaseList")
                ?.addField("units_tmp", Double::class.java)
                ?.transform { obj ->
                    obj.setDouble("units_tmp", obj.getInt("units").toDouble())
                }
                ?.removeField("units")
                ?.renameField("units_tmp", "units")

            oldVersion++
        }

        println("Realm migration oldVersion: $oldVersion")
    }
}