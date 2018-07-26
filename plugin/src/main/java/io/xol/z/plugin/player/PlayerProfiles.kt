package io.xol.z.plugin.player

import java.util.ArrayList

import io.xol.chunkstories.api.player.Player
import io.xol.z.plugin.XolioZPlugin

class PlayerProfiles(private val plugin: XolioZPlugin) {

    internal var playerProfiles: MutableList<PlayerProfile> = ArrayList()

    fun getPlayerProfile(uuid: Long): PlayerProfile {

        var result: PlayerProfile? = null
        for (pp in playerProfiles) {
            if (pp.uuid == uuid) {
                result = pp
                break
            }
        }

        if (result == null) {

            //Look if the player is already logged in
            val player = plugin.server.getPlayerByUUID(uuid)

            //If it is load it
            if (player != null) {
                val pp = PlayerProfile(uuid, player.name)
                playerProfiles.add(pp)
                return pp
            } else {
                val pp = PlayerProfile(uuid)
                playerProfiles.add(pp)
                return pp
            }//If it isn't, load it but don't assume the name
        }

        return result
    }

    fun removePlayerProfile(uuid: Long) {

        val profilesToDelete = ArrayList<PlayerProfile>()
        for (pp in playerProfiles) {

            if (pp.uuid == uuid) {
                pp.saveProfile()
                profilesToDelete.add(pp)
            }
        }

        for (delete in profilesToDelete) {
            playerProfiles.remove(delete)
        }
    }

    fun addPlayerProfile(uuid: Long, name: String) {
        val add = PlayerProfile(uuid, name)
        playerProfiles.add(add)
    }

    fun saveAll() {
        for (pp in playerProfiles) {
            pp.saveProfile()
        }
    }
}
