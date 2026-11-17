package com.ortiz.centrotareas.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ortiz.centrotareas.data.TareasRepository
import com.ortiz.centrotareas.model.CargaUi
import com.ortiz.centrotareas.model.PrimosUi
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout

class TareasViewModel(private val repo: TareasRepository) : ViewModel() {

    // ----- Tarea con progreso (pasos 1 a 4)
    private val _mensaje = MutableStateFlow("Listo")
    val mensaje: StateFlow<String> = _mensaje.asStateFlow()

    // Paso 3: progreso de 0f a 1f (la pantalla solo lee la versión de solo lectura)
    private val _progreso = MutableStateFlow(0f)
    val progreso: StateFlow<Float> = _progreso.asStateFlow()

    // Paso 2: reemplaza a tareaLargaMala(). La espera va en segundo plano con una corrutina
    // Paso 3: 100 vueltas de 40 ms (≈ 4 s) actualizando el progreso
    // Paso 4: el Job de la tarea para poder cancelarla
    private var trabajo: Job? = null

    // Paso 4: true mientras la tarea trabaja
    private val _corriendo = MutableStateFlow(false)
    val corriendo: StateFlow<Boolean> = _corriendo.asStateFlow()

    fun tareaLarga() {
        // Paso 4: si ya está corriendo, no se lanza otra
        if (_corriendo.value) return
        trabajo = viewModelScope.launch {
            _corriendo.value = true
            try {
                _progreso.value = 0f
                _mensaje.value = "Trabajando..."
                Log.d("HILO", "Antes: ${Thread.currentThread().name}")
                withContext(Dispatchers.Default) {
                    Log.d("HILO", "Dentro: ${Thread.currentThread().name}")
                    for (i in 1..100) {
                        delay(40) // delay ya revisa la cancelación
                        _progreso.value = i / 100f
                    }
                }
                Log.d("HILO", "Despues: ${Thread.currentThread().name}")
                _mensaje.value = "Terminó"
            } finally {
                // Termine bien o cancelada, deja de estar corriendo
                _corriendo.value = false
            }
        }
    }

    // Paso 4: cancela el Job; la barra se queda donde estaba
    fun cancelar() {
        trabajo?.cancel()
        _mensaje.value = "Cancelado"
    }

    // ----- Contar primos (paso 5)
    private val _primos = MutableStateFlow<PrimosUi>(PrimosUi.Inicial)
    val primos: StateFlow<PrimosUi> = _primos.asStateFlow()

    // Mejora IA 1: progreso del cálculo de primos (0f a 1f)
    private val _progresoPrimos = MutableStateFlow(0f)
    val progresoPrimos: StateFlow<Float> = _progresoPrimos.asStateFlow()

    // Paso 5: Job del cálculo para poder cancelarlo
    private var trabajoPrimos: Job? = null

    fun contar(hasta: Int) {
        if (hasta < 2 || _primos.value is PrimosUi.Calculando) return
        trabajoPrimos = viewModelScope.launch {
            _primos.value = PrimosUi.Calculando
            val inicio = System.currentTimeMillis()
            try {
                // El trabajo pesado lo hace el Repository en segundo plano
                _progresoPrimos.value = 0f
                // Mejora IA 1: el Repository informa el avance y el ViewModel lo publica
                val cantidad = repo.contarPrimos(hasta) { _progresoPrimos.value = it }
                _primos.value = PrimosUi.Listo(cantidad, System.currentTimeMillis() - inicio)
            } catch (e: CancellationException) {
                _primos.value = PrimosUi.Cancelado
                throw e // se vuelve a lanzar para no romper la cancelación
            }
        }
    }

    fun cancelarPrimos() {
        trabajoPrimos?.cancel()
    }

    // ----- Dos cargas (pasos 6 y 7)
    private val _carga = MutableStateFlow<CargaUi>(CargaUi.Inicial)
    val carga: StateFlow<CargaUi> = _carga.asStateFlow()

    // Paso 6: primero el perfil (2 s) y después los pedidos (3 s) ≈ 5 s
    fun cargarUnaPorUna() {
        if (_carga.value is CargaUi.Cargando) return
        viewModelScope.launch {
            _carga.value = CargaUi.Cargando
            val inicio = System.currentTimeMillis()
            try {
                val (perfil, pedidos) = conTiempoMaximo {
                    val perfil = repo.cargarPerfil()
                    val pedidos = repo.cargarPedidos(_simularError.value)
                    Pair(perfil, pedidos)
                }
                _carga.value = CargaUi.Exito(
                    "Una por una", perfil, pedidos.size, System.currentTimeMillis() - inicio
                )
            } catch (e: Exception) {
                // Mejora IA 2: el tiempo máximo se venció (va antes, porque también es una cancelación)
                if (e is TimeoutCancellationException) {
                    _carga.value = CargaUi.Error("Tardó demasiado (más de 2 s)")
                    return@launch
                }
                // Paso 7: primero deja pasar la cancelación
                if (e is CancellationException) throw e
                _carga.value = CargaUi.Error("No se pudo cargar: " + e.message)
            }
        }
    }

    // Paso 6: las dos cargas al mismo tiempo con async ≈ máximo(2 s, 3 s) = 3 s
    fun cargarALaVez() {
        if (_carga.value is CargaUi.Cargando) return
        viewModelScope.launch {
            _carga.value = CargaUi.Cargando
            val inicio = System.currentTimeMillis()
            try {
                val (perfil, pedidos) = conTiempoMaximo {
                    coroutineScope {
                        val a = async { repo.cargarPerfil() }
                        val b = async { repo.cargarPedidos(_simularError.value) }
                        Pair(a.await(), b.await())
                    }
                }
                _carga.value = CargaUi.Exito(
                    "A la vez", perfil, pedidos.size, System.currentTimeMillis() - inicio
                )
            } catch (e: Exception) {
                // Mejora IA 2: el tiempo máximo se venció (va antes, porque también es una cancelación)
                if (e is TimeoutCancellationException) {
                    _carga.value = CargaUi.Error("Tardó demasiado (más de 2 s)")
                    return@launch
                }
                // Paso 7: primero deja pasar la cancelación
                if (e is CancellationException) throw e
                _carga.value = CargaUi.Error("No se pudo cargar: " + e.message)
            }
        }
    }

    // Mejora IA 2: interruptor de tiempo máximo (2 s) para las cargas
    private val _tiempoMaximo = MutableStateFlow(false)
    val tiempoMaximo: StateFlow<Boolean> = _tiempoMaximo.asStateFlow()

    fun cambiarTiempoMaximo(valor: Boolean) {
        _tiempoMaximo.value = valor
    }

    // Mejora IA 2: si el interruptor está activo, la carga se corta a los 2 s con withTimeout
    private suspend fun <T> conTiempoMaximo(bloque: suspend () -> T): T =
        if (_tiempoMaximo.value) withTimeout(2000) { bloque() } else bloque()

    // Paso 7: interruptor para que los pedidos fallen con IOException("Sin conexión")
    private val _simularError = MutableStateFlow(false)
    val simularError: StateFlow<Boolean> = _simularError.asStateFlow()

    fun cambiarSimularError(valor: Boolean) {
        _simularError.value = valor
    }

    // Paso 7: Reintentar vuelve a ejecutar la carga A la vez
    fun reintentar() {
        cargarALaVez()
    }
}
