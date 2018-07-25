package io.xol.z.mod.items

import org.joml.Matrix4f
import org.joml.Vector3dc
import org.joml.Vector3f
import org.joml.Vector4f

import io.xol.chunkstories.api.Location
import io.xol.chunkstories.api.entity.Entity
import io.xol.chunkstories.api.entity.components.EntityRotation
import io.xol.chunkstories.api.item.Item
import io.xol.chunkstories.api.item.ItemDefinition
import io.xol.chunkstories.api.item.inventory.InventoryHolder
import io.xol.chunkstories.api.item.inventory.ItemPile
import io.xol.chunkstories.api.rendering.RenderingInterface
import io.xol.chunkstories.api.rendering.item.ItemRenderer
import io.xol.chunkstories.api.rendering.lightning.SpotLight
import io.xol.chunkstories.api.world.World
import io.xol.chunkstories.core.item.renderer.ItemModelRenderer

class ItemFlashlight(type: ItemDefinition) : Item(type) {

    override fun getCustomItemRenderer(fallbackRenderer: ItemRenderer): ItemRenderer {
        return TorchItemRenderer(fallbackRenderer)
    }

    internal inner class TorchItemRenderer(fallbackRenderer: ItemRenderer) : ItemModelRenderer(this@ItemFlashlight, fallbackRenderer, "./models/misc/torch/torch.obj", "./models/misc/torch/torchOn.png", "./textures/normalnormal.png", "./models/misc/torch/material.png") {

        override fun renderItemInWorld(context: RenderingInterface, pile: ItemPile?, world: World?, location: Location?, handTransformation: Matrix4f) {
            super.renderItemInWorld(context, pile, world, location, handTransformation)
            // Has it got an user ?
            if (pile!!.inventory != null) {
                val holder = pile.inventory!!.holder
                if (holder != null && holder is Entity) {
                    val entity = holder as Entity?
                    entity!!.components.with(EntityRotation::class.java) { er ->
                        val vec4 = Vector4f(0.5f, 0.0f, 0.0f, 1.0f)

                        handTransformation.transform(vec4)

                        val vec3 = Vector3f(vec4.x(), vec4.y() + 0.00f, vec4.z())

                        val dirD = er.directionLookingAt
                        val dir = Vector3f(dirD.x().toFloat(), dirD.y().toFloat(), dirD.z().toFloat())

                        context.lightsRenderer.queueLight(SpotLight(Vector3f(1f, 1f, 0.9f).mul(5.55f), vec3, 1f, 35f, dir))
                        // context.getLightsRenderer().queueLight(new SpotLight(new Vector3f(1f, 1f,
                        // 0.9f).mul(0.25f), vec3, 95f, 35f, dir));
                    }
                }
            }
        }
    }
}
