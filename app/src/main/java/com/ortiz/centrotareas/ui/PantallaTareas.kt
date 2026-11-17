package com.ortiz.centrotareas.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ortiz.centrotareas.model.CargaUi
import com.ortiz.centrotareas.model.PrimosUi
import com.ortiz.centrotareas.viewmodel.TareasViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaTareas(vm: TareasViewModel) {
    val mensaje by vm.mensaje.collectAsState()
    val progreso by vm.progreso.collectAsState()
    val corriendo by vm.corriendo.collectAsState()
    val primos by vm.primos.collectAsState()
    val progresoPrimos by vm.progresoPrimos.collectAsState()
    val carga by vm.carga.collectAsState()
    val simularError by vm.simularError.collectAsState()
    val tiempoMaximo by vm.tiempoMaximo.collectAsState()
    val intento by vm.intento.collectAsState()
    // Texto del campo numérico (500 000 tardaba ~600 ms en este emulador; se ajusta para que tarde 2-5 s)
    var textoN by rememberSaveable { mutableStateOf("1500000") }
    // Contador de toques: la "prueba de vida" de la pantalla
    var toques by rememberSaveable { mutableIntStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("CentroTareas") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Paso 1: si deja de sumar, la pantalla está congelada
            Button(
                onClick = { toques++ },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Tócame: $toques")
            }

            Tarjeta("Tarea con progreso") {
                // Paso 2 y 3: la tarea larga ya no bloquea el hilo principal
                // Paso 4: Iniciar deshabilitado mientras corre; Cancelar solo mientras corre
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { vm.tareaLarga() },
                        enabled = !corriendo
                    ) {
                        Text("Iniciar")
                    }
                    OutlinedButton(
                        onClick = { vm.cancelar() },
                        enabled = corriendo
                    ) {
                        Text("Cancelar")
                    }
                }
                // Paso 3: barra de progreso y porcentaje debajo
                // Mejora IA 1: misma barra "tipo trueno" para la tarea con progreso
                BarraRayo(progreso = progreso)
                Text("${(progreso * 100).toInt()} %")
                Text(mensaje)
            }

            Tarjeta("Contar números primos") {
                // Paso 5: campo numérico; solo acepta dígitos
                OutlinedTextField(
                    value = textoN,
                    onValueChange = { nuevo -> textoN = nuevo.filter { it.isDigit() } },
                    label = { Text("Contar primos hasta") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                val n = textoN.toIntOrNull()
                val calculando = primos is PrimosUi.Calculando
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        // Ignora textos no numéricos o menores que 2
                        onClick = { n?.let { vm.contar(it) } },
                        enabled = !calculando && n != null && n >= 2
                    ) {
                        Text("Contar")
                    }
                    OutlinedButton(
                        onClick = { vm.cancelarPrimos() },
                        enabled = calculando
                    ) {
                        Text("Cancelar")
                    }
                }
                when (val p = primos) {
                    PrimosUi.Inicial -> Text("Escribe un número y pulsa Contar")
                    PrimosUi.Calculando -> Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        // Mejora IA 1: barra "tipo trueno" con el avance del cálculo
                        BarraRayo(progreso = progresoPrimos)
                        Text("⚡ Calculando... ${(progresoPrimos * 100).toInt()} %")
                    }
                    is PrimosUi.Listo -> RecuadroVerde(
                        "Primos encontrados: ${p.cantidad}\nTiempo: ${p.milisegundos} ms"
                    )
                    PrimosUi.Cancelado -> Text("Cancelado")
                }
            }

            Tarjeta("Dos cargas") {
                // Paso 6: botones deshabilitados mientras carga (no se lanza una segunda carga)
                val cargando = carga is CargaUi.Cargando
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = { vm.cargarUnaPorUna() },
                        enabled = !cargando
                    ) {
                        Text("Una por una")
                    }
                    Button(
                        onClick = { vm.cargarALaVez() },
                        enabled = !cargando
                    ) {
                        Text("A la vez")
                    }
                }
                // Paso 7: interruptor Simular error
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Switch(
                        checked = simularError,
                        onCheckedChange = { vm.cambiarSimularError(it) }
                    )
                    Text("  Simular error")
                }
                // Mejora IA 2: interruptor de tiempo máximo
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Switch(
                        checked = tiempoMaximo,
                        onCheckedChange = { vm.cambiarTiempoMaximo(it) }
                    )
                    Text("  Tiempo máximo 2 s")
                }
                when (val c = carga) {
                    CargaUi.Inicial -> Text("Elige cómo cargar los datos")
                    CargaUi.Cargando -> Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        CircularProgressIndicator()
                        // Mejora IA 3: avisa en qué intento va
                        intento?.let { Text(it, color = MaterialTheme.colorScheme.secondary) }
                    }
                    is CargaUi.Exito -> RecuadroVerde(
                        "Modo: ${c.modo}\nPerfil: ${c.perfil}\nPedidos: ${c.pedidos}\nTiempo: ${c.milisegundos} ms"
                    )
                    is CargaUi.Error -> RecuadroError(c.mensaje) { vm.reintentar() }
                }
            }
        }
    }
}

// Cada tarea va en una Card con su título en azul
@Composable
private fun Tarjeta(titulo: String, contenido: @Composable ColumnScope.() -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                titulo,
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            contenido()
        }
    }
}

// Recuadro verde para mostrar resultados
@Composable
private fun RecuadroVerde(texto: String) {
    Text(
        texto,
        color = Color(0xFF1B5E20),
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFC8E6C9), RoundedCornerShape(8.dp))
            .padding(12.dp)
    )
}

// Paso 7: recuadro con color de error, el mensaje y el botón Reintentar
@Composable
private fun RecuadroError(mensaje: String, onReintentar: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.errorContainer, RoundedCornerShape(8.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(mensaje, color = MaterialTheme.colorScheme.onErrorContainer)
        Button(
            onClick = onReintentar,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.error,
                contentColor = MaterialTheme.colorScheme.onError
            )
        ) {
            Text("Reintentar")
        }
    }
}

// Mejora IA 1: barra de progreso "tipo trueno": degradado amarillo-naranja con un brillo que recorre la barra
@Composable
private fun BarraRayo(progreso: Float) {
    val brillo = rememberInfiniteTransition(label = "rayo")
    val desplazamiento by brillo.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(durationMillis = 900, easing = LinearEasing)),
        label = "brillo"
    )
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(12.dp)
    ) {
        val radio = CornerRadius(size.height / 2, size.height / 2)
        // Fondo de la barra
        drawRoundRect(color = Color(0xFF263238), cornerRadius = radio)
        val ancho = size.width * progreso.coerceIn(0f, 1f)
        if (ancho > 0f) {
            // Relleno eléctrico: amarillo -> naranja -> amarillo
            drawRoundRect(
                brush = Brush.horizontalGradient(
                    listOf(Color(0xFFFFEB3B), Color(0xFFFF9800), Color(0xFFFFD600))
                ),
                size = Size(ancho, size.height),
                cornerRadius = radio
            )
            // Destello blanco que corre sobre la parte llena
            val x = ancho * desplazamiento
            drawRoundRect(
                brush = Brush.horizontalGradient(
                    listOf(Color.Transparent, Color.White.copy(alpha = 0.8f), Color.Transparent),
                    startX = x - 40f,
                    endX = x + 40f
                ),
                size = Size(ancho, size.height),
                cornerRadius = radio
            )
        }
    }
}
