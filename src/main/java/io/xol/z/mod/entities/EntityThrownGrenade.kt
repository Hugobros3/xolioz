package io.xol.z.mod.entities

import org.joml.Vector2d
import org.joml.Vector3d
import org.joml.Vector3dc

import io.xol.chunkstories.api.Location
import io.xol.chunkstories.api.entity.Entity
import io.xol.chunkstories.api.entity.EntityDefinition
import io.xol.chunkstories.api.entity.components.EntityVelocity
import io.xol.chunkstories.api.entity.traits.TraitCollidable
import io.xol.chunkstories.api.math.Math2
import io.xol.chunkstories.api.physics.CollisionBox
import io.xol.chunkstories.api.sound.SoundSource.Mode
import io.xol.chunkstories.api.voxel.Voxel
import io.xol.chunkstories.api.world.WorldClient
import io.xol.chunkstories.api.world.WorldMaster

abstract class EntityThrownGrenade(type: EntityDefinition, loc: Location) : Entity(type, loc) {

    protected var tilt = 0f
    protected var direction = 0f
    protected var rotation = 0f

    val entityVelocity = EntityVelocity(this)
    val collisions: TraitCollidable

    init {

        collisions = object : TraitCollidable(this) {

            override fun getCollisionBoxes(): Array<CollisionBox> {
                return arrayOf(CollisionBox(0.25, 0.25, 0.25).translate(-0.125, 0.0, -0.125))
            }

        }
    }

    override fun tick() {

        val velocity = entityVelocity.velocity

        if (world is WorldMaster) {
            val voxelIn = world.peekSafely(entityLocation.get()).voxel
            val inWater = voxelIn!!.definition.isLiquid

            val terminalVelocity = if (inWater) -0.05 else -1.5
            if (velocity.y() > terminalVelocity && !collisions.isOnGround)
                velocity.y = velocity.y() - 0.016
            if (velocity.y() < terminalVelocity)
                velocity.y = terminalVelocity

            val remainingToMove = collisions.moveWithCollisionRestrain(velocity.x(), velocity.y(), velocity.z())
            if (remainingToMove.y() < -0.02 && collisions.isOnGround) {
                if (remainingToMove.y() < -0.01) {
                    // Bounce
                    val originalDownardsVelocity = velocity.y()
                    velocity.mul(0.65)
                    velocity.y = -originalDownardsVelocity * 0.65

                    world.getSoundManager().playSoundEffect("./sounds/dogez/weapon/grenades/grenade_bounce.ogg", Mode.NORMAL, location, 1f, 1f, 10f, 35f)
                } else
                    velocity.mul(0.0)
            }

            if (Math.abs(velocity.x()) < 0.02)
                velocity.x = 0.0
            if (Math.abs(velocity.z()) < 0.02)
                velocity.z = 0.0

            if (Math.abs(velocity.y()) < 0.01)
                velocity.y = 0.0

            entityVelocity.setVelocity(velocity)
        }

        if (world is WorldClient) {

            if (!collisions.isOnGround) {
                rotation += (entityVelocity.velocity.length() * Math.random()).toFloat()
                tilt = Math2.mix(0.0, tilt + entityVelocity.velocity.length() * Math.random(), Math2.clampd(velocity.length(), 0.0, 0.1) * 10.0)

                val direction2d = Vector2d(velocity.x(), velocity.z())

                if (direction2d.length() > 0.0) {
                    direction2d.normalize()

                    Math.acos(direction2d.x())
                    Math.asin(direction2d.y())
                    val directionDegrees: Double

                    // Y is sin by convention, if > 0 top part of the circle
                    if (direction2d.y() >= 0.0) {
                        directionDegrees = -Math.acos(direction2d.x())
                    } else
                        directionDegrees = Math.acos(direction2d.x())

                    direction = directionDegrees.toFloat()
                    // direction = (float) -Math.toRadians(directionDegrees);
                }
            }
        }
    }
}
