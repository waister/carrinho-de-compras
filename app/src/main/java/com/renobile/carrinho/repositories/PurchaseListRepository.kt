package com.renobile.carrinho.repositories

import com.renobile.carrinho.database.dao.PurchaseListDao
import com.renobile.carrinho.database.entities.PurchaseListEntity

interface PurchaseListRepository {
    suspend fun getAllLists(): List<PurchaseListEntity>
    suspend fun insertList(list: PurchaseListEntity)
    suspend fun deleteList(list: PurchaseListEntity)
}

class PurchaseListRepositoryImpl(
    private val purchaseListDao: PurchaseListDao
) : PurchaseListRepository {
    override suspend fun getAllLists(): List<PurchaseListEntity> {
        return purchaseListDao.getAll()
    }

    override suspend fun insertList(list: PurchaseListEntity) {
        purchaseListDao.insert(list)
    }

    override suspend fun deleteList(list: PurchaseListEntity) {
        purchaseListDao.delete(list)
    }
}
