//
// This file is a part of the XolioZ Mod for Chunk Stories
// Check out README.md for more information
// Website: https://chunkstories.xyz
// Github: https://github.com/Hugobros3/xolioz
//

package io.xol.z.plugin.loot.crashes

import io.xol.chunkstories.api.Location
import io.xol.chunkstories.api.item.inventory.Inventory
import io.xol.z.plugin.XolioZPlugin
import io.xol.z.plugin.loot.LootCategory
import io.xol.z.plugin.loot.LootPlace

class Crash(plugin: XolioZPlugin, location: Location, category: LootCategory, minAmountToSpawn: Int, maxAmountToSpawn: Int) : LootPlace(plugin, location, category, minAmountToSpawn, maxAmountToSpawn) {

    internal var timeSpawned: Long = 0
    internal var graceTimeStarted: Long = -1
    internal var lootOnce = false

    init {
        this.timeSpawned = System.currentTimeMillis()
    }

    override fun shouldRespawnLoot(): Boolean {
        return !lootOnce
    }

    override fun spawnLoot() {
        super.spawnLoot()
        println("Done once.")
        lootOnce = true
    }

    fun shouldCleanup(): Boolean {
        val inv = this.containerInv ?: return true

        if (graceTimeStarted == -1L) {
            if (!inv.iterator().hasNext())
            // If empty
                graceTimeStarted = System.currentTimeMillis()
            return false
        } else {
            val now = System.currentTimeMillis()
            return now - graceTimeStarted > 5000 // 5s of grace time
        }
    }
}
