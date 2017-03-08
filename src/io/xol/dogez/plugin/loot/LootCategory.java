package io.xol.dogez.plugin.loot;

//(c) 2014 XolioWare Interactive

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import io.xol.chunkstories.api.item.inventory.ItemPile;

public class LootCategory {
	
	float generalSpawnProb = 1f;
	
	private Map<String,LootType> types = new HashMap<String,LootType>();
	private int totalWeight = 0;
	int[] probaTable;
	String[] namesTable;
	
	public LootCategory(float gsp) {
		generalSpawnProb = gsp;
	}

	public void add(LootType lt)
	{
		if(lt.lootItem != null)
		{
			if(!(lt.lootItem.internalName == null))
			{
				types.put(lt.lootItem.internalName, lt);
				totalWeight+=lt.proba;
			}
			else
				System.out.println("DogeZ error : null internalName");
		}
		else
			System.out.println("DogeZ error : null loot type");
	}
	
	public void computeProbs()
	{
		int current = 0;
		int id = 0;
		probaTable = new int[types.size()];
		namesTable = new String[types.size()];
		for(Object o : types.values())
		{
			LootType lt = (LootType)o;
			current+=lt.proba;
			probaTable[id] = current;
			namesTable[id] = lt.lootItem.internalName;
			id++;
		}
	}
	
	public LootType getRandomSpawn()
	{
		if(Math.random() < generalSpawnProb)
		{
			Random rnd = new Random();
			int value = rnd.nextInt(totalWeight);
			int i = 0;
			while(value > probaTable[i] && i < probaTable.length-1)
				i++;
			LootType lt = types.get(namesTable[i]);
			return lt;
		}
		return null;
	}

	public List<ItemPile> getAllItems() {
		List<ItemPile> items = new ArrayList<ItemPile>();
		for(String n : namesTable)
		{
			LootType lt = types.get(n);
			items.add(lt.getItem());
		}
		return items;
	}
	
}
