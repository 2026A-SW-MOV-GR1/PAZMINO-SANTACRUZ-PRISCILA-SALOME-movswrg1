package com.example.persistenciadualmoviles.repository

import android.content.Context
import android.util.Log
import com.example.persistenciadualmoviles.model.Product
import com.example.persistenciadualmoviles.model.StorageMode

class ProductStorageManager(context: Context) {

    private val sqlRepository = SQLiteProductRepository(context)
    private val jsonRepository = JsonProductRepository(context)

    private var currentMode: StorageMode = StorageMode.SQL

    companion object {
        private const val TAG = "ProductStorageManager"
    }

    fun setStorageMode(mode: StorageMode) {
        currentMode = mode
        Log.d(TAG, "Modo de almacenamiento cambiado a: $currentMode")
    }

    fun getStorageMode(): StorageMode {
        return currentMode
    }

    private fun getCurrentRepository(): ProductRepository {
        return when (currentMode) {
            StorageMode.SQL -> sqlRepository
            StorageMode.NOSQL -> jsonRepository
        }
    }

    fun getAllProducts(): List<Product> {
        return getCurrentRepository().getAllProducts()
    }

    fun insertProduct(product: Product): Boolean {
        return getCurrentRepository().insertProduct(product)
    }

    fun updateProduct(product: Product): Boolean {
        return getCurrentRepository().updateProduct(product)
    }

    fun deleteProduct(id: Int): Boolean {
        return getCurrentRepository().deleteProduct(id)
    }
}