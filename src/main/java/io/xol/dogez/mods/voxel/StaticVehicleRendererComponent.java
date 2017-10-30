package io.xol.dogez.mods.voxel;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.joml.Matrix4f;
import org.joml.Vector3d;
import org.joml.Vector3f;

import io.xol.chunkstories.api.Location;
import io.xol.chunkstories.api.math.Math2;
import io.xol.chunkstories.api.player.LocalPlayer;
import io.xol.chunkstories.api.rendering.RenderingInterface;
import io.xol.chunkstories.api.rendering.lightning.Light;
import io.xol.chunkstories.api.serialization.StreamSource;
import io.xol.chunkstories.api.serialization.StreamTarget;
import io.xol.chunkstories.api.sound.SoundSource;
import io.xol.chunkstories.api.sound.SoundSource.Mode;
import io.xol.chunkstories.api.util.IterableIterator;
import io.xol.chunkstories.api.voxel.components.VoxelComponent;
import io.xol.chunkstories.api.voxel.components.VoxelComponentDynamicRenderer;
import io.xol.chunkstories.api.voxel.components.VoxelComponents;
import io.xol.chunkstories.api.world.World;
import io.xol.chunkstories.api.world.WorldClient;
import io.xol.chunkstories.api.world.chunk.Chunk.ChunkVoxelContext;

public class StaticVehicleRendererComponent extends VoxelComponentDynamicRenderer {

	final StaticVehicleVoxel vehicleType; // The type of carcass we're representing
	
	float fuelRemaining;
	int secondsOnFire;
	
	public StaticVehicleRendererComponent(StaticVehicleVoxel vehicleType, VoxelComponents holder, float initialFuel) {
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
		
		if(world instanceof WorldClient && vehicleType.isBurning) {
			Vector3d vel = new Vector3d(Math.random() * 2.0 - 1.0, Math.random() * 4.0 - 1.0, Math.random() * 2.0 - 1.0);
			vel.mul(0.02);
			Location loc = new Location(location);
			world.getParticlesManager().spawnParticleAtPositionWithVelocity("fire_small", loc.add(vehicleType.burnZoneStart.x + Math.random() * vehicleType.burnZoneSize.x, vehicleType.burnZoneStart.y + Math.random() * vehicleType.burnZoneSize.y, vehicleType.burnZoneStart.z + Math.random() * vehicleType.burnZoneSize.z), vel);
			
			//Sound magic is here
			LocalPlayer player = ((WorldClient)world).getClient().getPlayer();
			Location playerLocation = player.getLocation();
			if(playerLocation != null) {
				
				loc = new Location(location);
				loc.add(vehicleType.burnZoneStart).add(vehicleType.burnZoneSize.x / 2f, vehicleType.burnZoneSize.y / 2f, vehicleType.burnZoneSize.z / 2f);
				double distance = playerLocation.distance(loc);
				if(distance <= 15.0) {
					if(soundLoop == null || soundLoop.isDonePlaying()) {
						soundLoop = world.getSoundManager().playSoundEffect("./sounds/ambient/blaze_loop.ogg", Mode.STREAMED, loc, 1, 1, 5f, 15f);
					}
				}
				else {
					if(soundLoop != null) {
						soundLoop.stop();
						soundLoop = null;
					}
				}
			}
		}
		
		if(world instanceof WorldClient && vehicleType.darkSmoke) {

			Location loc = new Location(location);
			loc.add(vehicleType.burnZoneStart.x + Math.random() * vehicleType.burnZoneSize.x,
					vehicleType.burnZoneStart.y + Math.random() * 2 + vehicleType.burnZoneSize.y,
					vehicleType.burnZoneStart.z + Math.random() * vehicleType.burnZoneSize.z);
			//System.out.println("loc:"+loc);
			
			for(int i = 0; i< 3; i++) {
			Vector3d vel = new Vector3d(Math.random() * 2.0 - 1.0, 2 + Math.random() * 2.0 + Math.random() * Math2.clamp(Math.random() * 1.5f - 1f, 0, 1) * Math.random() * 50, Math.random() * 2.0 - 1.0);
			vel.mul(0.02);
				world.getParticlesManager().spawnParticleAtPositionWithVelocity("black_smoke", loc, vel);
			}
		}
	}

	static class StaticVehicleRenderer implements VoxelDynamicRenderer {
		
		final StaticVehicleVoxel vehicleType;
		
		public StaticVehicleRenderer(StaticVehicleVoxel vehicleType) {
			super();
			this.vehicleType = vehicleType;
		}


		@Override
		public void renderVoxels(RenderingInterface renderingInterface, IterableIterator<ChunkVoxelContext> voxelsOfThisType) {
			renderingInterface.bindNormalTexture(renderingInterface.textures().getTexture("./textures/normalnormal.png"));
			renderingInterface.bindMaterialTexture(renderingInterface.textures().getTexture("./textures/defaultmaterial.png"));
			
			for(ChunkVoxelContext context : voxelsOfThisType) {
			
					if(context.getMetaData() != 0)
						continue;
				
					VoxelComponent component = context.components().get("renderer");
					if(component != null)
						((StaticVehicleRendererComponent)component).tick();
					
					Matrix4f matrix = new Matrix4f();
					
					Vector3d loc = context.getLocation().add(0.0, 0.0, 0.0);
					matrix.translate((float)loc.x, (float)loc.y, (float)loc.z);
					//matrix.scale(0.5f);
					matrix.rotate((float)Math.PI / 2f * vehicleType.rotate / 90f, 0, 1, 0);
					matrix.translate(vehicleType.translate);
					
					renderingInterface.setObjectMatrix(matrix);
					
					renderingInterface.textures().getTexture(vehicleType.diffuseTexture).setMipMapping(false);
					renderingInterface.bindAlbedoTexture(renderingInterface.textures().getTexture(vehicleType.diffuseTexture));
					renderingInterface.meshes().getRenderableMeshByName(vehicleType.model).render(renderingInterface);
					
					if(vehicleType.isBurning)
						renderingInterface.getLightsRenderer().queueLight(new Light(new Vector3f(1.0f, 1.0f, 0.5f), new Vector3f((float)(loc.x + vehicleType.burnZoneStart.x + vehicleType.burnZoneSize.x /2f), (float)(loc.y + vehicleType.burnZoneStart.y + vehicleType.burnZoneSize.y /2f), (float)(loc.z + vehicleType.burnZoneStart.z + vehicleType.burnZoneSize.z /2f)), 15f));
				}
		}
	
	}

	@Override
	public VoxelDynamicRenderer getVoxelDynamicRenderer() {
		return new StaticVehicleRenderer(vehicleType);
	}
}
