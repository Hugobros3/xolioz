package io.xol.dogez.plugin.loot;

//Copyright 2014 XolioWare Interactive

import java.util.Random;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.inventory.Inventory;

import io.xol.dogez.plugin.DogeZPlugin;

public class LootPlace {

	public String type;
	public int minAmountToSpawn = 0;
	public int maxAmountToSpawn = 0;
	
	public int x;
	public int y;
	public int z;
	
	public long lastUpdate = 0;
	
	World w;
	
	public LootPlace(String ligne, World w) {
		this.w = w;
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
		return ((System.currentTimeMillis() - lastUpdate)/1000 > DogeZPlugin.config.timeBetweenReloots);
	}
	
	private Inventory getContainerInv()
	{
		Block b = w.getBlockAt(x, y, z);
		if(b.getType().equals(Material.CHEST))
		{
			Chest c = (Chest)b.getState();
			return c.getBlockInventory();
		}
		return null;
	}
	
	public void update()
	{
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
				LootType lt = LootTypes.getCategory(type).getRandomSpawn();
				if(lt != null)
				{
					int position = rng.nextInt(inv.getSize()-1);
					inv.setItem(position, lt.getItem());
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
