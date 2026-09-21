package com.jordi.cantforgetapp

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.jordi.cantforgetapp.ui.theme.CantForgetAppTheme
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

enum class TipoTarea {
    COMPRAR,
    HACER
}

data class Tarea(
    val id: String,
    val texto: String,
    val tipo: TipoTarea,
    val completada: Boolean = false
)

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            CantForgetAppTheme {
                AppRecordatorios(applicationContext)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppRecordatorios(context: Context) {

    var tareas by remember {
        mutableStateOf(cargarTareas(context))
    }

    var mostrarDialogo by remember {
        mutableStateOf(false)
    }

    var tareaEditando by remember {
        mutableStateOf<Tarea?>(null)
    }

    fun guardar(lista: List<Tarea>) {
        tareas = lista
        guardarTareas(context, lista)
    }

    Scaffold(

        topBar = {

            TopAppBar(

                title = {

                    Column {

                        Text(
                            text = stringResource(R.string.title_tomorrow),
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = stringResource(R.string.subtitle_tasks),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            )
        },

        floatingActionButton = {

            FloatingActionButton(
                onClick = {
                    mostrarDialogo = true
                }
            ) {
                Text("+")
            }
        }

    ) { padding ->

        LazyColumn(

            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),

            verticalArrangement = Arrangement.spacedBy(12.dp),

            contentPadding = PaddingValues(
                bottom = 100.dp
            )

        ) {

            item {
                TituloSeccion(
                    stringResource(R.string.buy)
                )
            }

            val compras = tareas.filter {
                it.tipo == TipoTarea.COMPRAR
            }

            if (compras.isEmpty()) {

                item {
                    MensajeVacio(
                        stringResource(R.string.no_shopping_items)
                    )
                }

            } else {

                items(
                    compras,
                    key = { it.id }
                ) { tarea ->

                    TarjetaTarea(

                        tarea = tarea,

                        onCompletar = {

                            guardar(
                                tareas.map {

                                    if (it.id == tarea.id) {

                                        it.copy(
                                            completada = !it.completada
                                        )

                                    } else {
                                        it
                                    }
                                }
                            )
                        },

                        onEditar = {
                            tareaEditando = tarea
                        },

                        onBorrar = {

                            guardar(
                                tareas.filterNot {
                                    it.id == tarea.id
                                }
                            )
                        }
                    )
                }
            }

            item {

                Spacer(
                    modifier = Modifier.height(10.dp)
                )

                TituloSeccion(
                    stringResource(R.string.do_tasks)
                )
            }

            val cosasHacer = tareas.filter {
                it.tipo == TipoTarea.HACER
            }

            if (cosasHacer.isEmpty()) {

                item {

                    MensajeVacio(
                        stringResource(R.string.no_tasks)
                    )
                }

            } else {

                items(
                    cosasHacer,
                    key = { it.id }
                ) { tarea ->

                    TarjetaTarea(

                        tarea = tarea,

                        onCompletar = {

                            guardar(
                                tareas.map {

                                    if (it.id == tarea.id) {

                                        it.copy(
                                            completada = !it.completada
                                        )

                                    } else {
                                        it
                                    }
                                }
                            )
                        },

                        onEditar = {
                            tareaEditando = tarea
                        },

                        onBorrar = {

                            guardar(
                                tareas.filterNot {
                                    it.id == tarea.id
                                }
                            )
                        }
                    )
                }
            }

            if (tareas.any { it.completada }) {

                item {

                    OutlinedButton(

                        modifier = Modifier.fillMaxWidth(),

                        onClick = {

                            guardar(
                                tareas.filterNot {
                                    it.completada
                                }
                            )
                        }

                    ) {
                        Text(
                            stringResource(R.string.clear_completed)
                        )
                    }
                }
            }
        }
    }

    if (mostrarDialogo) {

        DialogoTarea(

            titulo = stringResource(R.string.add_for_tomorrow),

            textoInicial = "",

            tipoInicial = TipoTarea.HACER,

            onCancelar = {
                mostrarDialogo = false
            },

            onGuardar = { texto, tipo ->

                if (texto.trim().isNotEmpty()) {

                    val nuevaTarea = Tarea(

                        id = UUID
                            .randomUUID()
                            .toString(),

                        texto = texto.trim(),

                        tipo = tipo
                    )

                    guardar(
                        tareas + nuevaTarea
                    )
                }

                mostrarDialogo = false
            }
        )
    }

    tareaEditando?.let { tarea ->

        DialogoTarea(

            titulo = stringResource(R.string.edit),

            textoInicial = tarea.texto,

            tipoInicial = tarea.tipo,

            onCancelar = {
                tareaEditando = null
            },

            onGuardar = { texto, tipo ->

                if (texto.trim().isNotEmpty()) {

                    guardar(
                        tareas.map {

                            if (it.id == tarea.id) {

                                it.copy(
                                    texto = texto.trim(),
                                    tipo = tipo
                                )

                            } else {
                                it
                            }
                        }
                    )
                }

                tareaEditando = null
            }
        )
    }
}

@Composable
fun TituloSeccion(texto: String) {

    Text(
        text = texto,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(top = 8.dp)
    )
}

@Composable
fun MensajeVacio(texto: String) {

    Card(

        modifier = Modifier.fillMaxWidth(),

        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )

    ) {

        Text(
            text = texto,
            modifier = Modifier.padding(16.dp),
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@Composable
fun TarjetaTarea(
    tarea: Tarea,
    onCompletar: () -> Unit,
    onEditar: () -> Unit,
    onBorrar: () -> Unit
) {

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {

        Column(
            modifier = Modifier.padding(12.dp)
        ) {

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {

                Checkbox(
                    checked = tarea.completada,
                    onCheckedChange = {
                        onCompletar()
                    }
                )

                Text(
                    text = tarea.texto,

                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 8.dp),

                    style = MaterialTheme.typography.bodyLarge,

                    textDecoration =
                        if (tarea.completada)
                            TextDecoration.LineThrough
                        else
                            TextDecoration.None
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {

                TextButton(
                    onClick = onEditar
                ) {
                    Text(
                        stringResource(R.string.edit)
                    )
                }

                TextButton(
                    onClick = onBorrar
                ) {
                    Text(
                        stringResource(R.string.delete)
                    )
                }
            }
        }
    }
}

@Composable
fun DialogoTarea(
    titulo: String,
    textoInicial: String,
    tipoInicial: TipoTarea,
    onCancelar: () -> Unit,
    onGuardar: (String, TipoTarea) -> Unit
) {

    var texto by remember {
        mutableStateOf(textoInicial)
    }

    var tipo by remember {
        mutableStateOf(tipoInicial)
    }

    AlertDialog(

        onDismissRequest = onCancelar,

        title = {
            Text(titulo)
        },

        text = {

            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                OutlinedTextField(
                    value = texto,

                    onValueChange = {
                        texto = it
                    },

                    label = {
                        Text(
                            stringResource(R.string.what_to_remember)
                        )
                    },

                    modifier = Modifier.fillMaxWidth(),

                    minLines = 2
                )

                Text(
                    text = stringResource(R.string.type),
                    fontWeight = FontWeight.SemiBold
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    RadioButton(
                        selected = tipo == TipoTarea.HACER,
                        onClick = {
                            tipo = TipoTarea.HACER
                        }
                    )

                    Text(
                        stringResource(R.string.do_tasks)
                    )

                    Spacer(
                        modifier = Modifier.width(20.dp)
                    )

                    RadioButton(
                        selected = tipo == TipoTarea.COMPRAR,
                        onClick = {
                            tipo = TipoTarea.COMPRAR
                        }
                    )

                    Text(
                        stringResource(R.string.buy)
                    )
                }
            }
        },

        confirmButton = {

            Button(

                onClick = {
                    onGuardar(
                        texto,
                        tipo
                    )
                },

                enabled = texto.isNotBlank()

            ) {
                Text(
                    stringResource(R.string.save)
                )
            }
        },

        dismissButton = {

            TextButton(
                onClick = onCancelar
            ) {
                Text(
                    stringResource(R.string.cancel)
                )
            }
        }
    )
}

fun guardarTareas(
    context: Context,
    tareas: List<Tarea>
) {

    val array = JSONArray()

    tareas.forEach { tarea ->

        array.put(

            JSONObject().apply {

                put("id", tarea.id)
                put("texto", tarea.texto)
                put("tipo", tarea.tipo.name)
                put("completada", tarea.completada)
            }
        )
    }

    context
        .getSharedPreferences(
            "cant_forget_tasks",
            Context.MODE_PRIVATE
        )
        .edit()
        .putString(
            "tareas",
            array.toString()
        )
        .apply()
}

fun cargarTareas(
    context: Context
): List<Tarea> {

    val textoGuardado =
        context
            .getSharedPreferences(
                "cant_forget_tasks",
                Context.MODE_PRIVATE
            )
            .getString(
                "tareas",
                null
            )
            ?: return emptyList()

    return try {

        val array =
            JSONArray(textoGuardado)

        buildList {

            for (i in 0 until array.length()) {

                val objeto =
                    array.getJSONObject(i)

                add(

                    Tarea(

                        id =
                            objeto.getString("id"),

                        texto =
                            objeto.getString("texto"),

                        tipo =
                            TipoTarea.valueOf(
                                objeto.getString("tipo")
                            ),

                        completada =
                            objeto.optBoolean(
                                "completada",
                                false
                            )
                    )
                )
            }
        }

    } catch (_: Exception) {
        emptyList()
    }
}