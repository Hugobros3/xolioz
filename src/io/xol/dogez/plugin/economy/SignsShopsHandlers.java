package io.xol.dogez.plugin.economy;

import java.util.Iterator;

import io.xol.chunkstories.api.Location;
import io.xol.chunkstories.api.compatibility.ChatColor;
import io.xol.chunkstories.api.entity.interfaces.EntityWithInventory;
import io.xol.chunkstories.api.item.ItemPile;
import io.xol.chunkstories.api.server.Player;
import io.xol.chunkstories.api.voxel.Voxel;
import io.xol.chunkstories.core.entity.voxel.EntitySign;
import io.xol.chunkstories.core.voxel.VoxelSign;
import io.xol.dogez.plugin.DogeZPlugin;
import io.xol.dogez.plugin.player.PlayerProfile;

//Copyright 2014 XolioWare Interactive

public class SignsShopsHandlers {

	private final DogeZPlugin plugin;

	public SignsShopsHandlers(DogeZPlugin dogeZPlugin) {
		plugin = dogeZPlugin;
	}
	
	public boolean handle(Player player, Voxel voxel, int x, int y, int z) {
		try{
			VoxelSign voxelSign = (VoxelSign) voxel;
			EntitySign s = voxelSign.getVoxelEntity(player.getWorld(), x, y, z);
			
			if(safeLineGet(s,0).equalsIgnoreCase("[Vente]") || safeLineGet(s,0).equalsIgnoreCase("[Achat]"))
			{
				if(safeLineGet(s,1).equals("") || safeLineGet(s,2).equals("") || safeLineGet(s,3).equals(""))
				{
					player.sendMessage(ChatColor.RED+"Panneau mal configuré.");
					return true;
				}
				float price = Float.parseFloat(safeLineGet(s,3).replace("xc", ""));
				ItemPile item = null;
				String[] itemLine = safeLineGet(s,2).split(":");
				
				if(plugin.getLootItems().contains(itemLine[0]))
				{
					item = plugin.getLootItems().getItem(itemLine[0]).getItem();
					if(safeLineGet(s,2).contains(":") && safeLineGet(s,2).split(":").length == 2)
					{
						item.setAmount(Integer.parseInt(itemLine[1]));
					}
				}
				//Sell or buy
				if(safeLineGet(s,0).equalsIgnoreCase("[Vente]") && item != null)
				{
					sellPlayer(player,safeLineGet(s,1),item,price);
				}
				if(safeLineGet(s,0).equalsIgnoreCase("[Achat]") && item != null)
				{
					buyPlayer(player,safeLineGet(s,1),item,price);
				}
				return true;
			}
			else if(safeLineGet(s,0).equalsIgnoreCase("[Transport]"))
			{
				PlayerProfile client = plugin.getPlayerProfiles().getPlayerProfile(player.getUUID());
				String[] coords = safeLineGet(s,2).split(" ");
				Location destination = new Location(player.getWorld(),Integer.parseInt(coords[0]),Integer.parseInt(coords[1]),Integer.parseInt(coords[2]));
				float price = Float.parseFloat(safeLineGet(s,3).replace("xc", ""));
				if(client.xcBalance >= price)
				{
					player.sendMessage(ChatColor.AQUA+"Vous êtes partis à "+safeLineGet(s,1)+" pour "+price+" xc.");
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

	private String safeLineGet(EntitySign s, int i) {
		if(s == null)
			return "";
		String content = s.getText();
		if(content == null)
			return "";
		
		String[] splitted = content.split("\n");
		
		if(i > splitted.length)
			i = splitted.length;
		
		return ChatColor.stripColor(splitted[i]);
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
