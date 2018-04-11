package io.xol.dogez.plugin.game;

import io.xol.chunkstories.api.entity.DamageCause;
import io.xol.chunkstories.api.entity.Entity;
import io.xol.chunkstories.api.entity.EntityLiving;
import io.xol.chunkstories.api.entity.interfaces.EntityControllable;
import io.xol.chunkstories.api.events.EventHandler;
import io.xol.chunkstories.api.events.Listener;
import io.xol.chunkstories.api.events.entity.EntityDamageEvent;
import io.xol.chunkstories.api.events.entity.EntityDeathEvent;
import io.xol.chunkstories.api.events.player.PlayerDeathEvent;
import io.xol.chunkstories.api.player.Player;
import io.xol.chunkstories.api.util.compatibility.ChatColor;
import io.xol.chunkstories.core.entity.EntityZombie;
import io.xol.chunkstories.core.item.FirearmShotEvent;
import io.xol.dogez.plugin.XolioZGamemodePlugin;
import io.xol.dogez.plugin.player.PlayerProfile;

public class EntityListener implements Listener {

	private final XolioZGamemodePlugin plugin;

	public EntityListener(XolioZGamemodePlugin plugin) {
		this.plugin = plugin;
	}

	// zombies don't loot and count kills
	@EventHandler
	public void onEntityDeath(EntityDeathEvent event) {
		
		if (!plugin.isActive())
			return;

		Entity entity = event.getEntity();
		if (!(entity instanceof EntityLiving))
			return;

		EntityLiving entityLiving = (EntityLiving) entity;

		if (entity instanceof EntityZombie) {
			DamageCause cause = entityLiving.getLastDamageCause();
			if (cause != null && cause instanceof EntityLiving) {
				if (cause instanceof EntityControllable
						&& ((EntityControllable) cause).getControllerComponent().getController() != null) {
					Player player = (Player) ((EntityControllable) cause).getControllerComponent().getController();
					PlayerProfile pp = plugin.getPlayerProfiles().getPlayerProfile(player.getUUID());
					pp.zombiesKilled++;
					pp.zombiesKilled_thisLife++;
				}
			}
		}

		if (entity instanceof EntityControllable
				&& ((EntityControllable) entity).getControllerComponent().getController() != null) {

			Player victim = (Player) ((EntityControllable) entity).getControllerComponent().getController();
			PlayerProfile victimProfile = plugin.getPlayerProfiles().getPlayerProfile(victim.getUUID());

			if (!victimProfile.inGame)
				return;

			victimProfile.playersKilled_thisLife = 0;
			victimProfile.zombiesKilled_thisLife = 0;
			victimProfile.deaths++;

			DamageCause cause = entityLiving.getLastDamageCause();
			if (cause != null && cause instanceof EntityLiving) {
				if (cause instanceof EntityControllable
						&& ((EntityControllable) cause).getControllerComponent().getController() != null)
				{
					Player player = (Player) ((EntityControllable) cause).getControllerComponent().getController();
					PlayerProfile killerProfile = plugin.getPlayerProfiles().getPlayerProfile(player.getUUID());

					killerProfile.playersKilled++;
					killerProfile.playersKilled_thisLife++;

					victimProfile.timeSurvivedLife = 0l;
				}
			}

			victimProfile.saveProfile();
			victim.sendMessage(ChatColor.RED + "#{dogez.youdied}");

			double minimalRandom = 0.9d - Math.max(0.85, Math.min(0.0, 30 - victimProfile.getTimeAlive() / 30.0));

			if (victimProfile.inGame && Math.random() > minimalRandom) {
				
			}
			victimProfile.inGame = false;
		}
	}

	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent event) {
		event.setDeathMessage(event.getPlayer().getName() + "#{dogez.waskilled}");
	}

	@EventHandler
	public void onDamage(EntityDamageEvent event) {
		if (event.getEntity() instanceof EntityLiving) {
			EntityLiving entityHurt = (EntityLiving) event.getEntity();
			if (entityHurt.getLastDamageCause() != null) {
				if (entityHurt.getLastDamageCause() instanceof Entity) {
					if (entityHurt.getLastDamageCause() instanceof Player) {
						
						Player player = (Player) ((EntityControllable) entityHurt).getController();
						// anti combat log
						PlayerProfile pp = plugin.getPlayerProfiles().getPlayerProfile(player.getUUID());
						if (pp != null) {
							if (System.currentTimeMillis() - pp.lastHitTime > 120 * 1000L)
								player.sendMessage(ChatColor.RED
										+ "#{dogez.gothit}");
							pp.lastHitTime = System.currentTimeMillis();

						}
					}
				}
			}
		}
	}
	
	@EventHandler
	public void onFirearmShit(FirearmShotEvent event) {
		for(Entity e : event.getShooter().getWorld().getAllLoadedEntities()) {
			if(e instanceof EntityZombie) {
				EntityZombie z = (EntityZombie)e;
				
				double d = z.getLocation().distance(event.getShooter().getLocation());
				if(d < event.getItemFirearm().soundRange * (0.5 + Math.random() * 0.25))
					z.attack(event.getShooter(), (float) (event.getItemFirearm().soundRange * 0.5 + Math.random() * 15));
			}
		}
	}
}
