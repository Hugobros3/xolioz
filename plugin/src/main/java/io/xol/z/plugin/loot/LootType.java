//
// This file is a part of the XolioZ Mod for Chunk Stories
// Check out README.md for more information
// Website: https://chunkstories.xyz
// Github: https://github.com/Hugobros3/xolioz
//

package io.xol.z.plugin.loot;

// Copyright 2014 XolioWare Interactive

import java.util.Random;

import io.xol.chunkstories.api.item.ItemDefinition;
import io.xol.chunkstories.api.item.inventory.ItemPile;

public class LootType {

	public ItemDefinition lootItem;
	public int proba = 0;
	public int minAmount = 1;
	public int maxAmount = 1;

	public LootType(ItemDefinition li, String initString) {
		String[] split = initString.split(":");
		if (split.length < 3) {
			return;
		} else {
			lootItem = li;
			proba = Integer.parseInt(split[1]);
			if (split.length >= 3) {
				minAmount = Integer.parseInt(split[2]);
			}
			if (split.length >= 4) {
				maxAmount = Integer.parseInt(split[3]);
			} else
				maxAmount = minAmount;
			// debug
			if (minAmount > maxAmount) {
				System.out.println("ERROR:" + maxAmount + ">" + minAmount + " (line:" + initString + ")");
				maxAmount = minAmount;
			}
			// System.out.println("Loaded lootType ok."+toString());
		}
	}

	public String toString() {
		return "(lootItem:" + lootItem.toString() + ",prob=" + proba + ",min=" + minAmount + ",max=" + maxAmount + ")";
	}

	public ItemPile getItem() {
		Random rng = new Random();
		ItemPile item = new ItemPile(lootItem.newItem());
		int amount = (maxAmount - minAmount == 0 ? 0 : rng.nextInt(maxAmount - minAmount)) + minAmount;
		// System.out.println(amount+"="+maxAmount+":"+minAmount+":");
		// amount = 5;
		item.setAmount(amount);
		return item;
	}
}