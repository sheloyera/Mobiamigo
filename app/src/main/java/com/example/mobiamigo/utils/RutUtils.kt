package com.example.mobiamigo.utils

fun rutToEmail(rut: String): String {
    return rut.replace(".", "").replace("-", "").lowercase() + "@rut.cl"
}

fun isValidRut(rut: String): Boolean {
    val cleanRut = rut.replace(".", "").replace("-", "").uppercase()
    if (cleanRut.length < 2) return false

    val body = cleanRut.dropLast(1)
    val dv = cleanRut.last()

    if (!body.all { it.isDigit() }) return false

    var sum = 0
    var multiplier = 2
    for (digit in body.reversed()) {
        sum += (digit.toString().toInt() * multiplier)
        multiplier = if (multiplier < 7) multiplier + 1 else 2
    }

    val expectedDv = 11 - (sum % 11)
    val result = when (expectedDv) {
        11 -> '0'
        10 -> 'K'
        else -> expectedDv.toString().first()
    }

    return dv == result
}