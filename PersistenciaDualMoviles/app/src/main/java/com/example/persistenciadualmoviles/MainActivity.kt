package com.example.persistenciadualmoviles

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.persistenciadualmoviles.model.Product
import com.example.persistenciadualmoviles.model.StorageMode
import com.example.persistenciadualmoviles.repository.ProductStorageManager
import com.example.persistenciadualmoviles.ui.theme.PersistenciaDualMovilesTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            PersistenciaDualMovilesTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    ProductScreen(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun ProductScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val storageManager = remember { ProductStorageManager(context) }

    var isNoSql by remember { mutableStateOf(false) }
    var products by remember { mutableStateOf(storageManager.getAllProducts()) }

    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var priceText by remember { mutableStateOf("") }
    var available by remember { mutableStateOf(true) }
    var message by remember { mutableStateOf("") }

    val currentMode = if (isNoSql) StorageMode.NOSQL else StorageMode.SQL

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Persistencia Dual en Móviles",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = "Usar NoSQL JSON")

            Switch(
                checked = isNoSql,
                onCheckedChange = { checked ->
                    isNoSql = checked

                    val mode = if (checked) StorageMode.NOSQL else StorageMode.SQL
                    storageManager.setStorageMode(mode)

                    products = storageManager.getAllProducts()
                    message = ""
                }
            )
        }

        Text(
            text = "Origen actual de datos: $currentMode",
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = name,
            onValueChange = { name = it },
            label = { Text("Nombre del producto") }
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = description,
            onValueChange = { description = it },
            label = { Text("Descripción") }
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = priceText,
            onValueChange = { priceText = it },
            label = { Text("Precio") },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Decimal
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = available,
                onCheckedChange = { available = it }
            )

            Text(text = "Disponible")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                val price = priceText.toDoubleOrNull()

                if (name.isBlank() || description.isBlank() || price == null) {
                    message = "Completa bien el nombre, descripción y precio."
                } else {
                    val product = Product(
                        name = name,
                        description = description,
                        price = price,
                        available = available
                    )

                    val saved = storageManager.insertProduct(product)

                    if (saved) {
                        products = storageManager.getAllProducts()

                        name = ""
                        description = ""
                        priceText = ""
                        available = true

                        message = "Producto guardado en $currentMode"
                    } else {
                        message = "No se pudo guardar el producto."
                    }
                }
            }
        ) {
            Text(text = "Guardar producto")
        }

        if (message.isNotBlank()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = message)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Lista de productos",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (products.isEmpty()) {
            Text(text = "No hay productos en este almacenamiento.")
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(products) { product ->
                    ProductCard(
                        product = product,
                        onDelete = {
                            storageManager.deleteProduct(product.id)
                            products = storageManager.getAllProducts()
                            message = "Producto eliminado de $currentMode"
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun ProductCard(
    product: Product,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Text(
                text = product.name,
                fontWeight = FontWeight.Bold
            )

            Text(text = product.description)
            Text(text = "Precio: \$${product.price}")
            Text(text = "Disponible: ${if (product.available) "Sí" else "No"}")

            Spacer(modifier = Modifier.height(8.dp))

            Button(onClick = onDelete) {
                Text(text = "Eliminar")
            }
        }
    }
}