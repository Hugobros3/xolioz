//
// This file is a part of the XolioZ Mod for Chunk Stories
// Check out README.md for more information
// Website: https://chunkstories.xyz
// Github: https://github.com/Hugobros3/xolioz
//

package io.xol.z.plugin.loot;

import io.xol.chunkstories.api.Location;
import io.xol.chunkstories.api.item.inventory.Inventory;
import io.xol.chunkstories.api.voxel.components.VoxelInventoryComponent;
import io.xol.chunkstories.api.world.cell.CellData;
import io.xol.chunkstories.api.world.chunk.Chunk.ChunkCell;
import io.xol.chunkstories.core.voxel.VoxelChest;
import io.xol.z.mod.voxels.StaticVehicleVoxel;
import io.xol.z.plugin.XolioZPlugin;

import java.util.Random;

public class LootPlace {
    private final XolioZPlugin plugin;

    public final Location location;
    public LootCategory category;

    public int minAmountToSpawn = 0;
    public int maxAmountToSpawn = 0;

    public long lastUpdate = 0;

    public LootPlace(XolioZPlugin plugin, Location location, LootCategory category, int minAmountToSpawn, int maxAmountToSpawn) {
        super();
        this.plugin = plugin;
        this.location = location;
        this.category = category;
        this.minAmountToSpawn = minAmountToSpawn;
        this.maxAmountToSpawn = maxAmountToSpawn;
    }

    public boolean shouldRespawnLoot() {
        return ((System.currentTimeMillis() - lastUpdate) / 1000 > plugin.getConfig().getLootRespawnDelay());
    }

    protected Inventory getContainerInv() {
        CellData peek = location.getWorld().peekSafely(location);

        // The chunk is loaded
        if (peek instanceof ChunkCell) {
            // The voxel is the correct type
            if (peek.getVoxel() instanceof VoxelChest) {
                // return ((VoxelChest) peek.getVoxel()).getInventory((ChunkVoxelContext) peek);
                return ((VoxelInventoryComponent) ((ChunkCell) peek).components().get("chestInventory")).getInventory();
            } else if (peek.getVoxel() instanceof StaticVehicleVoxel) {
                return ((VoxelInventoryComponent) ((ChunkCell) peek).components().get("inventory")).getInventory();
            }
        }

        return null;
    }

    public void update() {
        if (shouldRespawnLoot())
            spawnLoot();
    }

    public void spawnLoot() {
        Inventory inv = getContainerInv();

        if (inv == null)
            return;

        inv.clear();
        Random rng = new Random();
        int amount2spawn = (maxAmountToSpawn - minAmountToSpawn <= 0 ? 0 : rng.nextInt(maxAmountToSpawn - minAmountToSpawn)) + minAmountToSpawn;
        while (amount2spawn > 0) {

            // Randomize position inside chest
            int positionX = rng.nextInt(inv.getWidth());
            int positionY = rng.nextInt(inv.getHeight());

            inv.setItemPileAt(positionX, positionY, category.generateItemPile());

            amount2spawn--;
        }
        lastUpdate = System.currentTimeMillis();
    }

    public String toString() {
        return "[LootPlace location: " + location + ", type:" + category + "]";
    }

    public String save() {
        return ((int) location.x) + ":" + ((int) location.y) + ":" + ((int) location.z) + ":" + category.getName() + ":" + minAmountToSpawn
                + (minAmountToSpawn == maxAmountToSpawn ? "" : ":" + maxAmountToSpawn);
    }

    public Location getLocation() {
        return location;
    }
}
