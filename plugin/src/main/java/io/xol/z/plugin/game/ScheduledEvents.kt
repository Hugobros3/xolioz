//
// This file is a part of the XolioZ Mod for Chunk Stories
// Check out README.md for more information
// Website: https://chunkstories.xyz
// Github: https://github.com/Hugobros3/xolioz
//

package io.xol.z.plugin.game

import java.util.Date
import java.util.Random

import io.xol.chunkstories.api.math.Math2
import io.xol.chunkstories.api.player.Player
import io.xol.chunkstories.api.plugin.Scheduler
import io.xol.chunkstories.api.sound.SoundSource
import io.xol.chunkstories.api.util.compatibility.ChatColor
import io.xol.z.plugin.XolioZPlugin
import io.xol.z.plugin.map.PlacesNames
import io.xol.z.plugin.player.PlayerProfile

class ScheduledEvents(val plugin: XolioZPlugin) {
    private var ticksCounter: Long = 0
    private val scheduler: Scheduler = plugin.gameWorld.gameLogic.scheduler

    init {

        scheduler.scheduleSyncRepeatingTask(plugin, {
            for (p in plugin.gameWorld.players) {

                if (!p.hasSpawned())
                    continue

                val pp = p.profile
                if (pp != null) {
                    val currentPlace = PlacesNames.getPlayerPlaceName(p)
                    if (pp.lastPlace != currentPlace) {
                        pp.lastPlace = currentPlace
                        p.sendMessage(ChatColor.GRAY.toString() + "~" + currentPlace)
                    }
                }
            }
            plugin.spawner.countZombies()
            plugin.spawner.spawnZombies()
            if (plugin.config.irlTimeSync) {

                // Synchs time
                val time = Date()
                var cstime = (time.hours * 60 + time.minutes + plugin.config.timeSyncOffsetInMinutes).toDouble()
                cstime = cstime / 1440
                cstime = cstime * 10000
                cstime %= 10000.0
                cstime += 10000.0
                cstime %= 10000.0
                plugin.gameWorld.time = cstime.toLong()

                // Messes weather randomly
                var currentWeather = plugin.gameWorld.weather
                val modifier = (Math.random() - 0.5f).toFloat() * 0.05f
                currentWeather += modifier
                currentWeather = Math2.clamp(currentWeather.toDouble(), 0.0, 1.0)
                plugin.gameWorld.weather = currentWeather
            }
        }, 0L, 10 * 10L * 6) // Chaque 10s

        // lel
        scheduler.scheduleSyncRepeatingTask(plugin, { ticksCounter++ }, 0, 1) // every tick

        scheduler.scheduleSyncRepeatingTask(plugin, object : Runnable {
            var last = 0
            internal var rnd = Random()

            override fun run() {
                var zik = 0
                while (zik == last) {
                    zik = 1 + rnd.nextInt(10)
                }

                plugin.gameWorld.soundManager.playSoundEffect(
                        "sounds/dogez/music/zik$zik.ogg",
                        SoundSource.Mode.STREAMED, null, 1f, 1f, 1f, 1f)
            }
        }, 0, 60 * 60L) // every minute
    }

    fun unschedule() {
        // TODO: API: give a reference to scheduled shit so we can unschedule it proper
    }

    private val Player.profile: PlayerProfile
        get() = with(plugin.playerProfiles) {
            this@profile.profile
        }
}
