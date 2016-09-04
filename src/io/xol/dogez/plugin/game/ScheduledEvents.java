package io.xol.dogez.plugin.game;

//(c) 2014 XolioWare Interactive

import java.util.Date;
import java.util.Random;

import io.xol.chunkstories.api.compatibility.ChatColor;
import io.xol.chunkstories.api.plugin.Scheduler;
import io.xol.chunkstories.api.server.Player;
import io.xol.chunkstories.server.Server;
import io.xol.dogez.plugin.DogeZPlugin;
import io.xol.dogez.plugin.map.PlacesNames;
import io.xol.dogez.plugin.player.PlayerProfile;

public class ScheduledEvents {
	public static long ticksCounter = 0;

	public static void startEvents(DogeZPlugin p) {

		Scheduler scheduler = DogeZPlugin.config.getWorld().getGameLogic().getScheduler();
		// BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
		// Players saving, zombies spawning, time synch
		scheduler.scheduleSyncRepeatingTask(p, new Runnable() {
			@Override
			public void run() {
				for (Player p : DogeZPlugin.config.getWorld().getPlayers()) {
					PlayerProfile pp = PlayerProfile.getPlayerProfile(p.getUUID());
					if (pp != null) {
						String currentPlace = PlacesNames.getPlayerPlaceName(p);
						if (!pp.lastPlace.equals(currentPlace)) {
							pp.lastPlace = currentPlace;
							p.sendMessage(ChatColor.GRAY + "~" + currentPlace);
						}
						
						//pp.decreaseBattery();
					}
					
					/*if (!PlacesNames.isInMap(p.getLocation()) && p.getGameMode().equals(GameMode.SURVIVAL)
							&& pp.inGame) {
						p.sendMessage(ChatColor.RED + "Le hors-map est interdit. Retournez sur vos pas !");
						p.damage(1);
					}*/
				}
				DogeZPlugin.spawner.countZombies();
				DogeZPlugin.spawner.spawnZombies();
				if (DogeZPlugin.config.synchTime) {
					Date time = new Date();
					@SuppressWarnings("deprecation")
					double mctime = (time.getHours() - 5) * 60 + time.getMinutes();
					mctime = mctime / 1440;
					mctime = mctime * 24000;
					DogeZPlugin.config.getWorld().setTime((long) mctime);

				}
			/*	for (Horse h : DogeZPlugin.config.getWorld().getEntitiesByClass(Horse.class)) {
					// System.out.println("attacking??");
					if (h.getTarget() == null) {
						for (Entity p : h.getNearbyEntities(35, 35, 35)) {
							if (p instanceof Player)
								h.setTarget((LivingEntity) p);
						}
						// System.out.println("attacking pigzombie target");
					}
				}*/
			}
		}, 0L, 10 * 10L * 6); // Chaque 10s
		// lel
		scheduler.scheduleSyncRepeatingTask(p, new Runnable() {
			@Override
			public void run() {
				ticksCounter++;
			}
		}, 0, 1); // every tick

		scheduler.scheduleSyncRepeatingTask(p, new Runnable() {
			// public int[] musicLengthInTicks = {14,11,14,7,16,16,15,19,28,18};
			public int last = 0;
			Random rnd = new Random();

			@Override
			public void run() {
				int zik = 0;
				while (zik == last) {
					zik = 1 + rnd.nextInt(10);
				}
				
				DogeZPlugin.config.getWorld().getSoundManager().playSoundEffect("dogez.music.zik" + zik);
				
				/*for (Player p : Server.getInstance().getConnectedPlayers()) {
					
					if (p.getGameMode().equals(GameMode.SURVIVAL)) {
						Location loc = p.getLocation();
						PacketPlayOutNamedSoundEffect packet = new PacketPlayOutNamedSoundEffect(
								"dogez.music.zik" + zik, loc.getX(), loc.getY(), loc.getZ(), 100, 1);
						((CraftPlayer) p).getHandle().playerConnection.sendPacket(packet);
					}
				}*/
			}
		}, 0, 60 * 600L); // every 5m
	}
}
