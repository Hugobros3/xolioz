//
// This file is a part of the XolioZ Mod for Chunk Stories
// Check out README.md for more information
// Website: https://chunkstories.xyz
// Github: https://github.com/Hugobros3/xolioz
//

package io.xol.z.mod.voxels;

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
	public void renderVoxels(RenderingInterface renderer, IterableIterator<ChunkCell> voxelsOfThisType) {
		renderer.useShader("entities");
		renderer.bindNormalTexture(renderer.textures().getTexture("./textures/normalnormal.png"));
		renderer.bindMaterialTexture(renderer.textures().getTexture("./textures/defaultmaterial.png"));

		for (ChunkCell cell : voxelsOfThisType) {
			if (cell.getMetaData() != 0)
				continue;

			// TODO Incorrect: we should tick using the actual world ticks, not rendering
			VoxelComponent component = cell.components().get("renderer_info");
			if (component != null)
				((StaticVehicleRendererComponent) component).tick();

			Matrix4f matrix = new Matrix4f();

			Vector3d loc = cell.getLocation().add(0.0, 0.0, 0.0);
			matrix.translate((float) loc.x, (float) loc.y, (float) loc.z);
			// matrix.scale(0.5f);
			matrix.rotate((float) Math.PI / 2f * vehicleType.rotate / 90f, 0, 1, 0);
			matrix.translate(vehicleType.translate);

			renderer.setObjectMatrix(matrix);

			renderer.currentShader().setUniform2f("worldLightIn", cell.getBlocklight(), cell.getSunlight());

			renderer.textures().getTexture(vehicleType.diffuseTexture).setMipMapping(false);
			renderer.bindAlbedoTexture(renderer.textures().getTexture(vehicleType.diffuseTexture));
			renderer.meshes().getRenderableMesh(vehicleType.model).render(renderer);

			if (vehicleType.isBurning)
				renderer.getLightsRenderer()
						.queueLight(new Light(new Vector3f(1.0f, 1.0f, 0.5f),
								new Vector3f((float) (loc.x + vehicleType.burnZoneStart.x + vehicleType.burnZoneSize.x / 2f),
										(float) (loc.y + vehicleType.burnZoneStart.y + vehicleType.burnZoneSize.y / 2f),
										(float) (loc.z + vehicleType.burnZoneStart.z + vehicleType.burnZoneSize.z / 2f)),
								15f));
		}
	}

}
