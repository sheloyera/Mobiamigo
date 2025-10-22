package com.example.mobiamigo.screens

import android.content.pm.ResolveInfo
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import androidx.navigation.NavController


@Composable
fun HomeScreen(navController: NavController, selectedApps: List<ResolveInfo>) {
    val context = LocalContext.current
    val packageManager = context.packageManager

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)) {
        Text(
            "Mis Aplicaciones",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        if (selectedApps.isEmpty()) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("Aún no tienes aplicaciones", fontSize = 18.sp, textAlign = TextAlign.Center)
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { navController.navigate("add_app") },
                    modifier = Modifier.size(width = 250.dp, height = 60.dp)
                ) {
                    Text("Agregar Aplicación", fontSize = 18.sp)
                }
            }
        } else {
            // Se usa un Box para que el botón flotante se posicione sobre la cuadrícula
            Box(modifier = Modifier.fillMaxSize()) {
                // Cuadrícula con las apps seleccionadas
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2), // Dos columnas
                    contentPadding = PaddingValues(8.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(selectedApps) { app ->
                        val appName = app.loadLabel(packageManager).toString()
                        val appIcon = app.loadIcon(packageManager).toBitmap().asImageBitmap()
                        val launchIntent = packageManager.getLaunchIntentForPackage(app.activityInfo.packageName)

                        Card(
                            modifier = Modifier
                                .aspectRatio(1f)
                                .clickable {
                                    if (launchIntent != null) {
                                        context.startActivity(launchIntent)
                                    }
                                },
                            elevation = CardDefaults.cardElevation(4.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Image(
                                    bitmap = appIcon,
                                    contentDescription = appName,
                                    modifier = Modifier.size(72.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = appName,
                                    fontSize = 16.sp,
                                    textAlign = TextAlign.Center,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
                // Botón flotante para añadir más apps
                FloatingActionButton(
                    onClick = { navController.navigate("add_app") },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(16.dp),
                ) {
                    Icon(Icons.Filled.Add, contentDescription = "Agregar aplicación")
                }
            }
        }
    }
}
