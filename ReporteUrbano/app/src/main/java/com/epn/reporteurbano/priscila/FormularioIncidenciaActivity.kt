package com.epn.reporteurbano.priscila

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class FormularioIncidenciaActivity : AppCompatActivity() {

    // Resumen de las coordenadas recibidas desde el mapa.
    private lateinit var tvResumenCoordenadas: TextView

    // Campos del formulario.
    private lateinit var actvTipoIncidente: MaterialAutoCompleteTextView
    private lateinit var etDescripcion: TextInputEditText
    private lateinit var actvPrioridad: MaterialAutoCompleteTextView

    // Contenedores para mostrar errores de validación.
    private lateinit var tilTipoIncidente: TextInputLayout
    private lateinit var tilDescripcion: TextInputLayout
    private lateinit var tilPrioridad: TextInputLayout

    // Botones.
    private lateinit var btnVolverMapa: MaterialButton
    private lateinit var btnEnviarReporte: MaterialButton

    // Coordenadas recibidas desde MainActivity.
    private var latitud: Double = Double.NaN
    private var longitud: Double = Double.NaN

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_formulario_incidencia)

        inicializarComponentes()
        recibirCoordenadas()
        configurarMenusDesplegables()
        configurarBotones()
    }

    /**
     * Relaciona las variables de Kotlin con los componentes del XML.
     */
    private fun inicializarComponentes() {

        tvResumenCoordenadas =
            findViewById(R.id.tvResumenCoordenadas)

        actvTipoIncidente =
            findViewById(R.id.actvTipoIncidente)

        etDescripcion =
            findViewById(R.id.etDescripcion)

        actvPrioridad =
            findViewById(R.id.actvPrioridad)

        tilTipoIncidente =
            findViewById(R.id.tilTipoIncidente)

        tilDescripcion =
            findViewById(R.id.tilDescripcion)

        tilPrioridad =
            findViewById(R.id.tilPrioridad)

        btnVolverMapa =
            findViewById(R.id.btnVolverMapa)

        btnEnviarReporte =
            findViewById(R.id.btnEnviarReporte)
    }

    /**
     * Obtiene la latitud y longitud enviadas desde MainActivity.
     */
    private fun recibirCoordenadas() {

        latitud = intent.getDoubleExtra(
            ContratoIncidencia.EXTRA_LATITUD,
            Double.NaN
        )

        longitud = intent.getDoubleExtra(
            ContratoIncidencia.EXTRA_LONGITUD,
            Double.NaN
        )

        if (!latitud.isNaN() && !longitud.isNaN()) {

            tvResumenCoordenadas.text = String.format(
                Locale.US,
                "Latitud: %.6f\nLongitud: %.6f",
                latitud,
                longitud
            )

        } else {

            tvResumenCoordenadas.text =
                "No se recibieron las coordenadas"
        }
    }

    /**
     * Configura los menús desplegables de tipo y prioridad.
     */
    private fun configurarMenusDesplegables() {

        // Evita que aparezca el teclado en los desplegables.
        actvTipoIncidente.showSoftInputOnFocus = false
        actvPrioridad.showSoftInputOnFocus = false

        // Limita la altura de las listas desplegables.
        actvTipoIncidente.dropDownHeight = convertirDp(280)
        actvPrioridad.dropDownHeight = convertirDp(180)

        // Deja una pequeña separación entre el campo y la lista.
        actvTipoIncidente.dropDownVerticalOffset = convertirDp(6)
        actvPrioridad.dropDownVerticalOffset = convertirDp(6)

        val tiposIncidencia = arrayOf(
            "Bache",
            "Luminaria dañada",
            "Fuga de agua",
            "Basura acumulada",
            "Semáforo defectuoso",
            "Acera deteriorada",
            "Alcantarilla dañada",
            "Otro"
        )

        val prioridades = arrayOf(
            "Baja",
            "Media",
            "Alta"
        )

        val adaptadorTipos = ArrayAdapter(
            this,
            R.layout.item_dropdown,
            R.id.tvItemDropdown,
            tiposIncidencia
        )

        val adaptadorPrioridades = ArrayAdapter(
            this,
            R.layout.item_dropdown,
            R.id.tvItemDropdown,
            prioridades
        )

        actvTipoIncidente.setAdapter(adaptadorTipos)
        actvPrioridad.setAdapter(adaptadorPrioridades)

        // Permite mostrar todas las opciones desde el primer toque.
        actvTipoIncidente.threshold = 0
        actvPrioridad.threshold = 0

        actvTipoIncidente.setOnClickListener {
            mostrarMenuDesplegable(actvTipoIncidente)
        }

        actvPrioridad.setOnClickListener {
            mostrarMenuDesplegable(actvPrioridad)
        }

        // Permite abrir los menús desde la flecha.
        tilTipoIncidente.setEndIconOnClickListener {
            mostrarMenuDesplegable(actvTipoIncidente)
        }

        tilPrioridad.setEndIconOnClickListener {
            mostrarMenuDesplegable(actvPrioridad)
        }

        // Elimina el error cuando se selecciona una opción.
        actvTipoIncidente.setOnItemClickListener { _, _, _, _ ->

            tilTipoIncidente.error = null
            actvTipoIncidente.clearFocus()
        }

        actvPrioridad.setOnItemClickListener { _, _, _, _ ->

            tilPrioridad.error = null
            actvPrioridad.clearFocus()
        }

        // Elimina el error de descripción al volver a escribir.
        etDescripcion.setOnFocusChangeListener { _, tieneFoco ->

            if (tieneFoco) {
                tilDescripcion.error = null
            }
        }
    }

    /**
     * Oculta el teclado y abre el menú desplegable.
     */
    private fun mostrarMenuDesplegable(
        menu: MaterialAutoCompleteTextView
    ) {

        ocultarTeclado()
        etDescripcion.clearFocus()
        menu.requestFocus()

        menu.postDelayed(
            {
                if (!isFinishing && !isDestroyed) {
                    menu.showDropDown()
                }
            },
            180
        )
    }

    /**
     * Configura las acciones de los botones.
     */
    private fun configurarBotones() {

        btnVolverMapa.setOnClickListener {

            ocultarTeclado()
            finish()
        }

        btnEnviarReporte.setOnClickListener {

            ocultarTeclado()

            if (validarFormulario()) {
                mostrarConfirmacionReporte()
            }
        }
    }

    /**
     * Comprueba que todos los campos obligatorios estén completos.
     */
    private fun validarFormulario(): Boolean {

        val tipoIncidente =
            actvTipoIncidente.text.toString().trim()

        val descripcion =
            etDescripcion.text?.toString()?.trim().orEmpty()

        val prioridad =
            actvPrioridad.text.toString().trim()

        var formularioValido = true

        // Limpia los errores anteriores.
        tilTipoIncidente.error = null
        tilDescripcion.error = null
        tilPrioridad.error = null

        if (tipoIncidente.isEmpty()) {

            tilTipoIncidente.error =
                "Selecciona un tipo de incidencia"

            formularioValido = false
        }

        if (descripcion.isEmpty()) {

            tilDescripcion.error =
                "Ingresa una descripción"

            formularioValido = false

        } else if (descripcion.length < 10) {

            tilDescripcion.error =
                "La descripción debe tener al menos 10 caracteres"

            formularioValido = false
        }

        if (prioridad.isEmpty()) {

            tilPrioridad.error =
                "Selecciona una prioridad"

            formularioValido = false
        }

        if (latitud.isNaN() || longitud.isNaN()) {

            Toast.makeText(
                this,
                "No existen coordenadas válidas",
                Toast.LENGTH_LONG
            ).show()

            formularioValido = false
        }

        return formularioValido
    }

    /**
     * Presenta un resumen para confirmar el reporte.
     */
    private fun mostrarConfirmacionReporte() {

        val tipoIncidente =
            actvTipoIncidente.text.toString().trim()

        val descripcion =
            etDescripcion.text?.toString()?.trim().orEmpty()

        val prioridad =
            actvPrioridad.text.toString().trim()

        val resumen = String.format(
            Locale.US,
            "Tipo de incidencia:\n%s\n\n" +
                    "Descripción:\n%s\n\n" +
                    "Prioridad:\n%s\n\n" +
                    "Ubicación:\n" +
                    "Latitud: %.6f\n" +
                    "Longitud: %.6f\n\n" +
                    "Estado:\n%s",
            tipoIncidente,
            descripcion,
            prioridad,
            latitud,
            longitud,
            ContratoIncidencia.ESTADO_REPORTADO
        )

        MaterialAlertDialogBuilder(this)
            .setTitle("Confirmar reporte")
            .setMessage(resumen)
            .setNegativeButton("Editar") { dialogo, _ ->
                dialogo.dismiss()
            }
            .setPositiveButton("Enviar") { _, _ ->
                enviarReporteASaul()
            }
            .show()
    }

    /**
     * Crea los datos del reporte y abre la aplicación de Saul.
     */
    private fun enviarReporteASaul() {

        val idIncidente =
            generarIdentificadorIncidente()

        val tipoIncidente =
            actvTipoIncidente.text.toString().trim()

        val descripcion =
            etDescripcion.text?.toString()?.trim().orEmpty()

        val prioridad =
            actvPrioridad.text.toString().trim()

        val fechaReporte =
            obtenerFechaActual()

        val intentSaul = Intent().apply {

            setClassName(
                ContratoIncidencia.PAQUETE_ATENCION,
                ContratoIncidencia.ACTIVIDAD_ATENCION
            )

            putExtra(
                ContratoIncidencia.EXTRA_ID_INCIDENTE,
                idIncidente
            )

            putExtra(
                ContratoIncidencia.EXTRA_TIPO_INCIDENTE,
                tipoIncidente
            )

            putExtra(
                ContratoIncidencia.EXTRA_DESCRIPCION,
                descripcion
            )

            putExtra(
                ContratoIncidencia.EXTRA_LATITUD,
                latitud
            )

            putExtra(
                ContratoIncidencia.EXTRA_LONGITUD,
                longitud
            )

            putExtra(
                ContratoIncidencia.EXTRA_PRIORIDAD,
                prioridad
            )

            putExtra(
                ContratoIncidencia.EXTRA_FECHA_REPORTE,
                fechaReporte
            )

            putExtra(
                ContratoIncidencia.EXTRA_ESTADO,
                ContratoIncidencia.ESTADO_REPORTADO
            )
        }

        try {

            startActivity(intentSaul)

        } catch (exception: ActivityNotFoundException) {

            Toast.makeText(
                this,
                "El reporte fue generado, pero la aplicación " +
                        "Atención Urbana todavía no está instalada",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    /**
     * Genera un identificador único usando la fecha y hora.
     */
    private fun generarIdentificadorIncidente(): String {

        val formatoId = SimpleDateFormat(
            "yyyyMMddHHmmss",
            Locale.US
        )

        return "INC-${formatoId.format(Date())}"
    }

    /**
     * Obtiene la fecha y hora actuales.
     */
    private fun obtenerFechaActual(): String {

        val formatoFecha = SimpleDateFormat(
            "yyyy-MM-dd HH:mm:ss",
            Locale.getDefault()
        )

        return formatoFecha.format(Date())
    }

    /**
     * Oculta el teclado virtual.
     */
    private fun ocultarTeclado() {

        val vistaActual = currentFocus

        if (vistaActual != null) {

            val administradorTeclado =
                getSystemService(
                    Context.INPUT_METHOD_SERVICE
                ) as InputMethodManager

            administradorTeclado.hideSoftInputFromWindow(
                vistaActual.windowToken,
                0
            )

            vistaActual.clearFocus()
        }
    }

    /**
     * Convierte una medida dp a píxeles.
     */
    private fun convertirDp(valorDp: Int): Int {

        return (
                valorDp *
                        resources.displayMetrics.density
                ).toInt()
    }
}