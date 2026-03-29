package com.renobile.carrinho.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.renobile.carrinho.database.dao.CartDao
import com.renobile.carrinho.database.dao.ProductDao
import com.renobile.carrinho.database.dao.PurchaseListDao
import com.renobile.carrinho.database.entities.CartEntity
import com.renobile.carrinho.database.entities.ProductEntity
import com.renobile.carrinho.database.entities.PurchaseListEntity

@Database(entities = [CartEntity::class, ProductEntity::class, PurchaseListEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun cartDao(): CartDao
    abstract fun productDao(): ProductDao
    abstract fun purchaseListDao(): PurchaseListDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "carrinho_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
