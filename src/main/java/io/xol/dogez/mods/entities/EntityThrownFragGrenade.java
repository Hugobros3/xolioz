package io.xol.dogez.mods.entities;

import org.joml.Matrix4f;
import org.joml.Vector3d;
import org.joml.Vector3f;

import io.xol.chunkstories.api.Location;
import io.xol.chunkstories.api.entity.DamageCause;
import io.xol.chunkstories.api.entity.Entity;
import io.xol.chunkstories.api.entity.EntityDefinition;
import io.xol.chunkstories.api.entity.components.EntityHealth;
import io.xol.chunkstories.api.entity.traits.TraitRenderable;
import io.xol.chunkstories.api.rendering.RenderingInterface;
import io.xol.chunkstories.api.rendering.entity.EntityRenderer;
import io.xol.chunkstories.api.rendering.entity.RenderingIterator;
import io.xol.chunkstories.api.rendering.textures.Texture2D;
import io.xol.chunkstories.api.world.WorldMaster;
import io.xol.chunkstories.api.world.cell.CellData;
import io.xol.chunkstories.core.util.WorldEffects;

public class EntityThrownFragGrenade extends EntityThrownGrenade implements DamageCause {

	int ignitionTimer = 60 * 5; // 4 seconds to ignite
	int deathTimer = 0 * 50; // Lives 50 seconds

	public EntityThrownFragGrenade(EntityDefinition type, Location loc) {
		super(type, loc);
		new TraitRenderable(this, ThrownFragGrenadeModelRenderer::new);
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
		public int renderEntities(RenderingInterface renderer, RenderingIterator<EntityThrownFragGrenade> renderableEntitiesIterator) {
			setupRender(renderer);
			int e = 0;

			renderer.setObjectMatrix(null);

			for (EntityThrownFragGrenade grenade : renderableEntitiesIterator.getElementsInFrustrumOnly()) {
				if (renderer.getCamera().getCameraPosition().distance(grenade.getLocation()) > 32)
					continue;

				e++;

				renderer.currentShader().setUniform3f("objectPosition", new Vector3f(0));

				CellData cell = grenade.getWorld().peekSafely(grenade.getLocation());

				renderer.currentShader().setUniform2f("worldLightIn", cell.getBlocklight(), cell.getSunlight());

				Matrix4f mutrix = new Matrix4f();

				mutrix.translate(new Vector3f(0.0f, 0.15f, 0.0f));
				mutrix.translate((float) grenade.getLocation().x(), (float) grenade.getLocation().y(), (float) grenade.getLocation().z());

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

		// Movement and stuff
		super.tick();

		if (ignitionTimer > 0)
			ignitionTimer--;
		else if (ignitionTimer == 0) {
			if (world instanceof WorldMaster) {
				WorldEffects.createFireball(world, getLocation().add(0.0, 0.5, 0.0), 5f, 0.1f, 2);
				double dmg_radius = 10;
				double dmg_radius_maxdmg = 5;

				double maxDmg = 200;
				for (Entity entityInBlastRadius : world.getEntitiesInBox(getLocation(), new Vector3d(dmg_radius))) {
					entityInBlastRadius.components.with(EntityHealth.class, eh -> {
						double distance = entityInBlastRadius.getLocation().distance(getLocation());
						if (distance > dmg_radius)
							return;

						float dmg = (float) (maxDmg * Math.min(Math.max(0, dmg_radius - distance), dmg_radius_maxdmg) / dmg_radius_maxdmg);

						// world.getGameLogic().logger().("Damaging "+entityInBlastRadius + " with
						// "+dmg);
						eh.damage(this, dmg);
					});
				}
			}
			ignitionTimer--;
		} else if (deathTimer > 0) {
			deathTimer--;
		} else if (world instanceof WorldMaster) {
			world.removeEntity(this);
		}
	}

	@Override
	public String getName() {
		return "Frag grenade";
	}
}
