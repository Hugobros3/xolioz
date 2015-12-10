package io.xol.dogez.plugin.weapon;

import io.xol.dogez.plugin.game.ScheduledEvents;
import io.xol.dogez.plugin.player.PlayerProfile;
import io.xol.dogez.plugin.player.PlayersPackets;

import org.bukkit.Effect;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

//Copyright 2014 XolioWare Interactive

public class WeaponFirethrower extends Weapon{

	public WeaponFirethrower(int id, int meta, String name, String desc,boolean isSniper, boolean isAuto, float damage, float range,float soundRange, long reloadTime, long cooldown,float imprecision, int nbShots) {
		super(id, meta, name, desc, isSniper, isAuto, damage, range, soundRange,
				reloadTime, cooldown, imprecision, nbShots);
	}

	public void clickEvent(Player shooter, boolean button)
	{
		//System.out.println("Firethrower !!!");
		PlayerProfile pp = PlayerProfile.getPlayerProfile(shooter.getUniqueId().toString());
		if(!button)
		{
			if(System.currentTimeMillis() - pp.lastShoot >= cooldown)
			{
				if(shooter.getGameMode().equals(GameMode.SURVIVAL))
				{
					boolean found = false;
					for(int i = 0; i < shooter.getInventory().getSize(); i++)
					{
						ItemStack slot = shooter.getInventory().getItem(i);
						if(slot != null)
						{
							if(slot.getType().equals(Material.GOLD_HOE))
							{
								//if(slot.getDurability() == 0)
									//slot.setDurability((short) 13);
								if(slot.getDurability() > 32)
								{
									shooter.getInventory().setItem(i, null);
									shooter.updateInventory();
									return;
								}
								else
									if(Math.random() > 0.750)
										slot.setDurability((short) (slot.getDurability()+1));
								shooter.updateInventory();
								found = true;
								i = 400;
							}
						}
					}
					if(!found)
						return;
				}
				flame(shooter,pp);
				pp.lastShoot = System.currentTimeMillis();
			}
			pp.lastTick = ScheduledEvents.ticksCounter;
			//System.out.println(name+":"+pp.lastTick+":"+isAuto);
		}
	}

	private void flame(Player shooter, PlayerProfile pp) {
		Location ploc = shooter.getLocation();
		
		double angleH = (ploc.getYaw() + 90) % 360;
		double angleV = ploc.getPitch() * -1;
		//raycast shots
		for(int i = 0; i < nbShots; i++)
		{
			raycastFire(shooter,angleH+imprecision*(Math.random()-0.5),angleV+imprecision*(Math.random()-0.5)*0.8);
		}
		/*
		for(Entity e : shooter.getWorld().getEntities())
		{
			if(e != shooter && isEntityInRange(shooter,e,10f,15f))
			{
				
			}
		}*/
	}
	
	private void raycastFire(Player shooter, double angleH, double angleV) {
		Location ploc = shooter.getEyeLocation();
		double x = ploc.getX();
		double y = ploc.getY();
		double z = ploc.getZ();
		
		double distance_traveled = 0d;
		double speed = 0.4f;
		//Weapon.playSound(ploc, "fire.fire", 1f, 1f);
		while (!isSolid(shooter.getWorld().getBlockAt((int) Math.floor(x), (int) Math.floor(y), (int) Math.floor(z)),x,y,z) && distance_traveled < range) {
			x = x + Math.cos(Math.toRadians(angleH)) * speed
					* Math.cos(Math.toRadians(angleV));
			z = z + Math.sin(Math.toRadians(angleH)) * speed
					* Math.cos(Math.toRadians(angleV));
			y = y + Math.sin(Math.toRadians(angleV)) * speed;
			distance_traveled += speed;
			Location floc = new Location(shooter.getWorld(),x,y,z);

			if(Math.random() > 0.985f)
				PlayersPackets.playSound(floc, "fire.fire", 1f, 1f);
			if(distance_traveled > 1.2f)
			{
				shooter.getWorld().spigot().playEffect(floc, Effect.FLAME, 0, 0, 0, 0, 0, 0, 1, 40);
	        	//shooter.getWorld().spigot().playEffect(floc, Effect.FLAME);
				if(distance_traveled > 2.2f)
				{
					shooter.getWorld().spigot().playEffect(floc, Effect.LARGE_SMOKE, 0, 0, 0, 1, 0, 0.05f, 1, 40);
		        	//shooter.getWorld().spigot().playEffect(floc, Effect.LARGE_SMOKE);
				}
				//ParticleEffect.FLAME.display(0.1f, 0.1f, 0.1f, 0.02f, (int)distance_traveled/4+1, floc, 30);
				for(Entity ent : shooter.getWorld().getChunkAt(new Location(shooter.getWorld(),x,y,z)).getEntities())
				{
					/*if(x > ce.getHandle().boundingBox.a && y > ce.getHandle().boundingBox.b && z > ce.getHandle().boundingBox.c
							&& x < ce.getHandle().boundingBox.d && y < ce.getHandle().boundingBox.e && z < ce.getHandle().boundingBox.f && !ent.equals(shooter))*/
					
					if(floc.distance(ent.getLocation()) < 0.75f)
					{
						if (ent instanceof LivingEntity) {
							LivingEntity victim = (LivingEntity) ent;
							double h = (double)victim.getHealth();
							if(h > 0)
							{
								hitEntity(ent,shooter);
								victim.setNoDamageTicks(15);
								//spillparticle(new Location(shooter.getWorld(),x,y,z),87);
							}
						}
					}
				}
			}
			if(isSolid(shooter.getWorld().getBlockAt((int) Math.floor(x), (int) Math.floor(y), (int) Math.floor(z)),x,y,z))
			{
				x = x - Math.cos(Math.toRadians(angleH)) * speed
						* Math.cos(Math.toRadians(angleV));
				z = z - Math.sin(Math.toRadians(angleH)) * speed
						* Math.cos(Math.toRadians(angleV));
				y = y - Math.sin(Math.toRadians(angleV)) * speed;
				//Glass-breaking
				@SuppressWarnings("deprecation")
				int blockID = shooter.getWorld().getBlockAt((int) Math.floor(x), (int) Math.floor(y), (int) Math.floor(z)).getTypeId();
				if(blockID == 0)
				{
					//System.out.println("glass should break mdr");
					/*if(shooter.hasPermission("dogez.breakglass"))
					{
						//spillparticle(new Location(shooter.getWorld(),x,y,z),shooter.getWorld().getBlockAt((int) Math.floor(x), (int) Math.floor(y), (int) Math.floor(z)).getTypeId());
						ChunksCleaner.setTempTile(shooter.getWorld(), (int) Math.floor(x), (int) Math.floor(y), (int) Math.floor(z),51,(byte) 0);
					}*/
				}
			}
		}
	}

	private void hitEntity(Entity e, Player p)
	{
		//System.out.println("chiennerie");
		/*CraftEntity ce = (CraftEntity)e;
		if(ce.getHandle() instanceof EntityLiving)
		{
			EntityLiving le = (EntityLiving)ce.getHandle();
			//System.out.println("go.fuck");
			//le.damageEntity(DamageSource.BURN, 4);
			//le.dam
			le.setOnFire(15);
		}*/
		if(e instanceof LivingEntity)
		{
			LivingEntity le = (LivingEntity)e;
			le.damage(4, p);
			le.setFireTicks(15);
		}
	}
	
	public static boolean isEntityInRange(Player player, Entity target, float distance, float angle)
	{
		double dist = target.getLocation().distance(player.getLocation());
		if(dist < distance)
		{
			Vector entloc = target.getLocation().toVector();
			Vector damloc = player.getLocation().toVector();
			Vector attackdir = entloc.subtract(damloc).setY(0).normalize();
			Vector hitdir = player.getLocation().getDirection().setY(0).normalize();
			if(dist != 0)
			{
				double eAngle = (attackdir.angle(hitdir) / (Math.PI * 2) * 360);
				if(eAngle < angle)
					return true;
			}
		}
		return false;
	}
}
