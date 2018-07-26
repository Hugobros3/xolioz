package io.xol.z.plugin.game;

import io.xol.chunkstories.api.entity.Controller;
import io.xol.chunkstories.api.entity.DamageCause;
import io.xol.chunkstories.api.entity.Entity;
import io.xol.chunkstories.api.entity.traits.serializable.TraitController;
import io.xol.chunkstories.api.entity.traits.serializable.TraitHealth;
import io.xol.chunkstories.api.events.EventHandler;
import io.xol.chunkstories.api.events.Listener;
import io.xol.chunkstories.api.events.entity.EntityDamageEvent;
import io.xol.chunkstories.api.events.entity.EntityDeathEvent;
import io.xol.chunkstories.api.events.player.PlayerDeathEvent;
import io.xol.chunkstories.api.player.Player;
import io.xol.chunkstories.api.util.compatibility.ChatColor;
import io.xol.chunkstories.core.entity.EntityLiving;
import io.xol.chunkstories.core.entity.EntityZombie;
import io.xol.chunkstories.core.item.FirearmShotEvent;
import io.xol.z.plugin.XolioZPlugin;
import io.xol.z.plugin.player.PlayerProfile;

public class EntityListener implements Listener {

	private final XolioZPlugin plugin;

	public EntityListener(XolioZPlugin plugin) {
		this.plugin = plugin;
	}

	// zombies don't loot and count kills
	@EventHandler
	public void onEntityDeath(EntityDeathEvent event) {
		Entity entity = event.getEntity();
		DamageCause cause = entity.traits.get(TraitHealth.class).getLastDamageCause();

		// If the killed entity was a zombie, lookup the killer and see if it's a player
		if (entity instanceof EntityZombie) {
			if (cause != null && cause instanceof Entity) {

				Entity killerEntity = (Entity) cause;
				killerEntity.traits.with(TraitController.class, kec -> {
					Controller killerController = kec.getController();
					if (killerController != null && killerController instanceof Player) {
						Player killer = (Player) killerController;

						// Improve the killer stats
						PlayerProfile killerProfile = plugin.getPlayerProfiles().getPlayerProfile(killer.getUUID());
						killerProfile.zombiesKilled++;
						killerProfile.zombiesKilled_thisLife++;
					}
				});
			}
		}

		entity.traits.with(TraitController.class, vec -> {
			Controller controller = vec.getController();

			// If the victim was a player...
			if (controller != null && controller instanceof Player) {
				Player victim = (Player) controller;

				PlayerProfile victimProfile = plugin.getPlayerProfiles().getPlayerProfile(victim.getUUID());

				if (!victimProfile.inGame)
					return;

				if (cause != null && cause instanceof Entity) {

					Entity killerEntity = (Entity) cause;
					killerEntity.traits.with(TraitController.class, kec -> {
						Controller killerController = kec.getController();
						if (killerController != null && killerController instanceof Player) {
							Player killer = (Player) killerController;

							// Improve the killer stats
							PlayerProfile killerProfile = plugin.getPlayerProfiles().getPlayerProfile(killer.getUUID());

							killerProfile.playersKilled++;
							killerProfile.playersKilled_thisLife++;
						}
					});
				}

				victim.sendMessage(ChatColor.RED + "#{dogez.youdied}");

				victimProfile.playersKilled_thisLife = 0;
				victimProfile.zombiesKilled_thisLife = 0;
				victimProfile.deaths++;

				victimProfile.timeSurvivedLife = 0l;
				victimProfile.inGame = false;

				victimProfile.saveProfile();
			}
		});
	}

	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent event) {
		event.setDeathMessage(event.getPlayer().getName() + "#{dogez.waskilled}");
	}

	@EventHandler
	public void onDamage(EntityDamageEvent event) {
		Entity entity = event.getEntity();
		DamageCause cause = entity.traits.get(TraitHealth.class).getLastDamageCause();

		entity.traits.with(TraitController.class, vec -> {
			Controller controller = vec.getController();

			// If the victim was a player...
			if (controller != null && controller instanceof Player) {
				Player victim = (Player) controller;

				PlayerProfile victimProfile = plugin.getPlayerProfiles().getPlayerProfile(victim.getUUID());

				// If he was hurt by another player
				if (cause != null && cause instanceof Player) {
					if (victimProfile != null) {

						// Warn him and set his logoff cooldown
						if (System.currentTimeMillis() - victimProfile.lastHitTime > 120 * 1000L)
							victim.sendMessage(ChatColor.RED + "#{dogez.gothit}");
						victimProfile.lastHitTime = System.currentTimeMillis();
					}
				}
			}
		});
	}

	@EventHandler
	public void onFirearmShit(FirearmShotEvent event) {
		for (Entity e : event.getShooter().getWorld().getAllLoadedEntities()) {
			if (e instanceof EntityZombie) {
				EntityZombie z = (EntityZombie) e;

				double d = z.getLocation().distance(event.getShooter().getLocation());
				if (d < event.getItemFirearm().soundRange * (0.5 + Math.random() * 0.25)) {
					Entity shooter = event.getShooter();
					if (shooter != null && shooter instanceof EntityLiving)
						z.attack((EntityLiving) shooter, (float) (event.getItemFirearm().soundRange * 0.5 + Math.random() * 15));
				}
			}
		}
	}
}
