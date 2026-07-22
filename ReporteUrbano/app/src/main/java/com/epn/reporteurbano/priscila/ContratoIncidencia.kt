package com.epn.reporteurbano.priscila

/**
 * Contiene los nombres de todos los parámetros utilizados
 * para comunicar las tres aplicaciones mediante Intents.
 *
 * Los nombres de los extras deben ser exactamente iguales
 * en Reporte Urbano, Atención Urbana y Resolución Urbana.
 */
object ContratoIncidencia {

    // =====================================================
    // DATOS GENERADOS POR REPORTE URBANO - PRISCILA
    // =====================================================

    const val EXTRA_ID_INCIDENTE =
        "idIncidente"

    const val EXTRA_TIPO_INCIDENTE =
        "tipoIncidente"

    const val EXTRA_DESCRIPCION =
        "descripcion"

    const val EXTRA_LATITUD =
        "latitud"

    const val EXTRA_LONGITUD =
        "longitud"

    const val EXTRA_PRIORIDAD =
        "prioridad"

    const val EXTRA_FECHA_REPORTE =
        "fechaReporte"

    const val EXTRA_ESTADO =
        "estado"

    // =====================================================
    // DATOS AGREGADOS POR ATENCIÓN URBANA - SAUL
    // =====================================================

    const val EXTRA_NOMBRE_INSPECTOR =
        "nombreInspector"

    const val EXTRA_BRIGADA_ASIGNADA =
        "brigadaAsignada"

    const val EXTRA_LATITUD_BRIGADA =
        "latitudBrigada"

    const val EXTRA_LONGITUD_BRIGADA =
        "longitudBrigada"

    const val EXTRA_RESULTADO_INSPECCION =
        "resultadoInspeccion"

    const val EXTRA_FECHA_INSPECCION =
        "fechaInspeccion"

    const val EXTRA_PRIORIDAD_CONFIRMADA =
        "prioridadConfirmada"

    // =====================================================
    // DATOS AGREGADOS POR RESOLUCIÓN URBANA - CLAUDIO
    // =====================================================

    const val EXTRA_TRABAJO_REALIZADO =
        "trabajoRealizado"

    const val EXTRA_MATERIALES_UTILIZADOS =
        "materialesUtilizados"

    const val EXTRA_RESPONSABLE_RESOLUCION =
        "responsableResolucion"

    const val EXTRA_FECHA_RESOLUCION =
        "fechaResolucion"

    const val EXTRA_OBSERVACIONES_FINALES =
        "observacionesFinales"

    const val EXTRA_ESTADO_FINAL =
        "estadoFinal"

    // =====================================================
    // ESTADOS DEL INCIDENTE
    // =====================================================

    const val ESTADO_REPORTADO =
        "REPORTADO"

    const val ESTADO_EN_ATENCION =
        "EN_ATENCION"

    const val ESTADO_RESUELTO =
        "RESUELTO"

    // =====================================================
    // APLICACIÓN DE SAUL
    // =====================================================

    const val PAQUETE_ATENCION =
        "com.epn.atencionurbana.saul"

    const val ACTIVIDAD_ATENCION =
        "com.epn.atencionurbana.saul.MainActivity"

    // =====================================================
    // APLICACIÓN DE CLAUDIO
    // =====================================================

    const val PAQUETE_RESOLUCION =
        "com.epn.resolucionurbana.claudio"

    const val ACTIVIDAD_RESOLUCION =
        "com.epn.resolucionurbana.claudio.MainActivity"
}