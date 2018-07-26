//
// This file is a part of the XolioZ Mod for Chunk Stories
// Check out README.md for more information
// Website: https://chunkstories.xyz
// Github: https://github.com/Hugobros3/xolioz
//

package io.xol.z.plugin.game

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

                        // Improve the killer stats
                        val killerProfile = killerController.profile
                        killerProfile.zombiesKilled++
                        killerProfile.zombiesKilledThisLife++
                    }
                }
            }
        }

        entity.traits.with(TraitController::class.java) { vec ->
            val controller = vec.controller

            // If the victim was a player...
            if (controller != null && controller is Player) {

                val victimProfile = controller.profile

                if (!victimProfile.inGame)
                    return@with

                if (cause != null && cause is Entity) {

                    val killerEntity = cause as Entity?
                    killerEntity!!.traits.with(TraitController::class.java) { kec ->
                        val killerController = kec.controller
                        if (killerController != null && killerController is Player) {

                            // Improve the killer stats
                            val killerProfile = killerController.profile

                            killerProfile.playersKilled++
                            killerProfile.playersKilledThisLife++
                        }
                    }
                }

                controller.sendMessage(ChatColor.RED.toString() + "#{dogez.youdied}")

                victimProfile.playersKilledThisLife = 0
                victimProfile.zombiesKilledThisLife = 0
                victimProfile.deaths++

                victimProfile.timeSurvivedLife = 0L
                victimProfile.inGame = false

                with(plugin.playerProfiles) {
                    victimProfile.saveProfile()
                }
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
            val victim = vec.controller

            // If the victim was a player...
            if (victim != null && victim is Player) {

                val victimProfile = victim.profile

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

    private val Player.profile: PlayerProfile
        get() = with(plugin.playerProfiles) {
            this@profile.profile
        }
}
