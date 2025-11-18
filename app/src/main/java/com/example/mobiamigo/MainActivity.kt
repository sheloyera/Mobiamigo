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
import androidx.navigation.navArgument // 👈 IMPORTACIÓN REQUERIDA
import androidx.navigation.NavType // 👈 IMPORTACIÓN REQUERIDA
import com.example.mobiamigo.data.AppItem
import com.example.mobiamigo.screens.AddAppScreen
import com.example.mobiamigo.screens.HomeScreen
import com.example.mobiamigo.screens.LoginScreen
import com.example.mobiamigo.screens.TutorialScreen // 👈 IMPORTACIÓN REQUERIDA
import com.example.mobiamigo.screens.TUTORIAL_NONE // 👈 IMPORTACIÓN REQUERIDA
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
    // Nota: Es mejor definir selectedApps fuera de NavHost para que no se reinicie
    val selectedApps = remember { mutableStateListOf<AppItem>() }

    NavHost(navController = navController, startDestination = "login") {

        composable("login") {
            LoginScreen(navController = navController)
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

        // 👇 NUEVA RUTA: PANTALLA DEL TUTORIAL
        composable(
            route = "tutorial_screen/{level}", // Define la ruta con el argumento dinámico {level}
            arguments = listOf(
                navArgument("level") {
                    type = NavType.StringType
                    defaultValue = TUTORIAL_NONE // Valor por defecto en caso de que no se pase
                }
            )
        ) { backStackEntry ->
            // Obtenemos el parámetro 'level' (full, medium o none) de los argumentos
            val level = backStackEntry.arguments?.getString("level") ?: TUTORIAL_NONE
            TutorialScreen(navController = navController, level = level) // Llama a la nueva pantalla
        }
    }
}