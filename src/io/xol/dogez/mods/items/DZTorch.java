package io.xol.dogez.mods.items;

import io.xol.chunkstories.api.Location;
import io.xol.chunkstories.api.entity.interfaces.EntityRotateable;
import io.xol.chunkstories.api.item.Item;
import io.xol.chunkstories.api.item.ItemType;
import io.xol.chunkstories.api.rendering.RenderingInterface;
import io.xol.chunkstories.api.rendering.lightning.SpotLight;
import io.xol.chunkstories.api.world.World;
import io.xol.chunkstories.item.ItemPile;
import io.xol.chunkstories.item.renderer.DefaultItemRenderer;
import io.xol.engine.math.lalgb.Matrix4f;
import io.xol.engine.math.lalgb.Vector3f;

//(c) 2015-2016 XolioWare Interactive
//http://chunkstories.xyz
//http://xol.io

public class DZTorch extends Item{

	public DZTorch(ItemType type) {
		super(type);
		itemRenderer = new TorchItemRenderer(this);
	}

	class TorchItemRenderer extends DefaultItemRenderer{

		public TorchItemRenderer(Item item) {
			super(item);
		}

		
		@Override
		public void renderItemInWorld(RenderingInterface context, ItemPile pile, World world, Location location, Matrix4f handTransformation)
		{
			super.renderItemInWorld(context, pile, world, location, handTransformation);
			
			//Has it got an user ?
			if(pile.inventory != null)
			{
				if(pile.inventory.holder != null)
				{
					if(pile.inventory.holder instanceof EntityRotateable)
					{
						context.addLight(new SpotLight(new Vector3f(1f, 1f, 0.9f).scale(2.0f), location.castToSimplePrecision(), 45f, 35f, ((EntityRotateable) pile.inventory.holder).getDirectionLookingAt().castToSimplePrecision()));
					}
				}
			}
		}
	}
}
