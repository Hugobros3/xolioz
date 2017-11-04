package io.xol.dogez.plugin.game;

import io.xol.chunkstories.api.entity.interfaces.EntityCreative;
import io.xol.chunkstories.api.entity.interfaces.EntityWithInventory;
import io.xol.chunkstories.api.item.inventory.ItemPile;
import io.xol.chunkstories.api.player.Player;
import io.xol.chunkstories.api.util.compatibility.ChatColor;
import io.xol.dogez.plugin.XolioZGamemodePlugin;

public class TalkieWalkiesHandler {
	
	private final XolioZGamemodePlugin plugin;

	public TalkieWalkiesHandler(XolioZGamemodePlugin dogeZPlugin) {
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
		
		if(controlledEntity.getCreativeModeComponent().get())
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
