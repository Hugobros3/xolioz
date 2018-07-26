package io.xol.z.mod.voxels;

import io.xol.chunkstories.api.entity.traits.serializable.TraitController;
import org.joml.Vector3d;
import org.joml.Vector3f;

import io.xol.chunkstories.api.client.ClientContent;
import io.xol.chunkstories.api.entity.Controller;
import io.xol.chunkstories.api.entity.Entity;
import io.xol.chunkstories.api.input.Input;
import io.xol.chunkstories.api.player.Player;
import io.xol.chunkstories.api.rendering.voxel.VoxelRenderer;
import io.xol.chunkstories.api.voxel.VoxelDefinition;
import io.xol.chunkstories.api.voxel.components.VoxelComponent;
import io.xol.chunkstories.api.voxel.components.VoxelInventoryComponent;
import io.xol.chunkstories.api.world.WorldMaster;
import io.xol.chunkstories.api.world.cell.CellData;
import io.xol.chunkstories.api.world.chunk.Chunk.ChunkCell;
import io.xol.chunkstories.api.world.chunk.Chunk.FreshChunkCell;
import io.xol.chunkstories.core.voxel.BigVoxel;

public class StaticVehicleVoxel extends BigVoxel {
	
	public final float rotate;
	public final Vector3f translate;
	public final boolean isBurning;
	public final boolean darkSmoke;
	public final String model;
	public final String diffuseTexture;
	
	public final Vector3d size;
	
	public final Vector3d burnZoneStart;
	public final Vector3d burnZoneSize;
	
	public final String lootCategoryName;
	public final int lootAmountMin;
	public final int lootAmountMax;
	
	public final StaticVehicleRenderer renderer;
	
	public StaticVehicleVoxel(VoxelDefinition type) {
		super(type);
		
		this.rotate = Float.parseFloat(type.resolveProperty("rotate", "0"));
		String ts = type.resolveProperty("translate", "0 0 0");
		String[] tss = ts.split(" ");
		
		this.translate = new Vector3f(Float.parseFloat(tss[0]), Float.parseFloat(tss[1]), Float.parseFloat(tss[2]));
		this.isBurning = type.resolveProperty("isBurning", "false").equals("true");
		this.darkSmoke = type.resolveProperty("darkSmoke", "false").equals("true");
		this.model = type.resolveProperty("model", "error");
		this.diffuseTexture = type.resolveProperty("diffuseTexture", "error");

		ts = type.resolveProperty("size", "1 1 1");
		tss = ts.split(" ");
		this.size = new Vector3d(Float.parseFloat(tss[0]), Float.parseFloat(tss[1]), Float.parseFloat(tss[2]));
		
		ts = type.resolveProperty("burnZoneStart", "1 1 1");
		tss = ts.split(" ");
		this.burnZoneStart = new Vector3d(Float.parseFloat(tss[0]), Float.parseFloat(tss[1]), Float.parseFloat(tss[2]));
		
		ts = type.resolveProperty("burnZoneSize", "1 1 1");
		tss = ts.split(" ");
		this.burnZoneSize = new Vector3d(Float.parseFloat(tss[0]), Float.parseFloat(tss[1]), Float.parseFloat(tss[2]));
		
		lootCategoryName = type.resolveProperty("lootCategory", "<name>");
		lootAmountMin = Integer.parseInt(type.resolveProperty("lootAmountMin", "1"));
		lootAmountMax = Integer.parseInt(type.resolveProperty("lootAmountMax", "<lootAmountMin>"));
		
		if(type.store().parent() instanceof ClientContent)
			renderer = new StaticVehicleRenderer(this, this.voxelRenderer);
		else
			renderer = null;
	}
	
	@Override
	public VoxelRenderer getVoxelRenderer(CellData info) {
		if(renderer != null)
			return renderer;
		return super.getVoxelRenderer(info);
	}

	@Override
	public void whenPlaced(FreshChunkCell cell) {
		//Only have components where it's actually relevant
		if(cell.getMetaData() == 0) {
			//TODO configure the size of the inventory depending on the entity config
			cell.registerComponent("inventory", new VoxelInventoryComponent(cell.components(), 10, 4));
			cell.registerComponent("renderer_info", new StaticVehicleRendererComponent(this, cell.components(), 60L));
		}
		super.whenPlaced(cell);
	}

	@Override
	public boolean handleInteraction(Entity entity, ChunkCell context, Input input) {
		if(input.getName().equals("mouse.right") && context.getWorld() instanceof WorldMaster) {

			int x = context.getX();
			int y = context.getY();
			int z = context.getZ();
			
			entity.traits.with(TraitController.class, ec -> {
				Controller c = ec.getController();
				
				if(c instanceof Player) {
					Player p = (Player)c;
					
					//Backpedal to find the root block
					int meta = context.getMetaData();
					
					int ap = (meta >> xShift) & xMask;
					int bp = (meta >> yShift) & yMask;
					int cp = (meta >> zShift) & zMask;
					
					int startX = x - ap;
					int startY = y - bp;
					int startZ = z - cp;
					
					CellData worldContext = context.getWorld().peekSafely(startX, startY, startZ);
					
					//Check the chunk holding the origin block is actually loaded
					if(worldContext instanceof ChunkCell) {
						VoxelComponent component = ((ChunkCell) worldContext).components().get("inventory");
						if(component != null) {
							VoxelInventoryComponent invComponent = (VoxelInventoryComponent)component;
							p.openInventory(invComponent.getInventory());
						}
					}
					
				}
			});
		}
		return false;
	}
}
