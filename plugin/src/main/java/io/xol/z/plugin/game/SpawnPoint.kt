package io.xol.z.plugin.game

import org.joml.Vector3i

class SpawnPoint(x: Int, y: Int, z: Int) : Vector3i(x, y, z) {
    var name : String? = null
}