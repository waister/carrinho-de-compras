package com.renobile.carrinho.database.dao

import androidx.room.*
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
