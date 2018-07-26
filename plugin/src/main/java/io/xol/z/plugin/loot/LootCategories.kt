//
// This file is a part of the XolioZ Mod for Chunk Stories
// Check out README.md for more information
// Website: https://chunkstories.xyz
// Github: https://github.com/Hugobros3/xolioz
//

package io.xol.z.plugin.loot

import io.xol.z.plugin.XolioZPlugin
import java.io.File
import java.io.FileReader
import java.io.IOException
import java.util.*

class LootCategories(private val plugin: XolioZPlugin) {
    var categories: MutableMap<String, LootCategory> = HashMap()

    fun loadLootCategories() {
        categories.clear()

        //TODO json
        loadLootCategories()
    }

    fun loadLegacyLootCategories() {
        val file = File(XolioZPlugin.pluginFolder + "lootCategories.dz")

        try {
            val fileContents = FileReader(file).use { it.readText() }

            var name: String? = null
            val types = arrayListOf<LootType>()

            fun pushCategory() {
                val categoryName = name ?: throw Exception("A category needs a name")
                categories[categoryName] = LootCategory(categoryName, types.toTypedArray())

                types.clear()
                name = null
            }

            for (line in fileContents.lines()) {
                when {
                    line.startsWith("=") -> {
                        if (types.isNotEmpty()) pushCategory()

                        name = line.substring(1)
                    }

                    line.contains(':') -> {
                        try {
                            types.add(fromLegacyString(plugin, line))
                        } catch (e: Exception) {
                            plugin.logger.warning("Loot type was malformed at line '$line' ($e)")
                        }
                    }
                }
            }

            if (types.isNotEmpty()) pushCategory()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun getCategory(category: String): LootCategory {
        return categories[category] ?: throw Exception("The looty category $category doesn't exist")
    }
}
