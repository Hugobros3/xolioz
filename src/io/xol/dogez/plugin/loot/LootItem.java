package io.xol.dogez.plugin.loot;

import java.util.List;

import io.xol.dogez.plugin.misc.ChatFormatter;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

//Copyright 2014 XolioWare Interactive

public class LootItem {

	public String name;
	public List<String> description;
	public String internalName;
	public int typeId = 1;
	public int metaData = 0;
	
	public LootItem(String n, String in, int ti, int me)
	{
		name = ChatFormatter.convertString(n);
		internalName = in;
		typeId = ti;
		metaData = me;
	}

	public ItemStack getItem() {
		@SuppressWarnings("deprecation")
		ItemStack item = new ItemStack(typeId);
		item.setDurability((short)metaData);
		ItemMeta m = item.getItemMeta();
		m.setDisplayName(name);
		if(description != null)
		{
			m.setLore(description);
		}
		item.setItemMeta(m);
		return item;
	}
}
