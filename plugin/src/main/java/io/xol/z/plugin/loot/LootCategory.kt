//
// This file is a part of the XolioZ Mod for Chunk Stories
// Check out README.md for more information
// Website: https://chunkstories.xyz
// Github: https://github.com/Hugobros3/xolioz
//

package io.xol.z.plugin.loot

import io.xol.chunkstories.api.item.inventory.ItemPile
import java.util.*

class LootCategory(val name: String, val types : Array<LootType>) {

    val totalWeight : Int
    var ceil : IntArray = intArrayOf(types.size)

    init {
        totalWeight = types.sumBy { lootType -> lootType.weight }

        var boundUpper = 0

        var i = 0
        for(type in types) {
            boundUpper += type.weight

            ceil[i] = boundUpper

            i++
        }
    }

    fun getRandomLootType() : LootType {
        val cursor = rng.nextInt(totalWeight)

        for(i in 0 until types.size) {
            if(ceil[i] > cursor)
                return types[i]
        }

        throw Exception("Probabilities were broken")
    }

    fun generateItemPile() : ItemPile {
        return getRandomLootType().generateItemPile()
    }

    fun allItems() : Collection<ItemPile> {
        val list = arrayListOf<ItemPile>()

        for(type in types)
            list.add(type.generateItemPile())

        return list
    }

    companion object {
        val rng = Random()
    }
}
