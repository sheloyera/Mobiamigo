package com.example.mobiamigo.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.mobiamigo.ui.theme.MobiamigoTheme
@Composable
fun LoginScreen(navController: NavController) {
    var rut by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("MobiAmigo", fontSize = 34.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(40.dp))

        OutlinedTextField(
            value = rut,
            onValueChange = { rut = it },
            label = { Text("Ingresa tu RUT (Opcional)") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = { /* Lógica de inicio de sesión por RUT aquí */ navController.navigate("home") },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text("Ingresar", fontSize = 18.sp)
        }
        Spacer(modifier = Modifier.height(24.dp))
        TextButton(onClick = { navController.navigate("home") }) {
            Text("Continuar sin iniciar sesión", fontSize = 16.sp)
        }
    }
}
@Preview(showBackground = true, device = "id:pixel_4")
@Composable
fun LoginScreenPreview() {
    MobiamigoTheme {
        LoginScreen(rememberNavController())
    }
}
