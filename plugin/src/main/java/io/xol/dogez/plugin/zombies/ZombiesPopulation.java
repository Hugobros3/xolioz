package io.xol.dogez.plugin.zombies;

import io.xol.chunkstories.api.Location;
import io.xol.chunkstories.api.entity.Entity;
import io.xol.chunkstories.api.entity.interfaces.EntityCreative;
import io.xol.chunkstories.api.player.Player;
import io.xol.chunkstories.api.voxel.Voxel;
import io.xol.chunkstories.api.world.WorldMaster;
import io.xol.chunkstories.api.world.chunk.Chunk;
import io.xol.chunkstories.core.entity.EntityZombie;

import io.xol.dogez.plugin.DogeZPlugin;

//Copyright 2014 XolioWare Interactive

public class ZombiesPopulation {

	DogeZPlugin plugin;
	public int zombiesCount = 0;
	
	public ZombiesPopulation(DogeZPlugin p)
	{
		plugin = p;
	}
	
	public void countZombies()
	{
		zombiesCount = 0;
		for(Entity e : plugin.getGameWorld().getAllLoadedEntities())
		{
			if(e instanceof EntityZombie)
			{
				zombiesCount++;
			}
		}
	}
	
	public void unspawnOldZombies() {
		
	}
	
	public void spawnZombies()
	{
		int minimalDistance = plugin.config.getInt("zombiesSpawnMinDistance");
		int maximalDistance = plugin.config.getInt("zombiesSpawnMaxDistance");
		
		for(Player p : plugin.getGameWorld().getPlayers())
		{
			if(!p.hasSpawned())
				continue;
			
			//Don't spawn zombies on players in creative mode
			if(!(p instanceof EntityCreative) || !((EntityCreative) p).getCreativeModeComponent().get())
			{
				int pZombCount = 0;
				for(Entity e : plugin.getGameWorld().getAllLoadedEntities())
				{
					if(e instanceof EntityZombie && e.getLocation().distance(p.getLocation()) < 120f)
					{
						pZombCount++;
					}
				}
				for(int i = pZombCount ; i < 16; i++)
				{
					
					int distance = (int) (minimalDistance+Math.random()*(maximalDistance - minimalDistance));
					double angle = Math.random()*3.14f*2;
					int posx = (int) (p.getLocation().x()+distance*Math.sin(angle));
					int posz = (int) (p.getLocation().z()+distance*Math.cos(angle));
					boolean foundGround = false;
					int posy = 255;
					
					String[] allowedMaterials = {"grass", "stone", "dirt", "sand", "wood"};
					
					while(posy > 0 && !foundGround)
					{
						posy--;
						
						Voxel v = plugin.getGameWorld().peekSafely(posx, posy, posz).getVoxel();
						
						if(v.getType().isLiquid())
							break;
						
						for(String m : allowedMaterials)
						{
							if(v.getMaterial().getName().equals(m))
							{
								foundGround = true;
								break;
							}
						}
						if(v.getType().isSolid())
							break;
					}
					if(foundGround && zombiesCount <= plugin.config.maxZombies)
					{
						zombiesCount++;
						spawnZombie(new Location(plugin.getGameWorld(), posx + 0.5, posy+1, posz + 0.5));
					}
				}
			}
		}
	}
	
	public void spawnZombie(Location loc)
	{
		WorldMaster world = plugin.getGameWorld();
		
		EntityZombie zomb = new EntityZombie(plugin.getPluginExecutionContext().getContent().entities().getEntityTypeByName("zombie"), loc);
		zomb.setHealth((float) (zomb.getHealth() * (0.5 + Math.random())));
		
		world.addEntity(zomb);
	}
	
	public void cleanChunk(Chunk c)
	{
		for(Entity entity : c.getEntitiesWithinChunk())
		{
			if(entity instanceof EntityZombie)
			{
				entity.getWorld().removeEntity(entity);
			}
		}
	}
}
