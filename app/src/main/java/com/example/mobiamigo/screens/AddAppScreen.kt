package com.example.mobiamigo.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.mobiamigo.data.AppItem
import com.example.mobiamigo.utils.AppManager
import androidx.core.graphics.drawable.toBitmap
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddAppScreen(
    navController: NavController,
    onAppsSelected: (List<AppItem>) -> Unit,
    currentlySelected: List<AppItem>
) {
    val context = LocalContext.current
    val allApps = remember { AppManager.getInstalledApps(context) }
    val selectedApps = remember {
        mutableStateListOf(*currentlySelected.toTypedArray())
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Seleccionar Aplicaciones", style = MaterialTheme.typography.titleLarge) },
                actions = {

                    Button(
                        onClick = {
                            onAppsSelected(selectedApps.toList())
                        },

                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text("Confirmar", style = MaterialTheme.typography.titleMedium)
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(paddingValues)
        ) {
            items(allApps) { app ->
                val isSelected = selectedApps.any { it.packageName == app.packageName }

                AppSelectionItem(
                    app = app,
                    isSelected = isSelected,
                    onToggle = {
                        if (isSelected) {
                            selectedApps.removeAll { it.packageName == app.packageName }
                        } else {
                            selectedApps.add(app)
                        }
                    }
                )
                Divider()
            }
        }
    }
}


@Composable
fun AppSelectionItem(app: AppItem, isSelected: Boolean, onToggle: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle)
            .padding(24.dp)
            .semantics { contentDescription = "Seleccionar ${app.label}" },
        verticalAlignment = Alignment.CenterVertically
    ) {

        app.icon?.let { drawable ->
            Image(
                bitmap = drawable.toBitmap().asImageBitmap(),
                contentDescription = null,
                modifier = Modifier.size(64.dp)
            )
        }

        Spacer(Modifier.width(20.dp))

        Text(
            text = app.label,
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.weight(1f)
        )

        Checkbox(
            checked = isSelected,
            onCheckedChange = { onToggle() },
            modifier = Modifier.size(48.dp)
        )
    }
}