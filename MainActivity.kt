package com.example.ejer_bd_p

import android.app.DatePickerDialog
import android.content.ContentValues
import android.database.sqlite.SQLiteConstraintException
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import java.util.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            InspeccionVehicularApp()
        }
    }
}

// Validaciones
fun isValidCI(ci: String): Boolean = ci.matches(Regex("^[0-9]{8,12}$"))
fun isValidNombre(nombre: String): Boolean = nombre.matches(Regex("^[A-Za-zÁÉÍÓÚáéíóúÑñ ]+$"))
fun isValidTelefono(telefono: String): Boolean = telefono.matches(Regex("^\\+?[0-9]{7,15}$"))
fun isValidPlaca(placa: String): Boolean = placa.matches(Regex("^[A-Z]{3}-[0-9]{3}$"))
fun isValidMarcaModelo(text: String): Boolean = text.matches(Regex("^[A-Za-z ]+$"))
fun isValidAnio(anio: String): Boolean = anio.toIntOrNull()?.let { it in 1900..2025 } ?: false
fun isValidIdInspeccion(id: String): Boolean = id.toIntOrNull()?.let { it > 0 } ?: false
fun isValidFecha(fecha: String): Boolean = fecha.matches(Regex("^[0-9]{4}-[0-9]{1,2}-[0-9]{1,2}$"))
fun isValidResultado(resultado: String): Boolean = resultado.length >= 5

// Verificar existencia de técnico y automóvil
fun existsTecnico(db: android.database.sqlite.SQLiteDatabase, ci: String): Boolean {
    val cursor = db.rawQuery("SELECT 1 FROM tecnico WHERE ci=?", arrayOf(ci))
    val exists = cursor.moveToFirst()
    cursor.close()
    return exists
}

fun existsAutomovil(db: android.database.sqlite.SQLiteDatabase, placa: String): Boolean {
    val cursor = db.rawQuery("SELECT 1 FROM automovil WHERE placa=?", arrayOf(placa))
    val exists = cursor.moveToFirst()
    cursor.close()
    return exists
}

@Composable
fun InspeccionVehicularApp() {
    val context = LocalContext.current
    val db = remember { BdSqlite(context).writableDatabase }
    val activity = (context as? ComponentActivity)

    var showWelcomeScreen by remember { mutableStateOf(true) }

    val primaryGreen = Color(0xFF2E7D32)
    val secondaryGreen = Color(0xFF4CAF50)
    val lightGreen = Color(0xFFE8F5E9)
    val accentGreen = Color(0xFF66BB6A)

    if (showWelcomeScreen) {
        WelcomeScreen(
            onStartClick = { showWelcomeScreen = false },
            primaryGreen = primaryGreen,
            secondaryGreen = secondaryGreen,
            lightGreen = lightGreen
        )
    } else {
        MainInspectionScreen(
            db = db,
            primaryGreen = primaryGreen,
            secondaryGreen = secondaryGreen,
            lightGreen = lightGreen,
            accentGreen = accentGreen,
            onExitClick = { activity?.finish() }
        )
    }
}

@Composable
fun WelcomeScreen(
    onStartClick: () -> Unit,
    primaryGreen: Color,
    secondaryGreen: Color,
    lightGreen: Color
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        AsyncImage(
            model = R.drawable.fondo_auto,
            contentDescription = "Background image of a vehicle inspection",
            modifier = Modifier
                .fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.4f))
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            Text(
                text = "Bienvenidos a",
                fontSize = 28.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .background(primaryGreen.copy(alpha = 0.7f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )
            Text(
                text = "Inspección Técnica Vehicular",
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .background(secondaryGreen.copy(alpha = 0.7f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .padding(bottom = 16.dp)
            )
            Text(
                text = "Registra y gestiona inspecciones de manera eficiente",
                fontSize = 18.sp,
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .background(primaryGreen.copy(alpha = 0.7f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .padding(bottom = 32.dp)
            )
            Button(
                onClick = onStartClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = secondaryGreen,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(56.dp)
            ) {
                Text(
                    text = "Comenzar",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
fun MainInspectionScreen(
    db: android.database.sqlite.SQLiteDatabase,
    primaryGreen: Color,
    secondaryGreen: Color,
    lightGreen: Color,
    accentGreen: Color,
    onExitClick: () -> Unit
) {
    val context = LocalContext.current

    var ci by remember { mutableStateOf("") }
    var nombre by remember { mutableStateOf("") }
    var telefono by remember { mutableStateOf("") }

    var placa by remember { mutableStateOf("") }
    var marca by remember { mutableStateOf("") }
    var modelo by remember { mutableStateOf("") }
    var anio by remember { mutableStateOf("") }

    var idInspeccion by remember { mutableStateOf("") }
    var fecha by remember { mutableStateOf("") }
    var detalles by remember { mutableStateOf("") }
    var resultado by remember { mutableStateOf("") }

    val calendar = Calendar.getInstance()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(lightGreen)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Inspección Técnica Vehicular",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = primaryGreen,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        CardSection(
            title = "Técnico",
            primaryGreen = primaryGreen,
            secondaryGreen = secondaryGreen
        ) {
            CustomTextField(
                label = "CI",
                value = ci,
                keyboardType = KeyboardType.Number,
                onValueChange = { if (it.all { char -> char.isDigit() }) ci = it },
                secondaryGreen = secondaryGreen
            )
            CustomTextField(
                label = "Nombre",
                value = nombre,
                onValueChange = { nombre = it },
                secondaryGreen = secondaryGreen
            )
            CustomTextField(
                label = "Teléfono",
                value = telefono,
                keyboardType = KeyboardType.Phone,
                onValueChange = { if (it.matches(Regex("^[0-9+]*$"))) telefono = it },
                secondaryGreen = secondaryGreen
            )

            RowButtons(
                onAdd = {
                    when {
                        ci.isBlank() || nombre.isBlank() || telefono.isBlank() ->
                            toast(context, "Complete todos los campos del técnico")
                        !isValidCI(ci) ->
                            toast(context, "CI debe tener 8-12 dígitos")
                        !isValidNombre(nombre) ->
                            toast(context, "Nombre solo debe contener letras y espacios")
                        !isValidTelefono(telefono) ->
                            toast(context, "Teléfono debe contener 7-15 dígitos, opcionalmente con +")
                        else -> {
                            try {
                                val values = ContentValues().apply {
                                    put("ci", ci)
                                    put("nombre", nombre)
                                    put("telefono", telefono)
                                }
                                val result = db.insert("tecnico", null, values)
                                toast(context, if (result != -1L) "Técnico agregado" else "Error al agregar técnico")
                            } catch (e: SQLiteConstraintException) {
                                toast(context, "El CI ya está registrado")
                            }
                        }
                    }
                },
                onSearch = {
                    if (!isValidCI(ci)) {
                        toast(context, "Ingrese un CI válido (8-12 dígitos)")
                        return@RowButtons
                    }
                    val cursor = db.rawQuery("SELECT nombre, telefono FROM tecnico WHERE ci=?", arrayOf(ci))
                    if (cursor.moveToFirst()) {
                        nombre = cursor.getString(0)
                        telefono = cursor.getString(1)
                        toast(context, "Técnico encontrado")
                    } else toast(context, "No encontrado")
                    cursor.close()
                },
                onUpdate = {
                    when {
                        ci.isBlank() || nombre.isBlank() || telefono.isBlank() ->
                            toast(context, "Complete todos los campos del técnico")
                        !isValidCI(ci) ->
                            toast(context, "CI debe tener 8-12 dígitos")
                        !isValidNombre(nombre) ->
                            toast(context, "Nombre solo debe contener letras y espacios")
                        !isValidTelefono(telefono) ->
                            toast(context, "Teléfono debe contener 7-15 dígitos, opcionalmente con +")
                        else -> {
                            val values = ContentValues().apply {
                                put("nombre", nombre)
                                put("telefono", telefono)
                            }
                            val result = db.update("tecnico", values, "ci=?", arrayOf(ci))
                            toast(context, if (result > 0) "Actualizado" else "No se encontró CI")
                        }
                    }
                },
                onDelete = {
                    if (!isValidCI(ci)) {
                        toast(context, "Ingrese un CI válido (8-12 dígitos)")
                        return@RowButtons
                    }
                    val result = db.delete("tecnico", "ci=?", arrayOf(ci))
                    toast(context, if (result > 0) "Eliminado" else "No se encontró CI")
                },
                secondaryGreen = secondaryGreen,
                accentGreen = accentGreen
            )
        }

        CardSection(
            title = "Automóvil",
            primaryGreen = primaryGreen,
            secondaryGreen = secondaryGreen
        ) {
            CustomTextField(
                label = "Placa",
                value = placa,
                onValueChange = { placa = it.uppercase() },
                secondaryGreen = secondaryGreen
            )
            CustomTextField(
                label = "Marca",
                value = marca,
                onValueChange = { marca = it },
                secondaryGreen = secondaryGreen
            )
            CustomTextField(
                label = "Modelo",
                value = modelo,
                onValueChange = { modelo = it },
                secondaryGreen = secondaryGreen
            )
            CustomTextField(
                label = "Año",
                value = anio,
                keyboardType = KeyboardType.Number,
                onValueChange = { if (it.all { char -> char.isDigit() } && it.length <= 4) anio = it },
                secondaryGreen = secondaryGreen
            )

            RowButtons(
                onAdd = {
                    when {
                        placa.isBlank() || marca.isBlank() || modelo.isBlank() || anio.isBlank() ->
                            toast(context, "Complete todos los campos del automóvil")
                        !isValidPlaca(placa) ->
                            toast(context, "Placa debe tener formato ABC-123")
                        !isValidMarcaModelo(marca) ->
                            toast(context, "Marca solo debe contener letras y espacios")
                        !isValidMarcaModelo(modelo) ->
                            toast(context, "Modelo solo debe contener letras y espacios")
                        !isValidAnio(anio) ->
                            toast(context, "El año debe ser un número entre 1900 y 2025")
                        else -> {
                            try {
                                val values = ContentValues().apply {
                                    put("placa", placa)
                                    put("marca", marca)
                                    put("modelo", modelo)
                                    put("anio", anio.toInt())
                                }
                                val result = db.insert("automovil", null, values)
                                toast(context, if (result != -1L) "Auto registrado" else "Error al registrar automóvil")
                            } catch (e: SQLiteConstraintException) {
                                toast(context, "La placa ya está registrada")
                            }
                        }
                    }
                },
                onSearch = {
                    if (!isValidPlaca(placa)) {
                        toast(context, "Ingrese una placa válida (formato ABC-123)")
                        return@RowButtons
                    }
                    val cursor = db.rawQuery("SELECT marca, modelo, anio FROM automovil WHERE placa=?", arrayOf(placa))
                    if (cursor.moveToFirst()) {
                        marca = cursor.getString(0)
                        modelo = cursor.getString(1)
                        anio = cursor.getString(2)
                        toast(context, "Auto encontrado")
                    } else {
                        toast(context, "No encontrado")
                    }
                    cursor.close()
                },
                onUpdate = {
                    when {
                        placa.isBlank() || marca.isBlank() || modelo.isBlank() || anio.isBlank() ->
                            toast(context, "Complete todos los campos del automóvil")
                        !isValidPlaca(placa) ->
                            toast(context, "Placa debe tener formato ABC-123")
                        !isValidMarcaModelo(marca) ->
                            toast(context, "Marca solo debe contener letras y espacios")
                        !isValidMarcaModelo(modelo) ->
                            toast(context, "Modelo solo debe contener letras y espacios")
                        !isValidAnio(anio) ->
                            toast(context, "El año debe ser un número entre 1900 y 2025")
                        else -> {
                            val values = ContentValues().apply {
                                put("marca", marca)
                                put("modelo", modelo)
                                put("anio", anio.toInt())
                            }
                            val result = db.update("automovil", values, "placa=?", arrayOf(placa))
                            toast(context, if (result > 0) "Actualizado" else "No se encontró placa")
                        }
                    }
                },
                onDelete = {
                    if (!isValidPlaca(placa)) {
                        toast(context, "Ingrese una placa válida (formato ABC-123)")
                        return@RowButtons
                    }
                    val result = db.delete("automovil", "placa=?", arrayOf(placa))
                    toast(context, if (result > 0) "Eliminado" else "No se encontró placa")
                },
                secondaryGreen = secondaryGreen,
                accentGreen = accentGreen
            )
        }

        CardSection(
            title = "Inspección",
            primaryGreen = primaryGreen,
            secondaryGreen = secondaryGreen
        ) {
            CustomTextField(
                label = "ID Inspección",
                value = idInspeccion,
                keyboardType = KeyboardType.Number,
                onValueChange = { if (it.all { char -> char.isDigit() }) idInspeccion = it },
                secondaryGreen = secondaryGreen
            )

            Button(
                onClick = {
                    val dpd = DatePickerDialog(
                        context,
                        { _, year, month, dayOfMonth ->
                            fecha = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth)
                        },
                        calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH)
                    )
                    dpd.show()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = secondaryGreen,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp)
            ) {
                Text(
                    text = if (fecha.isEmpty()) "Seleccionar fecha" else "Fecha: $fecha",
                    fontSize = 16.sp
                )
            }

            CustomTextField(
                label = "CI Técnico",
                value = ci,
                keyboardType = KeyboardType.Number,
                onValueChange = { if (it.all { char -> char.isDigit() }) ci = it },
                secondaryGreen = secondaryGreen
            )

            CustomTextField(
                label = "Placa Automóvil",
                value = placa,
                onValueChange = { placa = it.uppercase() },
                secondaryGreen = secondaryGreen
            )

            CustomTextField(
                label = "Detalles",
                value = detalles,
                onValueChange = { detalles = it },
                secondaryGreen = secondaryGreen
            )

            CustomTextField(
                label = "Resultado",
                value = resultado,
                onValueChange = { resultado = it },
                secondaryGreen = secondaryGreen
            )

            RowButtons(
                onAdd = {
                    when {
                        ci.isBlank() || placa.isBlank() || fecha.isBlank() || resultado.isBlank() ->
                            toast(context, "Complete CI, Placa, Fecha y Resultado")
                        !isValidCI(ci) ->
                            toast(context, "CI Técnico debe tener 8-12 dígitos")
                        !isValidPlaca(placa) ->
                            toast(context, "Placa debe tener formato ABC-123")
                        !isValidFecha(fecha) ->
                            toast(context, "Seleccione una fecha válida (AAAA-MM-DD)")
                        !isValidResultado(resultado) ->
                            toast(context, "Resultado debe tener al menos 5 caracteres")
                        !existsTecnico(db, ci) ->
                            toast(context, "El técnico no está registrado")
                        !existsAutomovil(db, placa) ->
                            toast(context, "El automóvil no está registrado")
                        else -> {
                            try {
                                val values = ContentValues().apply {
                                    put("id_tecnico", ci)
                                    put("id_automovil", placa)
                                    put("fecha", fecha)
                                    put("detalles", detalles)
                                    put("resultado", resultado)
                                }
                                val result = db.insert("inspecciona", null, values)
                                toast(context, if (result != -1L) "Inspección registrada" else "Error al registrar inspección")
                            } catch (e: SQLiteConstraintException) {
                                toast(context, "Error: Verifique que el técnico y el automóvil estén registrados")
                            }
                        }
                    }
                },
                onSearch = {
                    if (!isValidIdInspeccion(idInspeccion)) {
                        toast(context, "Ingrese un ID de inspección válido (número positivo)")
                        return@RowButtons
                    }
                    val cursor = db.rawQuery(
                        "SELECT id_tecnico, id_automovil, fecha, detalles, resultado FROM inspecciona WHERE id_inspeccion=?",
                        arrayOf(idInspeccion)
                    )
                    if (cursor.moveToFirst()) {
                        ci = cursor.getString(0)
                        placa = cursor.getString(1)
                        fecha = cursor.getString(2)
                        detalles = cursor.getString(3) ?: ""
                        resultado = cursor.getString(4)
                        toast(context, "Inspección encontrada")
                    } else {
                        toast(context, "No se encontró inspección")
                    }
                    cursor.close()
                },
                onUpdate = {
                    when {
                        idInspeccion.isBlank() ->
                            toast(context, "Ingrese ID de inspección")
                        !isValidIdInspeccion(idInspeccion) ->
                            toast(context, "Ingrese un ID de inspección válido (número positivo)")
                        !isValidCI(ci) ->
                            toast(context, "CI Técnico debe ser válido")
                        !isValidPlaca(placa) ->
                            toast(context, "Placa debe tener formato ABC-123")
                        !isValidFecha(fecha) ->
                            toast(context, "Seleccione una fecha válida (AAAA-MM-DD)")
                        !isValidResultado(resultado) ->
                            toast(context, "Resultado debe tener al menos 5 caracteres")
                        !existsTecnico(db, ci) ->
                            toast(context, "El técnico no está registrado")
                        !existsAutomovil(db, placa) ->
                            toast(context, "El automóvil no está registrado")
                        else -> {
                            val values = ContentValues().apply {
                                put("id_tecnico", ci)
                                put("id_automovil", placa)
                                put("fecha", fecha)
                                put("detalles", detalles)
                                put("resultado", resultado)
                            }
                            val result = db.update(
                                "inspecciona", values,
                                "id_inspeccion=?",
                                arrayOf(idInspeccion)
                            )
                            toast(context, if (result > 0) "Actualizado" else "No se encontró inspección")
                        }
                    }
                },
                onDelete = {
                    if (!isValidIdInspeccion(idInspeccion)) {
                        toast(context, "Ingrese un ID de inspección válido (número positivo)")
                        return@RowButtons
                    }
                    val result = db.delete(
                        "inspecciona",
                        "id_inspeccion=?",
                        arrayOf(idInspeccion)
                    )
                    toast(context, if (result > 0) "Eliminada" else "No se encontró inspección")
                },
                secondaryGreen = secondaryGreen,
                accentGreen = accentGreen
            )
        }

        Button(
            onClick = onExitClick,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFD32F2F),
                contentColor = Color.White
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .height(56.dp)
                .padding(top = 16.dp)
        ) {
            Text(
                text = "Salir",
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun CustomTextField(
    label: String,
    value: String,
    keyboardType: KeyboardType = KeyboardType.Text,
    onValueChange: (String) -> Unit,
    secondaryGreen: Color
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, color = secondaryGreen) },
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp),
        keyboardOptions = KeyboardOptions.Default.copy(keyboardType = keyboardType),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = secondaryGreen,
            unfocusedBorderColor = secondaryGreen.copy(alpha = 0.5f),
            cursorColor = secondaryGreen,
            focusedLabelColor = secondaryGreen,
            unfocusedLabelColor = secondaryGreen.copy(alpha = 0.7f)
        ),
        shape = RoundedCornerShape(8.dp)
    )
}

@Composable
fun CardSection(
    title: String,
    primaryGreen: Color,
    secondaryGreen: Color,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                color = primaryGreen,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            content()
        }
    }
}

@Composable
fun RowButtons(
    onAdd: () -> Unit,
    onSearch: () -> Unit,
    onUpdate: () -> Unit,
    onDelete: () -> Unit,
    secondaryGreen: Color,
    accentGreen: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Button(
            onClick = onAdd,
            modifier = Modifier
                .weight(1f)
                .padding(4.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = secondaryGreen,
                contentColor = Color.White
            ),
            shape = RoundedCornerShape(8.dp)
        ) { Text("Añadir", fontSize = 14.sp) }
        Button(
            onClick = onSearch,
            modifier = Modifier
                .weight(1f)
                .padding(4.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = accentGreen,
                contentColor = Color.White
            ),
            shape = RoundedCornerShape(8.dp)
        ) { Text("Buscar", fontSize = 14.sp) }
        Button(
            onClick = onUpdate,
            modifier = Modifier
                .weight(1f)
                .padding(4.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = secondaryGreen,
                contentColor = Color.White
            ),
            shape = RoundedCornerShape(8.dp)
        ) { Text("Modificar", fontSize = 14.sp) }
        Button(
            onClick = onDelete,
            modifier = Modifier
                .weight(1f)
                .padding(4.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFD32F2F),
                contentColor = Color.White
            ),
            shape = RoundedCornerShape(8.dp)
        ) { Text("Eliminar", fontSize = 14.sp) }
    }
}

fun toast(context: android.content.Context, msg: String) {
    Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
}