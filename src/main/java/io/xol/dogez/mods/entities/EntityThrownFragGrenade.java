package io.xol.dogez.mods.entities;

import org.joml.Matrix4f;
import org.joml.Vector3d;
import org.joml.Vector3f;

import io.xol.chunkstories.api.Location;
import io.xol.chunkstories.api.entity.DamageCause;
import io.xol.chunkstories.api.entity.Entity;
import io.xol.chunkstories.api.entity.EntityLiving;
import io.xol.chunkstories.api.entity.EntityType;
import io.xol.chunkstories.api.rendering.RenderingInterface;
import io.xol.chunkstories.api.rendering.entity.EntityRenderable;
import io.xol.chunkstories.api.rendering.entity.EntityRenderer;
import io.xol.chunkstories.api.rendering.entity.RenderingIterator;
import io.xol.chunkstories.api.rendering.textures.Texture2D;
import io.xol.chunkstories.api.voxel.VoxelFormat;
import io.xol.chunkstories.api.world.WorldMaster;
import io.xol.chunkstories.core.util.WorldEffects;

//(c) 2015-2017 XolioWare Interactive
//http://chunkstories.xyz
//http://xol.io

public class EntityThrownFragGrenade extends EntityThrownGrenade implements EntityRenderable, DamageCause {

	int ignitionTimer = 60 * 5; // 4 seconds to ignite
	int deathTimer = 0 * 50; // Lives 50 seconds

	public EntityThrownFragGrenade(EntityType type, Location loc) {
		super(type, loc);
	}

	@Override
	public EntityRenderer<? extends EntityRenderable> getEntityRenderer() {
		return new ThrownFragGrenadeModelRenderer();
	}

	static class ThrownFragGrenadeModelRenderer implements EntityRenderer<EntityThrownFragGrenade> {

		public void setupRender(RenderingInterface renderingContext) {
			renderingContext.setObjectMatrix(null);

			Texture2D diffuse = renderingContext.textures().getTexture("./models/weapon/frag_grenade/frag_grenade_albedo.png");
			diffuse.setLinearFiltering(false);
			renderingContext.bindAlbedoTexture(diffuse);
			renderingContext.bindNormalTexture(renderingContext.textures().getTexture("./textures/normalnormal.png"));
			renderingContext.bindMaterialTexture(renderingContext.textures().getTexture("./textures/defaultmaterial.png"));
		}

		@Override
		public int renderEntities(RenderingInterface renderingContext,
				RenderingIterator<EntityThrownFragGrenade> renderableEntitiesIterator) {
			setupRender(renderingContext);
			int e = 0;

			renderingContext.setObjectMatrix(null);

			for (EntityThrownFragGrenade grenade : renderableEntitiesIterator.getElementsInFrustrumOnly()) {
				if (renderingContext.getCamera().getCameraPosition().distance(grenade.getLocation()) > 32)
					continue;

				e++;

				renderingContext.currentShader().setUniform3f("objectPosition", new Vector3f(0));

				int modelBlockData = grenade.getWorld().peekSafely(grenade.getLocation()).getData();

				int lightSky = VoxelFormat.sunlight(modelBlockData);
				int lightBlock = VoxelFormat.blocklight(modelBlockData);
				renderingContext.currentShader().setUniform3f("givenLightmapCoords", lightBlock / 15f, lightSky / 15f,
						0f);

				Matrix4f mutrix = new Matrix4f();

				mutrix.translate(new Vector3f(0.0f, 0.15f, 0.0f));
				mutrix.translate((float)grenade.getLocation().x(), (float)grenade.getLocation().y(), (float)grenade.getLocation().z());

				mutrix.rotate(grenade.direction, new Vector3f(0, 1, 0));
				mutrix.rotate(grenade.rotation, new Vector3f(0, 0, 1));

				renderingContext.setObjectMatrix(mutrix);

				renderingContext.meshes().getRenderableMeshByName("./models/weapon/frag_grenade/frag_grenade.obj").render(renderingContext);
			}

			return e;
		}

		@Override
		public void freeRessources() {

		}

	}

	@Override
	public void tick() {

		//Movement and stuff
		super.tick();

		if (ignitionTimer > 0)
			ignitionTimer--;
		else if(ignitionTimer == 0)
		{
			if (world instanceof WorldMaster)
			{
				WorldEffects.createFireball(world, getLocation().add(0.0, 0.5, 0.0), 5f, 0.1f, 2);
				double dmg_radius = 10;
				double dmg_radius_maxdmg = 5;
				
				double maxDmg = 200;
				for(Entity e : world.getEntitiesInBox(getLocation(), new Vector3d(dmg_radius)))
				{
					if(e instanceof EntityLiving) {
						EntityLiving el = (EntityLiving)e;
						
						double distance = el.getLocation().distance(getLocation());
						if(distance > dmg_radius)
							continue;
						
						float dmg = (float) (maxDmg * Math.min(Math.max(0, dmg_radius - distance), dmg_radius_maxdmg) / dmg_radius_maxdmg);
						
						System.out.println("Damaging "+el.getName() + " with "+dmg);
						el.damage(this, dmg);
					}
				}
			}
			//world.getSoundManager().playSoundEffect("./sounds/dogez/weapon/grenades/smoke_puff.ogg", getLocation(), 1, 1, 15, 25);
			ignitionTimer--;
		}
		else if (deathTimer > 0) {
			deathTimer--;
			/*if (authority.isClient()) {
				world.getParticlesManager().spawnParticleAtPositionWithVelocity("smoke", this.getLocation(),
						new Vector3dm(Math.random() * 2.0 - 1.0, Math.random() * 2.0 - 0.5, Math.random() * 2.0 - 1.0)
								.normalize().scale(Math.random() * 0.05 + 0.02));
			}*/
		} else if (world instanceof WorldMaster) {
			world.removeEntity(this);
		}
	}

	@Override
	public String getName() {
		return "Frag grenade";
	}
}
