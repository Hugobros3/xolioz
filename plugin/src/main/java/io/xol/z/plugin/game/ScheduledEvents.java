//
// This file is a part of the XolioZ Mod for Chunk Stories
// Check out README.md for more information
// Website: https://chunkstories.xyz
// Github: https://github.com/Hugobros3/xolioz
//

package io.xol.z.plugin.game;

import java.util.Date;
import java.util.Random;

import io.xol.chunkstories.api.math.Math2;
import io.xol.chunkstories.api.player.Player;
import io.xol.chunkstories.api.plugin.Scheduler;
import io.xol.chunkstories.api.sound.SoundSource;
import io.xol.chunkstories.api.util.compatibility.ChatColor;
import io.xol.z.plugin.XolioZPlugin;
import io.xol.z.plugin.map.PlacesNames;
import io.xol.z.plugin.player.PlayerProfile;

public class ScheduledEvents {
	public long ticksCounter = 0;

	private final Scheduler scheduler;

	public ScheduledEvents(XolioZPlugin plugin) {

		scheduler = plugin.getGameWorld().getGameLogic().getScheduler();

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

				plugin.getGameWorld().getSoundManager().playSoundEffect(
						"sounds/dogez/music/zik" + zik + ".ogg",
						SoundSource.Mode.STREAMED, null, 1, 1, 1, 1);
			}
		}, 0, 60 * 60L); // every minute
	}

	public void unschedule() {
		// TODO: API: give a reference to scheduled shit so we can unschedule it proper
	}
}
