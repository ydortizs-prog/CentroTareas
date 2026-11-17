package com.ortiz.centrotareas.data

interface TareasRepository {
    // Mejora IA 1: alAvanzar informa el avance del cálculo de 0f a 1f
    suspend fun contarPrimos(hasta: Int, alAvanzar: (Float) -> Unit = {}): Int
    suspend fun cargarPerfil(): String
    suspend fun cargarPedidos(simularError: Boolean): List<String>
}
