package com.ortiz.centrotareas.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ortiz.centrotareas.viewmodel.TareasViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaTareas(vm: TareasViewModel) {
    val mensaje by vm.mensaje.collectAsState()
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
                // Paso 2: la tarea larga ya no bloquea el hilo principal
                Button(onClick = { vm.tareaLarga() }) {
                    Text("Tarea larga")
                }
                // TODO Paso 3: LinearProgressIndicator y porcentaje
                // TODO Paso 4: botones Iniciar / Cancelar
                Text(mensaje)
            }

            Tarjeta("Contar números primos") {
                // TODO Paso 5: campo numérico, botones Contar / Cancelar y resultado
                Text("Pendiente")
            }

            Tarjeta("Dos cargas") {
                // TODO Paso 6: botones Una por una / A la vez y resultado
                // TODO Paso 7: interruptor Simular error y Reintentar
                Text("Pendiente")
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
