package io.xol.dogez.plugin.game;

//(c) 2014 XolioWare Interactive

import java.util.ArrayList;
import java.util.List;

import io.xol.dogez.plugin.DogeZPlugin;
import io.xol.dogez.plugin.game.special.DeathRewards;
import io.xol.dogez.plugin.misc.TimeFormatter;
import io.xol.dogez.plugin.player.PlayerProfile;
import io.xol.dogez.plugin.weapon.ChunksCleaner;
import io.xol.dogez.plugin.weapon.Weapon;
import io.xol.dogez.plugin.zombies.ZombieType;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Egg;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityCombustEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerEggThrowEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.scheduler.BukkitScheduler;

public class EntityListener implements Listener{
	
	public DogeZPlugin plugin;
	//This class takes care of the entity-related crap.
	
	@EventHandler // No fire on zombies.
	public void onEntityCatchFire(EntityCombustEvent event) {
		if (!event.getEntity().getWorld().getName().equals(DogeZPlugin.config.activeWorld))
			return;
		if(!DogeZPlugin.isActive())
			return;
		Entity entity = event.getEntity();
		if (entity.getType().equals(EntityType.ZOMBIE) || entity.getType().equals(EntityType.SKELETON)) {
			event.setCancelled(true);
		}
	}
	//special spawn rules
	@EventHandler
	public void onCreatureSpawn(CreatureSpawnEvent event) {
		if (!event.getEntity().getWorld().getName().equals(DogeZPlugin.config.activeWorld))
			return;
		if(!DogeZPlugin.isActive())
			return;
		Entity entity = event.getEntity();
		if (entity.getType().equals(EntityType.ZOMBIE)) {
			if(!event.getSpawnReason().equals(SpawnReason.CUSTOM) && !event.getSpawnReason().equals(SpawnReason.SPAWNER_EGG))
			{
				//System.out.println("debug: Blocked normal spawning of zombie at "+event.getLocation().toString());
				event.setCancelled(true);
			}
			else
			{
				if(event.getSpawnReason().equals(SpawnReason.SPAWNER_EGG))
				{
					//System.out.println("debug: Requested spawning of custom DogeZ zombie at "+event.getLocation().toString());
					DogeZPlugin.spawner.spawnZombie(event.getLocation(), ZombieType.NORMAL);
					event.setCancelled(true);
				}
			}
		}
		else
		{
			if(event.getSpawnReason().equals(SpawnReason.CUSTOM) || event.getSpawnReason().equals(SpawnReason.SPAWNER_EGG))
			{
				return;
			}
			for(String m : DogeZPlugin.config.toleratedMobs)
			{
				if(entity.getType().name().equals(m))
					return;
			}
			//System.out.println("debug: Mobtype "+entity.getType().name()+" was not found in allowed mobs table. Cancelling spawn...");
			event.setCancelled(true);
		}
	}
	//zombies don't loot and count kills
	@EventHandler
	public void onEntityDeath(EntityDeathEvent event) {
		if (!event.getEntity().getWorld().getName().equals(DogeZPlugin.config.activeWorld))
			return;
		if(!DogeZPlugin.isActive())
			return;
		event.setDroppedExp(0);
		Entity entity = event.getEntity();
		if (entity.getType().equals(EntityType.ZOMBIE) || entity.getType().equals(EntityType.SKELETON)
				|| entity.getType().equals(EntityType.GIANT) || entity.getType().equals(EntityType.HORSE)
					|| entity.getType().equals(EntityType.SPIDER) || entity.getType().equals(EntityType.PIG_ZOMBIE)) {
			event.getDrops().clear();
			EntityDamageEvent cause = entity.getLastDamageCause();
			if(cause != null && cause.getCause().equals(DamageCause.ENTITY_ATTACK))
			{
				if(event.getEntity().getKiller().getType().equals(EntityType.PLAYER))
				{
					Player player = event.getEntity().getKiller();
					PlayerProfile pp = PlayerProfile.getPlayerProfile(player.getUniqueId().toString());
					pp.zombiesKilled++;
					pp.zombiesKilled_thisLife++;
					int xcwon = 2+(Math.random() > 0.5 ? 1 : 0);
					if(entity.getType().equals(EntityType.GIANT))
						xcwon = 100;
					if(pp.inGame)
						pp.addBalance(xcwon);
					event.getEntity().setCustomName(ChatColor.BLUE+"+"+xcwon+"!");
					event.getEntity().setCustomNameVisible(true);
				}
			}
		}
		if(entity.getType().equals(EntityType.SHEEP))
		{
			event.getDrops().clear();
		}
		if (entity.getType().equals(EntityType.PLAYER)) {
			Player victim = (Player)entity;
			PlayerProfile victimProfile = PlayerProfile.getPlayerProfile(victim.getUniqueId().toString());
			
			if(!victimProfile.inGame)
				return;
			
			victimProfile.playersKilled_thisLife = 0;
			victimProfile.zombiesKilled_thisLife = 0;
			victimProfile.deaths++;
			
			EntityDamageEvent cause = entity.getLastDamageCause();
			if(cause != null && cause.getCause().equals(DamageCause.ENTITY_ATTACK))
			{
				if(event.getEntity().getKiller() != null && event.getEntity().getKiller().getType().equals(EntityType.PLAYER))
				{
					Player player = event.getEntity().getKiller();
					PlayerProfile killerProfile = PlayerProfile.getPlayerProfile(player.getUniqueId().toString());
					
					if(DeathRewards.isPlayerKillingMachine(player))
						DeathRewards.onKill(player, killerProfile, victim);
					
					if(DeathRewards.isPlayerKillingMachine(victim))
						DeathRewards.onDeath(victim, victimProfile, player, killerProfile);
					killerProfile.playersKilled++;
					killerProfile.playersKilled_thisLife++;
					float lostMoney = (float) (victimProfile.xcBalance*0.05f);
					lostMoney*=100;
					lostMoney = (float) Math.floor(lostMoney);
					lostMoney/=100f;
					if(lostMoney > 500)
						lostMoney = 500;
					victimProfile.xcBalance-=lostMoney;
					killerProfile.xcBalance+=lostMoney;
					
					victimProfile.timeSurvivedLife = 0l;
					player.sendMessage(ChatColor.AQUA+"Vous avez tué "+victim.getName()+" et recu 5% de son argent, soit "+
							lostMoney+"xc.");
					victim.sendMessage(ChatColor.RED+"Vous êtes mort. Vos statistiques ont été réinitialisées et votre tueur à recu 5% de votre argent, soit "+
							lostMoney+"xc.");
					
					//Spawn victim's head
					ItemStack head = new ItemStack(Material.SKULL_ITEM, 1 , (short) 3);
					SkullMeta meta = (SkullMeta) head.getItemMeta();
					meta.setOwner(victim.getName());
					List<String> list = new ArrayList<String>();
					list.add(ChatColor.AQUA+"Tué le "+TimeFormatter.getCurrentDate());
					if(victim.getLocation().distance(player.getLocation()) < 30f)
						list.add(ChatColor.RED+"Par "+player.getName());
					else
						list.add(ChatColor.RED+"Trop loin pour savoir :s");
					meta.setLore(list);
					head.setItemMeta(meta);
					victim.getWorld().dropItemNaturally(victim.getLocation(), head);
					//return;

					DeathRewards.onDeath(victim, victimProfile, player, killerProfile);
				}
				else
					DeathRewards.onDeath(victim, victimProfile, null, null);
			}

			
			victimProfile.saveProfile();
			victim.sendMessage(ChatColor.RED+"Vous êtes mort. Vos statistiques ont été réinitialisées.");

			victimProfile.goToHell = false;
			double minimalRandom = 0.9d - Math.max(0.85, Math.min(0.0, 30-victimProfile.getTimeAlive() / 30.0 ));
			
			//System.out.println("+mr:"+minimalRandom);
			
			if(victimProfile.inGame && Math.random() > minimalRandom)
			{
				System.out.println("gotoHell");
				victimProfile.goToHell = true;
			}
			victimProfile.inGame = false;
			
			//Explosive chest
			/*if(victim.getInventory().contains(Material.GOLD_CHESTPLATE)
					|| event.getDrops().contains(Material.GOLD_CHESTPLATE))
			{
				System.out.println("kboom mdr");
				Location l = victim.getLocation();
				victim.getWorld().createExplosion(l.getX(), l.getY(), l.getZ(), 8f, false, false);
				victim.getWorld().spigot().playEffect(l, Effect.LARGE_SMOKE, 0, 0, 1, 1, 1, 0.5f, 35, 40);
			}*/
		}
	}
	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent event){
		event.setDeathMessage(((Player) event.getEntity()).getName()+" à été tué.");
	}
	
	//Change some items damage
	@EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event)
    {
        if (event.getDamager() instanceof Arrow)
        {
            event.setDamage(event.getDamage() + 10);
        }
    }
	
	@EventHandler
    public void onDamage(EntityDamageEvent event){
		if(event.getEntity() instanceof LivingEntity)
		{
			LivingEntity entity = (LivingEntity)event.getEntity();
	        if (entity.getLastDamageCause() != null){
	        	if(entity.getLastDamageCause().getEntity() != null){
	        		if(entity.getLastDamageCause().getEntity() instanceof Player) { 
	            Player player = (Player)entity.getLastDamageCause().getEntity();
	            //anti combat log
	            PlayerProfile pp = PlayerProfile.getPlayerProfile(player.getUniqueId().toString());
	            if(pp != null)
	            {
	            	if(System.currentTimeMillis() - pp.lastHitTime > 120*1000L)
	            		player.sendMessage(ChatColor.RED+"Vous avez prit un coup. Vous ne pouvez plus vous déconnecter 10s après chaque coup, pour éviter le déco combat.");
	            	pp.lastHitTime = System.currentTimeMillis();
	            	
	            }
	            ItemStack item = player.getItemInHand();
	            if(item != null)
	            {
	            	/*if(item.getType().equals(Material.GOLD_SWORD))
	            	{
		            	entity.setNoDamageTicks(0);
	            		entity.damage(6.5d*2,player);
	            	}
	            	if(item.getType().equals(Material.BLAZE_ROD))
	            	{
		            	entity.setNoDamageTicks(0);
	            		entity.damage(5.5d*2,player);
	            	}*/
	            }
	        }}}
		}
    }
	
	//Grenade explosion
	@EventHandler
	public void onProjectileHit(ProjectileHitEvent e)
	{
		if(e.getEntity() instanceof Snowball)
		{
			final Snowball grenade = (Snowball)e.getEntity();
			ProjectileSource source = grenade.getShooter();
			if(source instanceof Player)
			{
				final Player thrower = (Player)source;
				BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
		        scheduler.runTaskLater(plugin, new Runnable() {
			    	@SuppressWarnings("deprecation")
			        public void run() {
			    		Location l = grenade.getLocation();
			        	grenade.getWorld().createExplosion(l.getX(), l.getY(), l.getZ(), 4f, false, false);
			        	//entity damage
			        	for(Entity e : grenade.getWorld().getEntities())
			        	{
			        		if(e.getLocation().distanceSquared(l) < 6*6)
			        		{
			        			if(e instanceof LivingEntity)
			        			{
			        				((LivingEntity) e).damage(25-15*Math.min(1, e.getLocation().distanceSquared(l)/6*2),thrower);
			        			}
			        		}
			        	}
			        	//glass explosion
			        	for(int sx = l.getBlockX()-2; sx < l.getBlockX()+2; sx++)
			        	{
			        		for(int sy = l.getBlockY()-2; sy < l.getBlockY()+2; sy++)
				        	{
			        			for(int sz = l.getBlockZ()-2; sz < l.getBlockZ()+2; sz++)
					        	{
									int blockID = grenade.getWorld().getBlockAt(sx, sy, sz).getTypeId();
			        				if(blockID == 102 || blockID == 20 || blockID == 95 || blockID == 160)
			        				{
			        					Weapon.spillparticle(new Location(grenade.getWorld(),sx,sy,sz),grenade.getWorld().getBlockAt((int) Math.floor(sx), (int) Math.floor(sy), (int) Math.floor(sz)).getTypeId());
			        					ChunksCleaner.breakGlass(grenade.getWorld(), (int) Math.floor(sx), (int) Math.floor(sy), (int) Math.floor(sz));
			        				}
					        	}
				        	}
			        	}
			        	//Smoke
			        	grenade.getWorld().spigot().playEffect(grenade.getLocation(), Effect.FLAME, 0, 0, 1, 1, 1, 0.5f, 15, 40);
			        	grenade.getWorld().spigot().playEffect(grenade.getLocation(), Effect.LARGE_SMOKE, 0, 0, 1, 1, 1, 0.5f, 35, 40);
			        	//grenade.getWorld().spigot().playEffect(grenade.getLocation(), Effect.LARGE_SMOKE);
			        	//grenade.getWorld().spigot().playEffect(grenade.getLocation(), Effect.EXPLOSION_HUGE, 0, 0, 3, 3, 3, 0, 1, 40);
			        	
			        }
		    	}, (long)(20*1.8));
			}
			else
				return;
		}
		if(e.getEntity() instanceof Egg)
		{
			final Egg flash = (Egg)e.getEntity();
			ProjectileSource source = flash.getShooter();
			if(source instanceof Player)
			{
				//final Player thrower = (Player)source;
		        BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
		        scheduler.runTaskLater(plugin, new Runnable() {
			    	@SuppressWarnings("deprecation")
			        public void run() {
			    		Location l = flash.getLocation();
			        	flash.getWorld().createExplosion(l.getX(), l.getY(), l.getZ(), 3f, false, false);
			        	//entity damage
			        	for(Entity e : flash.getWorld().getEntities())
			        	{
			        		if(e.getLocation().distanceSquared(l) < 6*6)
			        		{
			        			if(e instanceof LivingEntity)
			        			{
			        				LivingEntity le = ((LivingEntity) e);
			        				le.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, 15*20, 2));
			        				le.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 10*20, 2));
			        				le.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 7*20, 2));
			        				le.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 5*20, 1));
			        			}
			        		}
			        	}
			        	//glass explosion
			        	for(int sx = l.getBlockX()-1; sx < l.getBlockX()+1; sx++)
			        	{
			        		for(int sy = l.getBlockY()-1; sy < l.getBlockY()+1; sy++)
				        	{
			        			for(int sz = l.getBlockZ()-1; sz < l.getBlockZ()+1; sz++)
					        	{
									int blockID = flash.getWorld().getBlockAt(sx, sy, sz).getTypeId();
			        				if(blockID == 102 || blockID == 20 || blockID == 95 || blockID == 160)
			        				{
			        					Weapon.spillparticle(new Location(flash.getWorld(),sx,sy,sz),flash.getWorld().getBlockAt((int) Math.floor(sx), (int) Math.floor(sy), (int) Math.floor(sz)).getTypeId());
			        					ChunksCleaner.breakGlass(flash.getWorld(), (int) Math.floor(sx), (int) Math.floor(sy), (int) Math.floor(sz));
			        				}
					        	}
				        	}
			        	}
			        	//Smoke

			        	flash.getWorld().spigot().playEffect(flash.getLocation(), Effect.LARGE_SMOKE, 0, 0, 1, 1, 1, 0.5f, 35, 40);
			        }
		    	}, (long)(20*1.8));
			}
			else
				return;
		}
		if(e.getEntity() instanceof Fireball)
		{
			Fireball fireball = (Fireball)e.getEntity();
			Location l = fireball.getLocation();
			fireball.getWorld().createExplosion(l.getX(), l.getY(), l.getZ(), 8f, false, false);
			fireball.getWorld().createExplosion(l.getX(), l.getY(), l.getZ(), 8f, false, false);
			fireball.getWorld().createExplosion(l.getX(), l.getY(), l.getZ(), 8f, false, false);
        	//entity damage
        	for(Entity en : fireball.getWorld().getEntities())
        	{
        		if(en.getLocation().distanceSquared(l) < 10*10)
        		{
        			if(en instanceof LivingEntity)
        			{
        				if(fireball.getShooter() instanceof Entity)
        					((LivingEntity) en).damage(60-15*Math.min(1, en.getLocation().distanceSquared(l)/6*2), (Entity) fireball.getShooter());
        			}
        		}
        	}
        	//glass explosion
        	for(int sx = l.getBlockX()-5; sx < l.getBlockX()+5; sx++)
        	{
        		for(int sy = l.getBlockY()-5; sy < l.getBlockY()+5; sy++)
	        	{
        			for(int sz = l.getBlockZ()-5; sz < l.getBlockZ()+5; sz++)
		        	{
						@SuppressWarnings("deprecation")
						int blockID = fireball.getWorld().getBlockAt(sx, sy, sz).getTypeId();
        				if(blockID == 102 || blockID == 20 || blockID == 95 || blockID == 160)
        				{
        					//Weapon.spillparticle(new Location(fireball.getWorld(),sx,sy,sz),fireball.getWorld().getBlockAt((int) Math.floor(sx), (int) Math.floor(sy), (int) Math.floor(sz)).getTypeId());
        					ChunksCleaner.breakGlass(fireball.getWorld(), (int) Math.floor(sx), (int) Math.floor(sy), (int) Math.floor(sz));
        				}
		        	}
	        	}
        	}
        	//Smoke
        	fireball.getWorld().spigot().playEffect(fireball.getLocation(), Effect.FLAME, 0, 0, 1, 1, 1, 0.5f, 15, 40);
        	fireball.getWorld().spigot().playEffect(fireball.getLocation(), Effect.LARGE_SMOKE, 0, 0, 1, 1, 1, 0.5f, 35, 40);
        	//grenade.getWorld().spigot().playEffect(grenade.getLocation(), Effect.LARGE_SMOKE);
        	//grenade.getWorld().spigot().playEffect(grenade.getLocation(), Effect.EXPLOSION_HUGE, 0, 0, 3, 3, 3, 0, 1, 40);
        	
		}
	}
	@EventHandler
	public void onEggThrown(PlayerEggThrowEvent e)
	{
		e.setHatching(false);
	}
	@EventHandler
	public void onSheepEatGrass(EntityChangeBlockEvent e) {
		EntityType a = EntityType.SHEEP;
		if (e.getEntity().getType() == a) {
		e.setCancelled(true);
		}
	}
}
