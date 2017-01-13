package io.xol.dogez.plugin.game.special;

import java.util.ArrayList;
import java.util.List;

import io.xol.chunkstories.api.compatibility.ChatColor;
import io.xol.chunkstories.api.entity.EntityLiving;
import io.xol.chunkstories.api.server.Player;
import io.xol.dogez.plugin.DogeZPlugin;
import io.xol.dogez.plugin.player.PlayerProfile;

// Copyright 2015 XolioWare Interactive

public class DeathRewards {

	public static boolean isPlayerKillingMachine(Player player)
	{
		return false; //player.getInventory().contains(Material.IRON_PICKAXE);
	}

	public static void onWield(Player player, PlayerProfile pp)
	{
		Player victim = DogeZPlugin.access.getServer().getPlayerByName(pp.deathRequest);
		if(pp.deathRequest.equals("") || victim == null || victim.hasPermission("dogez.notargetterinno"))
		{
			findVictim(player, pp);
			victim = DogeZPlugin.access.getServer().getPlayerByName(pp.deathRequest);
		}
		if(pp.deathRequest.equals(""))
			player.sendMessage(ChatColor.DARK_PURPLE+"Les pouvoirs maléfiques de la faux n'ont personne à vous faire tuer pour l'instant.");
		else
			player.sendMessage(ChatColor.DARK_PURPLE+"Les pouvoirs maléfiques de la faux vous demandent de tuer : "+ChatColor.LIGHT_PURPLE+victim.getName());
		
		//player.getWorld().playEffect(player.getLocation(), Effect.ENDER_SIGNAL, 0);
		//player.getWorld().playEffect(player.getLocation(), Effect.PORTAL, 0);
		//PlayersPackets.playSound(player.getLocation(), "portal.portal", 1, 1f);
	}
	
	public static KillerTier getActualLevel(int kills)
	{
		KillerTier tier = KillerTier.NONE;
		for(KillerTier t : KillerTier.values())
		{
			if(t.minimalLevel <= kills)
				tier = t;
		}
		return tier;
	}
	
	public static void onKill(Player player, PlayerProfile playerProfile, Player victim)
	{
		float multiplier = 0f;
		if(victim.getName().equals(playerProfile.deathRequest))
		{
			multiplier = 1f;
		}
		else
		{
			player.sendMessage(ChatColor.RED+"La faux ne vous avait pas demandé de tuer "+ChatColor.DARK_RED+victim.getName()+ChatColor.RED+", pas de bonus pour ce kill.");
			return;
		}
		KillerTier tier = getActualLevel(playerProfile.death_level);
		playerProfile.death_level++;
		float thunes = (float) Math.min(Math.floor(Math.pow(playerProfile.death_level, 1.5)*multiplier*10f), 500f);
		playerProfile.addBalance(thunes);
		player.sendMessage(ChatColor.RED+"Le meurtre de "+ChatColor.DARK_RED+victim.getName()+ChatColor.RED+" vous as rapporté "+thunes+"xc.");
		if(!getActualLevel(playerProfile.death_level).equals(tier))
		{
			player.sendMessage(ChatColor.RED+"Vous avez atteint un nouveau niveau : "+ChatColor.DARK_RED+getActualLevel(playerProfile.death_level).name);
		}
		
		//player.getWorld().playEffect(player.getLocation(), Effect.ENDER_SIGNAL, 0);
		//player.getWorld().playEffect(player.getLocation(), Effect.PORTAL, 0);
		//PlayersPackets.playSound(player.getLocation(), "portal.travel", 1, 1f);
		
		findVictim(player, playerProfile);
		if(playerProfile.deathRequest.equals(""))
			player.sendMessage(ChatColor.RED+"Vous n'avez pas de prochaine victime pour l'instant.");
		else
			player.sendMessage(ChatColor.RED+"Votre prochaine victime est : "+ChatColor.DARK_RED+victim.getName());
		
	}
	
	public static void findVictim(Player player, PlayerProfile playerProfile)
	{
		List<Player> potentialVictims = new ArrayList<Player>();
		for(Player p : player.getWorld().getPlayers())
		{
			if(!p.getName().equals(player))
			{
				if(!p.hasPermission("dogez.notargetterinno") && ((EntityLiving)p.getControlledEntity()).getHealth() != 0)
				{
					potentialVictims.add(p);
				}
			}
		}
		if(potentialVictims.size() > 0)
		{
			Player nextVictim = potentialVictims.get((int)(Math.random()*potentialVictims.size()));
			playerProfile.deathRequest = nextVictim.getName();
			//player.sendMessage("Votre prochaine victime est : "+ChatColor.RED+""+nextVictim.getName());
		}
		else
			playerProfile.deathRequest = "";
	}
	
	public static void onDeath(Player victim, PlayerProfile victimProfile, Player killer, PlayerProfile killerProfile) {
		if(victimProfile.death_level == -1)
			return;
		
		KillerTier tier = getActualLevel(victimProfile.death_level);
		String title = tier.name;
		String prefix = "";
		if(tier.lelal == 0)
			prefix = "Le ";
		if(tier.lelal == 1)
			prefix = "La ";
		if(tier.lelal == 2)
			prefix = "L'";
		
		if(killer == null)
			DogeZPlugin.access.getServer().broadcastMessage(ChatColor.RED+prefix+title+" "+ChatColor.DARK_RED+
				victim.getName()+ChatColor.RED+" est enfin décedé après "+victimProfile.death_level+" victimes.");
		else
		{
			DogeZPlugin.access.getServer().broadcastMessage(ChatColor.RED+prefix+title+" "+ChatColor.DARK_RED+
				victim.getName()+ChatColor.RED+" à été rekt par "+ChatColor.DARK_RED+killer.getName()+ChatColor.RED+" après "+ChatColor.DARK_RED+victimProfile.death_level+ChatColor.RED+" victimes.");
		}
		victimProfile.death_level = -1;
	}
	
	
	private enum KillerTier {
		
		NONE(0, "Innocent porteur de faux", 2),
		RECRUIT(1, "Recrue de la Mort", 1),
		APPRENTICE(3, "Apprentit de la Mort", 2),
		DISCIPLE(5, "Disciple de la Mort", 0),
		MASTER(10, "Maître de la Mort", 0),
		GENOCIDAIRE(15, "Génocidaire", 1),
		CENA(20, "JOHN CENA", 3);
		
		KillerTier(int l, String n, int p)
		{
			lelal = p;
			minimalLevel = l;
			name = n;
		}

		String name;
		int minimalLevel;
		int lelal = 0;
	}
}
