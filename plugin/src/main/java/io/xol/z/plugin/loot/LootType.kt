//
// This file is a part of the XolioZ Mod for Chunk Stories
// Check out README.md for more information
// Website: https://chunkstories.xyz
// Github: https://github.com/Hugobros3/xolioz
//

package io.xol.z.plugin.loot

import java.util.Random

import io.xol.chunkstories.api.item.ItemDefinition
import io.xol.chunkstories.api.item.inventory.ItemPile
import io.xol.z.plugin.XolioZPlugin

fun fromLegacyString(context: XolioZPlugin, string : String) : LootType {
    val splitted = string.split(':')
    if(splitted.size < 3) throw Exception("Malformed string !")

    val itemName = splitted[0]
    val itemDefinition = context.server.content.items().getItemDefinition(itemName) ?: throw Exception("Item type $itemName isn't defined in loaded content !")

    val weight = splitted[1].toInt()
    val minAmount = splitted[2].toInt()
    val maxAmount = splitted.getOrNull(3)?.toInt() ?: minAmount

    return LootType(itemDefinition, weight, minAmount, maxAmount)
}

data class LootType(val itemDefinition: ItemDefinition, val weight : Int, val minAmount : Int, val maxAmount : Int) {

    fun generateItemPile() : ItemPile {
        val item = ItemPile(itemDefinition.newItem())
        val amount = (if (maxAmount - minAmount == 0) 0 else rng.nextInt(maxAmount - minAmount)) + minAmount

        item.amount = amount
        return item
    }

    init {
        if(minAmount > maxAmount) throw Exception("Minimal amount shouldn't be greater than max !")
    }

    companion object {
        val rng = Random()
    }
}