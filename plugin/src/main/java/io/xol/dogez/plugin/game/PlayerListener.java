package io.xol.dogez.plugin.game;

import io.xol.chunkstories.api.Location;
import io.xol.chunkstories.api.compatibility.ChatColor;
import io.xol.chunkstories.api.entity.DamageCause;
import io.xol.chunkstories.api.entity.EntityLiving;
import io.xol.chunkstories.api.entity.interfaces.EntityControllable;
import io.xol.chunkstories.api.entity.interfaces.EntityWithSelectedItem;
import io.xol.chunkstories.api.events.EventHandler;
import io.xol.chunkstories.api.events.Listener;
import io.xol.chunkstories.api.events.player.PlayerInputPressedEvent;
import io.xol.chunkstories.api.events.player.PlayerLoginEvent;
import io.xol.chunkstories.api.events.player.PlayerLogoutEvent;
import io.xol.chunkstories.api.input.Input;
import io.xol.chunkstories.api.item.inventory.ItemPile;
import io.xol.chunkstories.api.player.Player;
import io.xol.chunkstories.api.voxel.Voxel;
import io.xol.chunkstories.api.world.VoxelContext;
import io.xol.chunkstories.core.voxel.VoxelChest;
import io.xol.chunkstories.core.voxel.VoxelSign;
import io.xol.dogez.plugin.XolioZGamemodePlugin;
import io.xol.dogez.plugin.loot.LootPlace;
import io.xol.dogez.plugin.misc.ChatFormatter;
import io.xol.dogez.plugin.player.PlayerProfile;

//(c) 2014 XolioWare Interactive

public class PlayerListener implements Listener {

	private final XolioZGamemodePlugin plugin;

	public PlayerListener(XolioZGamemodePlugin plugin) {
		this.plugin = plugin;
	}

	@EventHandler
	public void onPlayerJoin(PlayerLoginEvent ev) {
		Player player = ev.getPlayer();
		String prefix = "";
		if (plugin.config.getProperty("showConnectionMessages", "true").equals("true"))
			ev.setConnectionMessage(ChatColor.DARK_GRAY + "[" + ChatColor.GREEN + "+" + ChatColor.DARK_GRAY + "] "
					+ ChatFormatter.convertString(prefix) + ev.getPlayer().getName() + ChatColor.GRAY
					+ "#{dogez.loggedin}");
		else
			ev.setConnectionMessage(null);
		plugin.getPlayerProfiles().addPlayerProfile(player.getUUID(), player.getName());
	}

	@EventHandler
	public void onPlayerQuit(PlayerLogoutEvent ev) {
		Player player = ev.getPlayer();
		String prefix = "";
		if (plugin.config.getProperty("showConnectionMessages", "true").equals("true")) {
			if (!ev.getLogoutMessage().startsWith(ChatColor.DARK_GRAY + "["))
				ev.setLogoutMessage(ChatColor.DARK_GRAY + "[" + ChatColor.RED + "-" + ChatColor.DARK_GRAY + "] "
						+ ChatFormatter.convertString(prefix) + ev.getPlayer().getName() + ChatColor.GRAY
						+ "#{dogez.loggedout}");
		} else
			ev.setLogoutMessage(null);
		
		PlayerProfile pp = plugin.getPlayerProfiles().getPlayerProfile(player.getUUID());
		// Delete his torch
		// pp.updateTorch(false);
		// Anti log
		if (System.currentTimeMillis() - pp.lastHitTime < 7 * 1000L) {
			plugin.getLogger().info(player.getName() + " was killed for combat log ( "
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
		plugin.getPlayerProfiles().removePlayerProfile(player.getUUID());
	}

	@EventHandler
	public void onPlayerInput(PlayerInputPressedEvent event) {

		// We are only interested in mouse clicks
		Input input = event.getInput();
		if (!input.getName().startsWith("mouse."))
			return;

		Player player = event.getPlayer();

		EntityControllable playerEntity = player.getControlledEntity();
		Location selectedLocation = playerEntity.getBlockLookingAt(true);

		if (selectedLocation != null) {
			VoxelContext context = player.getWorld().peekSafely(selectedLocation);
			
			if (!plugin.isActive())
				return;

			ItemPile itemInHand = null;
			if (playerEntity instanceof EntityWithSelectedItem)
				itemInHand = ((EntityWithSelectedItem) playerEntity).getSelectedItem();

			// loot placement and removal
			if (player.hasPermission("dogez.admin")) {
				if (itemInHand != null && itemInHand.getItem().getName().equals("dz_loot_tool")) {
					
					if (context.getVoxel() instanceof VoxelChest) {
						PlayerProfile profile = plugin.getPlayerProfiles().getPlayerProfile(player.getUUID());
						
						String loot_coordinates = selectedLocation.x() + ":" + selectedLocation.y() + ":" + selectedLocation.z();
						
						if (profile.adding && profile.activeCategory != null) {
							/*LootPlace lootPlace = new LootPlace(plugin.getLootPlaces(),
									loot_coordinates + ":" + profile.activeCategory + ":" + profile.currentMin + ":" + profile.currentMax,
									player.getWorld());*/
							LootPlace lootPlace = new LootPlace(plugin, selectedLocation, profile.activeCategory, profile.currentMin, profile.currentMax);
							if (plugin.getLootPlaces().add(loot_coordinates, lootPlace, player.getWorld()))
								player.sendMessage(ChatColor.AQUA + "Point de loot ajouté " + lootPlace.toString());
							else
								player.sendMessage(ChatColor.RED + "Ce point existe déjà !");
						} else if (!profile.adding) {
							if (!plugin.getLootPlaces().removePlace(loot_coordinates, player.getWorld())) {
								player.sendMessage(ChatColor.RED + "Il n'y a pas de point de loot ici !");
							}
						}
						event.setCancelled(true);
					}
				}
			}
		
			// loot regeneration
			Voxel voxel = player.getWorld().peekSafely(selectedLocation).getVoxel();
			if (voxel instanceof VoxelChest) {
				String coords = (int) (double) selectedLocation.x() + ":" + (int) (double) selectedLocation.y() + ":" + (int) (double) selectedLocation.z();

				plugin.getLootPlaces().update(coords, player.getWorld());
			} 
			//Sign-shop stuff
			else if (voxel instanceof VoxelSign)
				//Pass the event to the SignShopsHandler that will decide what to do with it
				event.setCancelled(plugin.getSignShopsHandler().handle(player, voxel, (int) (double) selectedLocation.x(), (int) (double) selectedLocation.y(), (int) (double) selectedLocation.z()));
		}

	}
}
