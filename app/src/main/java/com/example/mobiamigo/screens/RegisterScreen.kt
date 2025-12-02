package com.example.mobiamigo.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.mobiamigo.utils.isValidRut
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun RegisterScreen(navController: NavController) {

    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val context = LocalContext.current


    var rut by remember { mutableStateOf("") }
    var nombre by remember { mutableStateOf("") }

    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by rememberSaveable { mutableStateOf(false) }
    var confirmPasswordVisible by rememberSaveable { mutableStateOf(false) }


    fun rutToEmail(rut: String): String =
        rut.replace(".", "").replace("-", "").lowercase() + "@rut.cl"


    fun formatearRut(input: String): String {
        val rutLimpio = input.replace(Regex("[^0-9kK]"), "").uppercase()
        if (rutLimpio.isEmpty()) return ""
        val rutRecortado = if (rutLimpio.length > 9) rutLimpio.substring(0, 9) else rutLimpio
        if (rutRecortado.length == 1) return rutRecortado
        val cuerpo = rutRecortado.substring(0, rutRecortado.length - 1)
        val verificador = rutRecortado.last()
        val cuerpoConPuntos = cuerpo.reversed().chunked(3).joinToString(".").reversed()
        return "$cuerpoConPuntos-$verificador"
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()), // Permite scrollear si el teclado tapa botones
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Crear cuenta", fontSize = 32.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(32.dp))

        Spacer(modifier = Modifier.height(16.dp))

        // 2. CAMPO RUT (Con formato automático y teclado numérico/texto)
        OutlinedTextField(
            value = rut,
            onValueChange = { rut = formatearRut(it) },
            label = { Text("RUT") },
            placeholder = { Text("12.345.678-9") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Characters,
                keyboardType = KeyboardType.Text // Text permite la 'K'
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 3. CAMPO CONTRASEÑA (Con validación de largo y Ojito)
        OutlinedTextField(
            value = password,
            onValueChange = {
                // VALIDACIÓN AL ESCRIBIR: No permite escribir más de 15 caracteres
                if (it.length <= 15) password = it
            },
            label = { Text("Contraseña (6-15 caracteres)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                val image = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(imageVector = image, contentDescription = "Ver contraseña")
                }
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 4. CAMPO CONFIRMAR CONTRASEÑA (Con validación de largo y Ojito propio)
        OutlinedTextField(
            value = confirmPassword,
            onValueChange = {
                if (it.length <= 15) confirmPassword = it
            },
            label = { Text("Confirmar contraseña") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                val image = if (confirmPasswordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                    Icon(imageVector = image, contentDescription = "Ver confirmación")
                }
            },
            isError = confirmPassword.isNotEmpty() && confirmPassword != password // Se pone rojo si no coinciden
        )
        // Mensaje de error si no coinciden
        if (confirmPassword.isNotEmpty() && confirmPassword != password) {
            Text("Las contraseñas no coinciden", color = Color.Red, fontSize = 12.sp, modifier = Modifier.align(Alignment.Start))
        }

        Spacer(modifier = Modifier.height(32.dp))

        // 5. BOTÓN DE REGISTRO
        Button(
            onClick = {
                // --- ZONA DE VALIDACIONES FINALES ---

                // A. Validar campos vacíos
                if (rut.isEmpty() || nombre.isEmpty() || password.isEmpty()) {
                    Toast.makeText(context, "Por favor completa todos los campos", Toast.LENGTH_SHORT).show()
                    return@Button
                }

                // B. Validar RUT real (matemáticamente)
                if (!isValidRut(rut)) {
                    Toast.makeText(context, "El RUT ingresado no es válido", Toast.LENGTH_SHORT).show()
                    return@Button
                }


                if (password.length < 6) {
                    Toast.makeText(context, "La contraseña es muy corta (mínimo 6)", Toast.LENGTH_LONG).show()
                    return@Button
                }


                if (password != confirmPassword) {
                    Toast.makeText(context, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show()
                    return@Button
                }

                // --- SI TODO ESTÁ BIEN, CREAMOS LA CUENTA ---
                val email = rutToEmail(rut)

                auth.createUserWithEmailAndPassword(email, password)
                    .addOnSuccessListener { result ->
                        // Guardamos datos adicionales en Firestore
                        val userId = result.user?.uid
                        val userMap = hashMapOf(
                            "uid" to userId,
                            "nombre" to nombre,
                            "rut" to rut,
                            "email" to email
                        )

                        userId?.let {
                            db.collection("users").document(it).set(userMap)
                                .addOnSuccessListener {
                                    Toast.makeText(context, "¡Cuenta creada con éxito!", Toast.LENGTH_LONG).show()
                                    navController.navigate("home") {
                                        popUpTo("login") { inclusive = true }
                                    }
                                }
                        }
                    }
                    .addOnFailureListener { e ->

                        val mensaje = when {
                            e.message?.contains("already in use") == true -> "Este RUT ya está registrado."
                            else -> "Error al registrar: ${e.localizedMessage}"
                        }
                        Toast.makeText(context, mensaje, Toast.LENGTH_LONG).show()
                    }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text("Registrar", fontSize = 18.sp)
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(onClick = { navController.popBackStack() }) {
            Text("Ya tengo cuenta, volver al ingreso")
        }
    }
}