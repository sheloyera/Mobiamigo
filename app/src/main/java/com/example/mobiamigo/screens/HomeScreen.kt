package com.example.mobiamigo.screens

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.mobiamigo.data.AppItem
import com.example.mobiamigo.utils.AppManager
import androidx.core.graphics.drawable.toBitmap
import com.example.mobiamigo.BuildConfig
import com.google.ai.client.generativeai.GenerativeModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.filled.Person
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat

/**
 * NIVELES DE AYUDA (Mismos que definimos anteriormente)
 */
const val TUTORIAL_NONE = "none"
const val TUTORIAL_MEDIUM = "medium"
const val TUTORIAL_FULL = "full"
// Constante para marcar que el tutorial ha finalizado y no debe volver a mostrarse
const val TUTORIAL_COMPLETED = "completed"


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    selectedApps: List<AppItem>
) {
    val context = LocalContext.current

    // Lógica del Tutorial: Usamos TUTORIAL_COMPLETED como marcador de que ya se mostró.
    var tutorialLevel by rememberSaveable { mutableStateOf<String?>(null) }

    // 👇 NUEVO BLOQUE: Leer el resultado de la pantalla de tutorial
    LaunchedEffect(navController) {
        navController.currentBackStackEntry?.savedStateHandle?.getLiveData<String>("tutorial_status")
            ?.observeForever { status ->
                if (status == TUTORIAL_COMPLETED) {
                    tutorialLevel = TUTORIAL_COMPLETED // Forzar a completado
                }
            }
    }

    // El diálogo solo se muestra si el nivel es NULL (primera vez) y no ha sido completado.
    if (tutorialLevel == null) {
        TutorialSelectionDialog(
            onSelection = { level ->
                tutorialLevel = level // Guarda la selección.
                if (level != TUTORIAL_NONE) {
                    // Navega a la pantalla de tutorial si se necesita ayuda
                    navController.navigate("tutorial_screen/$level")
                } else {
                    // Si selecciona "No necesito ayuda", marcamos como completado inmediatamente.
                    tutorialLevel = TUTORIAL_COMPLETED
                }
            }
        )
    }

    // Lógica para manejar la selección de contactos
    var selectedContacts by rememberSaveable { mutableStateOf(listOf<Pair<String, String>>()) }
    // ... (el resto del código de HomeScreen es el mismo)

    val contactLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val name = result.data?.getStringExtra("contact_name")
            val number = result.data?.getStringExtra("contact_number")
            if (name != null && number != null) {
                // Añade el nuevo contacto a la lista de contactos seleccionados
                selectedContacts = selectedContacts + (name to number)
            }
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(title = { Text("MobiAmigo Home") })
        }
    ) { paddingValues ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Sección 1: Aplicaciones seleccionadas
            items(selectedApps) { app ->
                AppGridItem(app = app, onClick = {
                    AppManager.launchApp(context, app)
                })
            }

            // Sección 2: Botones de Acción Fijos

            // Botón para añadir apps
            item {
                ActionGridItem(
                    icon = Icons.Default.Add,
                    label = "Añadir Apps",
                    description = "Botón para agregar más aplicaciones",
                    onClick = { navController.navigate("add_app") }
                )
            }

            // Botón Para añadir contactos
            item {
                ActionGridItem(
                    icon = Icons.Default.Add,
                    label = "Añadir contactos",
                    description = "Botón para agregar un contacto la pantalla principal",
                    onClick = {
                        // Navega a la pantalla de contactos para selección
                        val intent = Intent(context, ContactosScreen::class.java)
                        contactLauncher.launch(intent)
                    }
                )
            }

            // Botón de Asistencia / IA
            item {
                ActionGridItem(
                    icon = Icons.Default.SupportAgent,
                    label = "Asistencia/IA",
                    description = "Solicitar ayuda remota o con la Inteligencia Artificial",
                    onClick = {
                        solicitarAsistenciaIA(context)
                    }
                )
            }

            // Sección 3: Contactos Añadidos Dinámicamente
            // Muestra los contactos agregados como botones en la cuadrícula
            items(selectedContacts) { (name, number) ->
                ActionGridItem(
                    icon = Icons.Default.Person,
                    label = name,
                    description = "Llamar a $name",
                    onClick = {
                        val callIntent = Intent(Intent.ACTION_CALL, Uri.parse("tel:$number"))

                        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE)
                            == PackageManager.PERMISSION_GRANTED
                        ) {
                            context.startActivity(callIntent)
                        } else {
                            Toast.makeText(context, "Se requiere permiso para realizar llamadas", Toast.LENGTH_SHORT).show()
                        }
                    }
                )
            }
        }
    }
}

/**
 * Componente para mostrar una aplicación instalada.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppGridItem(app: AppItem, onClick: () -> Unit) {
    Card(
        shape = RoundedCornerShape(12.dp),
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .semantics { contentDescription = "Abrir ${app.label}" }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            app.icon?.let { drawable ->
                Image(
                    bitmap = drawable.toBitmap().asImageBitmap(),
                    contentDescription = null,
                    modifier = Modifier.size(80.dp)
                )
            }
            Spacer(Modifier.height(8.dp))
            Text(
                text = app.label,
                style = MaterialTheme.typography.titleLarge,
                maxLines = 1
            )
        }
    }
}

/**
 * Componente para mostrar un botón de acción (como Añadir Apps, Contactos, IA).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActionGridItem(icon: ImageVector, label: String, description: String, onClick: () -> Unit) {
    Card(
        shape = RoundedCornerShape(12.dp),
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .semantics { contentDescription = description }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(64.dp))
            Spacer(Modifier.height(8.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.titleLarge,
                maxLines = 1
            )
        }
    }
}


@Composable
fun TutorialSelectionDialog(onSelection: (String) -> Unit) {
    AlertDialog(
        onDismissRequest = { /* No se puede cerrar, debe seleccionar */ },
        title = { Text("Nivel de Ayuda Requerido") },
        text = {
            Text("¡Hola! Para tu primera vez, ¿cuánta guía necesitas para usar MobiAmigo?")
        },
        confirmButton = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                // Opción 1: Mucha ayuda
                Button(
                    onClick = { onSelection(TUTORIAL_FULL) },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                ) {
                    Text("Mucha Ayuda (Tutorial Detallado)")
                }
                // Opción 2: Media ayuda
                Button(
                    onClick = { onSelection(TUTORIAL_MEDIUM) },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                ) {
                    Text("Media Ayuda (Solo lo Esencial)")
                }
                // Opción 3: No necesita ayuda
                TextButton(
                    onClick = { onSelection(TUTORIAL_NONE) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("No necesito ayuda")
                }
            }
        }
    )
}

/**
 * Función que realiza la llamada asíncrona a la API de Gemini (IA).
 */
fun solicitarAsistenciaIA(context: Context) {
    Toast.makeText(context, "Solicitando asistencia de la IA...", Toast.LENGTH_SHORT).show()

    // 🔹 Ejecutar Gemini
    CoroutineScope(Dispatchers.IO).launch {
        try {
            val generativeModel = GenerativeModel(
                modelName = "gemini-1.5-flash",
                apiKey = BuildConfig.GEMINI_API_KEY
            )

            // Llamada suspendida
            val response = generativeModel.generateContent("Necesito ayuda con mi aplicación.")


            launch(Dispatchers.Main) {
                val responseText = response.text ?: "No se recibió respuesta de la IA."
                Toast.makeText(context, "Gemini respondió: $responseText", Toast.LENGTH_LONG).show()
            }
        } catch (e: Exception) {
            launch(Dispatchers.Main) {
                // Muestra un error más claro si la clave de API o la conexión fallan
                val errorMsg = if (e.message?.contains("API_KEY") == true) {
                    "Error: Clave de API inválida o faltante."
                } else {
                    "Error al contactar a Gemini: ${e.message}"
                }
                Toast.makeText(context, errorMsg, Toast.LENGTH_LONG).show()
            }
        }
    }
}