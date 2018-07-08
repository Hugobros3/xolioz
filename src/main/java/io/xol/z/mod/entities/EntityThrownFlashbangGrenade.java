package io.xol.z.mod.entities;

import org.joml.Matrix4f;
import org.joml.Vector3d;
import org.joml.Vector3dc;
import org.joml.Vector3f;
import org.joml.Vector4f;

import io.xol.chunkstories.api.Location;
import io.xol.chunkstories.api.entity.Entity;
import io.xol.chunkstories.api.entity.EntityDefinition;
import io.xol.chunkstories.api.entity.components.EntityRotation;
import io.xol.chunkstories.api.entity.traits.TraitHasOverlay;
import io.xol.chunkstories.api.entity.traits.TraitRenderable;
import io.xol.chunkstories.api.math.Math2;
import io.xol.chunkstories.api.rendering.RenderingInterface;
import io.xol.chunkstories.api.rendering.entity.EntityRenderer;
import io.xol.chunkstories.api.rendering.entity.RenderingIterator;
import io.xol.chunkstories.api.rendering.textures.Texture2D;
import io.xol.chunkstories.api.sound.SoundSource.Mode;
import io.xol.chunkstories.api.world.WorldClient;
import io.xol.chunkstories.api.world.WorldMaster;
import io.xol.chunkstories.api.world.cell.CellData;
import io.xol.chunkstories.core.entity.EntityPlayer;

public class EntityThrownFlashbangGrenade extends EntityThrownGrenade {

	int ignitionTimer = 60 * 4; // 4 seconds to ignite
	int deathTimer = 60 * 5; // Lives 50 seconds

	public EntityThrownFlashbangGrenade(EntityDefinition type, Location loc) {
		super(type, loc);
		new TraitRenderable(this, ThrownFlashbangGrenadeModelRenderer::new);
		
		new FlashbangOverlay(this);
	}

	static class ThrownFlashbangGrenadeModelRenderer extends EntityRenderer<EntityThrownFlashbangGrenade> {

		public void setupRender(RenderingInterface renderer) {
			renderer.useShader("entities");
			renderer.setObjectMatrix(null);

			Texture2D diffuse = renderer.textures().getTexture("./models/weapon/flashbang_grenade/flashbang_grenade_albedo.png");
			diffuse.setLinearFiltering(false);
			renderer.bindAlbedoTexture(diffuse);
			renderer.bindNormalTexture(renderer.textures().getTexture("./textures/normalnormal.png"));
			renderer.bindMaterialTexture(renderer.textures().getTexture("./textures/defaultmaterial.png"));
		}

		@Override
		public int renderEntities(RenderingInterface renderer,
				RenderingIterator<EntityThrownFlashbangGrenade> renderableEntitiesIterator) {
			setupRender(renderer);
			int e = 0;

			renderer.setObjectMatrix(null);

			for (EntityThrownFlashbangGrenade grenade : renderableEntitiesIterator.getElementsInFrustrumOnly()) {
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

				renderer.meshes().getRenderableMesh("./models/weapon/flashbang_grenade/flashbang_grenade.obj").render(renderer);
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
				world.getSoundManager().playSoundEffect("./sounds/dogez/weapon/grenades/flashbang.ogg", Mode.NORMAL, getLocation(), 1, 1, 25, 45);
			if (world instanceof WorldClient)
			{
				Entity e = ((WorldClient) world).getClient().getPlayer().getControlledEntity();
				if(e != null && e instanceof EntityPlayer)
				{
					double distance = e.getLocation().distance(getLocation());
					
					Vector3d dir = new Vector3d(getLocation());
					dir.sub(e.getLocation().add(0.0, 1.80, 0.0));
					
					dir.normalize();
					
					Vector3dc edir = e.components.tryWith(EntityRotation.class, rot -> rot.getDirectionLookingAt());
					
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
	
	class FlashbangOverlay extends TraitHasOverlay {

		public FlashbangOverlay(Entity entity) {
			super(entity);
		}
		
		@Override
		public void drawEntityOverlay(RenderingInterface renderingInterface)
		{
			float fade = ((int)Math.max(0L, fadeUntil - System.currentTimeMillis())) / 5000.0f;
			fade = Math2.clamp(fade, 0, 1);
			renderingInterface.getGuiRenderer().drawBox(-1, -1, 1, 1, 0, 0, 0, 0, null, true, false, new Vector4f(1,1,1,fade));
		}
	}
}
