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
import androidx.compose.runtime.Composable
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
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.compose.material.icons.filled.Person
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.core.app.ActivityCompat


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    selectedApps: List<AppItem>
) {
    val context = LocalContext.current

    Scaffold { paddingValues ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Mostrar las aplicaciones
            items(selectedApps) { app ->
                AppGridItem(app = app, onClick = {
                    AppManager.launchApp(context, app)
                })
            }

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
                val context = LocalContext.current
                var selectedContacts by remember { mutableStateOf(listOf<Pair<String, String>>()) }

                val contactLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.StartActivityForResult()
                ) { result ->
                    if (result.resultCode == Activity.RESULT_OK) {
                        val name = result.data?.getStringExtra("contact_name")
                        val number = result.data?.getStringExtra("contact_number")
                        if (name != null && number != null) {
                            selectedContacts = selectedContacts + (name to number)
                        }
                    }
                }
                Column {
                    // Boton agregar contacto
                    ActionGridItem(
                        icon = Icons.Default.Add,
                        label = "Añadir contactos",
                        description = "Botón para agregar un contacto la pantalla principal",
                        onClick = {
                            val intent = Intent(context, ContactosScreen::class.java)
                            contactLauncher.launch(intent)
                        }
                    )
                    // Mostrar contacto agregado
                    selectedContacts.forEach { (name, number) ->
                        Spacer(modifier = Modifier.height(8.dp))
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
        }
    }
}

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

            // Mostrar la respuesta
            launch(Dispatchers.Main) {
                Toast.makeText(context, "Gemini respondió: ${response.text}", Toast.LENGTH_LONG).show()
            }
        } catch (e: Exception) {
            launch(Dispatchers.Main) {
                Toast.makeText(context, "Error al contactar a Gemini: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
}


