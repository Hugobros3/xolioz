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
import io.xol.chunkstories.api.item.ItemPile;
import io.xol.chunkstories.api.server.Player;
import io.xol.chunkstories.api.voxel.Voxel;
import io.xol.chunkstories.core.events.PlayerInputPressedEvent;
import io.xol.chunkstories.core.events.PlayerLoginEvent;
import io.xol.chunkstories.core.events.PlayerLogoutEvent;
import io.xol.chunkstories.core.voxel.VoxelChest;
import io.xol.chunkstories.core.voxel.VoxelSign;
import io.xol.dogez.plugin.DogeZPlugin;
import io.xol.dogez.plugin.loot.LootPlace;
import io.xol.dogez.plugin.misc.ChatFormatter;
import io.xol.dogez.plugin.player.PlayerProfile;

//(c) 2014 XolioWare Interactive

public class PlayerListener implements Listener {

	private final DogeZPlugin plugin;

	public PlayerListener(DogeZPlugin plugin) {
		this.plugin = plugin;
	}

	@EventHandler
	public void onPlayerJoin(PlayerLoginEvent ev) {
		Player player = ev.getPlayer();
		String prefix = "";
		if (plugin.config.showUpConnectionMessages)
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
		if (plugin.config.showUpConnectionMessages) {
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
	// public void onPlayerInteract(PlayerInteractEvent event) {
	public void onPlayerInput(PlayerInputPressedEvent event) {

		// We are only interested in mouse clicks
		Input input = event.getInput();
		if (!input.getName().startsWith("mouse."))
			return;

		Player player = event.getPlayer();
		EntityControllable playerEntity = player.getControlledEntity();
		Location selectedLocation = playerEntity.getBlockLookingAt(true);

		Voxel v = plugin.getServer().getContent().voxels().getVoxelById(0);
		if (selectedLocation != null) {
			v = plugin.getServer().getContent().voxels()
					.getVoxelById(playerEntity.getWorld().getVoxelData(selectedLocation));

			// if
			// (!playerEntity.getWorld().getWorldInfo().getName().equals(DogeZPlugin.config.activeWorld))
			// return;
			if (!plugin.isActive())
				return;

			ItemPile itemInHand = null;
			if (playerEntity instanceof EntityWithSelectedItem)
				itemInHand = ((EntityWithSelectedItem) playerEntity).getSelectedItemComponent().getSelectedItem();

			// loot placement and removal
			if (player.hasPermission("dogez.admin")) {
				if (itemInHand != null && itemInHand.getItem().getName().equals("dz_loot_tool")) {
					// Block b = event.getClickedBlock();
					if (v instanceof VoxelChest) {
						PlayerProfile pp = plugin.getPlayerProfiles().getPlayerProfile(player.getUUID());
						String coords = selectedLocation.getX() + ":" + selectedLocation.getY() + ":"
								+ selectedLocation.getZ();
						if (pp.adding && pp.activeCategory != null) {
							LootPlace lp = new LootPlace(plugin.getLootPlaces(),
									coords + ":" + pp.activeCategory + ":" + pp.currentMin + ":" + pp.currentMax,
									player.getWorld());
							if (plugin.getLootPlaces().add(coords, lp, player.getWorld()))
								player.sendMessage(ChatColor.AQUA + "Point de loot ajouté " + lp.toString());
							else
								player.sendMessage(ChatColor.RED + "Ce point existe déjà !");
						} else if (!pp.adding) {
							if (!plugin.getLootPlaces().removePlace(coords, player.getWorld())) {
								player.sendMessage(ChatColor.RED + "Il n'y a pas de point de loot ici !");
							}
						}
						event.setCancelled(true);
					}
				}
			}
		}
		// loot generation
		if (selectedLocation != null) {
			if (v instanceof VoxelChest) {
				String coords = (int) (double) selectedLocation.getX() + ":" + (int) (double) selectedLocation.getY()
						+ ":" + (int) (double) selectedLocation.getZ();

				plugin.getLootPlaces().update(coords, player.getWorld());

			} else if (v instanceof VoxelSign)

				event.setCancelled(
						plugin.getSignShopsHandler().handle(player, v, (int) (double) selectedLocation.getX(),
								(int) (double) selectedLocation.getY(), (int) (double) selectedLocation.getZ()));
		}

	}
}
