//
// This file is a part of the XolioZ Mod for Chunk Stories
// Check out README.md for more information
// Website: https://chunkstories.xyz
// Github: https://github.com/Hugobros3/xolioz
//

package io.xol.z.plugin.game

import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import io.xol.z.plugin.XolioZPlugin
import java.io.File
import java.io.FileNotFoundException
import java.util.*
import java.util.ArrayList

class SpawnPoints(val plugin: XolioZPlugin) {

    private var points: MutableList<SpawnPoint> = ArrayList()

    val file: File = File(plugin.folder().absolutePath + "/spawnPoints.json")

    init {
        file.parentFile.mkdirs()
        load()
    }

    fun randomSpawn(): SpawnPoint {
        val random = Random()
        return points[random.nextInt(points.size)]
    }

    fun load() {
        try {
            if(!file.exists()) throw FileNotFoundException(file.absolutePath)

            val data = file.readText()
            points = gson.fromJson(data, object : TypeToken<ArrayList<SpawnPoint>>() { }.type)

        } catch (e: Exception) {
            plugin.logger.warning("Spawn points file not found or corrupted (${e.message}), using failsafe (0,100,0)")
            points = ArrayList()
            points.add(SpawnPoint(0, 100, 0))
        }

        plugin.logger.info("${points.size} spawn points loaded.")
    }

    fun save() {

    }

    companion object {
        val gson = GsonBuilder().setPrettyPrinting().create()
    }
}
