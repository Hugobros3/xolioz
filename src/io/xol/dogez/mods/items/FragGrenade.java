package io.xol.dogez.mods.items;

import io.xol.chunkstories.api.Location;
import io.xol.chunkstories.api.entity.Controller;
import io.xol.chunkstories.api.entity.Entity;
import io.xol.chunkstories.api.entity.interfaces.EntityCreative;
import io.xol.chunkstories.api.input.Input;
import io.xol.chunkstories.api.item.Item;
import io.xol.chunkstories.api.item.ItemType;
import io.xol.chunkstories.api.item.inventory.Inventory;
import io.xol.chunkstories.api.item.inventory.ItemPile;
import io.xol.chunkstories.api.item.renderer.ItemRenderer;
import io.xol.chunkstories.api.rendering.RenderingInterface;
import io.xol.chunkstories.api.world.World;
import io.xol.chunkstories.api.world.WorldMaster;
import io.xol.chunkstories.core.entity.EntityPlayer;
import io.xol.chunkstories.item.renderer.ObjViewModelRenderer;
import io.xol.dogez.mods.entities.EntityThrownFragGrenade;
import io.xol.chunkstories.api.math.Math2;
import io.xol.chunkstories.api.math.Matrix4f;
import io.xol.chunkstories.api.math.vector.dp.Vector3dm;
import io.xol.chunkstories.api.math.vector.sp.Vector3fm;

//(c) 2015-2016 XolioWare Interactive
//http://chunkstories.xyz
//http://xol.io

public class FragGrenade extends Item{

	public FragGrenade(ItemType type) {
		super(type);
	}
	
	@Override
	public ItemRenderer getCustomItemRenderer(ItemRenderer fallbackRenderer)
	{
		return new FragGrenadeItemRenderer(fallbackRenderer);
	}

	class FragGrenadeItemRenderer extends ObjViewModelRenderer{

		public FragGrenadeItemRenderer(ItemRenderer fallbackRenderer) {
			super(FragGrenade.this, fallbackRenderer, "./models/weapon/frag_grenade/frag_grenade.obj", "./models/weapon/frag_grenade/frag_grenade_albedo.png", "./textures/normalnormal.png", "./textures/defaultmaterial.png");
		}
		
		@Override
		public void renderItemInWorld(RenderingInterface context, ItemPile pile, World world, Location location, Matrix4f handTransformation)
		{
			//handTransformation.rotate((float) (Math.PI / 2f), new Vector3fm(1, 0, 0));
			handTransformation.translate(new Vector3fm(0.1f, 0.0f, 0.0f));
			super.renderItemInWorld(context, pile, world, location, handTransformation);
		}
	}
	
	public boolean onControllerInput(Entity owner, ItemPile itemPile, Input input, Controller controller)
	{
		if(owner != null && owner instanceof EntityPlayer && controller != null && input.getName().equals("mouse.right") && owner.getWorld() instanceof WorldMaster)
		{
			//Throw a grenade xDDDDDD
			Location pos = owner.getLocation();
			Vector3dm throwLocation = new Vector3dm(pos.getX(), pos.getY() + ((EntityPlayer)owner).eyePosition, pos.getZ());
			Vector3dm throwForce = ((EntityPlayer)owner).getDirectionLookingAt().scale(0.2 - Math2.clampd(((EntityPlayer)owner).getEntityRotationComponent().getVerticalRotation(), -45, 20) / 45f * 0.3f);
			
			throwForce.add(((EntityPlayer)owner).getVelocityComponent().getVelocity());
			
			EntityThrownFragGrenade grenade = (EntityThrownFragGrenade) this.getType().store().parent().entities().getEntityTypeByName("frag_grenade").create(pos.getWorld());
			grenade.getEntityComponentPosition().setPosition(throwLocation);
			
					//new EntityThrownFragGrenade(pos.getWorld(), throwLocation.getX(), throwLocation.getY(), throwLocation.getZ());
			grenade.getVelocityComponent().setVelocity(throwForce);
			
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
