package io.xol.dogez.mods.entities;

import io.xol.chunkstories.api.math.Matrix4f;
import io.xol.chunkstories.api.math.vector.sp.Vector3fm;
import io.xol.chunkstories.api.rendering.RenderingInterface;
import io.xol.chunkstories.api.rendering.entity.EntityRenderable;
import io.xol.chunkstories.api.rendering.entity.EntityRenderer;
import io.xol.chunkstories.api.rendering.entity.RenderingIterator;
import io.xol.chunkstories.api.voxel.VoxelFormat;
import io.xol.chunkstories.api.world.World;
import io.xol.chunkstories.api.world.WorldAuthority;
import io.xol.engine.graphics.textures.Texture2D;
import io.xol.engine.graphics.textures.TexturesHandler;
import io.xol.engine.model.ModelLibrary;

//(c) 2015-2017 XolioWare Interactive
//http://chunkstories.xyz
//http://xol.io

public class EntityThrownFragGrenade extends EntityThrownGrenade implements EntityRenderable {

	int ignitionTimer = 60 * 4; // 4 seconds to ignite
	int deathTimer = 60 * 50; // Lives 50 seconds

	public EntityThrownFragGrenade(World world, double x, double y, double z) {
		super(world, x, y, z);
	}

	@Override
	public EntityRenderer<? extends EntityRenderable> getEntityRenderer() {
		return new ThrownFragGrenadeModelRenderer();
	}

	static class ThrownFragGrenadeModelRenderer implements EntityRenderer<EntityThrownFragGrenade> {

		public void setupRender(RenderingInterface renderingContext) {
			renderingContext.setObjectMatrix(null);

			Texture2D diffuse = TexturesHandler.getTexture("./models/weapon/frag_grenade/frag_grenade_albedo.png");
			diffuse.setLinearFiltering(false);
			renderingContext.bindAlbedoTexture(diffuse);
			renderingContext.bindNormalTexture(TexturesHandler.getTexture("./textures/normalnormal.png"));
			renderingContext.bindMaterialTexture(TexturesHandler.getTexture("./textures/defaultmaterial.png"));
		}

		@Override
		public int renderEntities(RenderingInterface renderingContext,
				RenderingIterator<EntityThrownFragGrenade> renderableEntitiesIterator) {
			setupRender(renderingContext);
			int e = 0;

			renderingContext.setObjectMatrix(null);

			for (EntityThrownFragGrenade grenade : renderableEntitiesIterator.getElementsInFrustrumOnly()) {
				if (renderingContext.getCamera().getCameraPosition().distanceTo(grenade.getLocation()) > 32)
					continue;

				e++;

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

				ModelLibrary.getRenderableMesh("./models/weapon/frag_grenade/frag_grenade.obj").render(renderingContext);
			}

			return e;
		}

		@Override
		public void freeRessources() {

		}

	}

	@Override
	public void tick(WorldAuthority authority) {

		//Movement and stuff
		super.tick(authority);

		if (ignitionTimer > 0)
			ignitionTimer--;
		else if(ignitionTimer == 0)
		{
			world.getSoundManager().playSoundEffect("./sounds/dogez/weapon/grenades/smoke_puff.ogg", getLocation(), 1, 1, 15, 25);
			ignitionTimer--;
		}
		else if (deathTimer > 0) {
			deathTimer--;
			/*if (authority.isClient()) {
				world.getParticlesManager().spawnParticleAtPositionWithVelocity("smoke", this.getLocation(),
						new Vector3dm(Math.random() * 2.0 - 1.0, Math.random() * 2.0 - 0.5, Math.random() * 2.0 - 1.0)
								.normalize().scale(Math.random() * 0.05 + 0.02));
			}*/
		} else if (authority.isMaster()) {
			world.removeEntity(this);
		}
	}
}
