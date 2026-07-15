package com.example.lifecyclecounterapp

// Importa Bundle, que se usa para guardar y recuperar datos temporales
// cuando la Activity se destruye y se vuelve a crear, por ejemplo al rotar.
import android.os.Bundle

// Importa Log para imprimir mensajes en Logcat y observar el ciclo de vida.
import android.util.Log

// ComponentActivity es la clase base de la pantalla principal en Android.
import androidx.activity.ComponentActivity

// setContent permite cargar la interfaz hecha con Jetpack Compose.
import androidx.activity.compose.setContent

// Permite que la app use mejor el espacio de la pantalla, incluyendo bordes.
import androidx.activity.enableEdgeToEdge

// Imports necesarios para organizar visualmente los elementos en la pantalla.
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding

// Imports de componentes visuales de Material Design.
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text

// Imports para crear funciones Composable y manejar estado mutable del contador.
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableIntState
import androidx.compose.runtime.mutableIntStateOf

// Imports para alineación, modificadores, preview y tamaños.
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Importa el tema visual creado por el proyecto.
import com.example.lifecyclecounterapp.ui.theme.LifecycleCounterAppTheme

// MainActivity representa la pantalla principal de la aplicación.
// Aquí se controlan los eventos del ciclo de vida y la persistencia del contador.
class MainActivity : ComponentActivity() {

    // Tag usado para filtrar los mensajes en Logcat.
    // En Logcat se puede buscar con: tag:CICLO_VIDA
    private val tag = "CICLO_VIDA"

    // Clave con la que se guarda y recupera el valor del contador dentro del Bundle.
    private val keyCount = "contador"

    // Estado mutable del contador.
    // Se usa MutableIntState para que Compose actualice la interfaz cuando cambia el valor.
    private var countState: MutableIntState = mutableIntStateOf(0)

    // onCreate se ejecuta cuando la Activity se crea por primera vez
    // o cuando se recrea después de una rotación.
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Imprime en consola que se ejecutó onCreate.
        Log.d(tag, "onCreate")

        // Si savedInstanceState no es null, significa que Android guardó datos antes.
        // Esto ocurre, por ejemplo, cuando la pantalla rota y la Activity se recrea.
        if (savedInstanceState != null) {

            // Recupera el valor del contador guardado en el Bundle.
            // Si no existe el dato, usa 0 como valor por defecto.
            countState.intValue = savedInstanceState.getInt(keyCount, 0)

            // Imprime el valor recuperado del contador en Logcat.
            Log.d(tag, "Contador recuperado en onCreate: ${countState.intValue}")
        }

        // Activa el modo edge-to-edge para aprovechar mejor la pantalla.
        enableEdgeToEdge()

        // Carga la interfaz de usuario usando Jetpack Compose.
        setContent {

            // Aplica el tema visual de la app.
            LifecycleCounterAppTheme {

                // Scaffold sirve como estructura principal de la pantalla.
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->

                    // Llama a la pantalla del contador y le pasa:
                    // el valor actual del contador,
                    // la acción para incrementar,
                    // y el padding interno del Scaffold.
                    CounterScreen(
                        count = countState.intValue,
                        onIncrement = {
                            // Cada vez que se presiona el botón, aumenta el contador en 1.
                            countState.intValue++
                        },
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }

    // onStart se ejecuta cuando la Activity empieza a ser visible para el usuario.
    override fun onStart() {
        super.onStart()

        // Imprime en Logcat que la Activity pasó por onStart.
        Log.d(tag, "onStart")
    }

    // onResume se ejecuta cuando la Activity ya está activa y lista para interactuar.
    override fun onResume() {
        super.onResume()

        // Imprime en Logcat que la Activity pasó por onResume.
        Log.d(tag, "onResume")
    }

    // onPause se ejecuta cuando la Activity pierde el foco,
    // por ejemplo al salir al Home o antes de una rotación.
    override fun onPause() {
        super.onPause()

        // Imprime en Logcat que la Activity pasó por onPause.
        Log.d(tag, "onPause")
    }

    // onStop se ejecuta cuando la Activity deja de ser visible.
    // Puede ocurrir al ir al Home o durante una rotación.
    override fun onStop() {
        super.onStop()

        // Imprime en Logcat que la Activity pasó por onStop.
        Log.d(tag, "onStop")
    }

    // onRestart se ejecuta cuando la Activity estaba detenida
    // y vuelve a mostrarse, por ejemplo al regresar desde apps recientes.
    override fun onRestart() {
        super.onRestart()

        // Imprime en Logcat que la Activity pasó por onRestart.
        Log.d(tag, "onRestart")
    }

    // onDestroy se ejecuta cuando la Activity se destruye.
    // En la rotación normalmente se ejecuta porque Android destruye
    // la Activity anterior y crea una nueva.
    override fun onDestroy() {
        super.onDestroy()

        // Imprime en Logcat que la Activity pasó por onDestroy.
        Log.d(tag, "onDestroy")
    }

    // onSaveInstanceState se ejecuta antes de que Android destruya la Activity
    // por un cambio de configuración, como la rotación de pantalla.
    // Aquí se guarda el estado temporal que no queremos perder.
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        // Guarda el valor actual del contador dentro del Bundle.
        outState.putInt(keyCount, countState.intValue)

        // Imprime en Logcat el valor que se guardó.
        Log.d(tag, "onSaveInstanceState: contador guardado = ${countState.intValue}")
    }

    // onRestoreInstanceState se ejecuta después de recrear la Activity
    // cuando existe información guardada en el Bundle.
    // Aquí se restaura el valor que se guardó antes de la rotación.
    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)

        // Recupera el contador guardado previamente.
        countState.intValue = savedInstanceState.getInt(keyCount, 0)

        // Imprime en Logcat el valor restaurado.
        Log.d(tag, "onRestoreInstanceState: contador restaurado = ${countState.intValue}")
    }
}

// CounterScreen es la pantalla visual del contador.
// Recibe el valor del contador y una función para incrementarlo.
@Composable
fun CounterScreen(
    count: Int,
    onIncrement: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Column organiza los elementos verticalmente en la pantalla.
    Column(
        modifier = modifier
            // Hace que la columna ocupe toda la pantalla.
            .fillMaxSize()
            // Agrega espacio interno alrededor del contenido.
            .padding(24.dp),

        // Centra los elementos verticalmente.
        verticalArrangement = Arrangement.Center,

        // Centra los elementos horizontalmente.
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Texto que muestra el valor actual del contador.
        Text(
            text = "Contador: $count",
            fontSize = 32.sp,
            color = MaterialTheme.colorScheme.primary
        )

        // Botón que incrementa el contador cuando el usuario lo presiona.
        Button(
            onClick = onIncrement,
            modifier = Modifier.padding(top = 24.dp)
        ) {
            // Texto visible dentro del botón.
            Text(text = "Sumar +1")
        }
    }
}

// Preview permite ver una vista previa de la pantalla en Android Studio
// sin necesidad de ejecutar la app en el emulador.
@Preview(showBackground = true)
@Composable
fun CounterScreenPreview() {
    // Aplica el tema de la app a la vista previa.
    LifecycleCounterAppTheme {

        // Muestra la pantalla del contador con un valor inicial de prueba.
        CounterScreen(
            count = 0,
            onIncrement = {}
        )
    }
}