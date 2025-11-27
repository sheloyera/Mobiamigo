package com.example.mobiamigo.screens

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.navigation.NavController

import com.example.mobiamigo.BuildConfig
import com.example.mobiamigo.data.AppItem
import com.example.mobiamigo.utils.AppManager
import com.google.ai.client.generativeai.GenerativeModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

const val TUTORIAL_NONE = "none"
const val TUTORIAL_MEDIUM = "medium"
const val TUTORIAL_FULL = "full"
const val TUTORIAL_COMPLETED = "completed"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    selectedApps: List<AppItem>
) {
    val context = LocalContext.current


    val sharedPref = remember { context.getSharedPreferences("MobiAmigoPrefs", Context.MODE_PRIVATE) }


    var isTutorialCompleted by remember {
        mutableStateOf(sharedPref.getBoolean("is_tutorial_completed", false))
    }


    var showShowcase by remember { mutableStateOf(false) }
    var currentTutorialLevel by rememberSaveable { mutableStateOf<String?>(null) }
    val showcaseState = rememberShowcaseState()

    if (!isTutorialCompleted && currentTutorialLevel == null) {
        TutorialSelectionDialog(
            onSelection = { level ->
                currentTutorialLevel = level
                if (level == TUTORIAL_FULL || level == TUTORIAL_MEDIUM) {
                    showShowcase = true
                    if (level == TUTORIAL_MEDIUM) {
                        showcaseState.currentStep = 2
                    }
                } else {

                    sharedPref.edit().putBoolean("is_tutorial_completed", true).apply()
                    isTutorialCompleted = true
                    currentTutorialLevel = TUTORIAL_COMPLETED
                }
            }
        )
    }


    var selectedContacts by rememberSaveable { mutableStateOf(listOf<Pair<String, String>>()) }
    val contactLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val name = result.data?.getStringExtra("contact_name")
            val number = result.data?.getStringExtra("contact_number")
            if (name != null && number != null) selectedContacts = selectedContacts + (name to number)
        }
    }


    SimpleShowcase(
        state = showcaseState,
        isVisible = showShowcase,
        onFinished = {
            showShowcase = false
            currentTutorialLevel = TUTORIAL_COMPLETED


            sharedPref.edit().putBoolean("is_tutorial_completed", true).apply()
            isTutorialCompleted = true
        }
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Scaffold(
                topBar = {
                    CenterAlignedTopAppBar(
                        title = { Text("MobiAmigo") },

                        actions = {
                            IconButton(onClick = {

                                isTutorialCompleted = false
                                currentTutorialLevel = null
                                showShowcase = false
                                showcaseState.currentStep = 0
                            }) {
                                Icon(
                                    imageVector = Icons.Default.Help,
                                    contentDescription = "Repetir Tutorial"
                                )
                            }
                        }
                    )
                }
            ) { paddingValues ->
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.fillMaxSize().padding(paddingValues),
                    contentPadding = PaddingValues(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(selectedApps) { app ->
                        AppGridItem(app = app, onClick = { AppManager.launchApp(context, app) })
                    }


                    item {
                        ActionGridItem(
                            icon = Icons.Default.Add,
                            label = "Añadir Apps",
                            description = "Añadir apps",
                            modifier = Modifier.showcaseTarget(showcaseState, 0),
                            onClick = { navController.navigate("add_app") }
                        )
                    }


                    item {
                        ActionGridItem(
                            icon = Icons.Default.Add,
                            label = "Añadir contacto ",
                            description = "Boton para añadir contactos",
                            modifier = Modifier.showcaseTarget(showcaseState, 1),
                            onClick = {
                                val intent = Intent(context, ContactosScreen::class.java)
                                contactLauncher.launch(intent)
                            }
                        )
                    }


                    item {
                        ActionGridItem(
                            icon = Icons.Default.SupportAgent,
                            label = "Asistencia/IA",
                            description = "Ayuda IA",
                            modifier = Modifier.showcaseTarget(showcaseState, 2),
                            onClick = { solicitarAsistenciaIA(context) }
                        )
                    }

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
                                    Toast.makeText(context, "Se requiere permiso para llamar", Toast.LENGTH_SHORT).show()
                                }
                            }
                        )
                    }
                }
            }


            if (showShowcase && showcaseState.currentStep != -1) {
                val targetRect = showcaseState.targets[showcaseState.currentStep]
                if (targetRect != null) {
                    val message = when(showcaseState.currentStep) {
                        0 -> "Toca aquí para agregar tus aplicaciones favoritas."
                        1 -> "Aquí puedes añadir contactos de emergencia."
                        2 -> "¡Lo más importante! Toca aquí para pedir ayuda a la IA."
                        else -> ""
                    }
                    TutorialTextBox(
                        text = message,
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .offset { IntOffset(16, targetRect.bottom.toInt() + 20) }
                    )
                }
            }
        }
    }
}
@Composable
fun TutorialTextBox(text: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.width(250.dp).padding(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = text,
                color = Color.Black,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "(Toca cualquier parte para continuar)",
                color = Color.Gray,
                fontSize = 12.sp
            )
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
fun ActionGridItem(
    icon: ImageVector,
    label: String,
    description: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        onClick = onClick,
        modifier = modifier
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
        onDismissRequest = { /* No cerrar */ },
        title = { Text("Nivel de Ayuda Requerido") },
        text = {
            Text("¡Hola! Para tu primera vez, ¿cuánta guía necesitas para usar MobiAmigo?")
        },
        confirmButton = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Button(
                    onClick = { onSelection(TUTORIAL_FULL) },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                ) {
                    Text("Mucha Ayuda (Tutorial Detallado)")
                }
                Button(
                    onClick = { onSelection(TUTORIAL_MEDIUM) },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                ) {
                    Text("Ayuda media  (Solo lo Esencial)")
                }
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

fun solicitarAsistenciaIA(context: Context) {
    Toast.makeText(context, "Solicitando asistencia de la IA...", Toast.LENGTH_SHORT).show()
    CoroutineScope(Dispatchers.IO).launch {
        try {
            val generativeModel = GenerativeModel(
                modelName = "gemini-1.5-flash",
                apiKey = BuildConfig.GEMINI_API_KEY
            )
            val response = generativeModel.generateContent("Necesito ayuda con mi aplicación.")
            launch(Dispatchers.Main) {
                val responseText = response.text ?: "No se recibió respuesta."
                Toast.makeText(context, "Gemini: $responseText", Toast.LENGTH_LONG).show()
            }
        } catch (e: Exception) {
            launch(Dispatchers.Main) {
                Toast.makeText(context, "Error: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            }
        }
    }
}

class ShowcaseState {
    var currentStep by mutableStateOf(0)
    val targets = mutableStateMapOf<Int, Rect>()

    fun next() { currentStep++ }
    fun finish() { currentStep = -1 }
}

@Composable
fun rememberShowcaseState() = remember { ShowcaseState() }

fun Modifier.showcaseTarget(state: ShowcaseState, index: Int): Modifier = this.onGloballyPositioned { coordinates ->
    state.targets[index] = coordinates.boundsInRoot()
}

@Composable
fun SimpleShowcase(
    state: ShowcaseState,
    isVisible: Boolean,
    onFinished: () -> Unit,
    content: @Composable () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        content()

        if (isVisible && state.currentStep != -1) {
            val currentTarget = state.targets[state.currentStep]

            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .zIndex(10f)
                    .graphicsLayer(alpha = 0.99f)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        if (state.targets.containsKey(state.currentStep + 1)) {
                            state.next()
                        } else {
                            onFinished()
                        }
                    }
            ) {
                drawRect(Color.Black.copy(alpha = 0.8f))

                if (currentTarget != null) {
                    drawCircle(
                        center = currentTarget.center,
                        radius = currentTarget.width / 1.5f,
                        color = Color.Transparent,
                        blendMode = BlendMode.Clear
                    )
                }
            }
        }
    }
}