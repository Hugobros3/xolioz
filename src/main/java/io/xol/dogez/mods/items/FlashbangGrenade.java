package io.xol.dogez.mods.items;

import org.joml.Matrix4f;
import org.joml.Vector3d;
import org.joml.Vector3f;

import io.xol.chunkstories.api.Location;
import io.xol.chunkstories.api.entity.Controller;
import io.xol.chunkstories.api.entity.Entity;
import io.xol.chunkstories.api.entity.interfaces.EntityCreative;
import io.xol.chunkstories.api.input.Input;
import io.xol.chunkstories.api.item.Item;
import io.xol.chunkstories.api.item.ItemDefinition;
import io.xol.chunkstories.api.item.inventory.Inventory;
import io.xol.chunkstories.api.item.inventory.ItemPile;
import io.xol.chunkstories.api.math.Math2;
import io.xol.chunkstories.api.rendering.RenderingInterface;
import io.xol.chunkstories.api.rendering.item.ItemRenderer;
import io.xol.chunkstories.api.world.World;
import io.xol.chunkstories.api.world.WorldMaster;
import io.xol.chunkstories.core.entity.EntityPlayer;
import io.xol.chunkstories.core.item.renderer.ObjViewModelRenderer;
import io.xol.dogez.mods.entities.EntityThrownFlashbangGrenade;

//(c) 2015-2016 XolioWare Interactive
//http://chunkstories.xyz
//http://xol.io

public class FlashbangGrenade extends Item{

	public FlashbangGrenade(ItemDefinition type) {
		super(type);
	}

	@Override
	public ItemRenderer getCustomItemRenderer(ItemRenderer fallbackRenderer)
	{
		return new SmokeGrenadeItemRenderer(fallbackRenderer);
	}
	
	class SmokeGrenadeItemRenderer extends ObjViewModelRenderer{

		public SmokeGrenadeItemRenderer(ItemRenderer fallbackRenderer) {
			super(FlashbangGrenade.this, fallbackRenderer, "./models/weapon/flashbang_grenade/flashbang_grenade.obj", "./models/weapon/flashbang_grenade/flashbang_grenade_albedo.png", "./textures/normalnormal.png", "./textures/defaultmaterial.png");
		}
		
		@Override
		public void renderItemInWorld(RenderingInterface context, ItemPile pile, World world, Location location, Matrix4f handTransformation)
		{
			//handTransformation.rotate((float) (Math.PI / 2f), new Vector3fm(1, 0, 0));
			handTransformation.translate(new Vector3f(0.1f, 0.0f, 0.0f));
			super.renderItemInWorld(context, pile, world, location, handTransformation);
		}
	}
	
	public boolean onControllerInput(Entity owner, ItemPile itemPile, Input input, Controller controller)
	{
		if(owner != null && owner instanceof EntityPlayer && controller != null && input.getName().equals("mouse.right") && owner.getWorld() instanceof WorldMaster)
		{
			//Throw a grenade xDDDDDD
			Location pos = owner.getLocation();
			Location throwLocation = new Location(pos.getWorld(), pos.x(), pos.y() + ((EntityPlayer)owner).eyePosition, pos.z());
			Vector3d throwForce = new Vector3d(((EntityPlayer)owner).getDirectionLookingAt()).mul(0.2 - Math2.clampd(((EntityPlayer)owner).getEntityRotationComponent().getVerticalRotation(), -45, 20) / 45f * 0.3f);
			
			throwForce.add(((EntityPlayer)owner).getVelocityComponent().getVelocity());
			
			EntityThrownFlashbangGrenade grenade = (EntityThrownFlashbangGrenade) this.getDefinition().store().parent().entities().getEntityTypeByName("flash_grenade").create(throwLocation);
			grenade.setLocation(throwLocation);
			
			//EntityThrownFlashbangGrenade grenade = new EntityThrownFlashbangGrenade(pos.getWorld(), throwLocation.getX(), throwLocation.getY(), throwLocation.getZ());
			grenade.velocityComponent.setVelocity(throwForce);
			
			pos.getWorld().addEntity(grenade);
			
			if(owner instanceof EntityCreative && ((EntityCreative) owner).isCreativeMode())
			{
				//Ignore
			}
			else
			{
				Inventory inv = itemPile.getInventory();
				if(itemPile.getAmount() == 0 && inv != null)
					inv.setItemPileAt(itemPile.getX(), itemPile.getY(), null);
				else if(itemPile.getAmount() > 0)
					itemPile.setAmount(itemPile.getAmount() - 1);
			}
		}
		return false;
	}
}
