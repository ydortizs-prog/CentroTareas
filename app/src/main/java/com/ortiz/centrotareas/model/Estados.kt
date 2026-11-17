package com.ortiz.centrotareas.model

sealed interface PrimosUi {
    data object Inicial : PrimosUi
    data object Calculando : PrimosUi
    data class Listo(val cantidad: Int, val milisegundos: Long) : PrimosUi
    data object Cancelado : PrimosUi
}

sealed interface CargaUi {
    data object Inicial : CargaUi
    data object Cargando : CargaUi
    data class Exito(
        val modo: String,
        val perfil: String,
        val pedidos: Int,
        val milisegundos: Long
    ) : CargaUi
    data class Error(val mensaje: String) : CargaUi
}

// Mejora IA 4: tiempos acumulados de cada modo de carga, para compararlos
data class ComparacionTiempos(
    val unaPorUna: List<Long> = emptyList(),
    val aLaVez: List<Long> = emptyList()
) {
    val promedioUnaPorUna: Long? get() = unaPorUna.takeIf { it.isNotEmpty() }?.average()?.toLong()
    val promedioALaVez: Long? get() = aLaVez.takeIf { it.isNotEmpty() }?.average()?.toLong()
}
