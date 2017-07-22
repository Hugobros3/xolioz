package io.xol.dogez.mods.voxel;

import org.joml.Matrix4f;
import org.joml.Vector3d;

import io.xol.chunkstories.api.entity.EntityBase;
import io.xol.chunkstories.api.entity.EntityType;
import io.xol.chunkstories.api.entity.EntityVoxel;
import io.xol.chunkstories.api.rendering.RenderingInterface;
import io.xol.chunkstories.api.rendering.entity.EntityRenderable;
import io.xol.chunkstories.api.rendering.entity.EntityRenderer;
import io.xol.chunkstories.api.rendering.entity.RenderingIterator;
import io.xol.chunkstories.api.world.World;

public class StaticVehicleEntity extends EntityBase implements EntityVoxel, EntityRenderable {

	public StaticVehicleEntity(EntityType entityType, World world, double x, double y, double z) {
		super(entityType, world, x, y, z);
	}

	@Override
	public EntityRenderer<? extends EntityRenderable> getEntityRenderer() {
		return new EntityRenderer<StaticVehicleEntity>() {

			@Override
			public int renderEntities(RenderingInterface renderingInterface,
					RenderingIterator<StaticVehicleEntity> renderableEntitiesIterator) {
				
				while(renderableEntitiesIterator.hasNext()) {
					StaticVehicleEntity sve = renderableEntitiesIterator.next();
					Matrix4f matrix = new Matrix4f();
					
					Vector3d loc = sve.getLocation().add(0.0, 0.0, 0.0);
					matrix.translate((float)loc.x, (float)loc.y, (float)loc.z);
					matrix.scale(0.5f);
					matrix.rotate((float)Math.PI / 2f, 0, 1, 0);
					matrix.translate(-2f, 0, 4.5f);
					
					renderingInterface.setObjectMatrix(matrix);
					renderingInterface.bindAlbedoTexture(renderingInterface.textures().getTexture("./models/vehicles/uaz/uaz_tire_blown.png"));
					renderingInterface.meshes().getRenderableMeshByName("./models/vehicles/uaz/uaz_tire_blown.obj").render(renderingInterface);
				}
				
				
				return 0;
			}

			@Override
			public void freeRessources() {
				// TODO Auto-generated method stub
				
			}
			
		};
	}

	
}
