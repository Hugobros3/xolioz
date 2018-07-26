//
// This file is a part of the XolioZ Mod for Chunk Stories
// Check out README.md for more information
// Website: https://chunkstories.xyz
// Github: https://github.com/Hugobros3/xolioz
//

package io.xol.z.plugin.misc

import io.xol.chunkstories.api.util.compatibility.ChatColor

fun String.bukkitCodes2Hex(): String {
    var s = this
    // colors
    s = s.replace("&1", "" + ChatColor.DARK_BLUE)
    s = s.replace("&2", "" + ChatColor.DARK_GREEN)
    s = s.replace("&3", "" + ChatColor.DARK_AQUA)
    s = s.replace("&4", "" + ChatColor.DARK_RED)
    s = s.replace("&5", "" + ChatColor.DARK_PURPLE)
    s = s.replace("&6", "" + ChatColor.GOLD)
    s = s.replace("&7", "" + ChatColor.GRAY)
    s = s.replace("&8", "" + ChatColor.DARK_GRAY)
    s = s.replace("&9", "" + ChatColor.BLUE)
    s = s.replace("&a", "" + ChatColor.GREEN)
    s = s.replace("&b", "" + ChatColor.AQUA)
    s = s.replace("&c", "" + ChatColor.RED)
    s = s.replace("&d", "" + ChatColor.LIGHT_PURPLE)
    s = s.replace("&e", "" + ChatColor.YELLOW)
    s = s.replace("&f", "" + ChatColor.WHITE)
    // formating
    s = s.replace("&o", "" + ChatColor.ITALIC)
    s = s.replace("&l", "" + ChatColor.BOLD)
    s = s.replace("&n", "" + ChatColor.UNDERLINE)
    s = s.replace("&k", "" + ChatColor.MAGIC)
    s = s.replace("&r", "" + ChatColor.RESET)
    return s
}

