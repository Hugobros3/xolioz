package io.xol.z.plugin.game;

import io.xol.chunkstories.api.entity.Entity;
import io.xol.chunkstories.api.entity.components.EntityCreativeMode;
import io.xol.chunkstories.api.entity.components.EntityInventory;
import io.xol.chunkstories.api.item.inventory.ItemPile;
import io.xol.chunkstories.api.player.Player;
import io.xol.chunkstories.api.util.compatibility.ChatColor;
import io.xol.z.plugin.XolioZPlugin;

/** Helper class to deal with talkie-walkie logic */
public class TalkieWalkiesHandler {

	private final XolioZPlugin plugin;

	public TalkieWalkiesHandler(XolioZPlugin dogeZPlugin) {
		plugin = dogeZPlugin;
	}

	public boolean doesPlayerOwnTW(Player player) {
		Entity entity = player.getControlledEntity();
		
		if(entity == null)
			return false;

		return entity.components.tryWithBoolean(EntityInventory.class, inv -> {
			for (ItemPile i : inv.iterator()) {
				if (i != null && i.getItem().getInternalName().equals("dz_talkie_walkie"))
					return true;
			}
			return false;
		});
	}

	public boolean canPlayerUseTW(Player player) {
		Entity controlledEntity = player.getControlledEntity();

		if (controlledEntity.components.tryWithBoolean(EntityCreativeMode.class, ecm -> ecm.get()))
			return true;

		return doesPlayerOwnTW(player);
	}

	public void leakPrivateConversations(String from, String to, String msg) {
		for (Player p : plugin.getServer().getConnectedPlayers()) {
			if (!from.equals(p.getName()) && p.hasPermission("dogez.socialspy")) {
				p.sendMessage(ChatColor.GRAY + "[SS][" + from + "->" + to + "]:" + msg);
			}
		}
	}
}
