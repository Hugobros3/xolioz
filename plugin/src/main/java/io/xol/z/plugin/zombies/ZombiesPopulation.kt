//
// This file is a part of the XolioZ Mod for Chunk Stories
// Check out README.md for more information
// Website: https://chunkstories.xyz
// Github: https://github.com/Hugobros3/xolioz
//

package io.xol.z.plugin.zombies

import io.xol.chunkstories.api.Location
import io.xol.chunkstories.api.entity.Entity
import io.xol.chunkstories.api.entity.traits.serializable.TraitCreativeMode
import io.xol.chunkstories.api.entity.traits.serializable.TraitHealth
import io.xol.chunkstories.api.player.Player
import io.xol.chunkstories.api.voxel.Voxel
import io.xol.chunkstories.api.world.WorldMaster
import io.xol.chunkstories.core.entity.EntityZombie
import io.xol.z.plugin.XolioZPlugin

class ZombiesPopulation(internal var plugin: XolioZPlugin) {

    private fun countZombies() : Int {
        var zombiesCount = 0
        for (e in plugin.gameWorld.allLoadedEntities) {
            if (e is EntityZombie) {
                zombiesCount++
            }
        }

        return zombiesCount
    }

    fun spawnZombies() {
        var zombiesCount = countZombies()

        val minimalDistance = plugin.config.zombiesSpawnMinDistance
        val maximalDistance = plugin.config.zombiesSpawnMaxDistance

        for (player in plugin.gameWorld.players) {
            val playerEntity = player.controlledEntity ?: continue

            // Ignore unspawned players

            // Don't spawn zombies on players in creative mode
            if (!playerEntity.traits.tryWithBoolean(TraitCreativeMode::class.java) { creativeMode -> creativeMode.get() }) {
                var playersZombieCount = 0
                for (e in plugin.gameWorld.allLoadedEntities) {
                    if (e is EntityZombie && e.getLocation().distance(player.location) < 120f) {
                        playersZombieCount++
                    }
                }
                for (i in playersZombieCount..15) {

                    val distance = (minimalDistance + Math.random() * (maximalDistance - minimalDistance)).toInt()
                    val angle = Math.random() * 3.14 * 2.0
                    val posx = (player.location.x() + distance * Math.sin(angle)).toInt()
                    val posz = (player.location.z() + distance * Math.cos(angle)).toInt()
                    var foundGround = false
                    var posy = 255

                    val allowedMaterials = arrayOf("grass", "stone", "dirt", "sand", "wood")

                    while (posy > 0 && !foundGround) {
                        posy--

                        val v = plugin.gameWorld.peekSafely(posx, posy, posz).voxel

                        if (v!!.definition.isLiquid)
                            break

                        for (m in allowedMaterials) {
                            if (v.material.name == m) {
                                foundGround = true
                                break
                            }
                        }
                        if (v.definition.isSolid)
                            break
                    }
                    if (foundGround && zombiesCount <= plugin.config.maxZombiesOnMap) {
                        zombiesCount++
                        spawnZombie(Location(plugin.gameWorld, posx + 0.5, (posy + 1).toDouble(), posz + 0.5))
                    }
                }
            }
        }
    }

    fun spawnZombie(location: Location) {
        val world = plugin.gameWorld

        val zombie = plugin.pluginExecutionContext.content.entities().getEntityDefinition("zombie")!!.create(location) as EntityZombie

        //Fuzz the zombie's spawning health
        zombie.traits.with(TraitHealth::class.java) { eh -> eh.health = (eh.health * (0.5 + Math.random())).toFloat() }

        world.addEntity(zombie)
    }
}
