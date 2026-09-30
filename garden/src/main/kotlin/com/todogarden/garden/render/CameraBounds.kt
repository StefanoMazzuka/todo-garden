package com.todogarden.garden.render

/** Pure camera limits, in tiles. A viewport larger than the map stays centered. */
object CameraBounds {
    fun center(position: Float, visibleSize: Float, mapSize: Float): Float =
        if (visibleSize >= mapSize) mapSize / 2f
        else position.coerceIn(visibleSize / 2f, mapSize - visibleSize / 2f)

    fun fitZoom(viewportWidth: Float, viewportHeight: Float, mapWidth: Float, mapHeight: Float): Float =
        maxOf(mapWidth / viewportWidth, mapHeight / viewportHeight)
}
