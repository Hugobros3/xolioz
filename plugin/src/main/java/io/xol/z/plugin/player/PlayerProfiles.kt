//
// This file is a part of the XolioZ Mod for Chunk Stories
// Check out README.md for more information
// Website: https://chunkstories.xyz
// Github: https://github.com/Hugobros3/xolioz
//

package io.xol.z.plugin.player

import com.google.gson.GsonBuilder
import io.xol.chunkstories.api.player.Player
import io.xol.z.plugin.XolioZPlugin
import java.io.File
import java.io.FileReader
import java.io.FileWriter

class PlayerProfiles(private val plugin: XolioZPlugin) {

	val playerProfiles = HashMap<Player, PlayerProfile>()
	val gson = GsonBuilder().setPrettyPrinting().create()

	val Player.profile: PlayerProfile
		get() = playerProfiles[this] ?: loadProfile(this)

	private fun loadProfile(player: Player): PlayerProfile {
		val file = File(XolioZPlugin.pluginFolder + "users/" + player.uuid + ".json")

		val profile = if(file.exists()) {
			val reader = FileReader(file)
			reader.use {
				gson.fromJson(it, PlayerProfile::class.java)
			}
		} else {
			PlayerProfile(player.uuid, player.name)
		}

		playerProfiles[player] = profile
		return profile
	}

	fun PlayerProfile.saveProfile() {
		this.updateTime()

		val file = File(XolioZPlugin.pluginFolder + "users/" + this.uuid + ".json")

		val writer = FileWriter(file);
		writer.use { gson.toJson(this, it) }
	}

	fun Player.forgetProfile() = playerProfiles.remove(this)

	fun saveAll() {
		playerProfiles.values.forEach {it.saveProfile()}
	}
}
