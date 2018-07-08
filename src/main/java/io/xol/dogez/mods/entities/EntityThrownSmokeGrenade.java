package io.xol.dogez.mods.entities;

import org.joml.Matrix4f;
import org.joml.Vector3d;
import org.joml.Vector3f;

import io.xol.chunkstories.api.Location;
import io.xol.chunkstories.api.entity.EntityDefinition;
import io.xol.chunkstories.api.rendering.RenderingInterface;
import io.xol.chunkstories.api.rendering.entity.EntityRenderer;
import io.xol.chunkstories.api.rendering.entity.RenderingIterator;
import io.xol.chunkstories.api.rendering.textures.Texture2D;
import io.xol.chunkstories.api.sound.SoundSource.Mode;
import io.xol.chunkstories.api.world.WorldClient;
import io.xol.chunkstories.api.world.WorldMaster;
import io.xol.chunkstories.api.world.cell.CellData;

//(c) 2015-2017 XolioWare Interactive
//http://chunkstories.xyz
//http://xol.io

public class EntityThrownSmokeGrenade extends EntityThrownGrenade {

	int ignitionTimer = 60 * 4; // 4 seconds to ignite
	int deathTimer = 60 * 50; // Lives 50 seconds

	public EntityThrownSmokeGrenade(EntityDefinition type, Location location) {
		super(type, location);
	}

	@Override
	public EntityRenderer<? extends EntityRenderable> getEntityRenderer() {
		return new ThrownSmokeGrenadeModelRenderer();
	}

	static class ThrownSmokeGrenadeModelRenderer extends EntityRenderer<EntityThrownSmokeGrenade> {

		public void setupRender(RenderingInterface renderer) {
			renderer.useShader("entities");
			renderer.setObjectMatrix(null);

			Texture2D diffuse = renderer.textures().getTexture("./models/weapon/smoke_grenade/smoke_grenade_albedo.png");
			diffuse.setLinearFiltering(false);
			renderer.bindAlbedoTexture(diffuse);
			renderer.bindNormalTexture(renderer.textures().getTexture("./textures/normalnormal.png"));
			renderer.bindMaterialTexture(renderer.textures().getTexture("./textures/defaultmaterial.png"));
		}

		@Override
		public int renderEntities(RenderingInterface renderer,
				RenderingIterator<EntityThrownSmokeGrenade> renderableEntitiesIterator) {
			setupRender(renderer);
			int e = 0;

			renderer.setObjectMatrix(null);

			for (EntityThrownSmokeGrenade grenade : renderableEntitiesIterator.getElementsInFrustrumOnly()) {
				if (renderer.getCamera().getCameraPosition().distance(grenade.getLocation()) > 32)
					continue;

				e++;

				renderer.currentShader().setUniform3f("objectPosition", new Vector3f(0));

				CellData cell = grenade.getWorld().peekSafely(grenade.getLocation());
				renderer.currentShader().setUniform2f("worldLightIn", cell.getBlocklight(), cell.getSunlight());

				Matrix4f mutrix = new Matrix4f();

				mutrix.translate(new Vector3f(0.0f, 0.15f, 0.0f));
				mutrix.translate((float)grenade.getLocation().x(), (float)grenade.getLocation().y(), (float)grenade.getLocation().z());

				mutrix.rotate(grenade.direction, new Vector3f(0, 1, 0));
				mutrix.rotate(grenade.rotation, new Vector3f(0, 0, 1));

				renderer.setObjectMatrix(mutrix);

				renderer.meshes().getRenderableMesh("./models/weapon/smoke_grenade/smoke_grenade.obj").render(renderer);
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
				world.getSoundManager().playSoundEffect("./sounds/dogez/weapon/grenades/smoke_puff.ogg", Mode.NORMAL, getLocation(), 1, 1, 15, 25);
			ignitionTimer--;
		}
		else if (deathTimer > 0) {
			deathTimer--;
			if (world instanceof WorldClient) {
				world.getParticlesManager().spawnParticleAtPositionWithVelocity("smoke", this.getLocation(),
						new Vector3d(Math.random() * 2.0 - 1.0, Math.random() * 2.0 - 0.5, Math.random() * 2.0 - 1.0)
								.normalize().mul(Math.random() * 0.05 + 0.02));
			}
		} else if (world instanceof WorldMaster) {
			world.removeEntity(this);
		}
	}
}
