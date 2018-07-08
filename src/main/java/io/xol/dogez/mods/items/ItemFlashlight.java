package io.xol.dogez.mods.items;

import org.joml.Matrix4f;
import org.joml.Vector3dc;
import org.joml.Vector3f;
import org.joml.Vector4f;

import io.xol.chunkstories.api.Location;
import io.xol.chunkstories.api.entity.Entity;
import io.xol.chunkstories.api.entity.components.EntityRotation;
import io.xol.chunkstories.api.item.Item;
import io.xol.chunkstories.api.item.ItemDefinition;
import io.xol.chunkstories.api.item.inventory.InventoryHolder;
import io.xol.chunkstories.api.item.inventory.ItemPile;
import io.xol.chunkstories.api.rendering.RenderingInterface;
import io.xol.chunkstories.api.rendering.item.ItemRenderer;
import io.xol.chunkstories.api.rendering.lightning.SpotLight;
import io.xol.chunkstories.api.world.World;
import io.xol.chunkstories.core.item.renderer.ItemModelRenderer;

public class ItemFlashlight extends Item {

	public ItemFlashlight(ItemDefinition type) {
		super(type);
	}

	public ItemRenderer getCustomItemRenderer(ItemRenderer fallbackRenderer) {
		return new TorchItemRenderer(fallbackRenderer);
	}

	class TorchItemRenderer extends ItemModelRenderer {

		public TorchItemRenderer(ItemRenderer fallbackRenderer) {
			super(ItemFlashlight.this, fallbackRenderer, "./models/misc/torch/torch.obj", "./models/misc/torch/torchOn.png", "./textures/normalnormal.png",
					"./models/misc/torch/material.png");
		}

		@Override
		public void renderItemInWorld(RenderingInterface context, ItemPile pile, World world, Location location, Matrix4f handTransformation) {
			super.renderItemInWorld(context, pile, world, location, handTransformation);
			// Has it got an user ?
			if (pile.getInventory() != null) {
				InventoryHolder holder = pile.getInventory().getHolder();
				if (holder != null && holder instanceof Entity) {
					Entity entity = (Entity) holder;
					entity.components.with(EntityRotation.class, er -> {
						Vector4f vec4 = new Vector4f(0.5f, 0.0f, 0.0f, 1.0f);

						handTransformation.transform(vec4);

						Vector3f vec3 = new Vector3f(vec4.x(), vec4.y() + 0.00f, vec4.z());

						Vector3dc dirD = er.getDirectionLookingAt();
						Vector3f dir = new Vector3f((float) dirD.x(), (float) dirD.y(), (float) dirD.z());

						context.getLightsRenderer().queueLight(new SpotLight(new Vector3f(1f, 1f, 0.9f).mul(5.55f), vec3, 1f, 35f, dir));
						// context.getLightsRenderer().queueLight(new SpotLight(new Vector3f(1f, 1f,
						// 0.9f).mul(0.25f), vec3, 95f, 35f, dir));
					});
				}
			}
		}
	}
}
