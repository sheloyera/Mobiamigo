package com.example.mobiamigo.screens

import android.content.Intent
import android.content.pm.ResolveInfo
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import androidx.navigation.NavController

// --- Pantalla 3: Agregar Aplicaciones desde el Dispositivo ---
@Composable
fun AddAppScreen(
    navController: NavController,
    onAppsSelected: (List<ResolveInfo>) -> Unit,
    currentlySelected: List<ResolveInfo>
) {
    val context = LocalContext.current
    val packageManager = context.packageManager


    val intent = Intent(Intent.ACTION_MAIN, null).addCategory(Intent.CATEGORY_LAUNCHER)
    val allApps = packageManager.queryIntentActivities(intent, 0)

    val selectedAppsSet = remember { mutableStateOf(currentlySelected.toSet()) }

    Column(modifier = Modifier.fillMaxSize()) {
        // Lista de apps para seleccionar
        LazyColumn(modifier = Modifier.weight(1f)) {
            items(allApps.sortedBy { it.loadLabel(packageManager).toString() }) { app ->
                val appName = app.loadLabel(packageManager).toString()
                val appIcon = app.loadIcon(packageManager).toBitmap().asImageBitmap()
                val isSelected = selectedAppsSet.value.any { it.activityInfo.packageName == app.activityInfo.packageName }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            val currentSet = selectedAppsSet.value.toMutableSet()
                            if (isSelected) {
                                currentSet.removeAll { it.activityInfo.packageName == app.activityInfo.packageName }
                            } else {
                                currentSet.add(app)
                            }
                            selectedAppsSet.value = currentSet
                        }
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        bitmap = appIcon,
                        contentDescription = appName,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(appName, modifier = Modifier.weight(1f), fontSize = 18.sp)
                    Checkbox(
                        checked = isSelected,
                        onCheckedChange = null // La lógica está en el `clickable` del Row
                    )
                }
            }
        }

        Button(
            onClick = { onAppsSelected(selectedAppsSet.value.toList()) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .height(50.dp)
        ) {
            Text("Guardar Selección", fontSize = 18.sp)
        }
    }
}
