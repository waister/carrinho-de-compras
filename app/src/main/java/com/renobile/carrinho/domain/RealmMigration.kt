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

//        if (oldVersion == 1) {
//            schema.get("Person")
//                    .addField("id", long.class, FieldAttribute.PRIMARY_KEY)
//                    .addRealmObjectField("favoriteDog", schema.get("Dog"))
//                    .addRealmListField("dogs", schema.get("Dog"));
//            oldVersion++;
//        }

        println("Realm migration oldVersion: $oldVersion")
    }
}