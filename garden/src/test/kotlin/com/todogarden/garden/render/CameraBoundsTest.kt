package com.todogarden.garden.render

import kotlin.test.Test
import kotlin.test.assertEquals

class CameraBoundsTest {
    @Test fun clampsAtBothMapEdges() {
        assertEquals(6f, CameraBounds.center(-100f, 12f, 24f))
        assertEquals(18f, CameraBounds.center(100f, 12f, 24f))
        assertEquals(15f, CameraBounds.center(15f, 12f, 24f))
    }
    @Test fun centersWhenViewportIsLargerThanMap() {
        assertEquals(12f, CameraBounds.center(100f, 40f, 24f))
        assertEquals(12f, CameraBounds.center(-100f, 24f, 24f))
    }
    @Test fun overviewFitsPortraitAndLandscape() {
        assertEquals(2f, CameraBounds.fitZoom(12f, 20f, 24f, 24f))
        assertEquals(3f, CameraBounds.fitZoom(12f, 8f, 24f, 24f))
    }
}
