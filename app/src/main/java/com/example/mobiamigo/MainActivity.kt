package com.example.mobiamigo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.mobiamigo.data.AppItem
import com.example.mobiamigo.screens.AddAppScreen
import com.example.mobiamigo.screens.HomeScreen
import com.example.mobiamigo.screens.LoginScreen
import com.example.mobiamigo.screens.RegisterScreen
import com.example.mobiamigo.screens.CalendarioScreen
import com.example.mobiamigo.screens.Evento // IMPORTANTE: Necesario para crear la lista aquí
import com.example.mobiamigo.ui.theme.MobiamigoTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MobiamigoTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation()
                }
            }
        }
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    // Lista para las apps seleccionadas
    val selectedApps = remember { mutableStateListOf<AppItem>() }

    // --- NUEVO: Lista para los eventos del calendario (Persistencia) ---
    // Al crearla aquí, los eventos no se borran cuando sales de la pantalla Calendario
    val eventosGlobales = remember { mutableStateListOf<Evento>() }

    NavHost(navController = navController, startDestination = "login") {

        composable("login") {
            LoginScreen(navController = navController)
        }

        composable("register") {
            RegisterScreen(navController = navController)
        }

        composable("home") {
            HomeScreen(
                navController = navController,
                selectedApps = selectedApps
            )
        }

        composable("add_app") {
            AddAppScreen(
                navController = navController,
                onAppsSelected = { apps ->
                    selectedApps.clear()
                    selectedApps.addAll(apps)
                    navController.popBackStack()
                },
                currentlySelected = selectedApps.toList()
            )
        }

        // --- RUTA MODIFICADA ---
        composable("calendario") {
            // Le pasamos la lista global para que guarde y lea los mismos datos siempre
            CalendarioScreen(navController = navController, eventos = eventosGlobales)
        }
    }
}