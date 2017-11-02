package io.xol.dogez.plugin.economy;

import java.util.Iterator;

import io.xol.chunkstories.api.Location;
import io.xol.chunkstories.api.compatibility.ChatColor;
import io.xol.chunkstories.api.entity.interfaces.EntityWithInventory;
import io.xol.chunkstories.api.item.ItemType;
import io.xol.chunkstories.api.item.inventory.ItemPile;
import io.xol.chunkstories.api.player.Player;
import io.xol.chunkstories.api.voxel.Voxel;
import io.xol.chunkstories.api.voxel.components.VoxelComponent;
import io.xol.chunkstories.api.world.VoxelContext;
import io.xol.chunkstories.api.world.chunk.Chunk.ChunkVoxelContext;
import io.xol.chunkstories.core.voxel.VoxelSign;
import io.xol.chunkstories.core.voxel.components.VoxelComponentSignText;
import io.xol.dogez.plugin.XolioZGamemodePlugin;
import io.xol.dogez.plugin.player.PlayerProfile;

//Copyright 2014 XolioWare Interactive

public class SignsShopsHandlers {

	private final XolioZGamemodePlugin plugin;

	public SignsShopsHandlers(XolioZGamemodePlugin dogeZPlugin) {
		plugin = dogeZPlugin;
	}
	
	public boolean handle(Player player, Voxel voxel, int x, int y, int z) {
		try{
			VoxelContext peek = player.getWorld().peekSafely(x, y, z);
			//EntitySign s = voxelSign.getVoxelEntity(player.getWorld(), x, y, z);
			
			if(safeLineGet(peek,0).equalsIgnoreCase("[Vente]") || safeLineGet(peek,0).equalsIgnoreCase("[Achat]"))
			{
				if(safeLineGet(peek,1).equals("") || safeLineGet(peek,2).equals("") || safeLineGet(peek,3).equals(""))
				{
					player.sendMessage(ChatColor.RED+"Panneau mal configuré.");
					return true;
				}
				float price = Float.parseFloat(safeLineGet(peek,3).replace("xc", ""));
				ItemPile item = null;
				String[] itemLine = safeLineGet(peek,2).split(":");
				
				/*if(plugin.getLootItems().contains(itemLine[0]))
				{
					item = plugin.getLootItems().getItem(itemLine[0]).getItem();
					if(safeLineGet(s,2).contains(":") && safeLineGet(s,2).split(":").length == 2)
					{
						item.setAmount(Integer.parseInt(itemLine[1]));
					}
				}*/
				
				ItemType type = plugin.getPluginExecutionContext().getContent().items().getItemTypeByName(itemLine[0]);
				if(type != null)
					item = new ItemPile(type);
				
				//Sell or buy
				if(safeLineGet(peek,0).equalsIgnoreCase("[Vente]") && item != null)
				{
					sellPlayer(player,safeLineGet(peek,1),item,price);
				}
				if(safeLineGet(peek,0).equalsIgnoreCase("[Achat]") && item != null)
				{
					buyPlayer(player,safeLineGet(peek,1),item,price);
				}
				return true;
			}
			else if(safeLineGet(peek,0).equalsIgnoreCase("[Transport]"))
			{
				PlayerProfile client = plugin.getPlayerProfiles().getPlayerProfile(player.getUUID());
				String[] coords = safeLineGet(peek,2).split(" ");
				Location destination = new Location(player.getWorld(),Integer.parseInt(coords[0]),Integer.parseInt(coords[1]),Integer.parseInt(coords[2]));
				float price = Float.parseFloat(safeLineGet(peek,3).replace("xc", ""));
				if(client.xcBalance >= price)
				{
					player.sendMessage(ChatColor.AQUA+"Vous êtes partis à "+safeLineGet(peek,1)+" pour "+price+" xc.");
					player.setLocation(destination);
					client.addBalance(-price);
				}
				else
				{
					player.sendMessage(ChatColor.RED+"Vous n'avez pas les moyens d'utiliser ce téléporteur.");
					return false;
				}
			}
		}
		catch(Exception e)
		{
			player.sendMessage(ChatColor.RED+"Erreur :/");
			player.sendMessage(e.getLocalizedMessage());
			e.printStackTrace();
		}
		return false;
	}

	private String safeLineGet(VoxelContext peek, int i) {
		if(peek != null && peek instanceof ChunkVoxelContext) {
			VoxelComponent component = ((ChunkVoxelContext) peek).components().get("signData");
			if(component != null && component instanceof VoxelComponentSignText) {

				String content = ((VoxelComponentSignText) component).getSignText();
				if(content == null)
					return "";
				
				String[] splitted = content.split("\n");
				
				if(i > splitted.length)
					i = splitted.length;
				
				return ChatColor.stripColor(splitted[i]);
			}
		}

		return "";
	}

	private void sellPlayer(Player player, String name, ItemPile item, float price) {
		PlayerProfile client = plugin.getPlayerProfiles().getPlayerProfile(player.getUUID());
		if(client.xcBalance >= price)
		{
			if(((EntityWithInventory) player.getControlledEntity()).getInventory().addItemPile(item) != null)
			{
				player.sendMessage(ChatColor.RED+"Votre inventaire est plein.");
				return;
			}
			else
			{
				player.sendMessage(ChatColor.AQUA+"Vous avez acheté "+name+" pour "+price+" xc.");
				client.addBalance(-price);
			}
		}
		else
		{
			player.sendMessage(ChatColor.RED+"Vous n'avez pas les moyens d'acheter ça.");
			return;
		}
	}
	
	private void buyPlayer(Player player, String name, ItemPile item, float price) {
		PlayerProfile client = plugin.getPlayerProfiles().getPlayerProfile(player.getUUID());
		
		Iterator<ItemPile> i = ((EntityWithInventory) player.getControlledEntity()).getInventory().iterator();
		while(i.hasNext())
		{
			ItemPile pile = i.next();
			
			if(pile != null && pile.getItem().equals(item) && pile.getAmount() >= item.getAmount())
			{
				if(pile.getAmount() > item.getAmount())
					pile.setAmount(pile.getAmount() - item.getAmount());
				else
					i.remove();
				
				player.sendMessage(ChatColor.AQUA+"Vous avez vendu "+name+" pour "+price+" xc.");
				client.addBalance(price);
				return;
			}
		}
		
		player.sendMessage(ChatColor.RED+"Votre inventaire ne contient pas l'item.");
		return;
	}

}
