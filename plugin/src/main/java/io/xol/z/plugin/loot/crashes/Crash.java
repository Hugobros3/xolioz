//
// This file is a part of the XolioZ Mod for Chunk Stories
// Check out README.md for more information
// Website: https://chunkstories.xyz
// Github: https://github.com/Hugobros3/xolioz
//

package io.xol.z.plugin.loot.crashes;

import io.xol.chunkstories.api.Location;
import io.xol.chunkstories.api.item.inventory.Inventory;
import io.xol.z.plugin.XolioZPlugin;
import io.xol.z.plugin.loot.LootCategory;
import io.xol.z.plugin.loot.LootPlace;

public class Crash extends LootPlace {

	long timeSpawned;
	long graceTimeStarted = -1;
	boolean lootOnce = false;

	public Crash(XolioZPlugin plugin, Location location, LootCategory category, int minAmountToSpawn, int maxAmountToSpawn) {
		super(plugin, location, category, minAmountToSpawn, maxAmountToSpawn);
		this.timeSpawned = System.currentTimeMillis();
	}

	@Override
	public boolean shouldRespawnLoot() {
		return !lootOnce;
	}

	@Override
	public void spawnLoot() {
		super.spawnLoot();
		System.out.println("Done once.");
		lootOnce = true;
	}

	public boolean shouldCleanup() {
		Inventory inv = this.getContainerInv();
		if (inv == null)
			return true;

		if (graceTimeStarted == -1) {
			if (!inv.iterator().hasNext()) // If empty
				graceTimeStarted = System.currentTimeMillis();
			return false;
		} else {
			long now = System.currentTimeMillis();
			return (now - graceTimeStarted) > 5000; // 5s of grace time
		}
	}
}
