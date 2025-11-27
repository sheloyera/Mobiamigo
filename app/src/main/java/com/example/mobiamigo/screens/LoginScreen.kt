package com.example.mobiamigo.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.mobiamigo.utils.isValidRut
import com.google.firebase.auth.FirebaseAuth

@Composable
fun LoginScreen(navController: NavController) {

    val auth = FirebaseAuth.getInstance()
    var rut by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    val context = androidx.compose.ui.platform.LocalContext.current

    fun rutToEmail(rut: String): String =
        rut.replace(".", "").replace("-", "").lowercase() + "@rut.cl"

    val showRutError = rut.isNotEmpty() && !isValidRut(rut)

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
            label = { Text("RUT") },
            modifier = Modifier.fillMaxWidth(),
            isError = showRutError
        )
        if (showRutError) {
            Text(
                text = "RUT inválido",
                color = Color.Red,
                fontSize = 12.sp,
                modifier = Modifier
                    .align(Alignment.Start)
                    .padding(top = 2.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Contraseña") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (!isValidRut(rut)) {
                    Toast.makeText(context, "RUT inválido", Toast.LENGTH_LONG).show()
                    return@Button
                }

                val email = rutToEmail(rut)

                auth.signInWithEmailAndPassword(email, password)
                    .addOnSuccessListener {
                        Toast.makeText(context, "Bienvenido!", Toast.LENGTH_LONG).show()
                        navController.navigate("home")
                    }
                    .addOnFailureListener {
                        Toast.makeText(context, "Datos incorrectos", Toast.LENGTH_LONG).show()
                    }

            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text("Ingresar", fontSize = 18.sp)
        }

        Spacer(modifier = Modifier.height(24.dp))

        TextButton(onClick = { navController.navigate("register") }) {
            Text("Crear cuenta", fontSize = 16.sp)
        }
        Spacer(modifier = Modifier.height(16.dp))

        TextButton(onClick = { navController.navigate("home") }) {
            Text("Ingresar sin registrarse", fontSize = 16.sp)
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(onClick = { navController.navigate("home") }) {
            Text("Ingresar como asistente", fontSize = 16.sp)
        }
    }
}
