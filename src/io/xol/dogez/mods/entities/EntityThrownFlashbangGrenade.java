package io.xol.dogez.mods.entities;

import org.joml.Matrix4f;
import org.joml.Vector3d;
import org.joml.Vector3dc;
import org.joml.Vector3f;
import org.joml.Vector4f;

import io.xol.chunkstories.api.entity.Entity;
import io.xol.chunkstories.api.entity.EntityType;
import io.xol.chunkstories.api.entity.interfaces.EntityOverlay;
import io.xol.chunkstories.api.math.Math2;
import io.xol.chunkstories.api.rendering.RenderingInterface;
import io.xol.chunkstories.api.rendering.entity.EntityRenderable;
import io.xol.chunkstories.api.rendering.entity.EntityRenderer;
import io.xol.chunkstories.api.rendering.entity.RenderingIterator;
import io.xol.chunkstories.api.rendering.textures.Texture2D;
import io.xol.chunkstories.api.voxel.VoxelFormat;
import io.xol.chunkstories.api.world.World;
import io.xol.chunkstories.api.world.WorldClient;
import io.xol.chunkstories.api.world.WorldMaster;
import io.xol.chunkstories.core.entity.EntityPlayer;

//(c) 2015-2017 XolioWare Interactive
//http://chunkstories.xyz
//http://xol.io

public class EntityThrownFlashbangGrenade extends EntityThrownGrenade implements EntityRenderable, EntityOverlay {

	int ignitionTimer = 60 * 4; // 4 seconds to ignite
	int deathTimer = 60 * 5; // Lives 50 seconds

	public EntityThrownFlashbangGrenade(EntityType type, World world, double x, double y, double z) {
		super(type, world, x, y, z);
	}

	@Override
	public EntityRenderer<? extends EntityRenderable> getEntityRenderer() {
		return new ThrownFlashbangGrenadeModelRenderer();
	}

	static class ThrownFlashbangGrenadeModelRenderer implements EntityRenderer<EntityThrownFlashbangGrenade> {

		public void setupRender(RenderingInterface renderingContext) {
			renderingContext.setObjectMatrix(null);

			Texture2D diffuse = renderingContext.textures().getTexture("./models/weapon/flashbang_grenade/flashbang_grenade_albedo.png");
			diffuse.setLinearFiltering(false);
			renderingContext.bindAlbedoTexture(diffuse);
			renderingContext.bindNormalTexture(renderingContext.textures().getTexture("./textures/normalnormal.png"));
			renderingContext.bindMaterialTexture(renderingContext.textures().getTexture("./textures/defaultmaterial.png"));
		}

		@Override
		public int renderEntities(RenderingInterface renderingContext,
				RenderingIterator<EntityThrownFlashbangGrenade> renderableEntitiesIterator) {
			setupRender(renderingContext);
			int e = 0;

			renderingContext.setObjectMatrix(null);

			for (EntityThrownFlashbangGrenade grenade : renderableEntitiesIterator.getElementsInFrustrumOnly()) {
				if (renderingContext.getCamera().getCameraPosition().distance(grenade.getLocation()) > 32)
					continue;

				e++;

				renderingContext.currentShader().setUniform3f("objectPosition", new Vector3f(0));

				int modelBlockData = grenade.getWorld().getVoxelData(grenade.getLocation());

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

				renderingContext.meshes().getRenderableMeshByName("./models/weapon/flashbang_grenade/flashbang_grenade.obj").render(renderingContext);
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
				world.getSoundManager().playSoundEffect("./sounds/dogez/weapon/grenades/flashbang.ogg", getLocation(), 1, 1, 25, 45);
			if (world instanceof WorldClient)
			{
				Entity e = ((WorldClient) world).getClient().getPlayer().getControlledEntity();
				if(e != null && e instanceof EntityPlayer)
				{
					double distance = e.getLocation().distance(getLocation());
					
					Vector3d dir = new Vector3d(getLocation());
					dir.sub(e.getLocation().add(0.0, 1.80, 0.0));
					
					dir.normalize();
					
					Vector3dc edir = ((EntityPlayer)e).getDirectionLookingAt();
					
					Vector3d raytrace = world.collisionsManager().raytraceSolidOuter(e.getLocation().add(0.0, 1.80, 0.0), dir, 256.0);
					
					float raytraceOk = raytrace.distance(getLocation()) < 1.5 ? 1f : 0f;
					
					System.out.println(raytrace + " : " + getLocation() + " distance:" + raytrace.distance(getLocation()));
					
					long addedtime = 1000L + (long)(5000.0f * Math2.clamp((5f - distance) / 10f, 0, 1))
						+	(long)((5000.0f) * raytraceOk * Math2.clamp(5 * dir.dot(edir), 0.0, 1.0));
					
					System.out.println(addedtime);
					
					fadeUntil = System.currentTimeMillis() + addedtime;
				}
			}
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
	
	long fadeUntil = 0L;
	
	@Override
	public void drawEntityOverlay(RenderingInterface renderingInterface)
	{
		float fade = ((int)Math.max(0L, fadeUntil - System.currentTimeMillis())) / 5000.0f;
		fade = Math2.clamp(fade, 0, 1);
		renderingInterface.getGuiRenderer().drawBox(-1, -1, 1, 1, 0, 0, 0, 0, null, true, false, new Vector4f(1,1,1,fade));
	}
}
