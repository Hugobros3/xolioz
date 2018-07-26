//
// This file is a part of the XolioZ Mod for Chunk Stories
// Check out README.md for more information
// Website: https://chunkstories.xyz
// Github: https://github.com/Hugobros3/xolioz
//

package io.xol.z.plugin.game

import io.xol.z.plugin.XolioZPlugin
import org.joml.Vector3i
import java.io.*
import java.util.*

class SpawnPoints {

    private var points: MutableList<Vector3i> = ArrayList()

    private val file: File
        get() {
            val file = File(XolioZPlugin.pluginFolder + "spawnPoints.dz")
            if (!file.exists())
                try {
                    file.createNewFile()
                } catch (e1: IOException) {
                }

            return file
        }

    fun randomSpawn(): Vector3i {
        val random = Random()
        return points[random.nextInt(points.size)]
    }

    //TODO that's not the kotlin way
    fun load() {
        points.clear()
        val file = file
        try {
            val ips = FileInputStream(file)
            val ipsr = InputStreamReader(ips, "UTF-8")
            val br = BufferedReader(ipsr)
            var ligne: String? = null
            ligne = br.readLine()
            while (ligne != null) {
                val data = ligne.split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                if (data.size >= 3) {
                    points.add(Vector3i(Integer.parseInt(data[0]), Integer.parseInt(data[1]), Integer.parseInt(data[2])))
                }
                ligne = br.readLine()
            }
            br.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        println("[DogeZ]" + points.size + " spawn points loaded.")
    }
}
