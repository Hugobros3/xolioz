package io.xol.z.mod.entities

import org.joml.Matrix4f
import org.joml.Vector3d
import org.joml.Vector3f

import io.xol.chunkstories.api.Location
import io.xol.chunkstories.api.entity.DamageCause
import io.xol.chunkstories.api.entity.EntityDefinition
import io.xol.chunkstories.api.entity.traits.TraitRenderable
import io.xol.chunkstories.api.entity.traits.serializable.TraitHealth
import io.xol.chunkstories.api.rendering.RenderingInterface
import io.xol.chunkstories.api.rendering.entity.EntityRenderer
import io.xol.chunkstories.api.rendering.entity.RenderingIterator
import io.xol.chunkstories.api.world.WorldMaster
import io.xol.chunkstories.core.util.WorldEffects

class EntityThrownFragGrenade(type: EntityDefinition, loc: Location) : EntityThrownGrenade(type, loc), DamageCause {

    internal var ignitionTimer = 60 * 5 // 4 seconds to ignite
    internal var deathTimer = 0 * 50 // Lives 50 seconds

    init {
        TraitRenderable(this, TraitRenderable.RendererFactory {ThrownFragGrenadeModelRenderer() } )
    }

    class ThrownFragGrenadeModelRenderer : EntityRenderer<EntityThrownFragGrenade>() {

        fun setupRender(renderer: RenderingInterface) {
            renderer.useShader("entities")
            renderer.objectMatrix = null

            val diffuse = renderer.textures().getTexture("./models/weapon/frag_grenade/frag_grenade_albedo.png")
            diffuse.setLinearFiltering(false)
            renderer.bindAlbedoTexture(diffuse)
            renderer.bindNormalTexture(renderer.textures().getTexture("./textures/normalnormal.png"))
            renderer.bindMaterialTexture(renderer.textures().getTexture("./textures/defaultmaterial.png"))
        }

        override fun renderEntities(renderer: RenderingInterface, renderableEntitiesIterator: RenderingIterator<EntityThrownFragGrenade>): Int {
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

                renderer.meshes().getRenderableMesh("./models/weapon/frag_grenade/frag_grenade.obj").render(renderer)
            }

            return e
        }

        override fun freeRessources() {

        }

    }

    override fun tick() {

        // Movement and stuff
        super.tick()

        if (ignitionTimer > 0)
            ignitionTimer--
        else if (ignitionTimer == 0) {
            if (world is WorldMaster) {
                WorldEffects.createFireball(world, location.add(0.0, 0.5, 0.0), 5.0, 0.1, 2f)
                val dmg_radius = 10.0
                val dmg_radius_maxdmg = 5.0

                val maxDmg = 200.0
                for (entityInBlastRadius in world.getEntitiesInBox(location, Vector3d(dmg_radius))) {

                    //TODO
                    with(entityInBlastRadius.traits.get(TraitHealth::class.java)) {

                    }

                    entityInBlastRadius.traits.with<TraitHealth>(TraitHealth::class.java, damageLambda@ { eh ->
                        val distance = entityInBlastRadius.location.distance(location)
                        if (distance > dmg_radius)
                            return@damageLambda

                        val dmg = (maxDmg * Math.min(Math.max(0.0, dmg_radius - distance), dmg_radius_maxdmg) / dmg_radius_maxdmg).toFloat()

                        // world.getGameLogic().logger().("Damaging "+entityInBlastRadius + " with
                        // "+dmg);
                        eh.damage(this, dmg)
                    });
                }
            }
            ignitionTimer--
        } else if (deathTimer > 0) {
            deathTimer--
        } else if (world is WorldMaster) {
            world.removeEntity(this)
        }
    }

    override fun getName(): String {
        return "Frag grenade"
    }
}
