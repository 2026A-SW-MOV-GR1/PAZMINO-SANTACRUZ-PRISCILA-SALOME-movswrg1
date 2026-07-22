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
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.button.MaterialButton
import java.util.Locale

class MainActivity : AppCompatActivity() {

    // Componente visual de Google Maps.
    private lateinit var mapView: MapView

    // Objeto utilizado para controlar Google Maps.
    private lateinit var mapa: GoogleMap

    // Elementos de la interfaz.
    private lateinit var tvCoordenadas: TextView
    private lateinit var btnUsarUbicacion: MaterialButton
    private lateinit var btnConfirmarUbicacion: MaterialButton

    // Servicio de ubicación del dispositivo.
    private lateinit var locationManager: LocationManager

    // Marcador colocado actualmente.
    private var marcadorSeleccionado: Marker? = null

    // Coordenadas elegidas.
    private var latitudSeleccionada: Double? = null
    private var longitudSeleccionada: Double? = null

    // Controla el tiempo máximo de búsqueda de ubicación.
    private val manejadorUbicacion =
        Handler(Looper.getMainLooper())

    private var listenerUbicacion: LocationListener? = null
    private var tareaTiempoEspera: Runnable? = null

    /**
     * Solicita los permisos de ubicación.
     */
    private val solicitudPermisosUbicacion =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permisos ->

            val permisoPreciso =
                permisos[
                    Manifest.permission.ACCESS_FINE_LOCATION
                ] == true

            val permisoAproximado =
                permisos[
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ] == true

            if (permisoPreciso || permisoAproximado) {

                activarCapaMiUbicacion()
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

        setContentView(R.layout.activity_main)

        mapView =
            findViewById(R.id.mapView)

        tvCoordenadas =
            findViewById(R.id.tvCoordenadas)

        btnUsarUbicacion =
            findViewById(R.id.btnUsarUbicacion)

        btnConfirmarUbicacion =
            findViewById(R.id.btnConfirmarUbicacion)

        locationManager =
            getSystemService(
                Context.LOCATION_SERVICE
            ) as LocationManager

        /*
         * Inicializa el ciclo de vida del mapa
         * antes de solicitar GoogleMap.
         */
        mapView.onCreate(savedInstanceState)

        configurarMapa()
        configurarBotones()
    }

    /**
     * Inicializa Google Maps y centra inicialmente
     * la cámara en Quito.
     */
    private fun configurarMapa() {

        mapView.getMapAsync { mapaPreparado ->

            mapa = mapaPreparado

            // Configuración general del mapa.
            mapa.mapType = GoogleMap.MAP_TYPE_NORMAL

            mapa.uiSettings.isZoomControlsEnabled = false
            mapa.uiSettings.isCompassEnabled = true
            mapa.uiSettings.isMapToolbarEnabled = false
            mapa.uiSettings.isZoomGesturesEnabled = true
            mapa.uiSettings.isScrollGesturesEnabled = true
            mapa.uiSettings.isRotateGesturesEnabled = true
            mapa.uiSettings.isTiltGesturesEnabled = true

            val ubicacionQuito = LatLng(
                -0.1807,
                -78.4678
            )

            mapa.moveCamera(
                CameraUpdateFactory.newLatLngZoom(
                    ubicacionQuito,
                    14f
                )
            )

            /*
             * Permite seleccionar manualmente
             * un punto tocando el mapa.
             */
            mapa.setOnMapClickListener { punto ->

                seleccionarUbicacion(
                    punto = punto,
                    titulo = "Incidencia seleccionada"
                )
            }

            activarCapaMiUbicacion()
        }
    }

    /**
     * Configura las acciones de los botones.
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
     * Abre el formulario y envía las coordenadas.
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
     * Verifica los permisos de ubicación.
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

            activarCapaMiUbicacion()
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
     * Activa el punto azul de ubicación de Google Maps.
     */
    @SuppressLint("MissingPermission")
    private fun activarCapaMiUbicacion() {

        if (!::mapa.isInitialized) {
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

        if (permisoPreciso || permisoAproximado) {

            try {

                mapa.isMyLocationEnabled = true
                mapa.uiSettings.isMyLocationButtonEnabled = false

            } catch (exception: SecurityException) {

                mapa.isMyLocationEnabled = false
            }
        }
    }

    /**
     * Obtiene la ubicación mediante GPS y red.
     * La búsqueda termina después de 15 segundos.
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
        btnUsarUbicacion.text =
            "Obteniendo ubicación..."

        val ultimaUbicacionDisponible =
            obtenerUltimaUbicacionDisponible()

        /*
         * Utiliza inmediatamente una ubicación
         * registrada hace menos de cinco minutos.
         */
        if (
            ultimaUbicacionDisponible != null &&
            ubicacionEsReciente(
                ultimaUbicacionDisponible
            )
        ) {

            mostrarUbicacionActual(
                ultimaUbicacionDisponible
            )

            restaurarBotonUbicacion()
            return
        }

        val nuevoListener =
            object : LocationListener {

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
                     * El otro proveedor todavía
                     * puede entregar una ubicación.
                     */
                }
            }

        listenerUbicacion = nuevoListener

        try {

            /*
             * La red suele responder antes que el GPS.
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
             * El GPS se utiliza solamente cuando
             * existe permiso de ubicación precisa.
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
         * Cancela la búsqueda después de 15 segundos.
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
                            "Selecciona el punto manualmente en el mapa.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

        tareaTiempoEspera =
            nuevaTareaTiempoEspera

        manejadorUbicacion.postDelayed(
            nuevaTareaTiempoEspera,
            15_000L
        )
    }

    /**
     * Busca la ubicación guardada más reciente
     * entre GPS y red.
     */
    @SuppressLint("MissingPermission")
    private fun obtenerUltimaUbicacionDisponible(): Location? {

        val ubicaciones =
            mutableListOf<Location>()

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
     * Determina si la ubicación fue obtenida
     * durante los últimos cinco minutos.
     */
    private fun ubicacionEsReciente(
        ubicacion: Location
    ): Boolean {

        val antiguedad =
            System.currentTimeMillis() -
                    ubicacion.time

        return antiguedad in 0..300_000L
    }

    /**
     * Detiene las solicitudes de ubicación.
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
     * Reactiva el botón de ubicación.
     */
    private fun restaurarBotonUbicacion() {

        btnUsarUbicacion.isEnabled = true
        btnUsarUbicacion.text =
            "Usar mi ubicación actual"
    }

    /**
     * Muestra en Google Maps la ubicación
     * obtenida mediante Android.
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
     * Guarda las coordenadas y coloca
     * el marcador personalizado.
     */
    private fun seleccionarUbicacion(
        punto: LatLng,
        titulo: String
    ) {

        if (!::mapa.isInitialized) {
            return
        }

        latitudSeleccionada =
            punto.latitude

        longitudSeleccionada =
            punto.longitude

        // Elimina el marcador anterior.
        marcadorSeleccionado?.remove()

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
                16.5f
            )
        )

        tvCoordenadas.text =
            String.format(
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
     * Convierte el drawable vectorial
     * en un icono de Google Maps.
     */
    private fun crearIconoMarcador(): BitmapDescriptor? {

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

        val bitmap =
            Bitmap.createBitmap(
                tamanoPixeles,
                tamanoPixeles,
                Bitmap.Config.ARGB_8888
            )

        val canvas =
            Canvas(bitmap)

        drawable.setBounds(
            0,
            0,
            canvas.width,
            canvas.height
        )

        drawable.draw(canvas)

        return BitmapDescriptorFactory
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