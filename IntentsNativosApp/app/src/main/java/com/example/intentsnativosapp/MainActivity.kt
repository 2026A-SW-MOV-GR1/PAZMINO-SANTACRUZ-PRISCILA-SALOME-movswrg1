package com.example.intentsnativosapp

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {

    private val textoRecibido = mutableStateOf("Esperando datos externos...")
    private val imagenRecibida = mutableStateOf<Bitmap?>(null)
    private val estadoEntrada = mutableStateOf("Estado: Sin datos compartidos todavía")


    //Cuando la app se abre, revise si viene con datos compartidos
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        procesarIntentEntrante(intent)

        setContent {
            MaterialTheme {
                PantallaPrincipal(
                    textoRecibido = textoRecibido,
                    imagenRecibida = imagenRecibida,
                    estadoEntrada = estadoEntrada,
                    abrirMarcador = { numero ->
                        abrirDial(numero)
                    },
                    procesarFotoTomada = { bitmap ->
                        imagenRecibida.value = bitmap
                        textoRecibido.value = "Imagen tomada desde la cámara nativa."
                        estadoEntrada.value = "Estado: Foto capturada correctamente"
                    }
                )
            }
        }
    }

    //Cuando la app ya estaba abierta y recibe un nuevo texto o imagen compartida.
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        procesarIntentEntrante(intent)
    }

    //Para abrir el marcador del teléfono.
    private fun abrirDial(numero: String) {
        val uri = Uri.parse("tel:$numero")
        val intentDial = Intent(Intent.ACTION_DIAL, uri)
        startActivity(intentDial)
    }

    //Para revisar si llegó texto o imagen.
    private fun procesarIntentEntrante(intent: Intent?) {
        if (intent == null) return

        val accion = intent.action
        val tipo = intent.type

        if (accion == Intent.ACTION_SEND && tipo != null) {

            if (tipo == "text/plain") {
                val texto = intent.getStringExtra(Intent.EXTRA_TEXT)

                textoRecibido.value = texto ?: "No se recibió texto."
                imagenRecibida.value = null
                estadoEntrada.value = "Estado: Texto recibido desde otra app"
            }

            else if (tipo.startsWith("image/")) {
                val imagenUri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    intent.getParcelableExtra(Intent.EXTRA_STREAM, Uri::class.java)
                } else {
                    @Suppress("DEPRECATION")
                    intent.getParcelableExtra(Intent.EXTRA_STREAM)
                }

                if (imagenUri != null) {
                    val bitmap = convertirUriABitmap(imagenUri)

                    imagenRecibida.value = bitmap
                    textoRecibido.value = "Se recibió una imagen compartida."
                    estadoEntrada.value = "Estado: Imagen recibida desde otra app"
                } else {
                    textoRecibido.value = "No se pudo obtener la imagen."
                    imagenRecibida.value = null
                    estadoEntrada.value = "Estado: Error al recibir imagen"
                }
            }
        }
    }

    //Para convertir la imagen compartida en algo que se pueda mostrar en pantalla.
    private fun convertirUriABitmap(uri: Uri): Bitmap? {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val source = ImageDecoder.createSource(contentResolver, uri)
                ImageDecoder.decodeBitmap(source)
            } else {
                @Suppress("DEPRECATION")
                MediaStore.Images.Media.getBitmap(contentResolver, uri)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}

@Composable
fun PantallaPrincipal(
    textoRecibido: MutableState<String>,
    imagenRecibida: MutableState<Bitmap?>,
    estadoEntrada: MutableState<String>,
    abrirMarcador: (String) -> Unit,
    procesarFotoTomada: (Bitmap?) -> Unit
) {
    var numeroTelefono by remember { mutableStateOf("0987654321") }
    var fotoCamara by remember { mutableStateOf<Bitmap?>(null) }

    val launcherCamara = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        fotoCamara = bitmap
        procesarFotoTomada(bitmap)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        Text(
            text = "Taller Clase 06: Intents Nativos",
            style = MaterialTheme.typography.headlineSmall
        )

        Text(
            text = "Aplicación en Kotlin para enviar acciones al sistema Android y recibir datos compartidos desde otras apps.",
            style = MaterialTheme.typography.bodyMedium
        )

        ModuloIntentsSalientes(
            numeroTelefono = numeroTelefono,
            onNumeroChange = { numeroTelefono = it },
            abrirMarcador = {
                abrirMarcador(numeroTelefono)
            },
            fotoCamara = fotoCamara,
            tomarFoto = {
                launcherCamara.launch(null)
            }
        )

        ModuloIntentsEntrantes(
            estado = estadoEntrada.value,
            texto = textoRecibido.value,
            imagen = imagenRecibida.value
        )
    }
}

@Composable
fun ModuloIntentsSalientes(
    numeroTelefono: String,
    onNumeroChange: (String) -> Unit,
    abrirMarcador: () -> Unit,
    fotoCamara: Bitmap?,
    tomarFoto: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "MÓDULO: INTENTS SALIENTES",
                style = MaterialTheme.typography.titleLarge
            )

            Text(
                text = "Panel 1: Llamador Misterioso",
                style = MaterialTheme.typography.titleMedium
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = numeroTelefono,
                    onValueChange = onNumeroChange,
                    label = { Text("Teléfono") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.weight(1f)
                )

                Button(onClick = abrirMarcador) {
                    Text("INICIAR DIAL")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Panel 2: Foto Express",
                style = MaterialTheme.typography.titleMedium
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CajaImagen(
                    bitmap = fotoCamara,
                    textoVacio = "Miniatura"
                )

                Button(onClick = tomarFoto) {
                    Text("TOMAR FOTO")
                }
            }
        }
    }
}

@Composable
fun ModuloIntentsEntrantes(
    estado: String,
    texto: String,
    imagen: Bitmap?
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "MÓDULO: INTENTS ENTRANTES",
                style = MaterialTheme.typography.titleLarge
            )

            Text(
                text = estado,
                style = MaterialTheme.typography.bodyMedium
            )

            Text(
                text = "Texto recibido:",
                style = MaterialTheme.typography.titleMedium
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(8.dp)
                    )
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outline,
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(12.dp)
            ) {
                Text(text = texto)
            }

            Text(
                text = "Imagen recibida:",
                style = MaterialTheme.typography.titleMedium
            )

            CajaImagenGrande(
                bitmap = imagen,
                textoVacio = "Contenedor dinámico para imagen recibida"
            )
        }
    }
}

@Composable
fun CajaImagen(
    bitmap: Bitmap?,
    textoVacio: String
) {
    Box(
        modifier = Modifier
            .size(150.dp)
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(8.dp)
            )
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline,
                shape = RoundedCornerShape(8.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        if (bitmap != null) {
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = "Imagen tomada",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            Text(text = textoVacio)
        }
    }
}

@Composable
fun CajaImagenGrande(
    bitmap: Bitmap?,
    textoVacio: String
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(260.dp)
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(8.dp)
            )
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline,
                shape = RoundedCornerShape(8.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        if (bitmap != null) {
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = "Imagen recibida",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            Text(text = textoVacio)
        }
    }
}