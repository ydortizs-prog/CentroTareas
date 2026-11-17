package com.ortiz.centrotareas.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import java.io.IOException

// Cargas simuladas con delay (datos ficticios, sin internet)
class TareasRepositoryLocal : TareasRepository {

    override suspend fun cargarPerfil(): String {
        delay(2000)
        return "Ana Torres (ficticia)"
    }

    override suspend fun cargarPedidos(simularError: Boolean): List<String> {
        delay(3000)
        if (simularError) throw IOException("Sin conexión")
        return listOf("Pedido 101", "Pedido 102", "Pedido 103")
    }

    // Paso 5: cálculo pesado en Dispatchers.Default, cancelable a mitad del ciclo
    // Mejora IA 1: avisa el progreso cada 1 % para no saturar la pantalla
    override suspend fun contarPrimos(hasta: Int, alAvanzar: (Float) -> Unit): Int =
        withContext(Dispatchers.Default) {
            var cantidad = 0
            val paso = maxOf(1, hasta / 100)
            alAvanzar(0f)
            for (n in 2..hasta) {
                ensureActive() // si cancelan, se detiene aquí
                if (esPrimo(n)) cantidad++
                if (n % paso == 0) alAvanzar(n.toFloat() / hasta)
            }
            alAvanzar(1f)
            cantidad
        }

    // Paso 5: prueba dividir entre 2 y la raíz de n (d * d <= n evita calcular la raíz)
    private fun esPrimo(n: Int): Boolean {
        var d = 2
        while (d.toLong() * d <= n) {
            if (n % d == 0) return false
            d++
        }
        return true
    }
}
