//
// This file is a part of the XolioZ Mod for Chunk Stories
// Check out README.md for more information
// Website: https://chunkstories.xyz
// Github: https://github.com/Hugobros3/xolioz
//

package io.xol.z.plugin.zombies;

import io.xol.chunkstories.api.Location;
import io.xol.chunkstories.api.entity.Entity;
import io.xol.chunkstories.api.entity.traits.serializable.TraitCreativeMode;
import io.xol.chunkstories.api.entity.traits.serializable.TraitHealth;
import io.xol.chunkstories.api.player.Player;
import io.xol.chunkstories.api.voxel.Voxel;
import io.xol.chunkstories.api.world.WorldMaster;
import io.xol.chunkstories.core.entity.EntityZombie;
import io.xol.z.plugin.XolioZPlugin;

public class ZombiesPopulation {

	XolioZPlugin plugin;
	public int zombiesCount = 0;

	public ZombiesPopulation(XolioZPlugin p) {
		plugin = p;
	}

	public void countZombies() {
		zombiesCount = 0;
		for (Entity e : plugin.getGameWorld().getAllLoadedEntities()) {
			if (e instanceof EntityZombie) {
				zombiesCount++;
			}
		}
	}

	public void spawnZombies() {
		int minimalDistance = plugin.config.getZombiesSpawnMinDistance();
		int maximalDistance = plugin.config.getZombiesSpawnMaxDistance();

		for (Player player : plugin.getGameWorld().getPlayers()) {
			Entity playerEntity = player.getControlledEntity();

			// Ignore unspawned players
			if (playerEntity == null)
				continue;

			// Don't spawn zombies on players in creative mode
			if (!playerEntity.traits.tryWithBoolean(TraitCreativeMode.class, ecm -> ecm.get())) {
				int pZombCount = 0;
				for (Entity e : plugin.getGameWorld().getAllLoadedEntities()) {
					if (e instanceof EntityZombie && e.getLocation().distance(player.getLocation()) < 120f) {
						pZombCount++;
					}
				}
				for (int i = pZombCount; i < 16; i++) {

					int distance = (int) (minimalDistance + Math.random() * (maximalDistance - minimalDistance));
					double angle = Math.random() * 3.14f * 2;
					int posx = (int) (player.getLocation().x() + distance * Math.sin(angle));
					int posz = (int) (player.getLocation().z() + distance * Math.cos(angle));
					boolean foundGround = false;
					int posy = 255;

					String[] allowedMaterials = { "grass", "stone", "dirt", "sand", "wood" };

					while (posy > 0 && !foundGround) {
						posy--;

						Voxel v = plugin.getGameWorld().peekSafely(posx, posy, posz).getVoxel();

						if (v.getDefinition().isLiquid())
							break;

						for (String m : allowedMaterials) {
							if (v.getMaterial().getName().equals(m)) {
								foundGround = true;
								break;
							}
						}
						if (v.getDefinition().isSolid())
							break;
					}
					if (foundGround && zombiesCount <= plugin.config.getMaxZombiesOnMap()) {
						zombiesCount++;
						spawnZombie(new Location(plugin.getGameWorld(), posx + 0.5, posy + 1, posz + 0.5));
					}
				}
			}
		}
	}

	public void spawnZombie(Location location) {
		WorldMaster world = plugin.getGameWorld();

		// EntityZombie zombie = new
		// EntityZombie(plugin.getPluginExecutionContext().getContent().entities().getEntityDefinition("zombie"),
		// location);
		EntityZombie zombie = (EntityZombie) plugin.getPluginExecutionContext().getContent().entities().getEntityDefinition("zombie").create(location);
		zombie.traits.with(TraitHealth.class, eh -> {
			eh.setHealth((float) (eh.getHealth() * (0.5 + Math.random())));
		});

		world.addEntity(zombie);
	}
}
