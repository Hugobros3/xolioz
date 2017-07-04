package io.xol.dogez.plugin.loot;

import java.util.List;

import io.xol.chunkstories.api.item.ItemType;
import io.xol.chunkstories.api.item.inventory.ItemPile;
import io.xol.dogez.plugin.misc.ChatFormatter;

//Copyright 2014 XolioWare Interactive

public class LootItem {

	public String name;
	public List<String> description;
	public String internalName;
	
	public ItemType type;
	//public int typeId = 1;
	//public int metaData = 0;
	
	public LootItem(String n, String in, ItemType type)
	{
		name = ChatFormatter.convertString(n);
		internalName = in;
		this.type = type;
		//typeId = ti;
		//metaData = me;
	}

	public ItemPile getItem() {
		ItemPile item = new ItemPile(type);// new ItemStack(typeId);
		
		//item.setDurability((short)metaData);
		//ItemMeta m = item.getItemMeta();
		
		/*m.setDisplayName(name);
		if(description != null)
		{
			m.setLore(description);
		}
		item.setItemMeta(m);*/
		return item;
	}
}
