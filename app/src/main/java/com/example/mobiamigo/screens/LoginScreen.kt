package com.example.mobiamigo.screens

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.mobiamigo.R
import com.example.mobiamigo.utils.isValidRut
import com.google.firebase.auth.FirebaseAuth

@Composable
fun LoginScreen(navController: NavController) {

    val auth = FirebaseAuth.getInstance()
    var rut by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by rememberSaveable { mutableStateOf(false) }
    val context = LocalContext.current


    val colorPrimario = Color(0xFF0066CC)


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

    fun rutToEmail(rut: String): String =
        rut.replace(".", "").replace("-", "").lowercase() + "@rut.cl"

    val showRutError = rut.isNotEmpty() && !isValidRut(rut)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Image(
            painter = painterResource(id = R.drawable.logo_mba),
            contentDescription = "Logo MobiAmigo",
            modifier = Modifier.size(100.dp)
        )


        Spacer(modifier = Modifier.height(16.dp))


        Text(
            text = "MobiAmigo",
            fontSize = 40.sp,
            fontWeight = FontWeight.ExtraBold,
            fontFamily = FontFamily.Serif,
            color = colorPrimario
        )

        OutlinedTextField(
            value = rut,
            onValueChange = { rut = formatearRut(it) },
            label = { Text("RUT") },
            placeholder = { Text("12.345.678-9") },
            modifier = Modifier.fillMaxWidth(),
            isError = showRutError,
            singleLine = true,

            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = colorPrimario,
                focusedLabelColor = colorPrimario
            ),
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Characters,
                keyboardType = KeyboardType.Text
            )
        )

        if (showRutError) {
            Text(
                text = "RUT inválido",
                color = Color.Red,
                fontSize = 12.sp,
                modifier = Modifier.align(Alignment.Start).padding(start = 8.dp, top = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))


        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Contraseña") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = colorPrimario,
                focusedLabelColor = colorPrimario
            ),
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                val image = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(imageVector = image, contentDescription = "Ver contraseña", tint = Color.Gray)
                }
            }
        )

        Spacer(modifier = Modifier.height(24.dp))


        Button(
            onClick = {
                if (!isValidRut(rut)) {
                    Toast.makeText(context, "RUT inválido", Toast.LENGTH_LONG).show()
                    return@Button
                }
                val email = rutToEmail(rut)
                auth.signInWithEmailAndPassword(email, password)
                    .addOnSuccessListener {
                        Toast.makeText(context, "¡Bienvenido!", Toast.LENGTH_LONG).show()
                        navController.navigate("home")
                    }
                    .addOnFailureListener {
                        Toast.makeText(context, "Datos incorrectos", Toast.LENGTH_LONG).show()
                    }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(55.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = colorPrimario
            )
        ) {
            Text("Ingresar", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(24.dp))

        TextButton(onClick = { navController.navigate("register") }) {
            Text("Crear cuenta", fontSize = 16.sp, color = colorPrimario)
        }

        TextButton(onClick = { navController.navigate("home") }) {
            Text("Ingresar sin registrarse", fontSize = 16.sp, color = Color.Gray)
        }

        TextButton(onClick = { navController.navigate("home") }) {
            Text("Ingresar como asistente", fontSize = 16.sp, color = Color.Gray)
        }
    }
}