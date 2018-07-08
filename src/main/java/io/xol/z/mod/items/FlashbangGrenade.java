package io.xol.z.mod.items;

import org.joml.Matrix4f;
import org.joml.Vector3f;

import io.xol.chunkstories.api.Location;
import io.xol.chunkstories.api.item.ItemDefinition;
import io.xol.chunkstories.api.item.inventory.ItemPile;
import io.xol.chunkstories.api.rendering.RenderingInterface;
import io.xol.chunkstories.api.rendering.item.ItemRenderer;
import io.xol.chunkstories.api.world.World;
import io.xol.chunkstories.core.item.renderer.ItemModelRenderer;

public class FlashbangGrenade extends ItemThrowableGrenade {

	public FlashbangGrenade(ItemDefinition type) {
		super(type, "flash_grenade");
	}

	@Override
	public ItemRenderer getCustomItemRenderer(ItemRenderer fallbackRenderer) {
		return new SmokeGrenadeItemRenderer(fallbackRenderer);
	}

	class SmokeGrenadeItemRenderer extends ItemModelRenderer {

		public SmokeGrenadeItemRenderer(ItemRenderer fallbackRenderer) {
			super(FlashbangGrenade.this, fallbackRenderer, "./models/weapon/flashbang_grenade/flashbang_grenade.obj",
					"./models/weapon/flashbang_grenade/flashbang_grenade_albedo.png", "./textures/normalnormal.png", "./textures/defaultmaterial.png");
		}

		@Override
		public void renderItemInWorld(RenderingInterface context, ItemPile pile, World world, Location location, Matrix4f handTransformation) {
			handTransformation.translate(new Vector3f(0.1f, 0.0f, 0.0f));
			super.renderItemInWorld(context, pile, world, location, handTransformation);
		}
	}
}