package io.xol.dogez.mods.voxel;

import org.joml.Matrix4f;
import org.joml.Vector3d;
import org.joml.Vector3f;

import io.xol.chunkstories.api.Location;
import io.xol.chunkstories.api.entity.EntityBase;
import io.xol.chunkstories.api.entity.EntityType;
import io.xol.chunkstories.api.entity.EntityVoxel;
import io.xol.chunkstories.api.math.Math2;
import io.xol.chunkstories.api.physics.CollisionBox;
import io.xol.chunkstories.api.player.PlayerClient;
import io.xol.chunkstories.api.rendering.RenderingInterface;
import io.xol.chunkstories.api.rendering.entity.EntityRenderable;
import io.xol.chunkstories.api.rendering.entity.EntityRenderer;
import io.xol.chunkstories.api.rendering.entity.RenderingIterator;
import io.xol.chunkstories.api.rendering.lightning.Light;
import io.xol.chunkstories.api.sound.SoundSource;
import io.xol.chunkstories.api.sound.SoundSource.Mode;
import io.xol.chunkstories.api.world.World;
import io.xol.chunkstories.api.world.WorldClient;

public class StaticVehicleEntity extends EntityBase implements EntityVoxel, EntityRenderable {

	final float rotate;
	final Vector3f translate;
	final boolean isBurning;
	final boolean darkSmoke;
	final String model;
	final String diffuseTexture;
	
	final Vector3d size;
	
	final Vector3d burnZoneStart;
	final Vector3d burnZoneSize;
	
	SoundSource soundLoop = null;
	
	public StaticVehicleEntity(EntityType type, World world, double x, double y, double z) {
		super(type, world, x, y, z);
		
		this.rotate = Float.parseFloat(type.resolveProperty("rotate", "0"));
		String ts = type.resolveProperty("translate", "0 0 0");
		String[] tss = ts.split(" ");
		
		this.translate = new Vector3f(Float.parseFloat(tss[0]), Float.parseFloat(tss[1]), Float.parseFloat(tss[2]));
		this.isBurning = type.resolveProperty("isBurning", "false").equals("true");
		this.darkSmoke = type.resolveProperty("darkSmoke", "false").equals("true");
		this.model = type.resolveProperty("model", "error");
		this.diffuseTexture = type.resolveProperty("diffuseTexture", "error");

		ts = type.resolveProperty("size", "1 1 1");
		tss = ts.split(" ");
		this.size = new Vector3d(Float.parseFloat(tss[0]), Float.parseFloat(tss[1]), Float.parseFloat(tss[2]));
		
		ts = type.resolveProperty("burnZoneStart", "1 1 1");
		tss = ts.split(" ");
		this.burnZoneStart = new Vector3d(Float.parseFloat(tss[0]), Float.parseFloat(tss[1]), Float.parseFloat(tss[2]));
		
		ts = type.resolveProperty("burnZoneSize", "1 1 1");
		tss = ts.split(" ");
		this.burnZoneSize = new Vector3d(Float.parseFloat(tss[0]), Float.parseFloat(tss[1]), Float.parseFloat(tss[2]));
		
	}

	@Override
	public CollisionBox getBoundingBox() {
		return new CollisionBox(0,0,0,size.x,size.y,size.z);
	}

	@Override
	public void tick() {
		super.tick();
		
		if(world instanceof WorldClient && isBurning) {
			Vector3d vel = new Vector3d(Math.random() * 2.0 - 1.0, Math.random() * 4.0 - 1.0, Math.random() * 2.0 - 1.0);
			vel.mul(0.2);
		
			world.getParticlesManager().spawnParticleAtPositionWithVelocity("fire_small", this.getLocation().add(burnZoneStart.x + Math.random() * burnZoneSize.x, burnZoneStart.y + Math.random() * burnZoneSize.y, burnZoneStart.z + Math.random() * burnZoneSize.z), vel);
			
			//Sound magic is here
			PlayerClient player = ((WorldClient)world).getClient().getPlayer();
			Location playerLocation = player.getLocation();
			if(playerLocation != null) {
				
				Location loc = this.getLocation();
				loc.add(burnZoneStart).add(burnZoneSize.x / 2f, burnZoneSize.y / 2f, burnZoneSize.z / 2f);
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
		
		if(world instanceof WorldClient && darkSmoke) {
			
			for(int i = 0; i< 3; i++) {
			Vector3d vel = new Vector3d(Math.random() * 2.0 - 1.0, 2 + Math.random() * 2.0 + Math.random() * Math2.clamp(Math.random() * 1.5f - 1f, 0, 1) * Math.random() * 50, Math.random() * 2.0 - 1.0);
			vel.mul(0.02);
			world.getParticlesManager().spawnParticleAtPositionWithVelocity("black_smoke", this.getLocation().add(burnZoneStart.x + Math.random() * burnZoneSize.x, burnZoneStart.y + Math.random() * 2 + burnZoneSize.y, burnZoneStart.z + Math.random() * burnZoneSize.z), vel);
			}
		}
	}

	@Override
	public EntityRenderer<? extends EntityRenderable> getEntityRenderer() {
		return new EntityRenderer<StaticVehicleEntity>() {

			@Override
			public int renderEntities(RenderingInterface renderingInterface,
					RenderingIterator<StaticVehicleEntity> renderableEntitiesIterator) {
				
				//System.out.println(this.renderInPass(RenderingPass.ALPHA_BLENDED));
				//renderingInterface.useShader("entities");

				renderingInterface.bindNormalTexture(renderingInterface.textures().getTexture("./textures/normalnormal.png"));
				renderingInterface.bindMaterialTexture(renderingInterface.textures().getTexture("./textures/defaultmaterial.png"));
				
				for(StaticVehicleEntity sve : renderableEntitiesIterator.getElementsInFrustrumOnly()) {
				
						Matrix4f matrix = new Matrix4f();
						
						Vector3d loc = sve.getLocation().add(0.0, 0.0, 0.0);
						matrix.translate((float)loc.x, (float)loc.y, (float)loc.z);
						//matrix.scale(0.5f);
						matrix.rotate((float)Math.PI / 2f * rotate / 90f, 0, 1, 0);
						matrix.translate(translate);
						
						renderingInterface.setObjectMatrix(matrix);
						
						renderingInterface.textures().getTexture(diffuseTexture).setMipMapping(false);
						renderingInterface.bindAlbedoTexture(renderingInterface.textures().getTexture(diffuseTexture));
						renderingInterface.meshes().getRenderableMeshByName(model).render(renderingInterface);
						
						//renderingInterface.getLightsRenderer().queueLight(new Light(new Vector3f(1.0f, 1.0f, 0.5f), new Vector3f((float)loc.x + 2.5f, (float)loc.y + 2f, (float)loc.z + 1), 15f));
						//world.getParticlesManager().spawnParticleAtPosition("fire_light", loc);
					}
				
				if(isBurning)
				for(StaticVehicleEntity sve : renderableEntitiesIterator) {
						Vector3d loc = sve.getLocation().add(0.0, 0.0, 0.0);
						
						renderingInterface.getLightsRenderer().queueLight(new Light(new Vector3f(1.0f, 1.0f, 0.5f), new Vector3f((float)(loc.x + burnZoneStart.x + burnZoneSize.x /2f), (float)(loc.y + burnZoneStart.y + burnZoneSize.y /2f), (float)(loc.z + burnZoneStart.z + burnZoneSize.z /2f)), 15f));
						//world.getParticlesManager().spawnParticleAtPosition("fire_light", loc);
					}
			
				//
				
				return 0;
			}

			@Override
			public void freeRessources() {
				// TODO Auto-generated method stub
				
			}
			
		};
	}

	
}
