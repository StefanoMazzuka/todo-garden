package com.todogarden.garden.render

import com.badlogic.gdx.ApplicationAdapter
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.input.GestureDetector
import com.badlogic.gdx.maps.tiled.BaseTiledMapLoader
import com.badlogic.gdx.maps.tiled.TiledMap
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer
import com.badlogic.gdx.maps.tiled.TmxMapLoader
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer
import com.badlogic.gdx.math.Vector2
import kotlin.math.sin

class GardenRenderer(
    private val mapId: String,
    private val mapVersion: Int,
    private val onError: (String) -> Unit,
) : ApplicationAdapter() {
    private var map: TiledMap? = null
    private var renderer: OrthogonalTiledMapRenderer? = null
    private var shapes: ShapeRenderer? = null
    private val camera = OrthographicCamera()
    private var mapWidth = 24f
    private var mapHeight = 24f
    private var maxZoom = 1f
    private var elapsed = 0f
    private var ready = false
    private var firstResize = true
    private var pinchZoom: Float? = null
    private val water = mutableListOf<Vector2>()
    private var backLayers = intArrayOf()
    private var frontLayers = intArrayOf()
    var animated = true

    override fun create() {
        try {
            val params = BaseTiledMapLoader.Parameters().apply {
                textureMinFilter = Texture.TextureFilter.Nearest
                textureMagFilter = Texture.TextureFilter.Nearest
            }
            val loaded = TmxMapLoader().load("maps/$mapId.tmx", params)
            map = loaded
            require(loaded.properties.get("mapId") == mapId) { "Identificador de mapa incompatible" }
            require((loaded.properties.get("mapVersion") as Number).toInt() == mapVersion) { "Versión de mapa incompatible" }
            listOf("ground", "decoration_back", "blocked", "plantable", "decoration_front", "anchors", "props").forEach {
                require(loaded.layers.get(it) != null) { "Falta la capa $it" }
            }
            val ground = loaded.layers.get("ground") as TiledMapTileLayer
            mapWidth = ground.width.toFloat()
            mapHeight = ground.height.toFloat()
            require(ground.tileWidth == 32 && ground.tileHeight == 32) { "Se requieren tiles de 32 px" }
            for (x in 0 until ground.width) for (y in 0 until ground.height) {
                val terrain = ground.getCell(x, y)?.tile?.properties?.get("terrainId") as? String
                if (terrain?.startsWith("water_") == true) water.add(Vector2(x.toFloat(), y.toFloat()))
            }
            backLayers = intArrayOf(loaded.layers.getIndex("ground"), loaded.layers.getIndex("decoration_back"))
            frontLayers = intArrayOf(loaded.layers.getIndex("decoration_front"))
            renderer = OrthogonalTiledMapRenderer(loaded, 1f / 32f)
            shapes = ShapeRenderer()
            Gdx.input.inputProcessor = GestureDetector(object : GestureDetector.GestureAdapter() {
                override fun pan(x: Float, y: Float, deltaX: Float, deltaY: Float): Boolean {
                    camera.position.x -= deltaX * camera.viewportWidth * camera.zoom / Gdx.graphics.width
                    camera.position.y += deltaY * camera.viewportHeight * camera.zoom / Gdx.graphics.height
                    constrain()
                    return true
                }
                override fun zoom(initialDistance: Float, distance: Float): Boolean {
                    if (pinchZoom == null) pinchZoom = camera.zoom
                    if (distance > 0f) {
                        camera.zoom = (pinchZoom!! * initialDistance / distance).coerceIn(0.4f, maxZoom)
                        constrain()
                    }
                    return true
                }
                override fun pinchStop() { pinchZoom = null }
                override fun tap(x: Float, y: Float, count: Int, button: Int): Boolean {
                    if (count == 2) resetCamera()
                    return true
                }
            })
            ready = true
        } catch (e: Exception) {
            Gdx.app.error("Garden", "No se pudo cargar el mapa", e)
            onError("No se pudo abrir el mapa de este jardín.")
        }
    }

    override fun resize(width: Int, height: Int) {
        if (!ready || width <= 0 || height <= 0) return
        camera.viewportWidth = 12f
        camera.viewportHeight = 12f * height / width
        maxZoom = CameraBounds.fitZoom(camera.viewportWidth, camera.viewportHeight, mapWidth, mapHeight)
        if (firstResize) { resetCamera(); firstResize = false }
        camera.zoom = camera.zoom.coerceIn(0.4f, maxZoom)
        constrain()
    }

    fun resetCamera() {
        if (!ready) return
        camera.position.set(mapWidth / 2f, mapHeight / 2f, 0f)
        camera.zoom = maxZoom
        constrain()
    }

    fun changeZoom(factor: Float) {
        if (!ready) return
        camera.zoom = (camera.zoom * factor).coerceIn(0.4f, maxZoom)
        constrain()
    }

    private fun constrain() {
        camera.position.x = CameraBounds.center(camera.position.x, camera.viewportWidth * camera.zoom, mapWidth)
        camera.position.y = CameraBounds.center(camera.position.y, camera.viewportHeight * camera.zoom, mapHeight)
        camera.update()
    }

    override fun render() {
        Gdx.gl.glClearColor(0.19f, 0.27f, 0.19f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
        if (!ready) return
        if (animated) elapsed += Gdx.graphics.deltaTime.coerceAtMost(0.05f)
        renderer!!.setView(camera)
        renderer!!.render(backLayers)
        // Small pixel ripples stay inside water cells, identified through TSX terrainId.
        val drawing = shapes!!
        drawing.projectionMatrix = camera.combined
        Gdx.gl.glEnable(GL20.GL_BLEND)
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA)
        drawing.begin(ShapeRenderer.ShapeType.Filled)
        water.forEach { cell ->
            val phase = elapsed * 1.4f + cell.x * 0.8f + cell.y * 1.3f
            val offset = sin(phase) * 0.10f
            drawing.setColor(0.80f, 0.96f, 0.94f, 0.22f + 0.12f * sin(phase))
            drawing.rect(cell.x + 0.18f + offset, cell.y + 0.33f, 0.34f, 1f / 32f)
            drawing.rect(cell.x + 0.47f - offset, cell.y + 0.70f, 0.22f, 1f / 32f)
        }
        drawing.end()
        Gdx.gl.glDisable(GL20.GL_BLEND)
        renderer!!.render(frontLayers)
    }

    override fun pause() { pinchZoom = null }
    override fun dispose() {
        Gdx.input.inputProcessor = null
        shapes?.dispose()
        renderer?.dispose()
        map?.dispose()
        ready = false
    }
}
