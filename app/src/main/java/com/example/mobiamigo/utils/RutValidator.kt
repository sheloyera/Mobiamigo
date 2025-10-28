package com.example.mobiamigo.utils
fun esRutValido(rutCompleto: String): Boolean {


    val regex = Regex("^\\d+-[0-9Kk]$")
    if (!rutCompleto.matches(regex)) {
        return false
    }

    val rutLimpio = rutCompleto.replace("-", "").uppercase()

    if (rutLimpio.length < 2) return false

    val cuerpo = rutLimpio.substring(0, rutLimpio.length - 1)
    val dvEsperado = rutLimpio.last()

    var suma = 0
    var multiplicador = 2

    for (i in cuerpo.length - 1 downTo 0) {
        suma += cuerpo[i].toString().toInt() * multiplicador
        multiplicador++
        if (multiplicador == 8) multiplicador = 2
    }

    val resto = suma % 11
    val dvCalculado = when (11 - resto) {
        11 -> '0'
        10 -> 'K'
        else -> (11 - resto).toString().first()
    }


    return dvCalculado == dvEsperado
}

// La función formatRut ya no se usará en el LoginScreen, pero la dejo por si acaso.
fun formatRut(rutLimpio: String): String {
    return rutLimpio
}