//
// This file is a part of the XolioZ Mod for Chunk Stories
// Check out README.md for more information
// Website: https://chunkstories.xyz
// Github: https://github.com/Hugobros3/xolioz
//

package io.xol.z.mod.items

import org.joml.Vector3d

import io.xol.chunkstories.api.Location
import io.xol.chunkstories.api.entity.Controller
import io.xol.chunkstories.api.entity.Entity
import io.xol.chunkstories.api.entity.traits.serializable.TraitCreativeMode
import io.xol.chunkstories.api.entity.traits.serializable.TraitRotation
import io.xol.chunkstories.api.entity.traits.serializable.TraitVelocity
import io.xol.chunkstories.api.input.Input
import io.xol.chunkstories.api.item.Item
import io.xol.chunkstories.api.item.ItemDefinition
import io.xol.chunkstories.api.item.inventory.ItemPile
import io.xol.chunkstories.api.math.Math2
import io.xol.chunkstories.api.world.WorldMaster
import io.xol.chunkstories.core.entity.components.EntityStance
import io.xol.z.mod.entities.EntityThrownGrenade

open class ItemThrowableGrenade(type: ItemDefinition, internal val grenadeEntityName: String) : Item(type) {

	override fun onControllerInput(entity: Entity?, itemPile: ItemPile?, input: Input?, controller: Controller?): Boolean {
		if (entity != null && input!!.name == "mouse.right" && entity.getWorld() is WorldMaster) {
			val pos = entity.location

			// Throw location is the entity location, plus the entity eye level, if
			// applicable
			val throwLocation = Location(pos.getWorld(), pos.x(), pos.y(), pos.z())
			entity.traits.with(EntityStance::class.java) { stance -> throwLocation.y += stance.get().eyeLevel }

			// Throw force
			val entityRotation = entity.traits.get(TraitRotation::class.java) ?: return true

			// We can't support throwing grenades from something that has no rotation.
			val throwForce = Vector3d(entityRotation.directionLookingAt)
					.mul(0.2 - Math2.clampd(entityRotation.verticalRotation.toDouble(), -45.0, 20.0) / 45f * 0.3f)

			// If the thrower entity has velocity, add it to the initial grenade velocity
			entity.traits.with(TraitVelocity::class.java) { ev -> throwForce.add(ev.velocity) }

			val grenade = this.definition.store().parent().entities()
					.getEntityDefinition(grenadeEntityName)!!.create(throwLocation) as EntityThrownGrenade
			grenade.entityLocation.set(throwLocation)
			grenade.entityVelocity.setVelocity(throwForce)

			pos.getWorld().addEntity(grenade)

			if (!entity.traits.tryWithBoolean(TraitCreativeMode::class.java) { ecm -> ecm.get() }) {
				val inv = itemPile!!.inventory
				if (itemPile.amount == 0 && inv != null)
					inv.setItemPileAt(itemPile.x, itemPile.y, null)
				else if (itemPile.amount > 0)
					itemPile.amount = itemPile.amount - 1
			}
		}
		return false
	}
}
