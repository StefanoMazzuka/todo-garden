package com.todogarden

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.todogarden.data.GardenDatabase
import com.todogarden.data.SavedGarden
import com.todogarden.integration.GardenActivity
import com.todogarden.ui.GardenTheme
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val dao = GardenDatabase.get(this).gardens()
        setContent {
            GardenTheme {
                val gardens by remember { dao.observeAll() }.collectAsStateWithLifecycle(initialValue = emptyList())
                var creating by rememberSaveable { mutableStateOf(false) }
                var name by rememberSaveable { mutableStateOf("") }
                var saving by remember { mutableStateOf(false) }
                var error by remember { mutableStateOf<String?>(null) }
                val scope = rememberCoroutineScope()
                fun open(garden: SavedGarden) {
                    startActivity(Intent(this, GardenActivity::class.java).putExtra("gardenId", garden.id))
                }
                Surface(Modifier.fillMaxSize()) {
                    LazyColumn(
                        Modifier.fillMaxSize().safeDrawingPadding(),
                        contentPadding = PaddingValues(24.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item {
                            Spacer(Modifier.height(20.dp))
                            Text("TO DO GARDEN", style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.height(12.dp))
                            Text("Un pequeño lugar\npara empezar.", style = MaterialTheme.typography.headlineLarge)
                            Spacer(Modifier.height(12.dp))
                            Text("Crea tu jardín y date un paseo entre sus caminos y su estanque.",
                                style = MaterialTheme.typography.bodyLarge)
                        }
                        item {
                            Column(Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.primaryContainer,
                                RoundedCornerShape(24.dp)).padding(24.dp)) {
                                Text("Tu próximo jardín", style = MaterialTheme.typography.titleLarge)
                                Spacer(Modifier.height(8.dp))
                                Text("Un estanque tranquilo, tres parcelas y espacio para lo que vendrá.")
                                Spacer(Modifier.height(20.dp))
                                Button(onClick = { name = ""; error = null; creating = true }) { Text("Crear jardín") }
                            }
                        }
                        item {
                            Text("Mis jardines", style = MaterialTheme.typography.titleLarge)
                            if (gardens.isEmpty()) {
                                Spacer(Modifier.height(12.dp))
                                Text("Todavía no tienes jardines. Crea el primero para explorar el mapa.")
                            }
                        }
                        items(gardens, key = { it.id }) { garden ->
                            OutlinedCard(onClick = { open(garden) }, modifier = Modifier.fillMaxWidth()) {
                                Column(Modifier.padding(20.dp)) {
                                    Text(garden.name, style = MaterialTheme.typography.titleMedium)
                                    Spacer(Modifier.height(6.dp))
                                    Text("Jardín del estanque · 24 × 24", style = MaterialTheme.typography.bodyMedium)
                                    Spacer(Modifier.height(12.dp))
                                    Text("Entrar al jardín →", color = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }
                        item { Text("Guardado en este dispositivo", style = MaterialTheme.typography.labelMedium) }
                    }
                }
                if (creating) AlertDialog(
                    onDismissRequest = { if (!saving) creating = false },
                    title = { Text("Un nuevo comienzo") },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text("Ponle nombre a tu jardín. Empezará con el mapa del estanque.")
                            OutlinedTextField(value = name, onValueChange = { name = it.take(40) },
                                label = { Text("Nombre del jardín") }, singleLine = true, enabled = !saving)
                            error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                        }
                    },
                    confirmButton = {
                        TextButton(enabled = name.isNotBlank() && !saving, onClick = {
                            saving = true
                            scope.launch {
                                try {
                                    val garden = SavedGarden(name = name.trim())
                                    dao.insert(garden)
                                    creating = false
                                    open(garden)
                                } catch (e: CancellationException) { throw e
                                } catch (e: Exception) {
                                    error = "No se pudo guardar el jardín. Inténtalo de nuevo."
                                } finally { saving = false }
                            }
                        }) { Text(if (saving) "Guardando…" else "Crear y entrar") }
                    },
                    dismissButton = { TextButton(enabled = !saving, onClick = { creating = false }) { Text("Cancelar") } }
                )
            }
        }
    }
}
