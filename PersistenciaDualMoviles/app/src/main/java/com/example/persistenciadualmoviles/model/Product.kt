package com.example.persistenciadualmoviles.model

data class Product(
    val id: Int = 0,
    val name: String,
    val description: String,
    val price: Double,
    val available: Boolean
)

enum class StorageMode {
    SQL,
    NOSQL
}