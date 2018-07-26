//
// This file is a part of the XolioZ Mod for Chunk Stories
// Check out README.md for more information
// Website: https://chunkstories.xyz
// Github: https://github.com/Hugobros3/xolioz
//

package io.xol.z.plugin

data class Config(
        var showConnectionMessages: Boolean = true,

        var zombiesSpawnMinDistance: Int = 30,
        var zombiesSpawnMaxDistance: Int = 60,
        var maxZombiesOnMap: Int = 420,

        var lootRespawnDelay: Int = 600,

        var timeSyncOffsetInMinutes: Int = 0,
        var irlTimeSync: Boolean = true
)