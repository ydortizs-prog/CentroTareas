package com.ortiz.centrotareas.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.Modifier
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
    val carga by vm.carga.collectAsState()
    // Texto del campo numérico (por defecto 500 000; ajústalo para que tarde 2-5 s)
    var textoN by rememberSaveable { mutableStateOf("500000") }
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
                LinearProgressIndicator(
                    progress = { progreso },
                    modifier = Modifier.fillMaxWidth()
                )
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
                    PrimosUi.Calculando -> Text("Calculando...")
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
                // TODO Paso 7: interruptor Simular error y Reintentar
                when (val c = carga) {
                    CargaUi.Inicial -> Text("Elige cómo cargar los datos")
                    CargaUi.Cargando -> CircularProgressIndicator()
                    is CargaUi.Exito -> RecuadroVerde(
                        "Modo: ${c.modo}\nPerfil: ${c.perfil}\nPedidos: ${c.pedidos}\nTiempo: ${c.milisegundos} ms"
                    )
                    is CargaUi.Error -> Text(c.mensaje) // Paso 7: recuadro de error
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
