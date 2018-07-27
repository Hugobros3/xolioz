//
// This file is a part of the XolioZ Mod for Chunk Stories
// Check out README.md for more information
// Website: https://chunkstories.xyz
// Github: https://github.com/Hugobros3/xolioz
//

package io.xol.z.plugin.loot.crashes

import java.util.HashMap
import java.util.HashSet

import io.xol.chunkstories.api.Location
import io.xol.chunkstories.api.entity.Entity
import io.xol.chunkstories.api.entity.traits.TraitVoxelSelection
import io.xol.chunkstories.api.events.EventHandler
import io.xol.chunkstories.api.events.Listener
import io.xol.chunkstories.api.events.player.PlayerInputPressedEvent
import io.xol.chunkstories.api.events.player.voxel.PlayerVoxelModificationEvent
import io.xol.chunkstories.api.input.Input
import io.xol.chunkstories.api.player.Player
import io.xol.chunkstories.api.voxel.Voxel
import io.xol.chunkstories.api.world.cell.CellData
import io.xol.z.mod.voxels.StaticVehicleVoxel
import io.xol.z.plugin.XolioZPlugin
import io.xol.z.plugin.loot.LootCategory

/** Remebers where, when and what was inside of a crash site Responsible for
 * creating and (eventually ?) removing them  */
class CrashesHandler(private val xolioZGamemodePlugin: XolioZPlugin) : Listener {
    private val registeredVoxels = HashSet<StaticVehicleVoxel>()

    private val crashes = HashMap<Location, Crash>()

    init {

        val i = xolioZGamemodePlugin.pluginExecutionContext.content.voxels().all()
        while (i.hasNext()) {
            val voxel = i.next()
            if (voxel is StaticVehicleVoxel) {
                registeredVoxels.add(voxel)
            }
        }
    }

    @EventHandler
    fun onPlayerPoke(event: PlayerVoxelModificationEvent) {
        if (!event.context.voxel!!.isAir) {
            val placed = event.context.voxel
            if (registeredVoxels.contains(placed)) {
                if (event.context.metaData == 0) {
                    val vehicleType = placed as StaticVehicleVoxel?
                    val category = xolioZGamemodePlugin.lootCategories.getCategory(vehicleType!!.lootCategoryName)
                            ?: return

                    println("Created crash object, calling spawnLoot")
                    val crash = Crash(xolioZGamemodePlugin, event.context.location, category, vehicleType.lootAmountMin,
                            vehicleType.lootAmountMax)
                    // crash.spawnLoot();
                    crashes[event.context.location] = crash
                }
            }
        } else {
            val removed = event.context.voxel
            if (registeredVoxels.contains(removed)) {
                if (event.context.metaData == 0) {
                    crashes.remove(event.context.location)
                }
            }
        }
    }

    @EventHandler
    fun onPlayerInput(event: PlayerInputPressedEvent) {

        // We are only interested in mouse clicks
        val input = event.input
        if (!input.name.startsWith("mouse."))
            return

        val player = event.player

        val playerEntity = player.controlledEntity
        val selectedLocation = playerEntity!!.traits.tryWith<TraitVoxelSelection, Location>(TraitVoxelSelection::class.java) { tvs -> tvs.getBlockLookingAt(true, false) }

        if (selectedLocation != null) {
            val context = player.world.peekSafely(selectedLocation)

            // loot regeneration
            val voxel = player.world.peekSafely(selectedLocation).voxel
            if (voxel is StaticVehicleVoxel) {
                val vehicleType = voxel as StaticVehicleVoxel?
                println("lookin for the crash " + crashes.size)

                val x = context.x
                val y = context.y
                val z = context.z

                // Backpedal to find the root block
                val meta = context.metaData

                val ap = meta shr vehicleType!!.xShift and vehicleType.xMask
                val bp = meta shr vehicleType.yShift and vehicleType.yMask
                val cp = meta shr vehicleType.zShift and vehicleType.zMask

                val startX = x - ap
                val startY = y - bp
                val startZ = z - cp

                val crash = crashes[Location(context.world, startX.toDouble(), startY.toDouble(), startZ.toDouble())]
                crash?.update()
            }
        }

    }
}
