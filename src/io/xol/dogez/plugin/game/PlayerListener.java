package io.xol.dogez.plugin.game;

import io.xol.chunkstories.api.Location;
import io.xol.chunkstories.api.compatibility.ChatColor;
import io.xol.chunkstories.api.entity.DamageCause;
import io.xol.chunkstories.api.entity.EntityLiving;
import io.xol.chunkstories.api.entity.interfaces.EntityControllable;
import io.xol.chunkstories.api.entity.interfaces.EntityWithSelectedItem;
import io.xol.chunkstories.api.events.EventHandler;
import io.xol.chunkstories.api.events.Listener;
import io.xol.chunkstories.api.input.Input;
import io.xol.chunkstories.api.server.Player;
import io.xol.chunkstories.api.voxel.Voxel;
import io.xol.chunkstories.core.events.PlayerInputPressedEvent;
import io.xol.chunkstories.core.events.PlayerLoginEvent;
import io.xol.chunkstories.core.events.PlayerLogoutEvent;
import io.xol.chunkstories.core.voxel.VoxelChest;
import io.xol.chunkstories.core.voxel.VoxelSign;
import io.xol.chunkstories.item.ItemPile;
import io.xol.chunkstories.voxel.Voxels;
import io.xol.dogez.plugin.DogeZPlugin;
import io.xol.dogez.plugin.economy.SignShop;
import io.xol.dogez.plugin.loot.LootPlace;
import io.xol.dogez.plugin.loot.LootPlaces;
import io.xol.dogez.plugin.misc.ChatFormatter;
import io.xol.dogez.plugin.player.PlayerProfile;

//(c) 2014 XolioWare Interactive

public class PlayerListener implements Listener {

	@EventHandler
	public void onPlayerJoin(PlayerLoginEvent ev) {
		Player player = ev.getPlayer();
		String prefix = "";
		if (DogeZPlugin.config.showUpConnectionMessages)
			ev.setConnectionMessage(ChatColor.DARK_GRAY + "[" + ChatColor.GREEN + "+" + ChatColor.DARK_GRAY + "] "
					+ ChatFormatter.convertString(prefix) + ev.getPlayer().getName() + ChatColor.GRAY
					+ " vient de se connecter.");
		else
			ev.setConnectionMessage(null);
		PlayerProfile.addPlayerProfile(player.getUUID(), player.getName());
	}

	@EventHandler
	public void onPlayerQuit(PlayerLogoutEvent ev) {
		Player player = ev.getPlayer();
		String prefix = "";
		if (DogeZPlugin.config.showUpConnectionMessages) {
			if (!ev.getLogoutMessage().startsWith(ChatColor.DARK_GRAY + "["))
				ev.setLogoutMessage(ChatColor.DARK_GRAY + "[" + ChatColor.RED + "-" + ChatColor.DARK_GRAY + "] "
						+ ChatFormatter.convertString(prefix) + ev.getPlayer().getName() + ChatColor.GRAY
						+ " vient de se déconnecter.");
		} else
			ev.setLogoutMessage(null);
		PlayerProfile pp = PlayerProfile.getPlayerProfile(player.getUUID());
		// Delete his torch
		// pp.updateTorch(false);
		// Anti log
		if (System.currentTimeMillis() - pp.lastHitTime < 7 * 1000L) {
			DogeZPlugin.access.getLogger().info(player.getName() + " was killed for combat log ( "
					+ (System.currentTimeMillis() - pp.lastHitTime) + " ms wait before logoff )");
			((EntityLiving) player.getControlledEntity()).damage(new DamageCause() {

				@Override
				public String getName() {
					return "Anti-combatlog";
				}

				@Override
				public long getCooldownInMs() {
					return 0;
				}
			}, 150000);
		}
		PlayerProfile.removePlayerProfile(player.getUUID());
	}

	/*
	 * @EventHandler void onPlayerKick(PlayerKickEvent ev) {
	 * ev.setLeaveMessage(ChatColor.DARK_GRAY+"["+ChatColor.RED+"-"+ChatColor.
	 * DARK_GRAY+"] "+ChatColor.RED+ev.getPlayer().getName()+" à été kické."); }
	 */

	@EventHandler
	// public void onPlayerInteract(PlayerInteractEvent event) {
	public void onPlayerInput(PlayerInputPressedEvent event) {

		
		//We are only interested in mouse clicks
		Input input = event.getInput();
		if(!input.getName().startsWith("mouse."))
			return;

		Player player = event.getPlayer();
		EntityControllable playerEntity = player.getControlledEntity();
		Location selectedLocation = playerEntity.getBlockLookingAt(true);
		
		Voxel v = Voxels.get(0);
		if (selectedLocation != null) {
			v = Voxels.get(playerEntity.getWorld().getVoxelData(selectedLocation));

		//if (!playerEntity.getWorld().getWorldInfo().getName().equals(DogeZPlugin.config.activeWorld))
		//	return;
		if (!DogeZPlugin.isActive())
			return;

		ItemPile itemInHand = null;
		if (playerEntity instanceof EntityWithSelectedItem)
			itemInHand = ((EntityWithSelectedItem) playerEntity).getSelectedItemComponent().getSelectedItem();

		// loot placement and removal
		if (player.hasPermission("dogez.admin")) {
			if (itemInHand != null && itemInHand.getItem().getName().equals("dz_loot_tool")) {
					//Block b = event.getClickedBlock();
					if (v instanceof VoxelChest) {
						PlayerProfile pp = PlayerProfile.getPlayerProfile(player.getUUID());
						String coords = selectedLocation.getX() + ":" + selectedLocation.getY() + ":" + selectedLocation.getZ();
						if (pp.adding && pp.activeCategory != null) {
							LootPlace lp = new LootPlace(
									coords + ":" + pp.activeCategory + ":" + pp.currentMin + ":" + pp.currentMax,
									player.getWorld());
							if (LootPlaces.add(coords, lp, player.getWorld()))
								player.sendMessage(ChatColor.AQUA + "Point de loot ajouté " + lp.toString());
							else
								player.sendMessage(ChatColor.RED + "Ce point existe déjà !");
						} else if (!pp.adding) {
							if (!LootPlaces.removePlace(coords, player.getWorld())) {
								player.sendMessage(ChatColor.RED + "Il n'y a pas de point de loot ici !");
							}
						}
						event.setCancelled(true);
					}
				}
			}
		}
		// Torch toggle
		/*if (player.getItemInHand().getType().equals(Material.STICK)) {
			ItemStack torchOff = LootItems.getItem("torchOff").getItem();
			player.getInventory().setItemInHand(torchOff);
			return;
		} else if (player.getItemInHand().getType().equals(Material.REDSTONE)) {
			ItemStack torchOn = LootItems.getItem("torchOn").getItem();
			player.getInventory().setItemInHand(torchOn);
			return;
		}*/
		// loot generation
		if (selectedLocation != null) {
			//Block b = event.getClickedBlock();
			if (v instanceof VoxelChest) {
				String coords = (int)selectedLocation.getX() + ":" + (int)selectedLocation.getY() + ":" +(int) selectedLocation.getZ();
				//player.sendMessage("debug:"+coords);
				
				LootPlaces.update(coords, player.getWorld());
				
			} else if (v instanceof VoxelSign)//(b.getType().equals(Material.WALL_SIGN) || b.getType().equals(Material.SIGN_POST)) {
				
				//if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)
					event.setCancelled(SignShop.handle(player, v, (int)selectedLocation.getX(), (int)selectedLocation.getY(),  (int)selectedLocation.getZ()));
		}
		

		// Weapon shooting !
		/*ItemStack item = player.getItemInHand();
		if (item != null) {
			if (Weapon.isWeapon(item.getTypeId(), item.getDurability())) {
				if (event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK)
					Weapon.getWeapon(item.getTypeId(), item.getDurability()).clickEvent(player, true);
				if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)
					Weapon.getWeapon(item.getTypeId(), item.getDurability()).clickEvent(player, false);
				event.setCancelled(true);
			} else if (item.getType().equals(Material.GOLD_SWORD)) {
				PlayersPackets.playSound(player.getLocation(), "dogez.weapon.chainsaw", 1f, 1f);
			}
		}*/

		//PlayerProfile pp = PlayerProfile.getPlayerProfile(player.getUniqueId().toString());
		//pp.updateXPLevel();
		
		/*
		 * StatusBarAPI.removeStatusBar(player);
		 * StatusBarAPI.setStatusBar(player, pp.getWeaponString(), pp.heat);
		 */
	}

	/*
	 * @EventHandler public void onPlayermove(PlayerMoveEvent event) { Player
	 * player = event.getPlayer(); PlayerProfile pp =
	 * PlayerProfile.getPlayerProfile(player.getUniqueId().toString());
	 * ItemStack itemStack = player.getItemInHand(); if(itemStack != null &&
	 * itemStack.getType().equals(Material.STICK)) { pp.updateTorch(true); }
	 * else pp.updateTorch(false); }
	 */

	/*
	 * @EventHandler public void onPlayerItemHeld(PlayerItemHeldEvent event){
	 * Player player = event.getPlayer(); PlayerProfile pp =
	 * PlayerProfile.getPlayerProfile(player.getUniqueId().toString());
	 * pp.isScoping = false; pp.updateXPLevel();
	 * 
	 * ItemStack stack =
	 * player.getInventory().getContents()[event.getNewSlot()]; if(stack !=
	 * null) { if(stack.getType().equals(Material.IRON_PICKAXE)) {
	 * DeathRewards.onWield(player, pp); } }
	 * 
	 * Weapon.refreshEffects(event.getPlayer()); }
	 */

	/*@SuppressWarnings("deprecation")
	@EventHandler
	public void onPlayerAttack(EntityDamageByEntityEvent event) {
		if (event.getDamager() instanceof Player) {
			// Weapon shooting !
			ItemStack item = ((Player) event.getDamager()).getItemInHand();
			if (item != null) {
				if (Weapon.isWeapon(item.getTypeId() % 16, item.getDurability())) {
					event.setCancelled(true);
				}
			}
		}
	}*/

	/*
	 * @EventHandler public void onFoodChange(FoodLevelChangeEvent event) {
	 * if(event.getEntity() instanceof Player) { Player player =
	 * (Player)event.getEntity(); if(player.getFoodLevel() >
	 * event.getFoodLevel() && Math.random() > 0.4f) event.setCancelled(true); }
	 * }
	 */

	/*
	 * @EventHandler(priority = EventPriority.HIGHEST) public void
	 * onPlayerRespaw(PlayerRespawnEvent event) { Player player =
	 * event.getPlayer(); PlayerProfile pp =
	 * PlayerProfile.getPlayerProfile(player.getUniqueId().toString()); if(pp !=
	 * null) { if(pp.goToHell) { //player.sendMessage("You've gone to hell.");
	 * event.setRespawnLocation(new Location(player.getWorld(), 1419, 67.5,
	 * 2022)); pp.goToHell = false; } else { //player.sendMessage(
	 * "Normal respawn."); event.setRespawnLocation(new
	 * Location(player.getWorld(), 2011, 101.5, 2012)); } } }
	 */
}
