package com.example.dispositivomobil
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.dispositivomobil.components.EditNoteDialog
import com.example.dispositivomobil.components.NoteItem
import com.example.dispositivomobil.models.Note
import org.json.JSONArray
import org.json.JSONObject

class MainActivity : ComponentActivity() {

    private val notesList = mutableStateListOf<Note>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DispositivoMobilTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    NotesApp()
                }
            }
        }

        loadNotes()
    }
    @Composable
    fun DispositivoMobilTheme(content: @Composable () -> Unit) {
        val darkColors = darkColorScheme(
            primary = Color(0xFF000000), // Botones negros
            onPrimary = Color(0xFFFFFFFF), // Letras blancas en los botones
            background = Color(0xFFFFE4E1), // Fondo rozado super claro
            surface = Color(0xFFFFE4E1), // Superficie de componentes
            onBackground = Color(0xFF000000), // Texto sobre el fondo
            onSurface = Color(0xFF000000), // Texto sobre superficies
            error = Color(0xFFCF6679) // Color de error
        )

        val lightColors = lightColorScheme(
            primary = Color(0xFF000000), // Botones negros
            onPrimary = Color(0xFFFFFFFF), // Letras blancas en los botones
            background = Color(0xFFFFE4E1), // Fondo rozado super claro
            surface = Color(0xFFFFE4E1), // Superficie de componentes
            onBackground = Color(0xFF000000), // Texto sobre el fondo
            onSurface = Color(0xFF000000), // Texto sobre superficies
            error = Color(0xFFB00020) // Color de error
        )

        MaterialTheme(
            colorScheme = if (isSystemInDarkTheme()) darkColors else lightColors,
            typography = Typography(),
            content = {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Personaliza los cuadros de escribir
                    val textFieldColors = TextFieldDefaults.colors(
                        focusedIndicatorColor = Color(0xFF8B4513), // Marrón claro
                        unfocusedIndicatorColor = Color(0xFF8B4513), // Marrón claro
                        focusedLabelColor = Color(0xFF8B4513), // Marrón claro
                        unfocusedLabelColor = Color(0xFF8B4513) // Marrón claro
                    )

                    // Personaliza los botones
                    val buttonColors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF000000), // Botones negros
                        contentColor = Color(0xFFFFFFFF) // Letras blancas en los botones
                    )

                    // Aplica los colores a los botones y cuadros de escribir
                    Button(
                        onClick = { /* Acción del botón */ },
                        colors = buttonColors
                    ) {
                        Text("Botón")
                    }

                    TextField(
                        value = "",
                        onValueChange = { /* Acción del campo de texto */ },
                        colors = textFieldColors
                    )
                    // Agrega el contenido aquí
                    content()
                }
            }
        )
    }



    @Composable
    fun NotesApp() {
        var title by remember { mutableStateOf("") }
        var description by remember { mutableStateOf("") }
        var showEditDialog by remember { mutableStateOf(false) }
        var noteToEdit by remember { mutableStateOf<Note?>(null) }
        var errorMessage by remember { mutableStateOf<String?>(null) }
        val context = LocalContext.current

        Column(modifier = Modifier.padding(55.dp)) {
            TextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Titulo") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            TextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Descripción") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(55.dp))
            Button(onClick = {

                try {
                    if (title.isNotEmpty() && description.isNotEmpty()) {
                        val note = Note(
                            title = title,
                            description = description
                        )
                        notesList.add(note)
                        saveNotes()
                        title = ""
                        description = ""
                        errorMessage = null
                        Toast.makeText(context, "Nota guardada", Toast.LENGTH_SHORT).show()
                    } else {
                        errorMessage = "Por favor, complete todos los campos"
                    }
                } catch (e: Exception) {
                    errorMessage = "Error al guardar la nota: ${e.message}"
                }
            }) {
                Text("Guardar")
            }
            Spacer(modifier = Modifier.absoluteOffset (70.dp))
            NotesList(
                notes = notesList,
                onEditNote = { index ->
                    noteToEdit = notesList[index]
                    showEditDialog = true
                },
                onDeleteNote = { index ->
                    try {
                        notesList.removeAt(index)
                        saveNotes()
                        errorMessage = null
                    } catch (e: Exception) {
                        errorMessage = "Error al eliminar la nota: ${e.message}"
                    }
                }
            )

            // Mostrar el cuadro de diálogo de edición si se ha seleccionado una nota para editar
            noteToEdit?.let { note ->
                if (showEditDialog) {
                    EditNoteDialog(
                        note = note,
                        onNoteUpdated = { updatedNote ->
                            try {
                                val index = notesList.indexOf(note)
                                notesList[index] = updatedNote
                                saveNotes()
                                showEditDialog = false
                                errorMessage = null
                            } catch (e: Exception) {
                                errorMessage = "Error al actualizar la nota: ${e.message}"
                            }
                        },
                        onDismiss = {
                            showEditDialog = false
                        }
                    )
                }
            }


            errorMessage?.let {
                Text(text = it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }
        }
    }

    @Composable
    fun NotesList(notes: List<Note>, onEditNote: (Int) -> Unit, onDeleteNote: (Int) -> Unit) {
        LazyColumn {
            items(notes) { note ->
                val index = notes.indexOf(note)
                NoteItem(
                    note = note,
                    onEdit = { onEditNote(index) },
                    onDelete = { onDeleteNote(index) }
                )
            }
        }
    }

    private fun saveNotes() {
        try {
            val sharedPreferences = getSharedPreferences("notes_prefs", MODE_PRIVATE)
            val editor = sharedPreferences.edit()

            val jsonArray = JSONArray()
            for (note in notesList) {
                val jsonObject = JSONObject()
                jsonObject.put("Titulo", note.title)
                jsonObject.put("Descripción", note.description)
                jsonArray.put(jsonObject)
            }

            editor.putString("notes", jsonArray.toString())
            editor.apply()
        } catch (e: Exception) {
            // Log error or handle accordingly
        }
    }

    private fun loadNotes() {
        try {
            val sharedPreferences = getSharedPreferences("notes_prefs", MODE_PRIVATE)
            val notesJson = sharedPreferences.getString("notes", "[]")

            val jsonArray = JSONArray(notesJson)
            notesList.clear()
            for (i in 0 until jsonArray.length()) {
                val jsonObject = jsonArray.getJSONObject(i)
                val title = jsonObject.getString("Titulo")
                val description = jsonObject.getString("Descripción")
                notesList.add(Note(title, description))
            }
        } catch (e: Exception) {
            // Log error or handle accordingly
        }
    }
}
