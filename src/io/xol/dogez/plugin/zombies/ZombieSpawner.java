package io.xol.dogez.plugin.zombies;

import io.xol.chunkstories.api.Location;
import io.xol.chunkstories.api.entity.Entity;
import io.xol.chunkstories.api.entity.interfaces.EntityCreative;
import io.xol.chunkstories.api.server.Player;
import io.xol.chunkstories.api.voxel.Voxel;
import io.xol.chunkstories.api.world.WorldMaster;
import io.xol.chunkstories.api.world.chunk.Chunk;
import io.xol.chunkstories.core.entity.EntityZombie;

import io.xol.dogez.plugin.DogeZPlugin;

import java.lang.reflect.Field;

//Copyright 2014 XolioWare Interactive

public class ZombieSpawner {

	DogeZPlugin plugin;
	public int zombiesCount = 0;
	
	public ZombieSpawner(DogeZPlugin p)
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
	
	public void spawnZombies()
	{
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
					if(e instanceof EntityZombie && e.getLocation().distanceTo(p.getLocation()) < 75f)
					{
						pZombCount++;
					}
				}
				for(int i = pZombCount ; i < 16; i++)
				{
					int distance = (int) (20+Math.random()*50);
					double angle = Math.random()*3.14f*2;
					int posx = (int) (p.getLocation().getX()+distance*Math.sin(angle));
					int posz = (int) (p.getLocation().getZ()+distance*Math.cos(angle));
					boolean foundGround = false;
					int posy = 255;
					
					String[] allowedMaterials = {"grass", "stone", "dirt", "sand"};
					
					while(posy > 0 && !foundGround)
					{
						posy--;
						
						Voxel v = plugin.getServer().getContent().voxels().getVoxelById(plugin.getGameWorld().getVoxelData(posx, posy, posz));
						
						if(v.isVoxelLiquid())
							break;
						
						for(String m : allowedMaterials)
						{
							if(v.getMaterial().getName().equals(m))
							{
								foundGround = true;
								break;
							}
						}
						if(v.isVoxelSolid())
							break;
					}
					if(foundGround && zombiesCount <= plugin.config.maxZombies)
					{
						zombiesCount++;
						spawnZombie(new Location(plugin.getGameWorld(), posx + 0.5, posy+1, posz + 0.5),ZombieType.NORMAL);
					}
				}
			}
		}
	}
	
	static public Field forName_Field(@SuppressWarnings("rawtypes") Class Source, String... Names)
	{
		Field[] f = Source.getDeclaredFields();
			for (Field f0 : f)
			{
				for (String s0 : Names)
				{
					if (f0.getName().equals(s0))
					return f0;
				}
			}
			if (Source.getSuperclass() == null)
			return null;
		return forName_Field(Source.getSuperclass(),Names);
	}
	
	public void spawnZombie(Location loc, ZombieType type)
	{
		WorldMaster world = plugin.getGameWorld();
		
		EntityZombie zomb = new EntityZombie(world, loc.getX(), loc.getY(), loc.getZ());
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
