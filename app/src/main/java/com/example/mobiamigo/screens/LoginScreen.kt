package com.example.mobiamigo.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.mobiamigo.ui.theme.MobiamigoTheme
import com.example.mobiamigo.utils.esRutValido

@Composable
fun LoginScreen(navController: NavController) {
    var rutInput by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }

    val maxRutLength = 10

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
            value = rutInput,
            onValueChange = { newValue ->

                val filteredValue = newValue.filter {
                    it.isDigit() || it == '-' || it.equals('k', ignoreCase = true)
                }.uppercase()


                rutInput = if (filteredValue.length <= maxRutLength) filteredValue else filteredValue.substring(0, maxRutLength)

                errorMessage = ""
            },
            label = { Text("Ingresa tu RUT (ej: 12345678-K)") },

            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
            modifier = Modifier.fillMaxWidth()
        )
        if (errorMessage.isNotEmpty()) {
            Text(errorMessage, color = Color.Red, modifier = Modifier.padding(top = 8.dp))
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (rutInput.isEmpty()) {
                    navController.navigate("home")
                } else if (esRutValido(rutInput)) {

                    errorMessage = " "

                    navController.navigate("home")
                } else {
                    errorMessage = "RUT Inválido o Debe incluir el guion (ej: 12345678-K)."
                }
            },
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