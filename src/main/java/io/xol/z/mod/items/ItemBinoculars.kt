package io.xol.z.mod.items

import org.joml.Vector4f

import io.xol.chunkstories.api.entity.Entity
import io.xol.chunkstories.api.item.Item
import io.xol.chunkstories.api.item.ItemDefinition
import io.xol.chunkstories.api.item.interfaces.ItemOverlay
import io.xol.chunkstories.api.item.interfaces.ItemZoom
import io.xol.chunkstories.api.item.inventory.ItemPile
import io.xol.chunkstories.api.client.LocalPlayer
import io.xol.chunkstories.api.entity.traits.serializable.TraitController
import io.xol.chunkstories.api.rendering.RenderingInterface

class ItemBinoculars(type: ItemDefinition) : Item(type), ItemZoom, ItemOverlay {
    private var isScoped = false

    override fun tickInHand(owner: Entity?, itemPile: ItemPile?) {

        owner!!.traits.with(TraitController::class.java) { ec ->
            val controller = ec.controller

            if (controller is LocalPlayer) {
                isScoped = controller.inputsManager.getInputByName("mouse.right").isPressed
            }
        }
    }

    override fun getZoomFactor(): Float {

        return if (isScoped) 4f else 1f
    }

    override fun drawItemOverlay(renderingInterface: RenderingInterface, itemPile: ItemPile) {
        if (!isScoped)
            return

        val min = Math.min(renderingInterface.window.width, renderingInterface.window.height)
        val max = Math.max(renderingInterface.window.width, renderingInterface.window.height)

        val bandwidth = (max - min) / 2
        var x = 0.0F;

        renderingInterface.guiRenderer.drawBoxWindowsSpace(x.toFloat(), 0f, x + bandwidth, renderingInterface.window.height.toFloat(), 0f, 0f, 0f, 0f, null, false,
                false, Vector4f(0.0f, 0.0f, 0.0f, 1.0f))

        x += bandwidth;
        renderingInterface.guiRenderer.drawBoxWindowsSpace(x.toFloat(), 0f, (x + min).toFloat(), renderingInterface.window.height.toFloat(), 0.0f, 1f, 1f, 0.0f,
                renderingInterface.textures().getTexture("./textures/gui/binoculars.png"), false, false, null)

        x += min;
        renderingInterface.guiRenderer.drawBoxWindowsSpace(x.toFloat(), 0f, (x + bandwidth).toFloat(), renderingInterface.window.height.toFloat(), 0f, 0f, 0f, 0f, null, false,
                false, Vector4f(0.0f, 0.0f, 0.0f, 1.0f))

    }

}
