//
// This file is a part of the XolioZ Mod for Chunk Stories
// Check out README.md for more information
// Website: https://chunkstories.xyz
// Github: https://github.com/Hugobros3/xolioz
//

package io.xol.z.plugin.map

import io.xol.chunkstories.api.player.Player
import io.xol.z.plugin.XolioZPlugin
import java.awt.image.BufferedImage
import java.io.*
import java.util.*
import javax.imageio.ImageIO

class PlacesNames(val plugin : XolioZPlugin) {

    private var placesImage: BufferedImage? = null
    private val placesNames: MutableMap<Int, String> = HashMap()

    init {
        loadData()
    }

    companion object {
        val hexTable = charArrayOf('0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F')
    }

    fun loadData() {
        placesNames.clear()
        try {
            println("Loading places names...")
            placesImage = ImageIO.read(File(XolioZPlugin.pluginFolder + "places.png"))
            val text = FileReader(File(XolioZPlugin.pluginFolder + "places.dz")).use { it.readText() }
            for (line in text.lines()) {
                if (line.contains(":")) {
                    val s = line.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                    placesNames[hexToInt(s[0])] = s[1]
                }
            }
            println("[XolioZ-Plugin] Done !")
        } catch (e: IOException) {
            println("[XolioZ-Plugin] Error while loading places names:")
            e.printStackTrace()
        }

    }

    fun getPlayerPlaceName(player: Player): String {
        val loc = player.location

        val placesImage = placesImage ?: return "Could not load image for area map"

        //TODO this bit is specific to the map you are playing on
        val x = loc.x().toInt() / 2
        val y = loc.z().toInt() / 2

        if (x >= 0 && y >= 0 && x < placesImage.width && y < placesImage.height) {
            val rgb = placesImage.getRGB(x, y) and 0xFFFFFF

            if (rgb == 0)
                return "Wilderness"

            var name: String? = placesNames[rgb]
            if (name == null)
                name = "Malformed area map ! ( $rgb )"
            return name
        } else {
            return "Out of maps bound"
        }
    }

    fun hexToInt(hex: String): Int {
        var value = 0
        for (c in hex.toCharArray()) {
            value *= 16
            var index = 0
            for (d in hexTable) {
                if (c == d) {
                    value += index
                }
                index++
            }
        }
        return value
    }
}
