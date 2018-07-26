//
// This file is a part of the XolioZ Mod for Chunk Stories
// Check out README.md for more information
// Website: https://chunkstories.xyz
// Github: https://github.com/Hugobros3/xolioz
//

package io.xol.z.plugin.game

import io.xol.chunkstories.api.entity.traits.serializable.TraitCreativeMode
import io.xol.chunkstories.api.entity.traits.serializable.TraitInventory
import io.xol.chunkstories.api.player.Player
import io.xol.chunkstories.api.util.compatibility.ChatColor
import io.xol.z.plugin.XolioZPlugin

/** Helper class to deal with talkie-walkie logic  */
class TalkieWalkiesHandler(private val plugin: XolioZPlugin) {

	//TODO re-implement ?
	fun leakPrivateConversations(from: String, to: String, msg: String) {
		for (p in plugin.server.connectedPlayers) {
			if (from != p.name && p.hasPermission("dogez.socialspy")) {
				p.sendMessage(ChatColor.GRAY.toString() + "[SS][" + from + "->" + to + "]:" + msg)
			}
		}
	}
}

fun Player.canUseTalkieWalkie(): Boolean {
	val controlledEntity = this.controlledEntity

	return if (controlledEntity!!.traits.tryWithBoolean(TraitCreativeMode::class.java) { ecm -> ecm.get() }) true else this.doesPlayerOwnTW()

}

fun Player.doesPlayerOwnTW(): Boolean {
	val entity = this.controlledEntity ?: return false

	return entity.traits.tryWithBoolean(TraitInventory::class.java) { inv ->
		for (i in inv.iterator()) {
			if (i != null && i.item.internalName == "dz_talkie_walkie")
				return@tryWithBoolean true
		}
		false
	}
}