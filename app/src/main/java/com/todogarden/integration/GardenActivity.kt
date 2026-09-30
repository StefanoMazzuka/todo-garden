package com.todogarden.integration

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentContainerView
import androidx.lifecycle.lifecycleScope
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration
import com.badlogic.gdx.backends.android.AndroidFragmentApplication
import com.todogarden.R
import com.todogarden.data.GardenDatabase
import com.todogarden.garden.render.GardenRenderer
import com.todogarden.ui.GardenTheme
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

class GardenActivity : FragmentActivity(), AndroidFragmentApplication.Callbacks {
    private var gardenName by mutableStateOf("Abriendo jardín…")
    private var error by mutableStateOf<String?>(null)
    internal var waterAnimated by mutableStateOf(true)
    private var loaded by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        waterAnimated = savedInstanceState?.getBoolean("animated", true) ?: true
        val root = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        ViewCompat.setOnApplyWindowInsetsListener(root) { view, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout())
            view.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }
        root.addView(ComposeView(this).apply {
            setContent {
                GardenTheme {
                    Surface {
                        Column(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp)) {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                TextButton(onClick = { finish() }) { Text("← Mis jardines") }
                                TextButton(enabled = loaded && error == null, onClick = { command { resetCamera() } }) {
                                    Text("Centrar")
                                }
                            }
                            Text(gardenName, style = MaterialTheme.typography.titleLarge,
                                modifier = Modifier.padding(horizontal = 12.dp))
                            error?.let {
                                Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(12.dp))
                            }
                        }
                    }
                }
            }
        })
        root.addView(FragmentContainerView(this).apply { id = R.id.garden_container },
            LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f))
        root.addView(ComposeView(this).apply {
            setContent {
                GardenTheme {
                    Surface {
                        Column(Modifier.fillMaxWidth().padding(12.dp)) {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                                OutlinedButton(enabled = loaded && error == null, onClick = { command { changeZoom(1.25f) } }) { Text("−") }
                                TextButton(enabled = loaded && error == null, onClick = {
                                    waterAnimated = !waterAnimated
                                    val enabled = waterAnimated
                                    command { animated = enabled }
                                }) { Text(if (waterAnimated) "Pausar agua" else "Animar agua") }
                                OutlinedButton(enabled = loaded && error == null, onClick = { command { changeZoom(0.8f) } }) { Text("+") }
                            }
                            Text("Arrastra para moverte · Pellizca para ampliar", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        })
        setContentView(root)
        lifecycleScope.launch {
            try {
                val id = intent.getStringExtra("gardenId") ?: error("Falta el jardín")
                val garden = GardenDatabase.get(this@GardenActivity).gardens().find(id)
                    ?: error("Jardín no encontrado")
                gardenName = garden.name
                if (supportFragmentManager.findFragmentByTag("garden") == null) {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.garden_container, GardenFragment().apply {
                            arguments = Bundle().apply {
                                putString("mapId", garden.mapId)
                                putInt("mapVersion", garden.mapVersion)
                                putBoolean("animated", waterAnimated)
                            }
                        }, "garden").commit()
                }
                loaded = true
            } catch (e: CancellationException) { throw e
            } catch (e: Exception) { showMapError("No se pudo abrir el jardín. Vuelve al menú e inténtalo de nuevo.") }
        }
    }

    private fun command(action: GardenRenderer.() -> Unit) {
        (supportFragmentManager.findFragmentByTag("garden") as? GardenFragment)?.command(action)
    }

    fun showMapError(message: String) { runOnUiThread { error = message } }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putBoolean("animated", waterAnimated)
        super.onSaveInstanceState(outState)
    }

    override fun exit() { runOnUiThread { finish() } }
}

class GardenFragment : AndroidFragmentApplication() {
    private var engine: GardenRenderer? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val args = requireArguments()
        val game = GardenRenderer(args.getString("mapId") ?: "garden_01", args.getInt("mapVersion", 1)) {
            (activity as? GardenActivity)?.showMapError(it)
        }.apply { animated = savedInstanceState?.getBoolean("animated") ?: args.getBoolean("animated", true) }
        engine = game
        return initializeForView(game, AndroidApplicationConfiguration().apply {
            useAccelerometer = false
            useCompass = false
            useGyroscope = false
            useImmersiveMode = false
            disableAudio = true
        })
    }

    fun command(action: GardenRenderer.() -> Unit) {
        val game = engine ?: return
        // Post to this fragment's GL thread, never a stale global Gdx application.
        postRunnable { game.action() }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putBoolean("animated", (activity as? GardenActivity)?.waterAnimated ?: true)
        super.onSaveInstanceState(outState)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        engine = null
    }
}
