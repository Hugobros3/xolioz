package io.xol.z.plugin.loot.crashes;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import io.xol.chunkstories.api.Location;
import io.xol.chunkstories.api.entity.Entity;
import io.xol.chunkstories.api.entity.traits.TraitVoxelSelection;
import io.xol.chunkstories.api.events.EventHandler;
import io.xol.chunkstories.api.events.Listener;
import io.xol.chunkstories.api.events.player.PlayerInputPressedEvent;
import io.xol.chunkstories.api.events.player.voxel.PlayerVoxelModificationEvent;
import io.xol.chunkstories.api.input.Input;
import io.xol.chunkstories.api.player.Player;
import io.xol.chunkstories.api.voxel.Voxel;
import io.xol.chunkstories.api.world.cell.CellData;
import io.xol.z.plugin.XolioZPlugin;
import io.xol.z.plugin.loot.LootCategory;
import io.xol.z.mod.voxels.StaticVehicleVoxel;

/** 
 * Remebers where, when and what was inside of a crash site 
 * Responsible for creating and (eventually ?) removing them
 */
public class CrashesHandler implements Listener {

	private final XolioZPlugin xolioZGamemodePlugin;
	private final Set<StaticVehicleVoxel> registeredVoxels = new HashSet<StaticVehicleVoxel>();
	
	private Map<Location, Crash> crashes = new HashMap<>();
	
	public CrashesHandler(XolioZPlugin xolioZGamemodePlugin) {
		this.xolioZGamemodePlugin = xolioZGamemodePlugin;
		
		Iterator<Voxel> i = xolioZGamemodePlugin.getPluginExecutionContext().getContent().voxels().all();
		while(i.hasNext()) {
			Voxel voxel = i.next();
			if(voxel instanceof StaticVehicleVoxel) {
				registeredVoxels.add((StaticVehicleVoxel) voxel);
			}
		}
	}
	
	@EventHandler
	public void onPlayerPoke(PlayerVoxelModificationEvent event) {
		if(!event.getContext().getVoxel().isAir()) {
			Voxel placed = event.getContext().getVoxel();
			if(registeredVoxels.contains(placed)) {
				if(event.getContext().getMetaData() == 0) {
					StaticVehicleVoxel vehicleType = (StaticVehicleVoxel)placed;
					LootCategory category = xolioZGamemodePlugin.getLootTypes().getCategory(vehicleType.lootCategoryName);
					
					if(category == null)
						return;
					
					System.out.println("Created crash object, calling spawnLoot");
					Crash crash = new Crash(xolioZGamemodePlugin, event.getContext().getLocation(), category, vehicleType.lootAmountMin, vehicleType.lootAmountMax);
					//crash.spawnLoot();
					crashes.put(event.getContext().getLocation(), crash);
				}
			}
		} else {
			Voxel removed = event.getContext().getVoxel();
			if(registeredVoxels.contains(removed)) {
				if(event.getContext().getMetaData() == 0) {
					crashes.remove(event.getContext().getLocation());
				}
			}
		}
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
		
			// loot regeneration
			Voxel voxel = player.getWorld().peekSafely(selectedLocation).getVoxel();
			if (voxel instanceof StaticVehicleVoxel) {
				StaticVehicleVoxel vehicleType = (StaticVehicleVoxel)voxel;
				System.out.println("lookin for the crash "+crashes.size());
				
				int x = context.getX();
				int y = context.getY();
				int z = context.getZ();
				
				//Backpedal to find the root block
				int meta = context.getMetaData();
				
				int ap = (meta >> vehicleType.xShift) & vehicleType.xMask;
				int bp = (meta >> vehicleType.yShift) & vehicleType.yMask;
				int cp = (meta >> vehicleType.zShift) & vehicleType.zMask;
				
				int startX = x - ap;
				int startY = y - bp;
				int startZ = z - cp;
				
				Crash crash = crashes.get(new Location(context.getWorld(), startX, startY, startZ));
				if(crash != null) {
					crash.update();
				}
			} 
		}

	}
}
