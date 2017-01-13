package io.xol.dogez.plugin.game;

import io.xol.chunkstories.api.compatibility.ChatColor;
import io.xol.chunkstories.api.entity.interfaces.EntityCreative;
import io.xol.chunkstories.api.entity.interfaces.EntityWithInventory;
import io.xol.chunkstories.api.item.ItemPile;
import io.xol.chunkstories.api.server.Player;
import io.xol.chunkstories.server.Server;
import io.xol.dogez.plugin.DogeZPlugin;

//(c) 2015 XolioWare Interactive

public class TalkieWalkie {
	//Helper class for talkie walkie
	
	public static boolean hasPlayerGear(Player player)
	{
		EntityWithInventory p = (EntityWithInventory) player.getControlledEntity();
		
		for(ItemPile i : p.getInventory().iterator())
		{
			if(i != null && i.getItem().getInternalName().equals("dz_talkie_walkie"))
				return true;
		}
		return false;
	}
	
	public static boolean canPlayerUse(Player player)
	{
		EntityCreative p = (EntityCreative) player.getControlledEntity();
		
		if(p.getCreativeModeComponent().isCreativeMode())
		//if(p.getGameMode().equals(GameMode.CREATIVE))
			return true;
		return hasPlayerGear(player);
	}
	
	public static void notifyListenersAdmins(String from, String to, String msg)
	{
		for(Player p : DogeZPlugin.access.getServer().getConnectedPlayers())
		{
			if(!from.equals(p.getName()) && p.hasPermission("dogez.socialspy"))
			{
				p.sendMessage(ChatColor.GRAY+"[SS]["+from+"->"+to+"]:"+msg);
			}
		}
	}
}
