package com.example.mobiamigo.screens

import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

// Movemos el data class aquí para asegurar que todos lo vean
data class Evento(val fecha: LocalDate, val hora: String, val descripcion: String, val esRecurrente: Boolean = false)

// Quitamos el @RequiresApi porque ya arreglamos el Gradle, pero si te da error déjalo.
// NOTA: Ahora recibimos 'eventos' como parámetro (MutableList<Evento>)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarioScreen(navController: NavHostController, eventos: MutableList<Evento>) {

    var currentMonth by remember { mutableStateOf(YearMonth.now()) }
    // YA NO creamos la lista aquí con 'remember', usamos la que viene de parámetros

    var showDialog by remember { mutableStateOf(false) }
    var selectedDate by remember { mutableStateOf<LocalDate?>(null) }
    var eventText by remember { mutableStateOf("") }
    var eventTime by remember { mutableStateOf("") }
    var esRecurrente by remember { mutableStateOf(false) }

    val context = LocalContext.current // Para mostrar mensajes (Toasts)

    val onDateClick: (LocalDate) -> Unit = { date ->
        selectedDate = date
        showDialog = true
    }

    Column(modifier = Modifier.padding(16.dp)) {
        HeaderCalendario(currentMonth, onPrevMonth = { currentMonth = currentMonth.minusMonths(1) }, onNextMonth = { currentMonth = currentMonth.plusMonths(1) })
        DiasDeLaSemana()
        CuerpoCalendario(currentMonth, eventos, onDateClick)

        Spacer(modifier = Modifier.height(16.dp))

        Text("Eventos Agendados:", style = MaterialTheme.typography.titleMedium)

        LazyColumn {
            val eventosMostrados = eventos.filter {
                // Filtramos para mostrar en la lista de abajo
                !it.esRecurrente && it.fecha.year == currentMonth.year && it.fecha.month == currentMonth.month
            } + eventos.filter { it.esRecurrente }

            items(eventosMostrados.sortedBy { it.fecha }.sortedBy { it.hora }, key = { it.hashCode() }) { evento ->
                val textoFecha = if (evento.esRecurrente) "Cada ${evento.fecha.dayOfWeek.getDisplayName(java.time.format.TextStyle.FULL, Locale("es", "ES"))}"
                else evento.fecha.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))

                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "- $textoFecha a las ${evento.hora}: ${evento.descripcion}",
                        modifier = Modifier.weight(1f)
                    )
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Eliminar Evento",
                        modifier = Modifier
                            .clickable { eventos.remove(evento) }
                            .padding(start = 8.dp)
                    )
                }
            }
        }
    }

    if (showDialog && selectedDate != null) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Agendar para ${selectedDate?.format(DateTimeFormatter.ofPattern("dd MMMM yyyy"))}") },
            text = {
                Column {
                    TextField(
                        value = eventText,
                        onValueChange = { eventText = it },
                        label = { Text("Descripción") }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    TextField(
                        value = eventTime,
                        onValueChange = { newTime ->
                            // Tu lógica original de filtrado de hora
                            val filteredTime = newTime.filter { it.isDigit() }
                            if (filteredTime.length <= 4) {
                                val timeWithColon = when (filteredTime.length) {
                                    in 1..2 -> filteredTime
                                    else -> filteredTime.substring(0, 2) + ":" + filteredTime.substring(2)
                                }
                                eventTime = timeWithColon
                            }
                        },
                        label = { Text("Hora (ej. 14:30)") },
                        placeholder = { Text("HH:MM") }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = esRecurrente,
                            onCheckedChange = { esRecurrente = it }
                        )
                        Text("Repetir cada semana")
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (eventText.isNotBlank() && eventTime.length == 5) { // Simple check de longitud hh:mm

                        try {
                            // --- VALIDACIÓN DE FECHA Y HORA PASADA ---
                            val horaIngresada = LocalTime.parse(eventTime) // Puede fallar si el formato está mal
                            val fechaHoraEvento = LocalDateTime.of(selectedDate, horaIngresada)
                            val ahora = LocalDateTime.now()

                            if (fechaHoraEvento.isBefore(ahora)) {
                                // ERROR: Es pasado
                                Toast.makeText(context, "No puedes agendar en el pasado", Toast.LENGTH_SHORT).show()
                            } else {
                                // ÉXITO: Guardamos
                                eventos.add(Evento(selectedDate!!, eventTime, eventText, esRecurrente))

                                // Limpieza
                                eventText = ""
                                eventTime = ""
                                esRecurrente = false
                                showDialog = false
                                Toast.makeText(context, "Evento guardado", Toast.LENGTH_SHORT).show()
                            }
                        } catch (e: Exception) {
                            Toast.makeText(context, "Hora inválida. Usa formato HH:MM", Toast.LENGTH_SHORT).show()
                        }

                    } else {
                        Toast.makeText(context, "Completa los campos", Toast.LENGTH_SHORT).show()
                    }
                }) {
                    Text("Guardar")
                }
            },
            dismissButton = {
                Button(onClick = { showDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

// --- Componentes auxiliares (Header y Dias) se mantienen casi igual ---

@Composable
fun HeaderCalendario(currentMonth: YearMonth, onPrevMonth: () -> Unit, onNextMonth: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Button(onClick = onPrevMonth) { Text("<") }
        Text(
            text = currentMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale("es", "ES"))).replaceFirstChar { it.uppercase() },
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
        Button(onClick = onNextMonth) { Text(">") }
    }
}

@Composable
fun DiasDeLaSemana(modifier: Modifier = Modifier) {
    Row(modifier = modifier.fillMaxWidth()) {
        val dias = listOf("L", "M", "X", "J", "V", "S", "D")
        for (dia in dias) {
            Text(text = dia, modifier = Modifier.weight(1f), textAlign = TextAlign.Center, fontWeight = FontWeight.Bold)
        }
    }
}

// --- LÓGICA DE PINTADO (Modificada para corregir lo amarillo) ---

@Composable
fun CuerpoCalendario(yearMonth: YearMonth, eventos: List<Evento>, onDateClick: (LocalDate) -> Unit) {
    val daysInMonth = yearMonth.lengthOfMonth()
    val firstOfMonth = yearMonth.atDay(1)
    val prevMonth = yearMonth.minusMonths(1)
    val daysInPrevMonth = prevMonth.lengthOfMonth()
    val firstDayOfWeek = firstOfMonth.dayOfWeek.value
    val totalSlots = (firstDayOfWeek - 1 + daysInMonth + 6) / 7 * 7

    Column {
        val dayCells = (1..totalSlots).map { slotIndex ->
            val dayOfMonth: Int
            val date: LocalDate
            val isCurrentMonth: Boolean

            if (slotIndex < firstDayOfWeek) {
                dayOfMonth = daysInPrevMonth - (firstDayOfWeek - slotIndex - 1)
                date = prevMonth.atDay(dayOfMonth)
                isCurrentMonth = false
            } else if (slotIndex - firstDayOfWeek < daysInMonth) {
                dayOfMonth = slotIndex - firstDayOfWeek + 1
                date = yearMonth.atDay(dayOfMonth)
                isCurrentMonth = true
            } else {
                dayOfMonth = slotIndex - firstDayOfWeek - daysInMonth + 1
                date = yearMonth.plusMonths(1).atDay(dayOfMonth)
                isCurrentMonth = false
            }
            Pair(date, isCurrentMonth)
        }

        dayCells.chunked(7).forEach { week ->
            Row(Modifier.fillMaxWidth()) {
                week.forEach { (date, isCurrentMonth) ->

                    // --- AQUÍ ESTÁ EL CAMBIO DE VALIDACIÓN VISUAL ---
                    val hasEvent = eventos.any { evento ->
                        if (!evento.esRecurrente) {
                            // Caso normal: coincidencia exacta
                            evento.fecha == date
                        } else {
                            // Caso recurrente:
                            // 1. Coincide el día de la semana (ej. Lunes)
                            // 2. Y ADEMÁS la fecha del calendario es mayor o igual a la fecha de creación
                            (evento.fecha.dayOfWeek == date.dayOfWeek && !date.isBefore(evento.fecha))
                        }
                    }
                    // ------------------------------------------------

                    CeldaDia(date, isCurrentMonth, hasEvent, onDateClick, Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
fun CeldaDia(date: LocalDate, isCurrentMonth: Boolean, hasEvent: Boolean, onDateClick: (LocalDate) -> Unit, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
            .aspectRatio(1f)
            .padding(2.dp)
            .clickable { onDateClick(date) }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(if (hasEvent) Color.Yellow.copy(alpha = 0.5f) else Color.Transparent),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = date.dayOfMonth.toString(),
                color = if (isCurrentMonth) Color.Black else Color.Gray,
                textAlign = TextAlign.Center
            )
        }
    }
}