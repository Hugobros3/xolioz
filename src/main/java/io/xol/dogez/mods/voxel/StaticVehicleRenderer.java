package io.xol.dogez.mods.voxel;

import org.joml.Matrix4f;
import org.joml.Vector3d;
import org.joml.Vector3f;

import io.xol.chunkstories.api.rendering.RenderingInterface;
import io.xol.chunkstories.api.rendering.lightning.Light;
import io.xol.chunkstories.api.rendering.voxel.VoxelDynamicRenderer;
import io.xol.chunkstories.api.rendering.voxel.VoxelRenderer;
import io.xol.chunkstories.api.util.IterableIterator;
import io.xol.chunkstories.api.voxel.components.VoxelComponent;
import io.xol.chunkstories.api.world.chunk.Chunk.ChunkCell;

public class StaticVehicleRenderer implements VoxelDynamicRenderer {

	final StaticVehicleVoxel vehicleType;
	final VoxelRenderer ogRenderer;

	public StaticVehicleRenderer(StaticVehicleVoxel vehicleType, VoxelRenderer ogRenderer) {
		this.vehicleType = vehicleType;
		this.ogRenderer = ogRenderer;
	}

	@Override
	public void renderVoxels(RenderingInterface renderingInterface, IterableIterator<ChunkCell> voxelsOfThisType) {
		renderingInterface.bindNormalTexture(renderingInterface.textures().getTexture("./textures/normalnormal.png"));
		renderingInterface.bindMaterialTexture(renderingInterface.textures().getTexture("./textures/defaultmaterial.png"));

		for (ChunkCell context : voxelsOfThisType) {
			if (context.getMetaData() != 0)
				continue;

			// TODO Incorrect: we should tick using the actual world ticks, not rendering
			VoxelComponent component = context.components().get("renderer_info");
			if (component != null)
				((StaticVehicleRendererComponent) component).tick();

			Matrix4f matrix = new Matrix4f();

			Vector3d loc = context.getLocation().add(0.0, 0.0, 0.0);
			matrix.translate((float) loc.x, (float) loc.y, (float) loc.z);
			// matrix.scale(0.5f);
			matrix.rotate((float) Math.PI / 2f * vehicleType.rotate / 90f, 0, 1, 0);
			matrix.translate(vehicleType.translate);

			renderingInterface.setObjectMatrix(matrix);

			renderingInterface.textures().getTexture(vehicleType.diffuseTexture).setMipMapping(false);
			renderingInterface.bindAlbedoTexture(renderingInterface.textures().getTexture(vehicleType.diffuseTexture));
			renderingInterface.meshes().getRenderableMeshByName(vehicleType.model).render(renderingInterface);

			if (vehicleType.isBurning)
				renderingInterface.getLightsRenderer()
						.queueLight(new Light(new Vector3f(1.0f, 1.0f, 0.5f),
								new Vector3f((float) (loc.x + vehicleType.burnZoneStart.x + vehicleType.burnZoneSize.x / 2f),
										(float) (loc.y + vehicleType.burnZoneStart.y + vehicleType.burnZoneSize.y / 2f),
										(float) (loc.z + vehicleType.burnZoneStart.z + vehicleType.burnZoneSize.z / 2f)),
								15f));
		}
	}

}
