package com.ortiz.centrotareas

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.ortiz.centrotareas.ui.PantallaTareas
import com.ortiz.centrotareas.ui.theme.CentroTareasTheme
import com.ortiz.centrotareas.viewmodel.TareasViewModel
import com.ortiz.centrotareas.viewmodel.TareasViewModelFactory

class MainActivity : ComponentActivity() {

    private val vm: TareasViewModel by viewModels { TareasViewModelFactory() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CentroTareasTheme {
                PantallaTareas(vm)
            }
        }
    }
}
