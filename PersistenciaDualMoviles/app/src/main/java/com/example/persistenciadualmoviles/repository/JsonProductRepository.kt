package com.example.persistenciadualmoviles.repository

import android.content.Context
import android.util.Log
import com.example.persistenciadualmoviles.model.Product
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

class JsonProductRepository(context: Context) : ProductRepository {

    private val jsonFile: File = File(
        context.getExternalFilesDir(null),
        "products_nosql.json"
    )

    companion object {
        private const val TAG = "JsonRepository"
    }

    override fun getAllProducts(): List<Product> {
        val jsonString = readJsonFile()
        val jsonArray = JSONArray(jsonString)
        val products = mutableListOf<Product>()

        for (i in 0 until jsonArray.length()) {
            val item = jsonArray.getJSONObject(i)

            val product = Product(
                id = item.getInt("id"),
                name = item.getString("name"),
                description = item.getString("description"),
                price = item.getDouble("price"),
                available = item.getBoolean("available")
            )

            products.add(product)
        }

        Log.d(TAG, "Productos consultados desde archivo NoSQL JSON: ${products.size}")
        Log.d(TAG, "Ruta del archivo JSON: ${jsonFile.absolutePath}")

        return products.sortedByDescending { it.id }
    }

    override fun insertProduct(product: Product): Boolean {
        val products = getAllProducts().toMutableList()

        val newProduct = product.copy(
            id = getNextId(products)
        )

        products.add(newProduct)
        saveAllProducts(products)

        Log.d(TAG, "Producto insertado en archivo NoSQL JSON: $newProduct")
        return true
    }

    override fun updateProduct(product: Product): Boolean {
        val products = getAllProducts().toMutableList()
        val index = products.indexOfFirst { it.id == product.id }

        if (index == -1) {
            Log.d(TAG, "Producto no encontrado para actualizar en JSON")
            return false
        }

        products[index] = product
        saveAllProducts(products)

        Log.d(TAG, "Producto actualizado en archivo NoSQL JSON: $product")
        return true
    }

    override fun deleteProduct(id: Int): Boolean {
        val products = getAllProducts().toMutableList()
        val removed = products.removeIf { it.id == id }

        if (removed) {
            saveAllProducts(products)
            Log.d(TAG, "Producto eliminado del archivo NoSQL JSON con id: $id")
        } else {
            Log.d(TAG, "Producto no encontrado para eliminar en JSON")
        }

        return removed
    }

    private fun readJsonFile(): String {
        if (!jsonFile.exists()) {
            jsonFile.writeText("[]")
            Log.d(TAG, "Archivo JSON creado en: ${jsonFile.absolutePath}")
        }

        return jsonFile.readText()
    }

    private fun saveAllProducts(products: List<Product>) {
        val jsonArray = JSONArray()

        products.forEach { product ->
            val item = JSONObject().apply {
                put("id", product.id)
                put("name", product.name)
                put("description", product.description)
                put("price", product.price)
                put("available", product.available)
            }

            jsonArray.put(item)
        }

        jsonFile.writeText(jsonArray.toString(4))

        Log.d(TAG, "Archivo JSON actualizado en: ${jsonFile.absolutePath}")
    }

    private fun getNextId(products: List<Product>): Int {
        return if (products.isEmpty()) {
            1
        } else {
            products.maxOf { it.id } + 1
        }
    }
}