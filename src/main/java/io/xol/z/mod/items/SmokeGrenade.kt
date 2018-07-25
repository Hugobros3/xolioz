package io.xol.z.mod.items

import org.joml.Matrix4f
import org.joml.Vector3f

import io.xol.chunkstories.api.Location
import io.xol.chunkstories.api.item.ItemDefinition
import io.xol.chunkstories.api.item.inventory.ItemPile
import io.xol.chunkstories.api.rendering.RenderingInterface
import io.xol.chunkstories.api.rendering.item.ItemRenderer
import io.xol.chunkstories.api.world.World
import io.xol.chunkstories.core.item.renderer.ItemModelRenderer

class SmokeGrenade(type: ItemDefinition) : ItemThrowableGrenade(type, "smoke_grenade") {

    override fun getCustomItemRenderer(fallbackRenderer: ItemRenderer): ItemRenderer {
        return SmokeGrenadeItemRenderer(fallbackRenderer)
    }

    internal inner class SmokeGrenadeItemRenderer(fallbackRenderer: ItemRenderer) : ItemModelRenderer(this@SmokeGrenade, fallbackRenderer, "./models/weapon/smoke_grenade/smoke_grenade.obj", "./models/weapon/smoke_grenade/smoke_grenade_albedo.png", "./textures/normalnormal.png", "./textures/defaultmaterial.png") {

        override fun renderItemInWorld(context: RenderingInterface, pile: ItemPile?, world: World?, location: Location?, handTransformation: Matrix4f) {
            handTransformation.rotate((Math.PI / 2f).toFloat(), Vector3f(1f, 0f, 0f))
            handTransformation.translate(Vector3f(0.1f, 0.0f, 0.0f))
            super.renderItemInWorld(context, pile, world, location, handTransformation)
        }
    }
}
