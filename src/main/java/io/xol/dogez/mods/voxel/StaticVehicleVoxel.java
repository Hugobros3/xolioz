package io.xol.dogez.mods.voxel;

import org.joml.Vector3d;
import org.joml.Vector3f;

import io.xol.chunkstories.api.entity.Controller;
import io.xol.chunkstories.api.entity.Entity;
import io.xol.chunkstories.api.entity.interfaces.EntityControllable;
import io.xol.chunkstories.api.events.voxel.WorldModificationCause;
import io.xol.chunkstories.api.exceptions.world.voxel.IllegalBlockModificationException;
import io.xol.chunkstories.api.input.Input;
import io.xol.chunkstories.api.item.inventory.BasicInventory;
import io.xol.chunkstories.api.player.Player;
import io.xol.chunkstories.api.voxel.VoxelCustomIcon;
import io.xol.chunkstories.api.voxel.VoxelDynamicallyRendered;
import io.xol.chunkstories.api.voxel.VoxelFormat;
import io.xol.chunkstories.api.voxel.VoxelInteractive;
import io.xol.chunkstories.api.voxel.VoxelLogic;
import io.xol.chunkstories.api.voxel.VoxelType;
import io.xol.chunkstories.api.voxel.components.VoxelComponent;
import io.xol.chunkstories.api.voxel.components.VoxelComponentDynamicRenderer;
import io.xol.chunkstories.api.voxel.components.VoxelInventoryComponent;
import io.xol.chunkstories.api.world.VoxelContext;
import io.xol.chunkstories.api.world.WorldMaster;
import io.xol.chunkstories.api.world.chunk.Chunk.ChunkVoxelContext;
import io.xol.chunkstories.core.voxel.BigVoxel;

//(c) 2015-2017 XolioWare Interactive
//http://chunkstories.xyz
//http://xol.io

public class StaticVehicleVoxel extends BigVoxel implements VoxelCustomIcon, VoxelLogic, VoxelInteractive, VoxelDynamicallyRendered {
	
	final float rotate;
	final Vector3f translate;
	final boolean isBurning;
	final boolean darkSmoke;
	final String model;
	final String diffuseTexture;
	
	final Vector3d size;
	
	final Vector3d burnZoneStart;
	final Vector3d burnZoneSize;
	
	public StaticVehicleVoxel(VoxelType type) {
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
	}
	
	@Override
	public int onPlace(ChunkVoxelContext context, int voxelData, WorldModificationCause cause) throws IllegalBlockModificationException {
		
		//TODO configure the size of the inventory depending on the entity config
		if(VoxelFormat.meta(voxelData) == 0)
			context.components().put("inventory", new VoxelInventoryComponent(context.components(), new BasicInventory(10, 4)));
		context.components().put("renderer", new StaticVehicleRendererComponent(this, context.components(), 60L));
		return super.onPlace(context, voxelData, cause);
	}

	@Override
	public void onRemove(ChunkVoxelContext context, WorldModificationCause cause) throws IllegalBlockModificationException {
		
		context.components().erase();
		super.onRemove(context, cause);
	}

	@Override
	public boolean handleInteraction(Entity entity, ChunkVoxelContext context, Input input) {
		if(input.getName().equals("mouse.right") && context.getWorld() instanceof WorldMaster) {

			int x = context.getX();
			int y = context.getY();
			int z = context.getZ();
			
			//Only actual players can open that kind of stuff
			if(entity instanceof EntityControllable) {
				EntityControllable e = (EntityControllable)entity;
				Controller c = e.getController();
				
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
					
					VoxelContext worldContext = context.getWorld().peekSafely(startX, startY, startZ);
					
					//Check the chunk holding the origin block is actually loaded
					if(worldContext instanceof ChunkVoxelContext) {
						VoxelComponent component = ((ChunkVoxelContext) worldContext).components().get("inventory");
						if(component != null) {
							VoxelInventoryComponent invComponent = (VoxelInventoryComponent)component;
							p.openInventory(invComponent.getInventory());
							return true;
						}
					}
					
				}
				
			}
		}
		return false;
	}

	@Override
	public VoxelComponentDynamicRenderer getDynamicRendererComponent(ChunkVoxelContext context) {
		return (VoxelComponentDynamicRenderer) context.components().get("renderer");
	}

}
