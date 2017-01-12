package io.xol.dogez.plugin.zombies;

import io.xol.chunkstories.api.Location;
import io.xol.chunkstories.api.entity.Entity;
import io.xol.chunkstories.api.entity.interfaces.EntityCreative;
import io.xol.chunkstories.api.server.Player;
import io.xol.chunkstories.api.voxel.Voxel;
import io.xol.chunkstories.api.world.chunk.Chunk;
import io.xol.chunkstories.core.entity.EntityZombie;
import io.xol.chunkstories.world.WorldServer;
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
		for(Entity e : DogeZPlugin.config.getWorld().getAllLoadedEntities())
		{
			if(e instanceof EntityZombie)
			{
				zombiesCount++;
			}
		}
	}
	
	public void spawnZombies()
	{
		for(Player p : DogeZPlugin.config.getWorld().getPlayers())
		{
			if(!p.hasSpawned())
				continue;
			
			if(!(p instanceof EntityCreative) || !((EntityCreative) p).getCreativeModeComponent().isCreativeMode())
			//if(p.getGameMode().equals(GameMode.SURVIVAL))
			{
				int pZombCount = 0;
				for(Entity e : DogeZPlugin.config.getWorld().getAllLoadedEntities())
				{
					if(e instanceof EntityZombie && e.getLocation().distanceTo(p.getLocation()) < 75f)
					{
						pZombCount++;
					}
				}
				for(int i = pZombCount ; i < 16; i++)
				{
					//System.out.println("why tho"+p+" / "+p.getLocation());
					int distance = (int) (20+Math.random()*50);
					double angle = Math.random()*3.14f*2;
					int posx = (int) (p.getLocation().getX()+distance*Math.sin(angle));
					int posz = (int) (p.getLocation().getZ()+distance*Math.cos(angle));
					boolean foundGround = false;
					int posy = 255;
					
					String[] allowedMaterials = {"grass", "stone", "dirt", "sand"};
					//Material[] allowedMaterials = {Material.GRASS,Material.STONE,Material.SMOOTH_BRICK};
					while(posy > 0 && !foundGround)
					{
						posy--;
						//Block b = DogeZPlugin.config.getWorld().getBlockAt(posx, posy, posz);
						Voxel v = DogeZPlugin.access.getServer().getContent().voxels().getVoxelById(DogeZPlugin.config.getWorld().getVoxelData(posx, posy, posz));
						
						if(v.isVoxelLiquid())
							break;
						
						for(String m : allowedMaterials)
						{
							if(v.getMaterial().getName().equals(m))
							{
								//System.out.println("Found ground : "+v.getId()+" mat"+v.getMaterial().getName());
								foundGround = true;
								break;
							}
						}
						if(v.isVoxelSolid())
							break;
						
						/*for(Material m : allowedMaterials)
						{
							if(m.equals(b.getType()))
								foundGround = true;
						}*/
						//trolol+=b.getTypeId();
					}
					if(foundGround && zombiesCount <= DogeZPlugin.config.maxZombies)
					{
						zombiesCount++;
						if(DogeZPlugin.config.getWorld().getTime() > 12500 && DogeZPlugin.config.getWorld().getTime() < 23000)
						{
							double d = Math.random();
							if(d > 0.98f)
								spawnZombie(new Location(DogeZPlugin.config.getWorld(), posx, posy+1, posz),ZombieType.RIDER);
							else if(d > 0.93f)
								spawnZombie(new Location(DogeZPlugin.config.getWorld(), posx, posy+1, posz),ZombieType.SKELY);
							else if(d > 0.85f)
								spawnZombie(new Location(DogeZPlugin.config.getWorld(), posx, posy+1, posz),ZombieType.BLACK);
							else if(d > 0.70f)
								spawnZombie(new Location(DogeZPlugin.config.getWorld(), posx, posy+1, posz),ZombieType.SPIDEY);
							else
								spawnZombie(new Location(DogeZPlugin.config.getWorld(), posx, posy+1, posz),ZombieType.NORMAL);
						}
						else
							spawnZombie(new Location(DogeZPlugin.config.getWorld(), posx, posy+1, posz),ZombieType.NORMAL);
					}
					//System.out.println("debug: spawning zombie2 "+foundGround+" at "+posx+":"+posy+":"+posz);
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
		//World world = ((CraftWorld) loc.getWorld()).getHandle();
		/*CustomEntityCreature zomb = null;
		if(type.equals(ZombieType.NORMAL))
		{
			zomb = CustomEntities.getNewCustomEntityZombie(loc);
			zomb.removeGoalSelectorPathfinderGoalAll();
			zomb.newGoalSelectorPathfinderGoalRandomStroll(0.5f);
			zomb.newGoalSelectorPathfinderGoalLookAtPlayer(15);
			zomb.newGoalSelectorPathfinderGoalRestrictOpenDoorDefault();
			zomb.newGoalSelectorPathfinderGoalMeleeAttack("EntityHuman", 1.0D, false);//Makes the entity attack Players
			//zomb.newGoalSelectorPathfinderGoalMeleeAttack(EntityName.ENTITYPLAYER, 2d, true);
		}
		else if(type.equals(ZombieType.RIDER))
		{
			zomb = CustomEntities.getNewCustomEntityHorse(loc);
			zomb.removeGoalSelectorPathfinderGoalAll();
			//zomb.newGoalSelectorPathfinderGoalMoveTowardsTarget(2.5, 15);
			zomb.newGoalSelectorPathfinderGoalRandomStroll(0.5f);
			zomb.newGoalSelectorPathfinderGoalLookAtPlayer(35);
			zomb.newGoalSelectorPathfinderGoalMoveTowardsTarget(2d, 50);
			zomb.setHealth(zomb.getHealth()*1.5f);
			zomb.newGoalSelectorPathfinderGoalMeleeAttack("EntityHuman", 2.5D, false);
			
				CustomEntityCreature rider = CustomEntities.getNewCustomEntityPigZombie(loc);
				rider.removeGoalSelectorPathfinderGoalAll();
				rider.newGoalSelectorPathfinderGoalRandomStroll(0.5f);
				rider.newGoalSelectorPathfinderGoalLookAtPlayer(15);
				rider.newGoalSelectorPathfinderGoalRestrictOpenDoorDefault();
				rider.newGoalSelectorPathfinderGoalMeleeAttack("EntityHuman", 1.2D, false);
				((org.bukkit.entity.PigZombie)rider.getBukkitEntity()).getEquipment().setItemInHand(new ItemStack(267));
				zomb.getBukkitEntity().setPassenger(rider.getBukkitEntity());
			
			//zomb.newGoalSelectorPathfinderGoalMeleeAttack(EntityName.ENTITYPLAYER, 1d, true);
			//zomb.newGoalSelectorPathfinderGoalMeleeAttack("EntityHuman", 1.0D, false);
		}
		else if(type.equals(ZombieType.BLACK))
		{
			zomb = CustomEntities.getNewCustomEntityPigZombie(loc);
			zomb.removeGoalSelectorPathfinderGoalAll();
			zomb.newGoalSelectorPathfinderGoalRandomStroll(0.5f);
			zomb.newGoalSelectorPathfinderGoalLookAtPlayer(15);
			zomb.newGoalSelectorPathfinderGoalRestrictOpenDoorDefault();
			zomb.newGoalSelectorPathfinderGoalMeleeAttack("EntityHuman", 1.2D, false);//Makes the entity attack Players
			((org.bukkit.entity.PigZombie)zomb.getBukkitEntity()).setAnger(500);
			((org.bukkit.entity.PigZombie)zomb.getBukkitEntity()).getEquipment().setItemInHand(new ItemStack(267));
		}
		else if(type.equals(ZombieType.SKELY))
		{
			zomb = CustomEntities.getNewCustomEntitySkeleton(loc);
			//zomb.removeGoalSelectorPathfinderGoalAll();
			//((CustomEntitySkeleton) zomb).removeGoalSelectorPathfinderGoalArrowAttack();
		}
		else if(type.equals(ZombieType.SPIDEY))
		{
			zomb = CustomEntities.getNewCustomEntitySpider(loc);
			zomb.removeGoalSelectorPathfinderGoalAll();
			zomb.setHealth(zomb.getHealth()*1.5f);
			zomb.newGoalSelectorPathfinderGoalRandomStroll(0.4f);
			zomb.newGoalSelectorPathfinderGoalLookAtPlayer(10);
			zomb.newGoalSelectorPathfinderGoalMeleeAttack("EntityHuman", 1.3D, false);
		}
		else if(type.equals(ZombieType.GIANT))
		{
			zomb = CustomEntities.getNewCustomEntityGiantZombie(loc);
			zomb.removeGoalSelectorPathfinderGoalAll();
			zomb.setHealth(zomb.getHealth()*10);
			zomb.newGoalSelectorPathfinderGoalRandomStroll(0.2f);
			zomb.newGoalSelectorPathfinderGoalLookAtPlayer(15);
			zomb.newGoalSelectorPathfinderGoalMeleeAttack("EntityHuman", 0.3D, false);
		}
		if(zomb == null)
			return;
		ZombieType.prepareToSpawn(type, zomb);
		//mod-me !
		zomb.setHealth(zomb.getHealth()/2);
		*/
		WorldServer world = DogeZPlugin.config.getWorld();
		
		EntityZombie zomb = new EntityZombie(world, loc.getX(), loc.getY(), loc.getZ());
		//zomb.locX = loc.getX();
		//zomb.locY = loc.getY();
		//zomb.locZ = loc.getZ();
		
		//zomb.setLocation(loc.getX(), loc.getY(), loc.getZ(),loc.getPitch(), loc.getYaw());
		zomb.setHealth(zomb.getHealth()/2);
		//zomb.spawnIn(world);
		world.addEntity(zomb);
	}
	
	public void cleanChunk(Chunk c)
	{
		for(Entity e : c.getEntitiesWithinChunk())
		{
			if(e instanceof EntityZombie)
			{
				e.removeFromWorld();
			}
		}
	}
}
