package io.xol.dogez.plugin.loot;

//Copyright 2014 XolioWare Interactive

import java.util.Random;

import io.xol.chunkstories.api.item.inventory.Inventory;
import io.xol.chunkstories.api.voxel.components.VoxelInventoryComponent;
import io.xol.chunkstories.api.world.VoxelContext;
import io.xol.chunkstories.api.world.World;
import io.xol.chunkstories.api.world.chunk.Chunk.ChunkVoxelContext;
import io.xol.chunkstories.core.voxel.VoxelChest;

public class LootPlace {

	public String type;
	public int minAmountToSpawn = 0;
	public int maxAmountToSpawn = 0;
	
	public int x;
	public int y;
	public int z;
	
	public long lastUpdate = 0;
	
	World world;
	
	private final LootPlaces parent;
	
	public LootPlace(LootPlaces dad, String ligne, World world) {
		
		this.parent = dad;
		this.world = world;
		String[] split = ligne.split(":");
		if(split.length >= 5)
		{
			// x:y:z:type:minamount:max
			x = Integer.parseInt(split[0]);
			y = Integer.parseInt(split[1]);
			z = Integer.parseInt(split[2]);
			type = split[3];
			minAmountToSpawn = Integer.parseInt(split[4]);
			if(split.length >= 6)
				maxAmountToSpawn = Integer.parseInt(split[5]);
			else
				maxAmountToSpawn = minAmountToSpawn;
		}
	}

	public boolean shouldReloot(){
		return ((System.currentTimeMillis() - lastUpdate)/1000 > parent.getPlugin().config.timeBetweenReloots);
	}
	
	private Inventory getContainerInv()
	{
		VoxelContext peek = world.peekSafely(x, y, z);
		
		//The chunk is loaded
		if(peek instanceof ChunkVoxelContext) {
			//The voxel is the correct type
			if(peek.getVoxel() instanceof VoxelChest)// || peek.getVoxel() instanceof StaticVehicleVoxel)
			{
				return ((VoxelInventoryComponent)((ChunkVoxelContext) peek).components().get("inventory")).getInventory();
			}
		}
		
		return null;
	}
	
	public void update()
	{
		//System.out.println("Updating place "+this);
		if(shouldReloot())
		{
			Inventory inv = getContainerInv();
			if(inv == null)
				return;
			inv.clear();
			Random rng = new Random();
			int amount2spawn = (maxAmountToSpawn-minAmountToSpawn == 0 ? 0 : rng.nextInt(maxAmountToSpawn-minAmountToSpawn))+minAmountToSpawn;
			while(amount2spawn > 0)
			{
				LootType lt = parent.getPlugin().getLootTypes().getCategory(type).getRandomSpawn();
				if(lt != null)
				{
					//Randomize position inside chest
					int positionX = rng.nextInt(inv.getWidth());
					int positionY = rng.nextInt(inv.getHeight());
					
					inv.setItemPileAt(positionX, positionY, lt.getItem());
					//int position = rng.nextInt(inv.getSize()-1);
					//inv.setItem(position, lt.getItem());
				}
				amount2spawn--;
			}
			lastUpdate = System.currentTimeMillis();
		}
	}
	
	public String toString()
	{
		return "(x="+x+",y="+y+",z="+z+",type="+type+")";
	}

	public String save() {
		return x+":"+y+":"+z+":"+type+":"+minAmountToSpawn+(minAmountToSpawn == maxAmountToSpawn ? "" : ":"+maxAmountToSpawn);
	}
}
