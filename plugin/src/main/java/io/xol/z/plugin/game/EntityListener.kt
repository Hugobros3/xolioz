//
// This file is a part of the XolioZ Mod for Chunk Stories
// Check out README.md for more information
// Website: https://chunkstories.xyz
// Github: https://github.com/Hugobros3/xolioz
//

package io.xol.z.plugin.game

import io.xol.chunkstories.api.entity.Controller
import io.xol.chunkstories.api.entity.DamageCause
import io.xol.chunkstories.api.entity.Entity
import io.xol.chunkstories.api.entity.traits.serializable.TraitController
import io.xol.chunkstories.api.entity.traits.serializable.TraitHealth
import io.xol.chunkstories.api.events.EventHandler
import io.xol.chunkstories.api.events.Listener
import io.xol.chunkstories.api.events.entity.EntityDamageEvent
import io.xol.chunkstories.api.events.entity.EntityDeathEvent
import io.xol.chunkstories.api.events.player.PlayerDeathEvent
import io.xol.chunkstories.api.player.Player
import io.xol.chunkstories.api.util.compatibility.ChatColor
import io.xol.chunkstories.core.entity.EntityLiving
import io.xol.chunkstories.core.entity.EntityZombie
import io.xol.chunkstories.core.item.FirearmShotEvent
import io.xol.z.plugin.XolioZPlugin
import io.xol.z.plugin.player.PlayerProfile

class EntityListener(private val plugin: XolioZPlugin) : Listener {

    // zombies don't loot and count kills
    @EventHandler
    fun onEntityDeath(event: EntityDeathEvent) {
        val entity = event.entity
        val cause = entity.traits.get(TraitHealth::class.java)!!.lastDamageCause

        // If the killed entity was a zombie, lookup the killer and see if it's a player
        if (entity is EntityZombie) {
            if (cause != null && cause is Entity) {

                val killerEntity = cause as Entity?
                killerEntity!!.traits.with(TraitController::class.java) { kec ->
                    val killerController = kec.controller
                    if (killerController != null && killerController is Player) {
                        val killer = killerController as Player?

                        // Improve the killer stats
                        val killerProfile = plugin.playerProfiles.getPlayerProfile(killer!!.uuid)
                        killerProfile.zombiesKilled++
                        killerProfile.zombiesKilled_thisLife++
                    }
                }
            }
        }

        entity.traits.with(TraitController::class.java) { vec ->
            val controller = vec.controller

            // If the victim was a player...
            if (controller != null && controller is Player) {
                val victim = controller as Player?

                val victimProfile = plugin.playerProfiles.getPlayerProfile(victim!!.uuid)

                if (!victimProfile.inGame)
                    return@with

                if (cause != null && cause is Entity) {

                    val killerEntity = cause as Entity?
                    killerEntity!!.traits.with(TraitController::class.java) { kec ->
                        val killerController = kec.controller
                        if (killerController != null && killerController is Player) {
                            val killer = killerController as Player?

                            // Improve the killer stats
                            val killerProfile = plugin.playerProfiles.getPlayerProfile(killer!!.uuid)

                            killerProfile.playersKilled++
                            killerProfile.playersKilled_thisLife++
                        }
                    }
                }

                victim.sendMessage(ChatColor.RED.toString() + "#{dogez.youdied}")

                victimProfile.playersKilled_thisLife = 0
                victimProfile.zombiesKilled_thisLife = 0
                victimProfile.deaths++

                victimProfile.timeSurvivedLife = 0L
                victimProfile.inGame = false

                victimProfile.saveProfile()
            }
        }
    }

    @EventHandler
    fun onPlayerDeath(event: PlayerDeathEvent) {
        event.deathMessage = event.player.name + "#{dogez.waskilled}"
    }

    @EventHandler
    fun onDamage(event: EntityDamageEvent) {
        val entity = event.entity
        val cause = entity.traits.get(TraitHealth::class.java)!!.lastDamageCause

        entity.traits.with(TraitController::class.java) { vec ->
            val controller = vec.controller

            // If the victim was a player...
            if (controller != null && controller is Player) {
                val victim = controller as Player?

                val victimProfile = plugin.playerProfiles.getPlayerProfile(victim!!.uuid)

                // If he was hurt by another player
                if (cause != null && cause is Player) {
                    if (victimProfile != null) {

                        // Warn him and set his logoff cooldown
                        if (System.currentTimeMillis() - victimProfile.lastHitTime > 120 * 1000L)
                            victim.sendMessage(ChatColor.RED.toString() + "#{dogez.gothit}")
                        victimProfile.lastHitTime = System.currentTimeMillis()
                    }
                }
            }
        }
    }

    @EventHandler
    fun onFirearmShit(event: FirearmShotEvent) {
        for (e in event.shooter.getWorld().allLoadedEntities) {
            if (e is EntityZombie) {

                val d = e.location.distance(event.shooter.location)
                if (d < event.itemFirearm.soundRange * (0.5 + Math.random() * 0.25)) {
                    val shooter = event.shooter
                    if (shooter != null && shooter is EntityLiving)
                        e.attack(shooter, (event.itemFirearm.soundRange * 0.5 + Math.random() * 15).toFloat())
                }
            }
        }
    }
}
