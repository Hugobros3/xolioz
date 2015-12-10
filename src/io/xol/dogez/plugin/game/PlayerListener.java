package io.xol.dogez.plugin.game;

import io.xol.dogez.plugin.DogeZPlugin;
import io.xol.dogez.plugin.economy.SignShop;
import io.xol.dogez.plugin.game.special.DeathRewards;
import io.xol.dogez.plugin.loot.LootItems;
import io.xol.dogez.plugin.loot.LootPlace;
import io.xol.dogez.plugin.loot.LootPlaces;
import io.xol.dogez.plugin.misc.ChatFormatter;
import io.xol.dogez.plugin.player.PlayerProfile;
import io.xol.dogez.plugin.player.PlayersPackets;
import io.xol.dogez.plugin.weapon.Weapon;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;

import ru.tehkode.permissions.PermissionUser;
import ru.tehkode.permissions.bukkit.PermissionsEx;

//(c) 2014 XolioWare Interactive

public class PlayerListener implements Listener{
	
	@EventHandler
	void onPlayerJoin(PlayerJoinEvent ev)
	{
		Player player = ev.getPlayer();
		PermissionUser user = PermissionsEx.getUser(player);
		String prefix = user.getPrefix();
		if(DogeZPlugin.config.showUpConnectionMessages)
			ev.setJoinMessage(ChatColor.DARK_GRAY+"["+ChatColor.GREEN+"+"+ChatColor.DARK_GRAY+"] "+ChatFormatter.convertString(prefix)+ev.getPlayer().getName()+ChatColor.GRAY+" vient de se connecter.");
		else
			ev.setJoinMessage(null);
		PlayerProfile.addPlayerProfile(player.getUniqueId().toString(), player.getName());
	}
	
	@EventHandler
	void onPlayerQuit(PlayerQuitEvent ev)
	{
		Player player = ev.getPlayer();
		PermissionUser user = PermissionsEx.getUser(ev.getPlayer());
		String prefix = user.getPrefix();
		if(DogeZPlugin.config.showUpConnectionMessages)
		{
			if(!ev.getQuitMessage().startsWith(ChatColor.DARK_GRAY+"["))
				ev.setQuitMessage(ChatColor.DARK_GRAY+"["+ChatColor.RED+"-"+ChatColor.DARK_GRAY+"] "+ChatFormatter.convertString(prefix)+ev.getPlayer().getName()+ChatColor.GRAY+" vient de se déconnecter.");
		}
		else
			ev.setQuitMessage(null);
		PlayerProfile pp = PlayerProfile.getPlayerProfile(player.getUniqueId().toString());
		//Delete his torch
		pp.updateTorch(false);
		//Anti log
		if(System.currentTimeMillis() - pp.lastHitTime < 7*1000L)
		{
			Bukkit.getLogger().info(player.getName()+" was killed for combat log ( "+(System.currentTimeMillis() - pp.lastHitTime)+" ms wait before logoff )");
			player.damage(150000);
		}
		PlayerProfile.removePlayerProfile(player.getUniqueId().toString());
	}
	
	@EventHandler
	void onPlayerKick(PlayerKickEvent ev)
	{
		ev.setLeaveMessage(ChatColor.DARK_GRAY+"["+ChatColor.RED+"-"+ChatColor.DARK_GRAY+"] "+ChatColor.RED+ev.getPlayer().getName()+" à été kické.");
	}
	
	@SuppressWarnings("deprecation")
	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {
		Player player = event.getPlayer();
		if(player.getWorld().getName().equals(DogeZPlugin.config.activeWorld) || player.getWorld().getName().equals("namalsk-map"))
		{
			//loot placement and removal
			if(player.hasPermission("dogez.admin"))
			{
				if(player.getItemInHand().getType().equals(Material.BOOK))
				{
					if(event.hasBlock())
					{
						Block b = event.getClickedBlock();
						if(b.getType().equals(Material.CHEST))
						{
							Location loc = b.getLocation();
							PlayerProfile pp = PlayerProfile.getPlayerProfile(player.getUniqueId().toString());
							String coords = loc.getBlockX()+":"+loc.getBlockY()+":"+loc.getBlockZ();
							if(pp.adding && pp.activeCategory != null)
							{
								LootPlace lp = new LootPlace(coords+":"+pp.activeCategory+":"+pp.currentMin+":"+pp.currentMax,player.getWorld());
								if(LootPlaces.add(coords,lp,player.getWorld()))
									player.sendMessage(ChatColor.AQUA+"Point de loot ajouté "+lp.toString());
								else
									player.sendMessage(ChatColor.RED+"Ce point existe déjà !");
							}
							else if(!pp.adding)
							{
								if(!LootPlaces.removePlace(coords,player.getWorld()))
								{
									player.sendMessage(ChatColor.RED+"Il n'y a pas de point de loot ici !");
								}
							}
							event.setCancelled(true);
						}
					}
				}
			}
			//Torch toggle
			if(player.getItemInHand().getType().equals(Material.STICK))
			{
				ItemStack torchOff = LootItems.getItem("torchOff").getItem();
				player.getInventory().setItemInHand(torchOff);
				return;
			}
			else if(player.getItemInHand().getType().equals(Material.REDSTONE))
			{
				ItemStack torchOn = LootItems.getItem("torchOn").getItem();
				player.getInventory().setItemInHand(torchOn);
				return;
			}
			//loot generation
			if(event.hasBlock())
			{
				Block b = event.getClickedBlock();
				if(b.getType().equals(Material.CHEST))
				{
					Location loc = b.getLocation();
					String coords = loc.getBlockX()+":"+loc.getBlockY()+":"+loc.getBlockZ();
					//player.sendMessage("debug:"+coords);
					LootPlaces.update(coords,player.getWorld());
				}
				else if(b.getType().equals(Material.WALL_SIGN) || b.getType().equals(Material.SIGN_POST))
				{
					if(event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK )
						event.setCancelled(SignShop.handle(player,b));
				}
			}
		}
		//Weapon shooting !
		ItemStack item = player.getItemInHand();
		if(item != null)
		{
			if(Weapon.isWeapon(item.getTypeId(), item.getDurability()))
			{
				if(event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK )
					Weapon.getWeapon(item.getTypeId(), item.getDurability()).clickEvent(player,true);
				if(event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK )
					Weapon.getWeapon(item.getTypeId(), item.getDurability()).clickEvent(player,false);
				event.setCancelled(true);
			}
			else if(item.getType().equals(Material.GOLD_SWORD))
			{
				PlayersPackets.playSound(player.getLocation(), "dogez.weapon.chainsaw", 1f, 1f);
			}
		}

		PlayerProfile pp = PlayerProfile.getPlayerProfile(player.getUniqueId().toString());
		pp.updateXPLevel();
		/*StatusBarAPI.removeStatusBar(player);
		StatusBarAPI.setStatusBar(player, pp.getWeaponString(), pp.heat);*/
	}
	
	@EventHandler
	public void onPlayermove(PlayerMoveEvent event)
	{
		Player player = event.getPlayer();
		PlayerProfile pp = PlayerProfile.getPlayerProfile(player.getUniqueId().toString());
		ItemStack itemStack = player.getItemInHand();
		if(itemStack != null && itemStack.getType().equals(Material.STICK))
		{
			pp.updateTorch(true);
		}
		else
			pp.updateTorch(false);
	}
	
	@EventHandler
    public void onPlayerItemHeld(PlayerItemHeldEvent event){
		Player player = event.getPlayer();
		PlayerProfile pp = PlayerProfile.getPlayerProfile(player.getUniqueId().toString());
		pp.isScoping = false;
		pp.updateXPLevel();
		
		ItemStack stack = player.getInventory().getContents()[event.getNewSlot()];
		if(stack != null)
		{
			if(stack.getType().equals(Material.IRON_PICKAXE))
			{
				DeathRewards.onWield(player, pp);
			}
		}
		
		Weapon.refreshEffects(event.getPlayer());
	}
    @SuppressWarnings("deprecation")
	@EventHandler
    public void onPlayerAttack(EntityDamageByEntityEvent event)
    {
	    if(event.getDamager() instanceof Player)
	    {
	    	//Weapon shooting !
			ItemStack item = ((Player)event.getDamager()).getItemInHand();
			if(item != null)
			{
				if(Weapon.isWeapon(item.getTypeId()%16, item.getDurability()))
				{
					event.setCancelled(true);
				}
			}
	    }
    }
    @EventHandler
    public void onFoodChange(FoodLevelChangeEvent event)
    {
    	if(event.getEntity() instanceof Player)
    	{
    		Player player = (Player)event.getEntity();
        	if(player.getFoodLevel() > event.getFoodLevel() && Math.random() > 0.4f)
        		event.setCancelled(true);
    	}
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerRespaw(PlayerRespawnEvent event)
    {
    	Player player = event.getPlayer();
    	PlayerProfile pp = PlayerProfile.getPlayerProfile(player.getUniqueId().toString());
    	if(pp != null)
    	{
    		if(pp.goToHell)
    		{
    			//player.sendMessage("You've gone to hell.");
    			event.setRespawnLocation(new Location(player.getWorld(), 1419, 67.5, 2022));
    			pp.goToHell = false;
    		}
    		else
    		{
    			//player.sendMessage("Normal respawn.");
    			event.setRespawnLocation(new Location(player.getWorld(), 2011, 101.5, 2012));
    		}
    	}
    }
}
