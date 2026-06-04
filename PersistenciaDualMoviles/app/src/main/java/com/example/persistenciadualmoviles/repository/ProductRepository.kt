package com.example.persistenciadualmoviles.repository

import com.example.persistenciadualmoviles.model.Product

interface ProductRepository {

    fun getAllProducts(): List<Product>

    fun insertProduct(product: Product): Boolean

    fun updateProduct(product: Product): Boolean

    fun deleteProduct(id: Int): Boolean
}