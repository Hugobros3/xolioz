package io.xol.dogez.plugin.game;

import io.xol.chunkstories.api.compatibility.ChatColor;
import io.xol.chunkstories.api.entity.interfaces.EntityCreative;
import io.xol.chunkstories.api.entity.interfaces.EntityWithInventory;
import io.xol.chunkstories.api.item.ItemPile;
import io.xol.chunkstories.api.server.Player;
import io.xol.dogez.plugin.DogeZPlugin;

//(c) 2015 XolioWare Interactive

public class TalkieWalkiesHandler {
	
	private final DogeZPlugin plugin;

	public TalkieWalkiesHandler(DogeZPlugin dogeZPlugin) {
		plugin = dogeZPlugin;
	}
	
	//Helper class for talkie walkie
	
	public boolean hasPlayerGear(Player player)
	{
		EntityWithInventory p = (EntityWithInventory) player.getControlledEntity();
		
		for(ItemPile i : p.getInventory().iterator())
		{
			if(i != null && i.getItem().getInternalName().equals("dz_talkie_walkie"))
				return true;
		}
		return false;
	}
	
	public boolean canPlayerUse(Player player)
	{
		EntityCreative controlledEntity = (EntityCreative) player.getControlledEntity();
		
		if(controlledEntity.getCreativeModeComponent().isCreativeMode())
			return true;
		
		return hasPlayerGear(player);
	}
	
	public void notifyListenersAdmins(String from, String to, String msg)
	{
		for(Player p : plugin.getServer().getConnectedPlayers())
		{
			if(!from.equals(p.getName()) && p.hasPermission("dogez.socialspy"))
			{
				p.sendMessage(ChatColor.GRAY+"[SS]["+from+"->"+to+"]:"+msg);
			}
		}
	}
}
