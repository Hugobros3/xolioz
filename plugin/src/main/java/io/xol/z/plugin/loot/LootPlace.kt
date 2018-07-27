//
// This file is a part of the XolioZ Mod for Chunk Stories
// Check out README.md for more information
// Website: https://chunkstories.xyz
// Github: https://github.com/Hugobros3/xolioz
//

package io.xol.z.plugin.loot

import io.xol.chunkstories.api.Location
import io.xol.chunkstories.api.item.inventory.Inventory
import io.xol.chunkstories.api.voxel.components.VoxelInventoryComponent
import io.xol.chunkstories.api.world.chunk.Chunk.ChunkCell
import io.xol.chunkstories.core.voxel.VoxelChest
import io.xol.z.mod.voxels.StaticVehicleVoxel
import io.xol.z.plugin.XolioZPlugin
import java.util.*

open class LootPlace(private val plugin: XolioZPlugin, val location: Location, var category: LootCategory, var minAmountToSpawn: Int, var maxAmountToSpawn: Int) {

    var lastUpdate: Long = 0

    protected
    val containerInv: Inventory?
        get() {
            val peek = location.getWorld().peekSafely(location)
            if (peek is ChunkCell) {
                if (peek.getVoxel() is VoxelChest) {
                    return (peek.components().get("chestInventory") as VoxelInventoryComponent).inventory
                } else if (peek.getVoxel() is StaticVehicleVoxel) {
                    return (peek.components().get("inventory") as VoxelInventoryComponent).inventory
                }
            }

            return null
        }

    open fun shouldRespawnLoot(): Boolean {
        return (System.currentTimeMillis() - lastUpdate) / 1000 > plugin.config.lootRespawnDelay
    }

    fun update() {
        if (shouldRespawnLoot())
            spawnLoot()
    }

    open fun spawnLoot() {
        val inv = containerInv ?: return

        inv.clear()
        val rng = Random()
        var amount2spawn = (if (maxAmountToSpawn - minAmountToSpawn <= 0) 0 else rng.nextInt(maxAmountToSpawn - minAmountToSpawn)) + minAmountToSpawn
        while (amount2spawn > 0) {

            // Randomize position inside chest
            val positionX = rng.nextInt(inv.width)
            val positionY = rng.nextInt(inv.height)

            inv.setItemPileAt(positionX, positionY, category.generateItemPile())

            amount2spawn--
        }
        lastUpdate = System.currentTimeMillis()
    }

    override fun toString(): String {
        return "[LootPlace location: $location, type:$category]"
    }

    fun save(): String {
        return (location.x.toInt().toString() + ":" + location.y.toInt() + ":" + location.z.toInt() + ":" + category.name + ":" + minAmountToSpawn
                + if (minAmountToSpawn == maxAmountToSpawn) "" else ":$maxAmountToSpawn")
    }
}
