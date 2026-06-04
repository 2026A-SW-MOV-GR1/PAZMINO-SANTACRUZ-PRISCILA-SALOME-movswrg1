package com.example.persistenciadualmoviles.repository

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import com.example.persistenciadualmoviles.model.Product

class SQLiteProductRepository(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION),
    ProductRepository {

    companion object {
        private const val DATABASE_NAME = "products_sql.db"
        private const val DATABASE_VERSION = 1

        private const val TABLE_PRODUCTS = "products"
        private const val COLUMN_ID = "id"
        private const val COLUMN_NAME = "name"
        private const val COLUMN_DESCRIPTION = "description"
        private const val COLUMN_PRICE = "price"
        private const val COLUMN_AVAILABLE = "available"

        private const val TAG = "SQLiteRepository"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTable = """
            CREATE TABLE $TABLE_PRODUCTS (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_NAME TEXT NOT NULL,
                $COLUMN_DESCRIPTION TEXT NOT NULL,
                $COLUMN_PRICE REAL NOT NULL,
                $COLUMN_AVAILABLE INTEGER NOT NULL
            )
        """.trimIndent()

        db.execSQL(createTable)
        Log.d(TAG, "Tabla SQL creada correctamente")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_PRODUCTS")
        onCreate(db)
    }

    override fun getAllProducts(): List<Product> {
        val products = mutableListOf<Product>()
        val db = readableDatabase

        val cursor = db.rawQuery(
            "SELECT * FROM $TABLE_PRODUCTS ORDER BY $COLUMN_ID DESC",
            null
        )

        cursor.use {
            while (it.moveToNext()) {
                val product = Product(
                    id = it.getInt(it.getColumnIndexOrThrow(COLUMN_ID)),
                    name = it.getString(it.getColumnIndexOrThrow(COLUMN_NAME)),
                    description = it.getString(it.getColumnIndexOrThrow(COLUMN_DESCRIPTION)),
                    price = it.getDouble(it.getColumnIndexOrThrow(COLUMN_PRICE)),
                    available = it.getInt(it.getColumnIndexOrThrow(COLUMN_AVAILABLE)) == 1
                )

                products.add(product)
            }
        }

        Log.d(TAG, "Productos consultados desde SQL: ${products.size}")
        return products
    }

    override fun insertProduct(product: Product): Boolean {
        val db = writableDatabase

        val values = ContentValues().apply {
            put(COLUMN_NAME, product.name)
            put(COLUMN_DESCRIPTION, product.description)
            put(COLUMN_PRICE, product.price)
            put(COLUMN_AVAILABLE, if (product.available) 1 else 0)
        }

        val result = db.insert(TABLE_PRODUCTS, null, values)

        Log.d(TAG, "Insertar producto SQL resultado: $result")
        return result != -1L
    }

    override fun updateProduct(product: Product): Boolean {
        val db = writableDatabase

        val values = ContentValues().apply {
            put(COLUMN_NAME, product.name)
            put(COLUMN_DESCRIPTION, product.description)
            put(COLUMN_PRICE, product.price)
            put(COLUMN_AVAILABLE, if (product.available) 1 else 0)
        }

        val result = db.update(
            TABLE_PRODUCTS,
            values,
            "$COLUMN_ID = ?",
            arrayOf(product.id.toString())
        )

        Log.d(TAG, "Actualizar producto SQL resultado: $result")
        return result > 0
    }

    override fun deleteProduct(id: Int): Boolean {
        val db = writableDatabase

        val result = db.delete(
            TABLE_PRODUCTS,
            "$COLUMN_ID = ?",
            arrayOf(id.toString())
        )

        Log.d(TAG, "Eliminar producto SQL resultado: $result")
        return result > 0
    }
}