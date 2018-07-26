package io.xol.z.mod.entities

import org.joml.Matrix4f
import org.joml.Vector3d
import org.joml.Vector3dc
import org.joml.Vector3f
import org.joml.Vector4f

import io.xol.chunkstories.api.Location
import io.xol.chunkstories.api.entity.Entity
import io.xol.chunkstories.api.entity.EntityDefinition
import io.xol.chunkstories.api.entity.traits.TraitHasOverlay
import io.xol.chunkstories.api.entity.traits.TraitRenderable
import io.xol.chunkstories.api.entity.traits.serializable.TraitRotation
import io.xol.chunkstories.api.math.Math2
import io.xol.chunkstories.api.rendering.RenderingInterface
import io.xol.chunkstories.api.rendering.entity.EntityRenderer
import io.xol.chunkstories.api.rendering.entity.RenderingIterator
import io.xol.chunkstories.api.sound.SoundSource.Mode
import io.xol.chunkstories.api.world.WorldClient
import io.xol.chunkstories.api.world.WorldMaster
import io.xol.chunkstories.core.entity.EntityPlayer

class EntityThrownFlashbangGrenade(type: EntityDefinition, loc: Location) : EntityThrownGrenade(type, loc) {

    internal var ignitionTimer = 60 * 4 // 4 seconds to ignite
    internal var deathTimer = 60 * 5 // Lives 50 seconds

    internal var fadeUntil = 0L

    init {
        TraitRenderable(this, TraitRenderable.RendererFactory<EntityThrownFlashbangGrenade> { ThrownFlashbangGrenadeModelRenderer() })

        FlashbangOverlay(this)
    }

    internal class ThrownFlashbangGrenadeModelRenderer : EntityRenderer<EntityThrownFlashbangGrenade>() {

        fun setupRender(renderer: RenderingInterface) {
            renderer.useShader("entities")
            renderer.objectMatrix = null

            val diffuse = renderer.textures().getTexture("./models/weapon/flashbang_grenade/flashbang_grenade_albedo.png")
            diffuse.setLinearFiltering(false)
            renderer.bindAlbedoTexture(diffuse)
            renderer.bindNormalTexture(renderer.textures().getTexture("./textures/normalnormal.png"))
            renderer.bindMaterialTexture(renderer.textures().getTexture("./textures/defaultmaterial.png"))
        }

        override fun renderEntities(renderer: RenderingInterface,
                                    renderableEntitiesIterator: RenderingIterator<EntityThrownFlashbangGrenade>): Int {
            setupRender(renderer)
            var e = 0

            renderer.objectMatrix = null

            for (grenade in renderableEntitiesIterator.elementsInFrustrumOnly) {
                if (renderer.camera.cameraPosition.distance(grenade.location) > 32)
                    continue

                e++

                renderer.currentShader().setUniform3f("objectPosition", Vector3f(0f))

                val cell = grenade.getWorld().peekSafely(grenade.location)
                //int modelBlockData = grenade.getWorld().peekSafely(grenade.getLocation()).getData();

                //int lightSky = VoxelFormat.sunlight(modelBlockData);
                //int lightBlock = VoxelFormat.blocklight(modelBlockData);
                renderer.currentShader().setUniform2f("worldLightIn", cell.blocklight.toFloat(), cell.sunlight.toFloat())

                val mutrix = Matrix4f()

                mutrix.translate(Vector3f(0.0f, 0.15f, 0.0f))
                mutrix.translate(grenade.location.x().toFloat(), grenade.location.y().toFloat(), grenade.location.z().toFloat())

                mutrix.rotate(grenade.direction, Vector3f(0f, 1f, 0f))
                mutrix.rotate(grenade.rotation, Vector3f(0f, 0f, 1f))

                renderer.objectMatrix = mutrix

                renderer.meshes().getRenderableMesh("./models/weapon/flashbang_grenade/flashbang_grenade.obj").render(renderer)
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
                world.getSoundManager().playSoundEffect("./sounds/dogez/weapon/grenades/flashbang.ogg", Mode.NORMAL, location, 1f, 1f, 25f, 45f)
            if (world is WorldClient) {
                val e = world.client.player!!.controlledEntity
                if (e != null && e is EntityPlayer) {
                    val distance = e.location.distance(location)

                    val dir = Vector3d(location)
                    dir.sub(e.location.add(0.0, 1.80, 0.0))

                    dir.normalize()

                    val edir = e.traits.tryWith(TraitRotation::class.java) { rot -> rot.directionLookingAt }

                    val raytrace = world.collisionsManager().raytraceSolidOuter(e.location.add(0.0, 1.80, 0.0), dir, 256.0)

                    val raytraceOk = if (raytrace!!.distance(location) < 1.5) 1f else 0f

                    println(raytrace.toString() + " : " + location + " distance:" + raytrace.distance(location))

                    val addedtime = (1000L + (5000.0f * Math2.clamp((5f - distance) / 10f, 0.0, 1.0)).toLong()
                            + (5000.0f * raytraceOk * Math2.clamp(5 * dir.dot(edir!!), 0.0, 1.0)).toLong())

                    println(addedtime)

                    fadeUntil = System.currentTimeMillis() + addedtime
                }
            }
            ignitionTimer--
        } else if (deathTimer > 0) {
            deathTimer--
            /*if (authority.isClient()) {
				world.getParticlesManager().spawnParticleAtPositionWithVelocity("smoke", this.getLocation(),
						new Vector3dm(Math.random() * 2.0 - 1.0, Math.random() * 2.0 - 0.5, Math.random() * 2.0 - 1.0)
								.normalize().scale(Math.random() * 0.05 + 0.02));
			}*/
        } else if (world is WorldMaster) {
            world.removeEntity(this)
        }
    }

    internal inner class FlashbangOverlay(entity: Entity) : TraitHasOverlay(entity) {

        override fun drawEntityOverlay(renderingInterface: RenderingInterface) {
            var fade = Math.max(0L, fadeUntil - System.currentTimeMillis()).toInt() / 5000.0f
            fade = Math2.clamp(fade.toDouble(), 0.0, 1.0)
            renderingInterface.guiRenderer.drawBox(-1f, -1f, 1f, 1f, 0f, 0f, 0f, 0f, null, true, false, Vector4f(1f, 1f, 1f, fade))
        }
    }
}
