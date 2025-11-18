package com.example.mobiamigo.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign



data class TutorialStep(
    val title: String,
    val description: String,
    val mediumDetail: String,
    val fullDetail: String
)

val mobiAmigoTutorialSteps = listOf(
    TutorialStep(
        title = "Pantalla de Inicio (Home)",
        description = "Este es tu punto de partida en MobiAmigo, donde accederás a todas las funciones y tus accesos rápidos.",
        mediumDetail = "Aquí accedes a las funciones principales como el asistente y tus aplicaciones favoritas.",
        fullDetail = "La pantalla Home se compone de una cuadrícula de botones. Los botones son accesos directos personalizables (tus Apps, Contactos y Asistente). La lista de apps y contactos se guarda localmente con 'rememberSaveable'."
    ),
    TutorialStep(
        title = "El Botón 'Añadir Apps'",
        description = "Permite seleccionar cualquier aplicación instalada en tu teléfono para crear un acceso directo en Home.",
        mediumDetail = "Útil para organizar tus aplicaciones más importantes en un solo lugar.",
        fullDetail = "Al presionar, navegas a la pantalla `AddAppScreen`. Esta función necesita el permiso de Android para ver todas las aplicaciones instaladas en tu dispositivo (requiere permiso `QUERY_ALL_PACKAGES` en versiones recientes de Android) para mostrar la lista completa."
    ),
    TutorialStep(
        title = "Añadir Contactos",
        description = "Este botón te permite anclar a una persona importante directamente en tu pantalla Home para llamar rápidamente.",
        mediumDetail = "Sirve para crear botones de llamada directa a tus contactos más frecuentes.",
        fullDetail = "La función primero te lleva a una pantalla de selección de contactos. El botón de llamada directa final requiere el permiso **`CALL_PHONE`** para evitar la pantalla de marcación. Si no tienes este permiso, la aplicación te notificará que la llamada no puede ser iniciada."
    ),
    TutorialStep(
        title = "Asistente / IA (Gemini)",
        description = "Tu ayudante virtual personal, disponible 24/7 para responder preguntas o ayudarte con tareas.",
        mediumDetail = "Toca este botón para usar la Inteligencia Artificial de Gemini.",
        fullDetail = "Al presionarlo, se ejecuta la función asíncrona `solicitarAsistenciaIA()`. Esta función es crítica y requiere que el archivo **`google-services.json`** esté en la carpeta `app/` y la **API Key de Gemini** esté configurada en `BuildConfig` para establecer la comunicación."
    )
)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TutorialScreen(navController: NavController, level: String) {
    // Estado para rastrear el paso actual del tutorial
    var currentPage by remember { mutableStateOf(0) }

    // Filtramos los pasos para el nivel de ayuda
    val stepsToShow = if (level == TUTORIAL_MEDIUM) {
        mobiAmigoTutorialSteps.take(3)
    } else {
        mobiAmigoTutorialSteps
    }

    val totalPages = stepsToShow.size
    val isLastStep = currentPage == totalPages - 1
    val isFinishPage = currentPage >= totalPages
    val finishTutorial: () -> Unit = {

        navController.previousBackStackEntry
            ?.savedStateHandle
            ?.set("tutorial_status", TUTORIAL_COMPLETED)


        navController.popBackStack()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isFinishPage) "Tutorial Finalizado" else "Tutorial MobiAmigo") },
                navigationIcon = {

                    IconButton(onClick = {
                        if (currentPage > 0 && !isFinishPage) {
                            currentPage--
                        } else {

                            finishTutorial()
                        }
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = if (currentPage == 0) "Volver a Home" else "Paso Anterior"
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {


            if (!isFinishPage) {
                LinearProgressIndicator(
                    progress = (currentPage + 1).toFloat() / totalPages,
                    modifier = Modifier.fillMaxWidth().height(8.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Paso ${currentPage + 1} de $totalPages",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(32.dp))
            }



            Box(modifier = Modifier.weight(1f)) {
                val scrollState = rememberScrollState()
                if (!isFinishPage) {
                    val currentStep = stepsToShow[currentPage]

                    Column(modifier = Modifier.verticalScroll(scrollState)) {
                        TutorialStepView(
                            step = currentStep,
                            isFullTutorial = level == TUTORIAL_FULL
                        )
                    }
                } else {

                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "🎉 Tutorial Completado 🎉",
                            style = MaterialTheme.typography.headlineMedium,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                        Text(
                            text = "¡Ahora estás listo para usar MobiAmigo con confianza! Vuelve a Inicio para comenzar.",
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = {
                    if (isFinishPage) {

                        finishTutorial()
                    } else {

                        currentPage++
                    }
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isFinishPage) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.tertiary
                )
            ) {
                Text(
                    text = if (isFinishPage) "Volver a Inicio" else if (isLastStep) "Finalizar Tutorial" else "Continuar",
                    fontSize = 18.sp
                )
            }
        }
    }
}


@Composable
fun TutorialStepView(step: TutorialStep, isFullTutorial: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {

            Text(
                text = step.title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(16.dp))


            Text(
                text = step.description,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(bottom = 16.dp)
            )


            val detailContent = if (isFullTutorial) step.fullDetail else step.mediumDetail

            Divider()
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = if (isFullTutorial) "🔍 Detalles Técnicos y Permisos:" else "💡 Función Esencial:",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = detailContent,
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(16.dp))

            when (step.title) {
                "El Botón 'Añadir Apps'" -> {
                    Button(
                        onClick = { /* No hace nada */ },
                        enabled = false,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Añadir Apps", fontWeight = FontWeight.SemiBold)
                    }
                    Text(text = " (Este es el botón en la pantalla Home)", style = MaterialTheme.typography.labelSmall)
                }
                "Añadir Contactos" -> {
                    Button(
                        onClick = { /* No hace nada */ },
                        enabled = false,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Añadir contactos", fontWeight = FontWeight.SemiBold)
                    }
                    Text(text = " (Este es el botón en la pantalla Home)", style = MaterialTheme.typography.labelSmall)
                }
                "Asistente / IA (Gemini)" -> {
                    Button(
                        onClick = { /* No hace nada */ },
                        enabled = false,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Asistencia/IA", fontWeight = FontWeight.SemiBold)
                    }
                    Text(text = " (Este es el botón en la pantalla Home)", style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}