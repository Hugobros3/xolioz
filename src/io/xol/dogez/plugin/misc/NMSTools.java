package io.xol.dogez.plugin.misc;

//Copyright 2014 XolioWare Interactive

import java.lang.reflect.Field;

import net.minecraft.server.v1_8_R3.*;

public class NMSTools {
	
	static public void setMaxStackSize(int itemID, int i)
	{
		setMaxStackSize(Item.getById(itemID),i);
	}
	
	static public void setMaxStackSize(Item item, int i) {
		try {

			Field field = Item.class.getDeclaredField("maxStackSize");
			field.setAccessible(true);
			field.setInt(item, i);

		} catch (Exception e) {
		}
	}
}
