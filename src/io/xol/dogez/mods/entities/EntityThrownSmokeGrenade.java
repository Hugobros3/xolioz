package io.xol.dogez.mods.entities;

import io.xol.chunkstories.api.rendering.RenderingInterface;
import io.xol.chunkstories.api.rendering.entity.EntityRenderable;
import io.xol.chunkstories.api.rendering.entity.EntityRenderer;
import io.xol.chunkstories.api.rendering.entity.RenderingIterator;
import io.xol.chunkstories.api.voxel.Voxel;
import io.xol.chunkstories.api.voxel.VoxelFormat;
import io.xol.chunkstories.api.world.World;
import io.xol.chunkstories.api.world.WorldAuthority;
import io.xol.chunkstories.core.entity.EntityHumanoid.EntityHumanoidStance;
import io.xol.chunkstories.entity.EntityImplementation;
import io.xol.chunkstories.physics.CollisionBox;
import io.xol.chunkstories.voxel.VoxelsStore;
import io.xol.engine.graphics.geometry.TextMeshObject;
import io.xol.engine.graphics.textures.Texture2D;
import io.xol.engine.graphics.textures.TexturesHandler;
import io.xol.engine.math.Math2;
import io.xol.engine.math.lalgb.Matrix4f;
import io.xol.engine.math.lalgb.vector.dp.Vector2dm;
import io.xol.engine.math.lalgb.vector.dp.Vector3dm;
import io.xol.engine.math.lalgb.vector.sp.Vector3fm;
import io.xol.engine.model.ModelLibrary;

//(c) 2015-2017 XolioWare Interactive
//http://chunkstories.xyz
//http://xol.io

public class EntityThrownSmokeGrenade extends EntityImplementation implements EntityRenderable {

	int ignitionTimer = 60 * 4; // 4 seconds to ignite
	int deathTimer = 60 * 50; // Lives 50 seconds

	public EntityThrownSmokeGrenade(World world, double x, double y, double z) {
		super(world, x, y, z);
	}

	@Override
	public EntityRenderer<? extends EntityRenderable> getEntityRenderer() {
		return new ThrownSmokeGrenadeModelRenderer();
	}

	float tilt = 0f;
	float direction = 0f;
	float rotation = 0f;

	static class ThrownSmokeGrenadeModelRenderer implements EntityRenderer<EntityThrownSmokeGrenade> {

		@Override
		public void setupRender(RenderingInterface renderingContext) {
			renderingContext.setObjectMatrix(null);

			Texture2D diffuse = TexturesHandler.getTexture("./entities/smoke_grenade/smoke_grenade_albedo.png");
			diffuse.setLinearFiltering(false);
			renderingContext.bindAlbedoTexture(diffuse);
			renderingContext.bindNormalTexture(TexturesHandler.getTexture("./textures/normalnormal.png"));
			renderingContext.bindMaterialTexture(TexturesHandler.getTexture("./textures/defaultmaterial.png"));
		}

		@Override
		public int forEach(RenderingInterface renderingContext,
				RenderingIterator<EntityThrownSmokeGrenade> renderableEntitiesIterator) {
			int e = 0;

			renderingContext.setObjectMatrix(null);

			for (EntityThrownSmokeGrenade grenade : renderableEntitiesIterator.getElementsInFrustrumOnly()) {
				if (renderingContext.getCamera().getCameraPosition().distanceTo(grenade.getLocation()) > 32)
					continue;

				e++;

				// System.out.println("rendering thown grenade etc");

				/*
				 * Texture2D diffuse =
				 * TexturesHandler.getTexture("./models/sign.png");
				 * diffuse.setLinearFiltering(false);
				 * renderingContext.bindAlbedoTexture(diffuse);
				 * renderingContext.bindNormalTexture(TexturesHandler.getTexture
				 * ("./textures/normalnormal.png"));
				 */

				renderingContext.currentShader().setUniform3f("objectPosition", new Vector3fm(0));

				int modelBlockData = grenade.getWorld().getVoxelData(grenade.getLocation());

				int lightSky = VoxelFormat.sunlight(modelBlockData);
				int lightBlock = VoxelFormat.blocklight(modelBlockData);
				renderingContext.currentShader().setUniform3f("givenLightmapCoords", lightBlock / 15f, lightSky / 15f,
						0f);

				Matrix4f mutrix = new Matrix4f();

				mutrix.translate(new Vector3fm(0.0f, 0.15f, 0.0f));
				mutrix.translate(grenade.getLocation().castToSinglePrecision());

				mutrix.rotate(grenade.direction, new Vector3fm(0, 1, 0));
				mutrix.rotate(grenade.rotation, new Vector3fm(0, 0, 1));

				renderingContext.setObjectMatrix(mutrix);

				ModelLibrary.getRenderableMesh("./entities/smoke_grenade/smoke_grenade.obj").render(renderingContext);
			}

			return e;
		}

		@Override
		public void freeRessources() {

		}

	}

	@Override
	public void tick(WorldAuthority authority) {

		Vector3dm velocity = getVelocityComponent().getVelocity();

		if (authority.isMaster()) {
			Voxel voxelIn = VoxelsStore.get()
					.getVoxelById(VoxelFormat.id(world.getVoxelData(positionComponent.getLocation())));
			boolean inWater = voxelIn.isVoxelLiquid();

			double terminalVelocity = inWater ? -0.05 : -1.5;
			if (velocity.getY() > terminalVelocity)
				velocity.setY(velocity.getY() - 0.016);
			if (velocity.getY() < terminalVelocity)
				velocity.setY(terminalVelocity);

			Vector3dm remainingToMove = moveWithCollisionRestrain(velocity.getX(), velocity.getY(), velocity.getZ());
			if (remainingToMove.getY() < -0.02 && this.isOnGround()) {
				if (remainingToMove.getY() < -0.02) {
					//Bounce
					double originalDownardsVelocity = velocity.getY();
					velocity.scale(0.65);
					velocity.setY(-originalDownardsVelocity * 0.65);
					
					world.getSoundManager().playSoundEffect("./sounds/dogez/weapon/grenades/grenade_bounce.ogg", getLocation(), 1, 1, 10, 15);
				} else
					velocity.scale(0d);
			}

			if (velocity.length() < 0.02)
				velocity.scale(0d);

			getVelocityComponent().setVelocity(velocity);
		}

		if (authority.isClient()) {
			rotation += getVelocityComponent().getVelocity().length() * Math.random();
			tilt = (float) Math2.mix(0.0, tilt + getVelocityComponent().getVelocity().length() * Math.random(),
					Math2.clampd(velocity.length(), 0.0, 0.1) * 10.0);

			Vector2dm direction2d = new Vector2dm(velocity.getX(), velocity.getZ());

			if (direction2d.length() > 0.0) {
				direction2d.normalize();
				
				Math.acos(direction2d.getX());
				Math.asin(direction2d.getY());
				double directionDegrees;
				
				//Y is sin by convention, if > 0 top part of the circle
				if(direction2d.getY() >= 0.0)
				{
					directionDegrees = -Math.acos(direction2d.getX());
				}
				else
					directionDegrees = 0 + Math.acos(direction2d.getX());
				
				direction = (float)directionDegrees;
				//direction = (float) -Math.toRadians(directionDegrees);
			}
		}

		if (ignitionTimer > 0)
			ignitionTimer--;
		else if(ignitionTimer == 0)
		{
			world.getSoundManager().playSoundEffect("./sounds/dogez/weapon/grenades/smoke_puff.ogg", getLocation(), 1, 1, 15, 25);
			ignitionTimer--;
		}
		else if (deathTimer > 0) {
			deathTimer--;
			if (authority.isClient()) {
				world.getParticlesManager().spawnParticleAtPositionWithVelocity("smoke", this.getLocation(),
						new Vector3dm(Math.random() * 2.0 - 1.0, Math.random() * 2.0 - 0.5, Math.random() * 2.0 - 1.0)
								.normalize().scale(Math.random() * 0.05 + 0.02));
			}
		} else if (authority.isMaster()) {
			world.removeEntity(this);
		}
	}

	public CollisionBox[] getCollisionBoxes() {
		return new CollisionBox[] { new CollisionBox(0.25, 0.25, 0.25).translate(-0.125, 0.0, -0.125) };
	}
}
