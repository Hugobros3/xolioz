//
// This file is a part of the XolioZ Mod for Chunk Stories
// Check out README.md for more information
// Website: https://chunkstories.xyz
// Github: https://github.com/Hugobros3/xolioz
//

package io.xol.z.mod.voxels;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.joml.Vector3d;

import io.xol.chunkstories.api.Location;
import io.xol.chunkstories.api.math.Math2;
import io.xol.chunkstories.api.client.LocalPlayer;
import io.xol.chunkstories.api.world.serialization.StreamSource;
import io.xol.chunkstories.api.world.serialization.StreamTarget;
import io.xol.chunkstories.api.sound.SoundSource;
import io.xol.chunkstories.api.sound.SoundSource.Mode;
import io.xol.chunkstories.api.voxel.components.VoxelComponent;
import io.xol.chunkstories.api.world.World;
import io.xol.chunkstories.api.world.WorldClient;
import io.xol.chunkstories.api.world.cell.CellComponents;

public class StaticVehicleRendererComponent extends VoxelComponent {

	final StaticVehicleVoxel vehicleType; // The type of carcass we're representing

	float fuelRemaining;
	int secondsOnFire;

	public StaticVehicleRendererComponent(StaticVehicleVoxel vehicleType, CellComponents holder, float initialFuel) {
		super(holder);

		this.vehicleType = vehicleType;
		this.fuelRemaining = initialFuel;
	}

	@Override
	public void push(StreamTarget destinator, DataOutputStream dos) throws IOException {
		dos.writeFloat(fuelRemaining);
	}

	@Override
	public void pull(StreamSource from, DataInputStream dis) throws IOException {
		fuelRemaining = dis.readFloat();
	}

	SoundSource soundLoop = null;

	public void tick() {
		World world = this.holder().getChunk().getWorld();
		Location location = new Location(world, holder().getX(), holder().getY(), holder().getZ());

		if (world instanceof WorldClient && vehicleType.isBurning) {
			Vector3d vel = new Vector3d(Math.random() * 2.0 - 1.0, Math.random() * 4.0 - 1.0, Math.random() * 2.0 - 1.0);
			vel.mul(0.02);
			Location loc = new Location(location);
			world.getParticlesManager().spawnParticleAtPositionWithVelocity("fire_small",
					loc.add(vehicleType.burnZoneStart.x + Math.random() * vehicleType.burnZoneSize.x,
							vehicleType.burnZoneStart.y + Math.random() * vehicleType.burnZoneSize.y,
							vehicleType.burnZoneStart.z + Math.random() * vehicleType.burnZoneSize.z),
					vel);

			// Sound magic is here
			LocalPlayer player = ((WorldClient) world).getClient().getPlayer();
			Location playerLocation = player.getLocation();
			if (playerLocation != null) {

				loc = new Location(location);
				loc.add(vehicleType.burnZoneStart).add(vehicleType.burnZoneSize.x / 2f, vehicleType.burnZoneSize.y / 2f, vehicleType.burnZoneSize.z / 2f);
				double distance = playerLocation.distance(loc);
				if (distance <= 15.0) {
					if (soundLoop == null || soundLoop.isDonePlaying()) {
						soundLoop = world.getSoundManager().playSoundEffect("./sounds/ambient/blaze_loop.ogg", Mode.STREAMED, loc, 1, 1, 5f, 15f);
					}
				} else {
					if (soundLoop != null) {
						soundLoop.stop();
						soundLoop = null;
					}
				}
			}
		}

		if (world instanceof WorldClient && vehicleType.darkSmoke) {

			Location loc = new Location(location);
			loc.add(vehicleType.burnZoneStart.x + Math.random() * vehicleType.burnZoneSize.x,
					vehicleType.burnZoneStart.y + Math.random() * 2 + vehicleType.burnZoneSize.y,
					vehicleType.burnZoneStart.z + Math.random() * vehicleType.burnZoneSize.z);
			// System.out.println("loc:"+loc);

			for (int i = 0; i < 3; i++) {
				Vector3d vel = new Vector3d(Math.random() * 2.0 - 1.0,
						2 + Math.random() * 2.0 + Math.random() * Math2.clamp(Math.random() * 1.5f - 1f, 0, 1) * Math.random() * 50, Math.random() * 2.0 - 1.0);
				vel.mul(0.02);
				world.getParticlesManager().spawnParticleAtPositionWithVelocity("black_smoke", loc, vel);
			}
		}
	}
}
