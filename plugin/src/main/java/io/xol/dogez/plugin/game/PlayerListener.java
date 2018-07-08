package io.xol.dogez.plugin.game;

import io.xol.chunkstories.api.Location;
import io.xol.chunkstories.api.entity.DamageCause;
import io.xol.chunkstories.api.entity.Entity;
import io.xol.chunkstories.api.entity.components.EntityHealth;
import io.xol.chunkstories.api.entity.components.EntitySelectedItem;
import io.xol.chunkstories.api.entity.traits.TraitVoxelSelection;
import io.xol.chunkstories.api.events.EventHandler;
import io.xol.chunkstories.api.events.Listener;
import io.xol.chunkstories.api.events.player.PlayerInputPressedEvent;
import io.xol.chunkstories.api.events.player.PlayerLoginEvent;
import io.xol.chunkstories.api.events.player.PlayerLogoutEvent;
import io.xol.chunkstories.api.input.Input;
import io.xol.chunkstories.api.item.inventory.ItemPile;
import io.xol.chunkstories.api.player.Player;
import io.xol.chunkstories.api.util.compatibility.ChatColor;
import io.xol.chunkstories.api.voxel.Voxel;
import io.xol.chunkstories.api.world.cell.CellData;
import io.xol.chunkstories.core.voxel.VoxelChest;
import io.xol.dogez.plugin.XolioZGamemodePlugin;
import io.xol.dogez.plugin.loot.LootPlace;
import io.xol.dogez.plugin.misc.ChatFormatter;
import io.xol.dogez.plugin.player.PlayerProfile;

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
			ev.setConnectionMessage(ChatColor.DARK_GRAY + "[" + ChatColor.GREEN + "+" + ChatColor.DARK_GRAY + "] " + ChatFormatter.convertString(prefix)
					+ ev.getPlayer().getName() + ChatColor.GRAY + "#{dogez.loggedin}");
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
				ev.setLogoutMessage(ChatColor.DARK_GRAY + "[" + ChatColor.RED + "-" + ChatColor.DARK_GRAY + "] " + ChatFormatter.convertString(prefix)
						+ ev.getPlayer().getName() + ChatColor.GRAY + "#{dogez.loggedout}");
		} else
			ev.setLogoutMessage(null);

		PlayerProfile pp = plugin.getPlayerProfiles().getPlayerProfile(player.getUUID());

		if (System.currentTimeMillis() - pp.lastHitTime < 7 * 1000L) {
			plugin.getLogger()
					.info(player.getName() + " was killed for combat leave ( " + (System.currentTimeMillis() - pp.lastHitTime) + " ms wait before logoff )");

			Entity playerEntity = player.getControlledEntity();
			if (playerEntity != null) {
				playerEntity.components.with(EntityHealth.class, eh -> {
					eh.damage(new DamageCause() {

						@Override
						public String getName() {
							return "Cowards no Rewards";
						}

						@Override
						public long getCooldownInMs() {
							return 0;
						}
					}, 150000);
				});
			}
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

		Entity playerEntity = player.getControlledEntity();
		Location selectedLocation = playerEntity.traits.tryWith(TraitVoxelSelection.class, tvs -> tvs.getBlockLookingAt(true, false));

		if (selectedLocation != null) {
			CellData context = player.getWorld().peekSafely(selectedLocation);

			if (!plugin.isActive())
				return;

			ItemPile itemInHand = playerEntity.components.tryWith(EntitySelectedItem.class, esi -> esi.getSelectedItem());

			// loot placement and removal
			if (player.hasPermission("dogez.admin")) {
				if (itemInHand != null && itemInHand.getItem().getName().equals("dz_loot_tool")) {

					if (context.getVoxel() instanceof VoxelChest) {
						PlayerProfile profile = plugin.getPlayerProfiles().getPlayerProfile(player.getUUID());

						//TODO use Vector3i here
						String loot_coordinates = selectedLocation.x() + ":" + selectedLocation.y() + ":" + selectedLocation.z();

						if (profile.adding && profile.activeCategory != null) {
							
							LootPlace lootPlace = new LootPlace(plugin, selectedLocation, profile.activeCategory, profile.currentMin, profile.currentMax);
							if (plugin.getLootPlaces().add(loot_coordinates, lootPlace, player.getWorld()))
								player.sendMessage(ChatColor.AQUA + "Loot point added " + lootPlace.toString());
							else
								player.sendMessage(ChatColor.RED + "This loot point already exists !");
						} else if (!profile.adding) {
							if (!plugin.getLootPlaces().removePlace(loot_coordinates, player.getWorld())) {
								player.sendMessage(ChatColor.RED + "There is no loot here !");
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
		}

	}
}
