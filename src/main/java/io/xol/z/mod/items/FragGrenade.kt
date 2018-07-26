//
// This file is a part of the XolioZ Mod for Chunk Stories
// Check out README.md for more information
// Website: https://chunkstories.xyz
// Github: https://github.com/Hugobros3/xolioz
//

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

class FragGrenade(type: ItemDefinition) : ItemThrowableGrenade(type, "frag_grenade") {

	override fun getCustomItemRenderer(fallbackRenderer: ItemRenderer): ItemRenderer {
		return FragGrenadeItemRenderer(fallbackRenderer)
	}

	internal inner class FragGrenadeItemRenderer(fallbackRenderer: ItemRenderer) : ItemModelRenderer(this@FragGrenade, fallbackRenderer, "./models/weapon/frag_grenade/frag_grenade.obj", "./models/weapon/frag_grenade/frag_grenade_albedo.png", "./textures/normalnormal.png", "./textures/defaultmaterial.png") {

		override fun renderItemInWorld(context: RenderingInterface, pile: ItemPile?, world: World?, location: Location?, handTransformation: Matrix4f) {
			// handTransformation.rotate((float) (Math.PI / 2f), new Vector3fm(1, 0, 0));
			handTransformation.translate(Vector3f(0.1f, 0.0f, 0.0f))
			super.renderItemInWorld(context, pile, world, location, handTransformation)
		}
	}
}
