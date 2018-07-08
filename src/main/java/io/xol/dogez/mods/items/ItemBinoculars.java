package io.xol.dogez.mods.items;

import org.joml.Vector4f;

import io.xol.chunkstories.api.entity.Controller;
import io.xol.chunkstories.api.entity.Entity;
import io.xol.chunkstories.api.entity.components.EntityController;
import io.xol.chunkstories.api.item.Item;
import io.xol.chunkstories.api.item.ItemDefinition;
import io.xol.chunkstories.api.item.interfaces.ItemOverlay;
import io.xol.chunkstories.api.item.interfaces.ItemZoom;
import io.xol.chunkstories.api.item.inventory.ItemPile;
import io.xol.chunkstories.api.client.LocalPlayer;
import io.xol.chunkstories.api.rendering.RenderingInterface;

public class ItemBinoculars extends Item implements ItemZoom, ItemOverlay {
	private boolean isScoped = false;

	public ItemBinoculars(ItemDefinition type) {
		super(type);
	}

	@Override
	public void tickInHand(Entity owner, ItemPile itemPile) {

		owner.components.with(EntityController.class, ec -> {
			Controller controller = ec.getController();

			if (controller instanceof LocalPlayer) {
				isScoped = controller.getInputsManager().getInputByName("mouse.right").isPressed();
			}
		});
	}

	@Override
	public float getZoomFactor() {

		return isScoped ? 4f : 1f;
	}

	@Override
	public void drawItemOverlay(RenderingInterface renderingInterface, ItemPile itemPile) {
		if (!isScoped)
			return;

		int min = Math.min(renderingInterface.getWindow().getWidth(), renderingInterface.getWindow().getHeight());
		int max = Math.max(renderingInterface.getWindow().getWidth(), renderingInterface.getWindow().getHeight());

		int bandwidth = (max - min) / 2;
		int x = 0;

		renderingInterface.getGuiRenderer().drawBoxWindowsSpace(x, 0, x += bandwidth, renderingInterface.getWindow().getHeight(), 0, 0, 0, 0, null, false,
				false, new Vector4f(0.0f, 0.0f, 0.0f, 1.0f));
		renderingInterface.getGuiRenderer().drawBoxWindowsSpace(x, 0, x += min, renderingInterface.getWindow().getHeight(), 0.0f, 1, 1, 0.0f,
				renderingInterface.textures().getTexture("./textures/gui/binoculars.png"), false, false, null);
		renderingInterface.getGuiRenderer().drawBoxWindowsSpace(x, 0, x += bandwidth, renderingInterface.getWindow().getHeight(), 0, 0, 0, 0, null, false,
				false, new Vector4f(0.0f, 0.0f, 0.0f, 1.0f));

	}

}
