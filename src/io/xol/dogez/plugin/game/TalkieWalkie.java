package io.xol.dogez.plugin.game;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

//(c) 2015 XolioWare Interactive

public class TalkieWalkie {
	//Helper class for talkie walkie
	
	public static boolean hasPlayerGear(Player p)
	{
		for(ItemStack i : p.getInventory().getContents())
		{
			if(i != null && i.getType().equals(Material.NETHER_BRICK_ITEM))
				return true;
		}
		return false;
	}
	
	public static boolean canPlayerUse(Player p)
	{
		if(p.getGameMode().equals(GameMode.CREATIVE))
			return true;
		return hasPlayerGear(p);
	}
	
	public static void notifyListenersAdmins(String from, String to, String msg)
	{
		for(Player p : Bukkit.getOnlinePlayers())
		{
			if(!from.equals(p.getName()) && p.hasPermission("dogez.socialspy"))
			{
				p.sendMessage(ChatColor.GRAY+"[SS]["+from+"->"+to+"]:"+msg);
			}
		}
	}
}
