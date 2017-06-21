package io.xol.dogez.mods.items;

import io.xol.chunkstories.api.Location;
import io.xol.chunkstories.api.entity.interfaces.EntityRotateable;
import io.xol.chunkstories.api.item.Item;
import io.xol.chunkstories.api.item.ItemType;
import io.xol.chunkstories.api.item.inventory.ItemPile;
import io.xol.chunkstories.api.item.renderer.ItemRenderer;
import io.xol.chunkstories.api.rendering.RenderingInterface;
import io.xol.chunkstories.api.rendering.lightning.SpotLight;
import io.xol.chunkstories.api.world.World;
import io.xol.chunkstories.item.renderer.ObjViewModelRenderer;
import io.xol.chunkstories.api.math.Matrix4f;
import io.xol.chunkstories.api.math.vector.sp.Vector3fm;
import io.xol.chunkstories.api.math.vector.sp.Vector4fm;

//(c) 2015-2016 XolioWare Interactive
//http://chunkstories.xyz
//http://xol.io

public class DZTorch extends Item{

	public DZTorch(ItemType type) {
		super(type);
	}
	
	public ItemRenderer getCustomItemRenderer(ItemRenderer fallbackRenderer)
	{
		return new TorchItemRenderer(fallbackRenderer);
	}

	class TorchItemRenderer extends ObjViewModelRenderer{

		public TorchItemRenderer(ItemRenderer fallbackRenderer) {
			super(DZTorch.this, fallbackRenderer, "./models/misc/torch/torch.obj", "./models/misc/torch/torchOn.png", "./textures/normalnormal.png", "./models/misc/torch/material.png");
		}
		
		@Override
		public void renderItemInWorld(RenderingInterface context, ItemPile pile, World world, Location location, Matrix4f handTransformation)
		{
			super.renderItemInWorld(context, pile, world, location, handTransformation);
			//Has it got an user ?
			if(pile.getInventory() != null)
			{
				if(pile.getInventory().getHolder() != null)
				{
					if(pile.getInventory().getHolder() instanceof EntityRotateable)
					{
						Vector4fm vec4 = new Vector4fm(0.0, 0.0, 0.0, 1.0);
						Matrix4f.transform(handTransformation, vec4, vec4);
						
						Vector3fm vec3 = new Vector3fm(vec4.getX(), vec4.getY(), vec4.getZ());
						
						context.getLightsRenderer().queueLight(new SpotLight(new Vector3fm(1f, 1f, 0.9f).scale(1.5f), vec3, 45f, 35f, ((EntityRotateable) pile.getInventory().getHolder()).getDirectionLookingAt().castToSinglePrecision()));
						context.getLightsRenderer().queueLight(new SpotLight(new Vector3fm(1f, 1f, 0.9f).scale(0.5f), vec3, 95f, 35f, ((EntityRotateable) pile.getInventory().getHolder()).getDirectionLookingAt().castToSinglePrecision()));
					}
				}
			}
		}
	}
}
