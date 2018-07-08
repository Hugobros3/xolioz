package io.xol.dogez.plugin.zombies;

import io.xol.chunkstories.api.Location;
import io.xol.chunkstories.api.entity.Entity;
import io.xol.chunkstories.api.entity.components.EntityCreativeMode;
import io.xol.chunkstories.api.entity.components.EntityHealth;
import io.xol.chunkstories.api.player.Player;
import io.xol.chunkstories.api.voxel.Voxel;
import io.xol.chunkstories.api.world.WorldMaster;
import io.xol.chunkstories.api.world.chunk.Chunk;
import io.xol.chunkstories.core.entity.EntityZombie;
import io.xol.dogez.plugin.XolioZGamemodePlugin;

public class ZombiesPopulation {

	XolioZGamemodePlugin plugin;
	public int zombiesCount = 0;

	public ZombiesPopulation(XolioZGamemodePlugin p) {
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

	public void unspawnOldZombies() {

	}

	public void spawnZombies() {
		int minimalDistance = plugin.config.getInt("zombiesSpawnMinDistance", 30);
		int maximalDistance = plugin.config.getInt("zombiesSpawnMaxDistance", 60);

		for (Player player : plugin.getGameWorld().getPlayers()) {
			Entity playerEntity = player.getControlledEntity();

			// Ignore unspawned players
			if (playerEntity == null)
				continue;

			// Don't spawn zombies on players in creative mode
			if (!playerEntity.components.tryWithBoolean(EntityCreativeMode.class, ecm -> ecm.get())) {
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
					if (foundGround && zombiesCount <= plugin.config.getInt("maxZombiesOnMap", 420)) {
						zombiesCount++;
						spawnZombie(new Location(plugin.getGameWorld(), posx + 0.5, posy + 1, posz + 0.5));
					}
				}
			}
		}
	}

	public void spawnZombie(Location location) {
		WorldMaster world = plugin.getGameWorld();

		EntityZombie zombie = new EntityZombie(plugin.getPluginExecutionContext().getContent().entities().getEntityDefinition("zombie"), location);
		zombie.components.with(EntityHealth.class, eh -> {
			eh.setHealth((float) (eh.getHealth() * (0.5 + Math.random())));
		});

		world.addEntity(zombie);
	}

	public void cleanChunk(Chunk c) {
		for (Entity entity : c.getEntitiesWithinChunk()) {
			if (entity instanceof EntityZombie) {
				entity.getWorld().removeEntity(entity);
			}
		}
	}
}
