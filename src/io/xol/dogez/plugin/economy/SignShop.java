package io.xol.dogez.plugin.economy;

import io.xol.dogez.plugin.loot.LootItems;
import io.xol.dogez.plugin.player.PlayerProfile;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

//Copyright 2014 XolioWare Interactive

public class SignShop {

	@SuppressWarnings("deprecation")
	public static boolean handle(Player player, Block b) {
		try{
			Sign s = (Sign) b.getState();
			if(safeLineGet(s,0).equalsIgnoreCase("[Vente]") || safeLineGet(s,0).equalsIgnoreCase("[Achat]"))
			{
				if(s.getLine(1).equals("") || safeLineGet(s,2).equals("") || safeLineGet(s,3).equals(""))
				{
					player.sendMessage(ChatColor.RED+"Panneau mal configuré.");
					return true;
				}
				float price = Float.parseFloat(safeLineGet(s,3).replace("xc", ""));
				ItemStack item = null;
				String[] itemLine = safeLineGet(s,2).split(":");
				if(safeLineGet(s,2).contains(":") && safeLineGet(s,2).split(":").length > 2)
				{
					
					if(itemLine.length > 0)
						item = new ItemStack(Integer.parseInt(itemLine[0])); //id
					if(itemLine.length > 1)
						item.setDurability(Short.parseShort(itemLine[1])); //meta
					if(itemLine.length > 2)
						item.setAmount(Integer.parseInt(itemLine[2])); //amount 
					if(itemLine.length > 3)
					{
						ItemMeta m = item.getItemMeta();
						m.setDisplayName(itemLine[3]); // name
						item.setItemMeta(m);
					}
				}
				else if(LootItems.contains(itemLine[0]))
				{
					item = LootItems.getItem(itemLine[0]).getItem();
					if(safeLineGet(s,2).contains(":") && safeLineGet(s,2).split(":").length == 2)
					{
						item.setAmount(Integer.parseInt(itemLine[1]));
					}
				}
				//Sell or buy
				if(safeLineGet(s,0).equalsIgnoreCase("[Vente]") && item != null)
				{
					sellPlayer(player,s.getLine(1),item,price);
				}
				if(safeLineGet(s,0).equalsIgnoreCase("[Achat]") && item != null)
				{
					buyPlayer(player,s.getLine(1),item,price);
				}
				return true;
			}
			else if(safeLineGet(s,0).equalsIgnoreCase("[Transport]"))
			{
				PlayerProfile client = PlayerProfile.getPlayerProfile(player.getUniqueId().toString());
				String[] coords = safeLineGet(s,2).split(" ");
				Location destination = new Location(player.getWorld(),Integer.parseInt(coords[0]),Integer.parseInt(coords[1]),Integer.parseInt(coords[2]));
				float price = Float.parseFloat(safeLineGet(s,3).replace("xc", ""));
				if(client.xcBalance >= price)
				{
					player.sendMessage(ChatColor.AQUA+"Vous êtes partis à "+s.getLine(1)+" pour "+price+" xc.");
					player.teleport(destination);
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

	private static String safeLineGet(Sign s, int i) {
		return ChatColor.stripColor(s.getLine(i));
	}

	private static void sellPlayer(Player player, String name, ItemStack item, float price) {
		PlayerProfile client = PlayerProfile.getPlayerProfile(player.getUniqueId().toString());
		if(client.xcBalance >= price)
		{
			ItemStack[] inv = player.getInventory().getContents();
			int i = 0;
			int emptySpot = -1;
			for(ItemStack it : inv)
			{
				if(it == null && emptySpot == -1)
					emptySpot = i;
				i++;
			}
			if(emptySpot == -1)
			{
				player.sendMessage(ChatColor.RED+"Votre inventaire est plein.");
				return;
			}
			else
			{
				player.sendMessage(ChatColor.AQUA+"Vous avez acheté "+name+" pour "+price+" xc.");
				client.addBalance(-price);
				player.getInventory().addItem(item);
				player.updateInventory();
			}
		}
		else
		{
			player.sendMessage(ChatColor.RED+"Vous n'avez pas les moyens d'acheter ça.");
			return;
		}
	}
	
	@SuppressWarnings("deprecation")
	private static void buyPlayer(Player player, String name, ItemStack item, float price) {
		PlayerProfile client = PlayerProfile.getPlayerProfile(player.getUniqueId().toString());
		ItemStack[] inv = player.getInventory().getContents();
		int i = 0;
		int goodSpot = -1;
		ItemStack sellme = null;
		for(ItemStack it : inv)
		{
			if(it != null && it.getTypeId() == item.getTypeId() && it.getAmount() >= item.getAmount())
			{
				sellme = it;
				goodSpot = i;
			}
			i++;
		}
		if(goodSpot == -1)
		{
			player.sendMessage(ChatColor.RED+"Votre inventaire ne contient pas l'item.");
			return;
		}
		else
		{
			player.sendMessage(ChatColor.AQUA+"Vous avez vendu "+name+" pour "+price+" xc.");
			client.addBalance(price);
			int newAmount = sellme.getAmount()-item.getAmount();
			if(newAmount > 0)
				player.getInventory().getItem(goodSpot).setAmount(newAmount);
			else
				player.getInventory().setItem(goodSpot, null);
			player.updateInventory();
		}
	}

}
