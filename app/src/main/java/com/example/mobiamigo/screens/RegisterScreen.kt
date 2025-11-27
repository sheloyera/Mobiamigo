package com.example.mobiamigo.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.mobiamigo.utils.isValidRut
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlin.text.lowercase
import kotlin.text.replace
import kotlin.to

@Composable
fun RegisterScreen(navController: NavController) {

    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val context = LocalContext.current

    var rut by remember { mutableStateOf("") }
    var pass by remember { mutableStateOf("") }
    var pass2 by remember { mutableStateOf("") }

    fun rutToEmail(rut: String): String =
        rut.replace(".", "").replace("-", "").lowercase() + "@rut.cl"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text("Crear cuenta", fontSize = 28.sp)

        Spacer(modifier = Modifier.height(20.dp))

        OutlinedTextField(
            value = rut,
            onValueChange = { rut = it },
            label = { Text("RUT") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = pass,
            onValueChange = { pass = it },
            label = { Text("Contraseña") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = pass2,
            onValueChange = { pass2 = it },
            label = { Text("Confirmar contraseña") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = {

                if (!isValidRut(rut)) {
                    Toast.makeText(context, "RUT inválido", Toast.LENGTH_LONG).show()
                    return@Button
                }

                if (pass != pass2) {
                    Toast.makeText(context, "Las contraseñas no coinciden", Toast.LENGTH_LONG).show()
                    return@Button
                }

                val email = rutToEmail(rut)

                auth.createUserWithEmailAndPassword(email, pass)
                    .addOnSuccessListener { result ->
                        val uid = result.user!!.uid


                        db.collection("usuarios").document(uid).set(
                            mapOf("rut" to rut)
                        )

                        Toast.makeText(context, "Cuenta creada", Toast.LENGTH_LONG).show()
                        navController.navigate("login")
                    }
                    .addOnFailureListener {
                        Toast.makeText(context, "Error: ${it.message}", Toast.LENGTH_LONG).show()
                    }

            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text("Registrar")
        }
    }
}




