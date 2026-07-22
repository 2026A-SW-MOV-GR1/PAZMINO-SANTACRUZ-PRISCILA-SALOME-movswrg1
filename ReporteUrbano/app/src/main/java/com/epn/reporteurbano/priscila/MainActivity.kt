package com.epn.reporteurbano.priscila

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import com.google.android.material.button.MaterialButton
import org.maplibre.android.MapLibre
import org.maplibre.android.annotations.Icon
import org.maplibre.android.annotations.IconFactory
import org.maplibre.android.annotations.Marker
import org.maplibre.android.annotations.MarkerOptions
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView
import java.util.Locale

@Suppress("DEPRECATION")
class MainActivity : AppCompatActivity() {

    private lateinit var mapView: MapView
    private lateinit var mapa: MapLibreMap

    private lateinit var tvCoordenadas: TextView
    private lateinit var btnUsarUbicacion: MaterialButton
    private lateinit var btnConfirmarUbicacion: MaterialButton

    private lateinit var locationManager: LocationManager

    private var marcadorSeleccionado: Marker? = null

    private var latitudSeleccionada: Double? = null
    private var longitudSeleccionada: Double? = null

    /*
     * Se utiliza para detener la búsqueda de ubicación
     * cuando se cumplen los 15 segundos.
     */
    private val manejadorUbicacion =
        Handler(Looper.getMainLooper())

    private var listenerUbicacion: LocationListener? = null
    private var tareaTiempoEspera: Runnable? = null

    private val solicitudPermisosUbicacion =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permisos ->

            val permisoPreciso =
                permisos[Manifest.permission.ACCESS_FINE_LOCATION] == true

            val permisoAproximado =
                permisos[Manifest.permission.ACCESS_COARSE_LOCATION] == true

            if (permisoPreciso || permisoAproximado) {

                obtenerUbicacionActual()

            } else {

                Toast.makeText(
                    this,
                    "Debes permitir el acceso a la ubicación",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        MapLibre.getInstance(this)

        setContentView(R.layout.activity_main)

        mapView = findViewById(R.id.mapView)
        tvCoordenadas = findViewById(R.id.tvCoordenadas)
        btnUsarUbicacion = findViewById(R.id.btnUsarUbicacion)

        btnConfirmarUbicacion =
            findViewById(R.id.btnConfirmarUbicacion)

        locationManager =
            getSystemService(Context.LOCATION_SERVICE) as LocationManager

        mapView.onCreate(savedInstanceState)

        configurarMapa()
        configurarBotones()
    }

    /**
     * Carga el mapa y lo centra inicialmente en Quito.
     */
    private fun configurarMapa() {

        mapView.getMapAsync { mapaPreparado ->

            mapa = mapaPreparado

            mapa.setStyle(
                "https://tiles.openfreemap.org/styles/liberty"
            ) {

                val ubicacionQuito = LatLng(
                    -0.1807,
                    -78.4678
                )

                mapa.cameraPosition =
                    CameraPosition.Builder()
                        .target(ubicacionQuito)
                        .zoom(14.0)
                        .build()

                mapa.addOnMapClickListener { punto ->

                    seleccionarUbicacion(
                        punto = punto,
                        titulo = "Incidencia seleccionada"
                    )

                    true
                }
            }
        }
    }

    /**
     * Configura los botones de la pantalla principal.
     */
    private fun configurarBotones() {

        btnUsarUbicacion.setOnClickListener {

            verificarPermisosUbicacion()
        }

        btnConfirmarUbicacion.setOnClickListener {

            val latitud = latitudSeleccionada
            val longitud = longitudSeleccionada

            if (latitud != null && longitud != null) {

                abrirFormulario(
                    latitud = latitud,
                    longitud = longitud
                )

            } else {

                Toast.makeText(
                    this,
                    "Selecciona primero una ubicación",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    /**
     * Abre el formulario y envía las coordenadas seleccionadas.
     */
    private fun abrirFormulario(
        latitud: Double,
        longitud: Double
    ) {

        val intentFormulario = Intent(
            this,
            FormularioIncidenciaActivity::class.java
        ).apply {

            putExtra(
                ContratoIncidencia.EXTRA_LATITUD,
                latitud
            )

            putExtra(
                ContratoIncidencia.EXTRA_LONGITUD,
                longitud
            )

            putExtra(
                ContratoIncidencia.EXTRA_ESTADO,
                ContratoIncidencia.ESTADO_REPORTADO
            )
        }

        startActivity(intentFormulario)
    }

    /**
     * Comprueba si la aplicación tiene permisos de ubicación.
     */
    private fun verificarPermisosUbicacion() {

        val permisoPreciso =
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            )

        val permisoAproximado =
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )

        if (
            permisoPreciso == PackageManager.PERMISSION_GRANTED ||
            permisoAproximado == PackageManager.PERMISSION_GRANTED
        ) {

            obtenerUbicacionActual()

        } else {

            solicitudPermisosUbicacion.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    /**
     * Obtiene la ubicación usando GPS y red.
     * La búsqueda se cancela después de 15 segundos.
     */
    @SuppressLint("MissingPermission")
    private fun obtenerUbicacionActual() {

        if (!::mapa.isInitialized) {

            Toast.makeText(
                this,
                "El mapa todavía está cargando",
                Toast.LENGTH_SHORT
            ).show()

            return
        }

        val permisoPreciso =
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED

        val permisoAproximado =
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED

        if (!permisoPreciso && !permisoAproximado) {

            verificarPermisosUbicacion()
            return
        }

        cancelarSolicitudUbicacion()

        val redDisponible =
            locationManager.isProviderEnabled(
                LocationManager.NETWORK_PROVIDER
            )

        val gpsDisponible =
            locationManager.isProviderEnabled(
                LocationManager.GPS_PROVIDER
            )

        if (!redDisponible && !gpsDisponible) {

            Toast.makeText(
                this,
                "Activa la ubicación del celular",
                Toast.LENGTH_LONG
            ).show()

            return
        }

        btnUsarUbicacion.isEnabled = false
        btnUsarUbicacion.text = "Obteniendo ubicación..."

        /*
         * Busca primero una ubicación previamente guardada
         * por GPS o por la red.
         */
        val ultimaUbicacionDisponible =
            obtenerUltimaUbicacionDisponible()

        /*
         * Cuando la ubicación guardada tiene menos de cinco minutos,
         * se usa inmediatamente y no se espera una nueva lectura.
         */
        if (
            ultimaUbicacionDisponible != null &&
            ubicacionEsReciente(ultimaUbicacionDisponible)
        ) {

            mostrarUbicacionActual(
                ultimaUbicacionDisponible
            )

            restaurarBotonUbicacion()
            return
        }

        val nuevoListener = object : LocationListener {

            override fun onLocationChanged(
                ubicacion: Location
            ) {

                cancelarSolicitudUbicacion()

                mostrarUbicacionActual(
                    ubicacion
                )

                restaurarBotonUbicacion()
            }

            override fun onProviderDisabled(
                provider: String
            ) {

                /*
                 * No se cancela inmediatamente porque el otro
                 * proveedor todavía podría responder.
                 */
            }
        }

        listenerUbicacion = nuevoListener

        try {

            /*
             * La ubicación por red normalmente responde más rápido.
             */
            if (redDisponible) {

                locationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER,
                    0L,
                    0f,
                    nuevoListener,
                    Looper.getMainLooper()
                )
            }

            /*
             * El GPS también se consulta cuando existe permiso preciso.
             */
            if (gpsDisponible && permisoPreciso) {

                locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    0L,
                    0f,
                    nuevoListener,
                    Looper.getMainLooper()
                )
            }

        } catch (exception: SecurityException) {

            cancelarSolicitudUbicacion()
            restaurarBotonUbicacion()

            Toast.makeText(
                this,
                "No se pudo acceder a la ubicación",
                Toast.LENGTH_LONG
            ).show()

            return
        }

        /*
         * Esta tarea se ejecutará si en 15 segundos
         * ningún proveedor entrega una ubicación.
         */
        val nuevaTareaTiempoEspera = Runnable {

            cancelarSolicitudUbicacion()
            restaurarBotonUbicacion()

            if (ultimaUbicacionDisponible != null) {

                mostrarUbicacionActual(
                    ultimaUbicacionDisponible
                )

                Toast.makeText(
                    this,
                    "Se utilizó la última ubicación disponible",
                    Toast.LENGTH_LONG
                ).show()

            } else {

                Toast.makeText(
                    this,
                    "No se pudo obtener tu ubicación. " +
                            "Acércate a una ventana o selecciona el punto en el mapa.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

        tareaTiempoEspera = nuevaTareaTiempoEspera

        manejadorUbicacion.postDelayed(
            nuevaTareaTiempoEspera,
            15_000L
        )
    }

    /**
     * Busca la ubicación más reciente entre GPS y red.
     */
    @SuppressLint("MissingPermission")
    private fun obtenerUltimaUbicacionDisponible(): Location? {

        val ubicaciones = mutableListOf<Location>()

        try {

            if (
                locationManager.isProviderEnabled(
                    LocationManager.NETWORK_PROVIDER
                )
            ) {

                locationManager.getLastKnownLocation(
                    LocationManager.NETWORK_PROVIDER
                )?.let { ubicacion ->

                    ubicaciones.add(ubicacion)
                }
            }

            if (
                locationManager.isProviderEnabled(
                    LocationManager.GPS_PROVIDER
                )
            ) {

                locationManager.getLastKnownLocation(
                    LocationManager.GPS_PROVIDER
                )?.let { ubicacion ->

                    ubicaciones.add(ubicacion)
                }
            }

        } catch (exception: SecurityException) {

            return null
        }

        return ubicaciones.maxByOrNull { ubicacion ->
            ubicacion.time
        }
    }

    /**
     * Comprueba si la ubicación tiene menos de cinco minutos.
     */
    private fun ubicacionEsReciente(
        ubicacion: Location
    ): Boolean {

        val antiguedad =
            System.currentTimeMillis() - ubicacion.time

        return antiguedad in 0..300_000L
    }

    /**
     * Cancela las solicitudes y el tiempo máximo de espera.
     */
    private fun cancelarSolicitudUbicacion() {

        tareaTiempoEspera?.let { tarea ->

            manejadorUbicacion.removeCallbacks(tarea)
        }

        tareaTiempoEspera = null

        listenerUbicacion?.let { listener ->

            try {

                locationManager.removeUpdates(listener)

            } catch (exception: SecurityException) {

                // No se necesita realizar otra acción.
            }
        }

        listenerUbicacion = null
    }

    /**
     * Vuelve a activar el botón de ubicación.
     */
    private fun restaurarBotonUbicacion() {

        btnUsarUbicacion.isEnabled = true
        btnUsarUbicacion.text =
            "Usar mi ubicación actual"
    }

    /**
     * Muestra la ubicación obtenida en el mapa.
     */
    private fun mostrarUbicacionActual(
        ubicacion: Location
    ) {

        val puntoActual = LatLng(
            ubicacion.latitude,
            ubicacion.longitude
        )

        seleccionarUbicacion(
            punto = puntoActual,
            titulo = "Mi ubicación actual"
        )
    }

    /**
     * Guarda las coordenadas y coloca el marcador personalizado.
     */
    private fun seleccionarUbicacion(
        punto: LatLng,
        titulo: String
    ) {

        if (!::mapa.isInitialized) {
            return
        }

        latitudSeleccionada = punto.latitude
        longitudSeleccionada = punto.longitude

        marcadorSeleccionado?.let { marcadorAnterior ->

            mapa.removeMarker(marcadorAnterior)
        }

        val opcionesMarcador =
            MarkerOptions()
                .position(punto)
                .title(titulo)
                .snippet(
                    String.format(
                        Locale.US,
                        "Latitud: %.6f | Longitud: %.6f",
                        punto.latitude,
                        punto.longitude
                    )
                )

        crearIconoMarcador()?.let { icono ->

            opcionesMarcador.icon(icono)
        }

        marcadorSeleccionado =
            mapa.addMarker(opcionesMarcador)

        mapa.animateCamera(
            CameraUpdateFactory.newLatLngZoom(
                punto,
                16.5
            )
        )

        tvCoordenadas.text = String.format(
            Locale.US,
            "Latitud: %.6f\nLongitud: %.6f",
            punto.latitude,
            punto.longitude
        )

        btnConfirmarUbicacion.isEnabled = true
        btnConfirmarUbicacion.text =
            "Confirmar ubicación"
    }

    /**
     * Convierte el marcador vectorial en un icono de MapLibre.
     */
    private fun crearIconoMarcador(): Icon? {

        val drawable =
            AppCompatResources.getDrawable(
                this,
                R.drawable.ic_marcador_incidente
            ) ?: return null

        val tamanoPixeles =
            (
                    52 *
                            resources.displayMetrics.density
                    ).toInt()

        val bitmap = Bitmap.createBitmap(
            tamanoPixeles,
            tamanoPixeles,
            Bitmap.Config.ARGB_8888
        )

        val canvas = Canvas(bitmap)

        drawable.setBounds(
            0,
            0,
            canvas.width,
            canvas.height
        )

        drawable.draw(canvas)

        return IconFactory
            .getInstance(this)
            .fromBitmap(bitmap)
    }

    override fun onStart() {
        super.onStart()
        mapView.onStart()
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        mapView.onPause()
        super.onPause()
    }

    override fun onStop() {

        cancelarSolicitudUbicacion()
        restaurarBotonUbicacion()

        mapView.onStop()
        super.onStop()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

    override fun onDestroy() {

        cancelarSolicitudUbicacion()

        mapView.onDestroy()
        super.onDestroy()
    }

    override fun onSaveInstanceState(
        outState: Bundle
    ) {

        mapView.onSaveInstanceState(outState)
        super.onSaveInstanceState(outState)
    }
}