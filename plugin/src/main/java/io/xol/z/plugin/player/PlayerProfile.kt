//
// This file is a part of the XolioZ Mod for Chunk Stories
// Check out README.md for more information
// Website: https://chunkstories.xyz
// Github: https://github.com/Hugobros3/xolioz
//

package io.xol.z.plugin.player

import io.xol.chunkstories.api.player.Player
import io.xol.z.plugin.loot.LootCategory

data class PlayerProfile(val uuid: Long) {
    constructor(uuid: Long, name: String) : this(uuid) {
        this.name = name
    }

    lateinit var name: String

    var inGame = false

    val dateOfJoin: Long = System.currentTimeMillis()

    var timeConnected: Long = 0

    var timeSurvivedTotal: Long = 0
    var timeSurvivedLife: Long = 0

    @Transient
    var then: Long = System.currentTimeMillis()

    var zombiesKilled = 0
    var playersKilled = 0
    var deaths = 0

    var zombiesKilledThisLife = 0
    var playersKilledThisLife = 0

    // map-building state (what loot type i'm placing etc)
    @Transient
    var adding = true
    @Transient
    var activeCategory: LootCategory? = null
    @Transient
    var currentMin = 1
    @Transient
    var currentMax = 5

    // used to notify area changes
    @Transient
    var lastPlace = ""

    @Transient
    var talkingToLast: Player? = null

    var lastHitTime: Long = 0

    internal fun updateTime() {
        val now = System.currentTimeMillis()

        if (then == -1L)
            then = now
        else {
            val timeElapsed = (now - then) / 1000
            timeConnected += timeElapsed
            if (inGame) {
                timeSurvivedTotal += timeElapsed
                timeSurvivedLife += timeElapsed
            }
            then = now
        }
    }
}
