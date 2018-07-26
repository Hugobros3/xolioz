//
// This file is a part of the XolioZ Mod for Chunk Stories
// Check out README.md for more information
// Website: https://chunkstories.xyz
// Github: https://github.com/Hugobros3/xolioz
//

package io.xol.z.plugin.loot

import io.xol.chunkstories.api.Location
import io.xol.chunkstories.api.player.Player
import io.xol.chunkstories.api.world.World
import io.xol.chunkstories.core.voxel.VoxelChest
import io.xol.z.plugin.XolioZPlugin
import io.xol.z.plugin.player.PlayerProfile
import java.io.*
import java.util.*

class LootPlaces(val plugin: XolioZPlugin) {

    // This class takes care of holding all the places on the world where the loot
    // do spawns.
    internal var places: MutableMap<String, LootPlace> = HashMap()

    fun getData(w: World?): MutableMap<String, LootPlace> {
        return places
    }

    fun respawnLoot(w: World): Int {
        val places = getData(w)
        var count = 0
        for (lp in places.values) {
            lp.lastUpdate = 0
            count++
        }
        return count
    }

    fun loadLootFile(world: World?) {
        val places = getData(world)
        val file = getFile(world)
        places.clear()
        try {
            val ips = FileInputStream(file)
            val ipsr = InputStreamReader(ips, "UTF-8")
            val br = BufferedReader(ipsr)
            var ligne: String = br.readLine()
            while (ligne != null) {

                val split = ligne.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                if (split.size >= 5) {
                    // x:y:z:type:minamount:max
                    val x = Integer.parseInt(split[0])
                    val y = Integer.parseInt(split[1])
                    val z = Integer.parseInt(split[2])
                    val location = Location(world, x.toDouble(), y.toDouble(), z.toDouble())

                    val type = split[3]
                    val category = this.plugin.lootCategories.getCategory(type) ?: continue

                    val minAmountToSpawn = Integer.parseInt(split[4])
                    val maxAmountToSpawn: Int

                    if (split.size >= 6)
                        maxAmountToSpawn = Integer.parseInt(split[5])
                    else
                        maxAmountToSpawn = minAmountToSpawn

                    val lootPlace = LootPlace(plugin, location, category, minAmountToSpawn, maxAmountToSpawn)
                    val coordinatesInText = location.x.toInt().toString() + ":" + location.y.toInt() + ":" + location.z.toInt()
                    places[coordinatesInText] = lootPlace
                }

                ligne = br.readLine()
            }
            br.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }

    }

    private fun getFile(w: World?): File {
        val file = File(XolioZPlugin.pluginFolder + "lootPlaces.dz")
        if (!file.exists())
            try {
                file.createNewFile()
            } catch (e1: IOException) {
            }

        return file
    }

    fun saveLootFile(w: World) {
        var file = getFile(w)
        val places = getData(w)
        try {
            val out = BufferedWriter(OutputStreamWriter(FileOutputStream(file), "UTF-8"))
            for (lp in places.values) {
                out.write(lp.save() + "\n")
            }
            // out.write(key + "=" + props.get(key)+"\n");
            out.close()
        } catch (e: FileNotFoundException) {
        } catch (e: IOException) {
            e.printStackTrace()
        }

        // These files have an infuriating tendency to self-corrupt, so yeah...
        val folder = File(XolioZPlugin.pluginFolder + "backups")
        if (!folder.exists())
            folder.mkdir()
        file = File(XolioZPlugin.pluginFolder + "backups/lootPlaces-" + System.currentTimeMillis() + ".dz")
        try {
            file.createNewFile()
            val out = BufferedWriter(OutputStreamWriter(FileOutputStream(file), "UTF-8"))
            for (lp in places.values) {
                out.write(lp.save() + "\n")
            }
            // out.write(key + "=" + props.get(key)+"\n");
            out.close()
        } catch (e: FileNotFoundException) {
        } catch (e: IOException) {
            e.printStackTrace()
        }

    }

    fun removePlace(coords: String, w: World): Boolean {
        val places = getData(w)
        return places.remove(coords) != null
    }

    fun add(coords: String, lp: LootPlace, w: World): Boolean {
        val places = getData(w)
        if (places.containsKey(coords))
            return false
        places[coords] = lp
        return true
    }

    fun count(w: World): Int {
        val places = getData(w)
        return places.size
    }

    fun update(coords: String, w: World) {
        val places = getData(w)

        if (places.containsKey(coords))
            places[coords]?.update()
    }

    fun generateLootPointsArroundPlayer(player: Player, radius: Int, forceReplace: Boolean): Int {
        val places = getData(player.controlledEntity!!.getWorld())
        var count = 0
        val pp = player.profile
        val loc = player.location
        var x = (loc.x() - radius).toInt()
        while (x < loc.x() + radius) {
            var z = (loc.z() - radius).toInt()
            while (z < loc.z() + radius) {
                for (y in 0..254) {
                    val v = player.world.peekSafely(x, y, z).voxel// XolioZPlugin.getServer().getContent().voxels().getVoxelById(player.getControlledEntity().getWorld().getVoxelData(x,
                    // y, z));

                    if (v != null && v is VoxelChest && pp.activeCategory != null) {
                        val coords = x.toString() + ":" + y + ":" + z

                        val lp = LootPlace(plugin, Location(player.world, x.toDouble(), y.toDouble(), z.toDouble()), pp.activeCategory, pp.currentMin, pp.currentMax)
                        // new LootPlace(this,
                        // coords+":"+pp.activeCategory+":"+pp.currentMin+":"+pp.currentMax,player.getControlledEntity().getWorld());
                        if (forceReplace || !places.containsKey(coords)) {
                            if (places.containsKey(coords))
                                places.remove(coords)
                            places[coords] = lp
                            count++
                        }
                    }
                }
                z++
            }
            x++
        }
        return count
    }

    private val Player.profile: PlayerProfile
        get() = with(plugin.playerProfiles) {
            this@profile.profile
        }

    /*companion object {
        //TODO bring back heatmap stuff
        /** Debug method that makes a heatmap of the loot points  */
        @JvmStatic
        fun main(a: Array<String>) {
            val scale = 4
            val img = BufferedImage(4096 / scale, 4096 / scale, BufferedImage.TYPE_INT_RGB)

            val lp = LootPlaces(null!!)
            lp.loadLootFile(null)

            var count = 0
            for (pl in lp.places.values) {
                val px = pl.getLocation().x.toInt() / scale
                val py = pl.getLocation().z.toInt() / scale

                img.setRGB(px, py, 255 shl 16)

                count++
            }

            try {
                ImageIO.write(img, "PNG", File("loot-density.png"))
            } catch (e: IOException) {
                // TODO Auto-generated catch block
                e.printStackTrace()
            }

            println("Found $count lootplaces.")
        }
    }*/


}
