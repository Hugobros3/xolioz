package io.xol.dogez.mods.entities;

import org.joml.Matrix4f;
import org.joml.Vector3d;
import org.joml.Vector3f;

import io.xol.chunkstories.api.Location;
import io.xol.chunkstories.api.entity.DamageCause;
import io.xol.chunkstories.api.entity.Entity;
import io.xol.chunkstories.api.entity.EntityDefinition;
import io.xol.chunkstories.api.entity.EntityLiving;
import io.xol.chunkstories.api.rendering.RenderingInterface;
import io.xol.chunkstories.api.rendering.entity.EntityRenderable;
import io.xol.chunkstories.api.rendering.entity.EntityRenderer;
import io.xol.chunkstories.api.rendering.entity.RenderingIterator;
import io.xol.chunkstories.api.rendering.textures.Texture2D;
import io.xol.chunkstories.api.world.WorldMaster;
import io.xol.chunkstories.api.world.cell.CellData;
import io.xol.chunkstories.core.util.WorldEffects;

//(c) 2015-2017 XolioWare Interactive
//http://chunkstories.xyz
//http://xol.io

public class EntityThrownFragGrenade extends EntityThrownGrenade implements EntityRenderable, DamageCause {

	int ignitionTimer = 60 * 5; // 4 seconds to ignite
	int deathTimer = 0 * 50; // Lives 50 seconds

	public EntityThrownFragGrenade(EntityDefinition type, Location loc) {
		super(type, loc);
	}

	@Override
	public EntityRenderer<? extends EntityRenderable> getEntityRenderer() {
		return new ThrownFragGrenadeModelRenderer();
	}

	static class ThrownFragGrenadeModelRenderer extends EntityRenderer<EntityThrownFragGrenade> {

		public void setupRender(RenderingInterface renderer) {
			renderer.useShader("entities");
			renderer.setObjectMatrix(null);

			Texture2D diffuse = renderer.textures().getTexture("./models/weapon/frag_grenade/frag_grenade_albedo.png");
			diffuse.setLinearFiltering(false);
			renderer.bindAlbedoTexture(diffuse);
			renderer.bindNormalTexture(renderer.textures().getTexture("./textures/normalnormal.png"));
			renderer.bindMaterialTexture(renderer.textures().getTexture("./textures/defaultmaterial.png"));
		}

		@Override
		public int renderEntities(RenderingInterface renderer,
				RenderingIterator<EntityThrownFragGrenade> renderableEntitiesIterator) {
			setupRender(renderer);
			int e = 0;

			renderer.setObjectMatrix(null);

			for (EntityThrownFragGrenade grenade : renderableEntitiesIterator.getElementsInFrustrumOnly()) {
				if (renderer.getCamera().getCameraPosition().distance(grenade.getLocation()) > 32)
					continue;

				e++;

				renderer.currentShader().setUniform3f("objectPosition", new Vector3f(0));

				CellData cell = grenade.getWorld().peekSafely(grenade.getLocation());
				//int modelBlockData = grenade.getWorld().peekSafely(grenade.getLocation()).getData();

				//int lightSky = VoxelFormat.sunlight(modelBlockData);
				//int lightBlock = VoxelFormat.blocklight(modelBlockData);
				renderer.currentShader().setUniform2f("worldLightIn", cell.getBlocklight(), cell.getSunlight());

				Matrix4f mutrix = new Matrix4f();

				mutrix.translate(new Vector3f(0.0f, 0.15f, 0.0f));
				mutrix.translate((float)grenade.getLocation().x(), (float)grenade.getLocation().y(), (float)grenade.getLocation().z());

				mutrix.rotate(grenade.direction, new Vector3f(0, 1, 0));
				mutrix.rotate(grenade.rotation, new Vector3f(0, 0, 1));

				renderer.setObjectMatrix(mutrix);

				renderer.meshes().getRenderableMesh("./models/weapon/frag_grenade/frag_grenade.obj").render(renderer);
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
