package io.xol.dogez.mods.items;

import io.xol.chunkstories.api.entity.Controller;
import io.xol.chunkstories.api.entity.Entity;
import io.xol.chunkstories.api.input.Input;
import io.xol.chunkstories.api.item.Item;
import io.xol.chunkstories.api.item.ItemType;
import io.xol.chunkstories.api.world.WorldMaster;
import io.xol.chunkstories.core.entity.EntityPlayer;
import io.xol.chunkstories.item.ItemPile;

//(c) 2015-2016 XolioWare Interactive
//http://chunkstories.xyz
//http://xol.io

public class FoodItem extends Item {

	private final float calories;

	public FoodItem(ItemType type) {
		super(type);
		calories = Float.parseFloat(type.getProperty("calories", "10.0"));
	}
	
	public boolean handleInteraction(Entity owner, ItemPile itemPile, Input input, Controller controller)
	{
		if(owner.getWorld() instanceof WorldMaster)
		{
			if(input.getName().equals("mouse.right") && owner instanceof EntityPlayer)
			{
				if(((EntityPlayer) owner).getFoodLevel() >= 100)
					return true;
				
				System.out.println(owner + " ate "+itemPile);
				
				((EntityPlayer)owner).setFoodLevel(((EntityPlayer) owner).getFoodLevel() + calories);
				itemPile.setAmount(itemPile.getAmount() - 1);
				return true;
			}
		}
		
		return false;
	}

}
