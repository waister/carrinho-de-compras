package com.renobile.carrinho.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.renobile.carrinho.database.entities.PurchaseListEntity

@Dao
interface PurchaseListDao {
    @Query("SELECT * FROM purchase_lists ORDER BY dateOpen DESC")
    suspend fun getAll(): List<PurchaseListEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(purchaseList: PurchaseListEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(purchaseLists: List<PurchaseListEntity>)

    @Delete
    suspend fun delete(purchaseList: PurchaseListEntity)
}
