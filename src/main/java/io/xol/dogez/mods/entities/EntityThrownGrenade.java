package io.xol.dogez.mods.entities;

import org.joml.Vector2d;
import org.joml.Vector3d;
import org.joml.Vector3dc;

import io.xol.chunkstories.api.Location;
import io.xol.chunkstories.api.entity.Entity;
import io.xol.chunkstories.api.entity.EntityDefinition;
import io.xol.chunkstories.api.entity.components.EntityVelocity;
import io.xol.chunkstories.api.entity.traits.TraitCollidable;
import io.xol.chunkstories.api.math.Math2;
import io.xol.chunkstories.api.physics.CollisionBox;
import io.xol.chunkstories.api.sound.SoundSource.Mode;
import io.xol.chunkstories.api.voxel.Voxel;
import io.xol.chunkstories.api.world.WorldClient;
import io.xol.chunkstories.api.world.WorldMaster;

public abstract class EntityThrownGrenade extends Entity {

	protected float tilt = 0f;
	protected float direction = 0f;
	protected float rotation = 0f;

	public final EntityVelocity entityVelocity = new EntityVelocity(this);
	public final TraitCollidable collisions;

	public EntityThrownGrenade(EntityDefinition type, Location loc) {
		super(type, loc);

		collisions = new TraitCollidable(this) {

			@Override
			public CollisionBox[] getCollisionBoxes() {
				return new CollisionBox[] { new CollisionBox(0.25, 0.25, 0.25).translate(-0.125, 0.0, -0.125) };
			}

		};
	}

	@Override
	public void tick() {

		Vector3d velocity = entityVelocity.getVelocity();

		if (world instanceof WorldMaster) {
			Voxel voxelIn = world.peekSafely(entityLocation.get()).getVoxel();
			boolean inWater = voxelIn.getDefinition().isLiquid();

			double terminalVelocity = inWater ? -0.05 : -1.5;
			if (velocity.y() > terminalVelocity && !collisions.isOnGround())
				velocity.y = (velocity.y() - 0.016);
			if (velocity.y() < terminalVelocity)
				velocity.y = (terminalVelocity);

			Vector3dc remainingToMove = collisions.moveWithCollisionRestrain(velocity.x(), velocity.y(), velocity.z());
			if (remainingToMove.y() < -0.02 && collisions.isOnGround()) {
				if (remainingToMove.y() < -0.01) {
					// Bounce
					double originalDownardsVelocity = velocity.y();
					velocity.mul(0.65);
					velocity.y = (-originalDownardsVelocity * 0.65);

					world.getSoundManager().playSoundEffect("./sounds/dogez/weapon/grenades/grenade_bounce.ogg", Mode.NORMAL, getLocation(), 1, 1, 10, 35);
				} else
					velocity.mul(0d);
			}

			if (Math.abs(velocity.x()) < 0.02)
				velocity.x = (0.0);
			if (Math.abs(velocity.z()) < 0.02)
				velocity.z = (0.0);

			if (Math.abs(velocity.y()) < 0.01)
				velocity.y = (0.0);

			entityVelocity.setVelocity(velocity);
		}

		if (world instanceof WorldClient) {

			if (!collisions.isOnGround()) {
				rotation += entityVelocity.getVelocity().length() * Math.random();
				tilt = (float) Math2.mix(0.0, tilt + entityVelocity.getVelocity().length() * Math.random(), Math2.clampd(velocity.length(), 0.0, 0.1) * 10.0);

				Vector2d direction2d = new Vector2d(velocity.x(), velocity.z());

				if (direction2d.length() > 0.0) {
					direction2d.normalize();

					Math.acos(direction2d.x());
					Math.asin(direction2d.y());
					double directionDegrees;

					// Y is sin by convention, if > 0 top part of the circle
					if (direction2d.y() >= 0.0) {
						directionDegrees = -Math.acos(direction2d.x());
					} else
						directionDegrees = Math.acos(direction2d.x());

					direction = (float) directionDegrees;
					// direction = (float) -Math.toRadians(directionDegrees);
				}
			}
		}
	}
}
