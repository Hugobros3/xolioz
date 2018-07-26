//
// This file is a part of the XolioZ Mod for Chunk Stories
// Check out README.md for more information
// Website: https://chunkstories.xyz
// Github: https://github.com/Hugobros3/xolioz
//

package io.xol.z.mod.entities

import org.joml.Matrix4f
import org.joml.Vector3d
import org.joml.Vector3f

import io.xol.chunkstories.api.Location
import io.xol.chunkstories.api.entity.EntityDefinition
import io.xol.chunkstories.api.entity.traits.TraitRenderable
import io.xol.chunkstories.api.rendering.RenderingInterface
import io.xol.chunkstories.api.rendering.entity.EntityRenderer
import io.xol.chunkstories.api.rendering.entity.RenderingIterator
import io.xol.chunkstories.api.rendering.textures.Texture2D
import io.xol.chunkstories.api.sound.SoundSource.Mode
import io.xol.chunkstories.api.world.WorldClient
import io.xol.chunkstories.api.world.WorldMaster
import io.xol.chunkstories.api.world.cell.CellData

class EntityThrownSmokeGrenade(type: EntityDefinition, location: Location) : EntityThrownGrenade(type, location) {

	internal var ignitionTimer = 60 * 4 // 4 seconds to ignite
	internal var deathTimer = 60 * 50 // Lives 50 seconds

	init {

		TraitRenderable(this, TraitRenderable.RendererFactory<EntityThrownSmokeGrenade> { ThrownSmokeGrenadeModelRenderer() })
	}

	internal class ThrownSmokeGrenadeModelRenderer : EntityRenderer<EntityThrownSmokeGrenade>() {

		fun setupRender(renderer: RenderingInterface) {
			renderer.useShader("entities")
			renderer.objectMatrix = null

			val diffuse = renderer.textures().getTexture("./models/weapon/smoke_grenade/smoke_grenade_albedo.png")
			diffuse.setLinearFiltering(false)
			renderer.bindAlbedoTexture(diffuse)
			renderer.bindNormalTexture(renderer.textures().getTexture("./textures/normalnormal.png"))
			renderer.bindMaterialTexture(renderer.textures().getTexture("./textures/defaultmaterial.png"))
		}

		override fun renderEntities(renderer: RenderingInterface,
									renderableEntitiesIterator: RenderingIterator<EntityThrownSmokeGrenade>): Int {
			setupRender(renderer)
			var e = 0

			renderer.objectMatrix = null

			for (grenade in renderableEntitiesIterator.elementsInFrustrumOnly) {
				if (renderer.camera.cameraPosition.distance(grenade.location) > 32)
					continue

				e++

				renderer.currentShader().setUniform3f("objectPosition", Vector3f(0f))

				val cell = grenade.getWorld().peekSafely(grenade.location)
				renderer.currentShader().setUniform2f("worldLightIn", cell.blocklight.toFloat(), cell.sunlight.toFloat())

				val mutrix = Matrix4f()

				mutrix.translate(Vector3f(0.0f, 0.15f, 0.0f))
				mutrix.translate(grenade.location.x().toFloat(), grenade.location.y().toFloat(), grenade.location.z().toFloat())

				mutrix.rotate(grenade.direction, Vector3f(0f, 1f, 0f))
				mutrix.rotate(grenade.rotation, Vector3f(0f, 0f, 1f))

				renderer.objectMatrix = mutrix

				renderer.meshes().getRenderableMesh("./models/weapon/smoke_grenade/smoke_grenade.obj").render(renderer)
			}

			return e
		}

		override fun freeRessources() {

		}

	}

	override fun tick() {

		//Movement and stuff
		super.tick()

		if (ignitionTimer > 0)
			ignitionTimer--
		else if (ignitionTimer == 0) {
			if (world is WorldMaster)
				world.getSoundManager().playSoundEffect("./sounds/dogez/weapon/grenades/smoke_puff.ogg", Mode.NORMAL, location, 1f, 1f, 15f, 25f)
			ignitionTimer--
		} else if (deathTimer > 0) {
			deathTimer--
			if (world is WorldClient) {
				world.getParticlesManager().spawnParticleAtPositionWithVelocity("smoke", this.location,
						Vector3d(Math.random() * 2.0 - 1.0, Math.random() * 2.0 - 0.5, Math.random() * 2.0 - 1.0)
								.normalize().mul(Math.random() * 0.05 + 0.02))
			}
		} else if (world is WorldMaster) {
			world.removeEntity(this)
		}
	}
}
