package io.xol.dogez.plugin.game;

//(c) 2014 XolioWare Interactive

import java.util.Date;
import java.util.Random;

import io.xol.chunkstories.api.compatibility.ChatColor;
import io.xol.chunkstories.api.math.Math2;
import io.xol.chunkstories.api.player.Player;
import io.xol.chunkstories.api.plugin.Scheduler;
import io.xol.dogez.plugin.XolioZGamemodePlugin;
import io.xol.dogez.plugin.map.PlacesNames;
import io.xol.dogez.plugin.player.PlayerProfile;

public class ScheduledEvents {
	public long ticksCounter = 0;

	public ScheduledEvents(XolioZGamemodePlugin plugin) {

		Scheduler scheduler = plugin.getGameWorld().getGameLogic().getScheduler();
		
		scheduler.scheduleSyncRepeatingTask(plugin, new Runnable() {
			@Override
			public void run() {
				for (Player p : plugin.getGameWorld().getPlayers()) {

					if (!p.hasSpawned())
						continue;

					PlayerProfile pp = plugin.getPlayerProfiles().getPlayerProfile(p.getUUID());
					if (pp != null) {
						String currentPlace = PlacesNames.getPlayerPlaceName(p);
						if (!pp.lastPlace.equals(currentPlace)) {
							pp.lastPlace = currentPlace;
							p.sendMessage(ChatColor.GRAY + "~" + currentPlace);
						}
					}
				}
				plugin.spawner.countZombies();
				plugin.spawner.spawnZombies();
				if (plugin.config.getBoolean("irlTimeCycleSync", true)) {

					// Synchs time
					Date time = new Date();
					@SuppressWarnings("deprecation")
					double cstime = (time.getHours() - 0) * 60 + time.getMinutes() + plugin.config.getInt("timeSyncOffsetInMinutes", 0);
					cstime = cstime / 1440;
					cstime = cstime * 10000;
					cstime %= 10000;
					cstime += 10000;
					cstime %= 10000;
					plugin.getGameWorld().setTime((long) cstime);

					// Messes weather randomly
					float currentWeather = plugin.getGameWorld().getWeather();
					float modifier = (float) (Math.random() - 0.5f) * 0.05f;
					currentWeather += modifier;
					currentWeather = Math2.clamp(currentWeather, 0f, 1f);
					plugin.getGameWorld().setWeather(currentWeather);
				}

			}
		}, 0L, 10 * 10L * 6); // Chaque 10s
		
		// lel
		scheduler.scheduleSyncRepeatingTask(plugin, new Runnable() {
			@Override
			public void run() {
				ticksCounter++;
			}
		}, 0, 1); // every tick

		scheduler.scheduleSyncRepeatingTask(plugin, new Runnable() {
			public int last = 0;
			Random rnd = new Random();

			@Override
			public void run() {
				int zik = 0;
				while (zik == last) {
					zik = 1 + rnd.nextInt(10);
				}

				//plugin.getGameWorld().getSoundManager().playMusic("sounds/dogez/music/zik" + zik + ".ogg", 0, 0, 0,
				//		(float) (0.5f + Math.random() * 0.5f), 1, true);
			}
		}, 0, 60 * 60L); // every minute
	}

	public void unschedule() {
		// TODO Auto-generated method stub
		
	}
}
