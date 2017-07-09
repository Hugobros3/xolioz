package io.xol.dogez.mods.entities;

import io.xol.chunkstories.api.rendering.entity.EntityRenderable;
import io.xol.chunkstories.api.voxel.Voxel;
import io.xol.chunkstories.api.voxel.VoxelFormat;
import io.xol.chunkstories.api.world.World;
import io.xol.chunkstories.api.world.WorldClient;
import io.xol.chunkstories.api.world.WorldMaster;
import io.xol.chunkstories.api.math.Math2;
import io.xol.chunkstories.api.math.vector.dp.Vector2dm;
import io.xol.chunkstories.api.math.vector.dp.Vector3dm;
import io.xol.chunkstories.api.physics.CollisionBox;
import io.xol.chunkstories.api.entity.EntityBase;
import io.xol.chunkstories.api.entity.EntityType;

public abstract class EntityThrownGrenade extends EntityBase implements EntityRenderable  {

	protected float tilt = 0f;
	protected float direction = 0f;
	protected float rotation = 0f;
	
	public EntityThrownGrenade(EntityType type, World world, double x, double y, double z) {
		super(type, world, x, y, z);
	}

	@Override
	public void tick() {

		Vector3dm velocity = getVelocityComponent().getVelocity();

		if (world instanceof WorldMaster) {
			Voxel voxelIn = world.getGameContext().getContent().voxels()
					.getVoxelById(VoxelFormat.id(world.getVoxelData(positionComponent.getLocation())));
			boolean inWater = voxelIn.getType().isLiquid();

			double terminalVelocity = inWater ? -0.05 : -1.5;
			if (velocity.getY() > terminalVelocity && !this.isOnGround())
				velocity.setY(velocity.getY() - 0.016);
			if (velocity.getY() < terminalVelocity)
				velocity.setY(terminalVelocity);

			Vector3dm remainingToMove = moveWithCollisionRestrain(velocity.getX(), velocity.getY(), velocity.getZ());
			if (remainingToMove.getY() < -0.02 && this.isOnGround()) {
				if (remainingToMove.getY() < -0.01) {
					//Bounce
					double originalDownardsVelocity = velocity.getY();
					velocity.scale(0.65);
					velocity.setY(-originalDownardsVelocity * 0.65);
					
					world.getSoundManager().playSoundEffect("./sounds/dogez/weapon/grenades/grenade_bounce.ogg", getLocation(), 1, 1, 10, 35);
				} else
					velocity.scale(0d);
			}

			if (Math.abs(velocity.getX()) < 0.02)
				velocity.setX(0.0);
			if (Math.abs(velocity.getZ()) < 0.02)
				velocity.setZ(0.0);
			
			if (Math.abs(velocity.getY()) < 0.01)
				velocity.setY(0.0);

			getVelocityComponent().setVelocity(velocity);
		}

		if (world instanceof WorldClient) {
			
			if(!this.isOnGround())
			{
			rotation += getVelocityComponent().getVelocity().length() * Math.random();
			tilt = (float) Math2.mix(0.0, tilt + getVelocityComponent().getVelocity().length() * Math.random(),
					Math2.clampd(velocity.length(), 0.0, 0.1) * 10.0);

			
			Vector2dm direction2d = new Vector2dm(velocity.getX(), velocity.getZ());

			if (direction2d.length() > 0.0) {
				direction2d.normalize();
				
				Math.acos(direction2d.getX());
				Math.asin(direction2d.getY());
				double directionDegrees;
				
				//Y is sin by convention, if > 0 top part of the circle
				if(direction2d.getY() >= 0.0)
				{
					directionDegrees = -Math.acos(direction2d.getX());
				}
				else
					directionDegrees = Math.acos(direction2d.getX());
				
				direction = (float)directionDegrees;
				//direction = (float) -Math.toRadians(directionDegrees);
			}
			}
		}
	}

	public CollisionBox[] getCollisionBoxes() {
		return new CollisionBox[] { new CollisionBox(0.25, 0.25, 0.25).translate(-0.125, 0.0, -0.125) };
	}
}
