package com.renobile.carrinho.database

import android.content.Context
import android.util.Log
import com.renobile.carrinho.database.entities.CartEntity
import com.renobile.carrinho.database.entities.ProductEntity
import com.renobile.carrinho.database.entities.PurchaseListEntity
import com.renobile.carrinho.domain.Cart
import com.renobile.carrinho.domain.Product
import com.renobile.carrinho.domain.PurchaseList
import io.realm.Realm
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class RealmToRoomMigration(private val context: Context) {

    suspend fun migrate() = withContext(Dispatchers.IO) {
        val database = AppDatabase.getDatabase(context)
        val sharedPrefs = context.getSharedPreferences("migration_prefs", Context.MODE_PRIVATE)
        
        if (sharedPrefs.getBoolean("realm_to_room_migrated", false)) {
            return@withContext
        }

        try {
            val realm = Realm.getDefaultInstance()
            
            // Migrate Carts
            val realmCarts = realm.where(Cart::class.java).findAll()
            val cartEntities = realmCarts.map { cart ->
                CartEntity(
                    id = cart.id,
                    name = cart.name,
                    dateOpen = cart.dateOpen,
                    dateClose = cart.dateClose,
                    products = cart.products,
                    units = cart.units,
                    valueTotal = cart.valueTotal,
                    keywords = cart.keywords
                )
            }
            database.cartDao().insertAll(cartEntities)

            // Migrate Products
            val realmProducts = realm.where(Product::class.java).findAll()
            val productEntities = realmProducts.map { product ->
                ProductEntity(
                    id = product.id,
                    cartId = product.cartId,
                    listId = product.listId,
                    name = product.name,
                    quantity = product.quantity,
                    price = product.price
                )
            }
            database.productDao().insertAll(productEntities)

            // Migrate PurchaseLists
            val realmPurchaseLists = realm.where(PurchaseList::class.java).findAll()
            val purchaseListEntities = realmPurchaseLists.map { list ->
                PurchaseListEntity(
                    id = list.id,
                    name = list.name,
                    dateOpen = list.dateOpen,
                    dateClose = list.dateClose,
                    products = list.products,
                    units = list.units,
                    valueTotal = list.valueTotal
                )
            }
            database.purchaseListDao().insertAll(purchaseListEntities)

            realm.close()
            
            sharedPrefs.edit().putBoolean("realm_to_room_migrated", true).apply()
            Log.d("Migration", "Realm to Room migration completed successfully")
        } catch (e: Exception) {
            Log.e("Migration", "Error migrating from Realm to Room", e)
        }
    }
}
