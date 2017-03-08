package io.xol.dogez.plugin.loot;

//Copyright 2014 XolioWare Interactive

import java.util.Random;

import io.xol.chunkstories.api.item.inventory.ItemPile;

public class LootType {

	public LootItem lootItem;
	public int proba = 0;
	public int minAmount = 1;
	public int maxAmount = 1;
	
	public LootType(LootItem li, String initString)
	{
		String[] split = initString.split(":");
		if(split.length < 3)
		{
			//System.out.println("Wrong initstrig given ("+initString+")");
			return;
		}
		else
		{
			lootItem = li;
			/*typeId = Integer.parseInt(split[0]);
			metaData = Integer.parseInt(split[1]);
			name = ChatFormatter.convertString(split[2]);*/
			proba = Integer.parseInt(split[1]);
			if(split.length >= 3)
			{
				minAmount = Integer.parseInt(split[2]);
			}
			if(split.length >= 4)
			{
				maxAmount = Integer.parseInt(split[3]);
			}
			else
				maxAmount = minAmount;
			//debug
			if(minAmount > maxAmount)
			{
				System.out.println("ERROR:"+maxAmount+">"+minAmount+" (line:"+initString+")");
				maxAmount = minAmount;
			}
			//System.out.println("Loaded lootType ok."+toString());
		}
	}
	
	public String toString()
	{
		return "(lootItem:"+lootItem.toString()+",prob="+proba+",min="+minAmount+",max="+maxAmount+")";
	}

	public ItemPile getItem() {
		Random rng = new Random();
		ItemPile item = lootItem.getItem();
		int amount = (maxAmount-minAmount == 0 ? 0 : rng.nextInt(maxAmount-minAmount))+minAmount;
		//System.out.println(amount+"="+maxAmount+":"+minAmount+":");
		//amount = 5;
		item.setAmount(amount);
		return item;
	}
}