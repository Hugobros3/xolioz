//
// This file is a part of the XolioZ Mod for Chunk Stories
// Check out README.md for more information
// Website: https://chunkstories.xyz
// Github: https://github.com/Hugobros3/xolioz
//

package io.xol.z.plugin.game

import io.xol.chunkstories.api.Location
import io.xol.chunkstories.api.entity.DamageCause
import io.xol.chunkstories.api.entity.traits.TraitVoxelSelection
import io.xol.chunkstories.api.entity.traits.serializable.TraitHealth
import io.xol.chunkstories.api.entity.traits.serializable.TraitSelectedItem
import io.xol.chunkstories.api.events.EventHandler
import io.xol.chunkstories.api.events.Listener
import io.xol.chunkstories.api.events.player.PlayerInputPressedEvent
import io.xol.chunkstories.api.events.player.PlayerLoginEvent
import io.xol.chunkstories.api.events.player.PlayerLogoutEvent
import io.xol.chunkstories.api.util.compatibility.ChatColor
import io.xol.chunkstories.core.voxel.VoxelChest
import io.xol.z.plugin.XolioZPlugin
import io.xol.z.plugin.loot.LootPlace
import io.xol.z.plugin.misc.ChatFormatter

class PlayerListener(private val plugin: XolioZPlugin) : Listener {

    @EventHandler
    fun onPlayerJoin(ev: PlayerLoginEvent) {
        val player = ev.player
        val prefix = ""
        if (plugin.config.showConnectionMessages)
            ev.connectionMessage = (ChatColor.DARK_GRAY.toString() + "[" + ChatColor.GREEN + "+" + ChatColor.DARK_GRAY + "] " + ChatFormatter.convertString(prefix)
                    + ev.player.name + ChatColor.GRAY + "#{dogez.loggedin}")
        else
            ev.connectionMessage = null
        plugin.playerProfiles.addPlayerProfile(player.uuid, player.name)
    }

    @EventHandler
    fun onPlayerQuit(ev: PlayerLogoutEvent) {
        val player = ev.player
        val prefix = ""
        if (plugin.config.showConnectionMessages) {
            if (!ev.logoutMessage.startsWith(ChatColor.DARK_GRAY.toString() + "["))
                ev.logoutMessage = (ChatColor.DARK_GRAY.toString() + "[" + ChatColor.RED + "-" + ChatColor.DARK_GRAY + "] " + ChatFormatter.convertString(prefix)
                        + ev.player.name + ChatColor.GRAY + "#{dogez.loggedout}")
        } else
            ev.logoutMessage = null

        val pp = plugin.playerProfiles.getPlayerProfile(player.uuid)

        if (System.currentTimeMillis() - pp.lastHitTime < 7 * 1000L) {
            plugin.logger
                    .info(player.name + " was killed for combat leave ( " + (System.currentTimeMillis() - pp.lastHitTime) + " ms wait before logoff )")

            val playerEntity = player.controlledEntity
            playerEntity?.traits?.with(TraitHealth::class.java) { eh ->
                eh.damage(object : DamageCause {

                    override fun getName(): String {
                        return "Cowards no Rewards"
                    }

                    override fun getCooldownInMs(): Long {
                        return 0
                    }
                }, 150000f)
            }
        }
        plugin.playerProfiles.removePlayerProfile(player.uuid)
    }

    @EventHandler
    fun onPlayerInput(event: PlayerInputPressedEvent) {

        // We are only interested in mouse clicks
        val input = event.input
        if (!input.name.startsWith("mouse."))
            return

        val player = event.player

        val playerEntity = player.controlledEntity
        val selectedLocation = playerEntity!!.traits.tryWith<TraitVoxelSelection, Location>(TraitVoxelSelection::class.java) { tvs -> tvs.getBlockLookingAt(true, false) }

        if (selectedLocation != null) {
            val context = player.world.peekSafely(selectedLocation)

            val itemInHand = playerEntity.traits.tryWith(TraitSelectedItem::class.java) { esi -> esi.selectedItem }

            // loot placement and removal
            if (player.hasPermission("dogez.admin")) {
                if (itemInHand != null && itemInHand.item.name == "dz_loot_tool") {

                    if (context.voxel is VoxelChest) {
                        val profile = plugin.playerProfiles.getPlayerProfile(player.uuid)

                        // TODO use Vector3i here
                        val loot_coordinates = selectedLocation.x().toString() + ":" + selectedLocation.y() + ":" + selectedLocation.z()

                        if (profile.adding && profile.activeCategory != null) {

                            val lootPlace = LootPlace(plugin, selectedLocation, profile.activeCategory, profile.currentMin, profile.currentMax)
                            if (plugin.lootPlaces.add(loot_coordinates, lootPlace, player.world))
                                player.sendMessage(ChatColor.AQUA.toString() + "Loot point added " + lootPlace.toString())
                            else
                                player.sendMessage(ChatColor.RED.toString() + "This loot point already exists !")
                        } else if (!profile.adding) {
                            if (!plugin.lootPlaces.removePlace(loot_coordinates, player.world)) {
                                player.sendMessage(ChatColor.RED.toString() + "There is no loot here !")
                            }
                        }
                        event.isCancelled = true
                    }
                }
            }

            // loot regeneration
            val voxel = player.world.peekSafely(selectedLocation).voxel
            if (voxel is VoxelChest) {
                val coords = selectedLocation.x().toInt().toString() + ":" + selectedLocation.y().toInt() + ":" + selectedLocation.z().toInt()

                plugin.lootPlaces.update(coords, player.world)
            }
        }

    }
}
