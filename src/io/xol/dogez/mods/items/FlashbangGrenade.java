package io.xol.dogez.mods.items;

import io.xol.chunkstories.api.Location;
import io.xol.chunkstories.api.entity.Controller;
import io.xol.chunkstories.api.entity.Entity;
import io.xol.chunkstories.api.input.Input;
import io.xol.chunkstories.api.item.Item;
import io.xol.chunkstories.api.item.ItemPile;
import io.xol.chunkstories.api.item.ItemType;
import io.xol.chunkstories.api.rendering.RenderingInterface;
import io.xol.chunkstories.api.world.World;
import io.xol.chunkstories.api.world.WorldMaster;
import io.xol.chunkstories.core.entity.EntityPlayer;
import io.xol.chunkstories.core.item.renderers.ObjViewModelRenderer;
import io.xol.dogez.mods.entities.EntityThrownFlashbangGrenade;
import io.xol.dogez.mods.entities.EntityThrownSmokeGrenade;
import io.xol.engine.math.Math2;
import io.xol.engine.math.lalgb.Matrix4f;
import io.xol.engine.math.lalgb.vector.dp.Vector3dm;
import io.xol.engine.math.lalgb.vector.sp.Vector3fm;

//(c) 2015-2016 XolioWare Interactive
//http://chunkstories.xyz
//http://xol.io

public class FlashbangGrenade extends Item{

	public FlashbangGrenade(ItemType type) {
		super(type);
		itemRenderer = new SmokeGrenadeItemRenderer(this);
	}

	class SmokeGrenadeItemRenderer extends ObjViewModelRenderer{

		public SmokeGrenadeItemRenderer(Item item) {
			super(item, "./models/weapon/flashbang_grenade/flashbang_grenade.obj", "./models/weapon/flashbang_grenade/flashbang_grenade_albedo.png", "./textures/normalnormal.png", "./textures/defaultmaterial.png");
		}
		
		@Override
		public void renderItemInWorld(RenderingInterface context, ItemPile pile, World world, Location location, Matrix4f handTransformation)
		{
			//handTransformation.rotate((float) (Math.PI / 2f), new Vector3fm(1, 0, 0));
			handTransformation.translate(new Vector3fm(0.1f, 0.0f, 0.0f));
			super.renderItemInWorld(context, pile, world, location, handTransformation);
		}
	}
	
	public boolean handleInteraction(Entity owner, ItemPile itemPile, Input input, Controller controller)
	{
		if(owner != null && owner instanceof EntityPlayer && controller != null && input.getName().equals("mouse.right") && owner.getWorld() instanceof WorldMaster)
		{
			//Throw a grenade xDDDDDD
			Location pos = owner.getLocation();
			Vector3dm throwLocation = new Vector3dm(pos.getX(), pos.getY() + ((EntityPlayer)owner).eyePosition, pos.getZ());
			Vector3dm throwForce = ((EntityPlayer)owner).getDirectionLookingAt().scale(0.2 - Math2.clampd(((EntityPlayer)owner).getEntityRotationComponent().getVerticalRotation(), -45, 20) / 45f * 0.3f);
			
			throwForce.add(((EntityPlayer)owner).getVelocityComponent().getVelocity());
			
			EntityThrownFlashbangGrenade grenade = new EntityThrownFlashbangGrenade(pos.getWorld(), throwLocation.getX(), throwLocation.getY(), throwLocation.getZ());
			grenade.getVelocityComponent().setVelocity(throwForce);
			
			pos.getWorld().addEntity(grenade);
		}
		return false;
	}
}
