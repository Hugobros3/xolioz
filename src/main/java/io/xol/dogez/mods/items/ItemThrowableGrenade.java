package io.xol.dogez.mods.items;

import org.joml.Vector3d;

import io.xol.chunkstories.api.Location;
import io.xol.chunkstories.api.entity.Controller;
import io.xol.chunkstories.api.entity.Entity;
import io.xol.chunkstories.api.entity.components.EntityCreativeMode;
import io.xol.chunkstories.api.entity.components.EntityRotation;
import io.xol.chunkstories.api.entity.components.EntityVelocity;
import io.xol.chunkstories.api.input.Input;
import io.xol.chunkstories.api.item.Item;
import io.xol.chunkstories.api.item.ItemDefinition;
import io.xol.chunkstories.api.item.inventory.Inventory;
import io.xol.chunkstories.api.item.inventory.ItemPile;
import io.xol.chunkstories.api.math.Math2;
import io.xol.chunkstories.api.world.WorldMaster;
import io.xol.chunkstories.core.entity.components.EntityStance;
import io.xol.dogez.mods.entities.EntityThrownGrenade;

public class ItemThrowableGrenade extends Item {

	final String grenadeEntityName;
	
	public ItemThrowableGrenade(ItemDefinition type, String grenadeEntityName) {
		super(type);
		this.grenadeEntityName = grenadeEntityName;
	}

	public boolean onControllerInput(Entity entity, ItemPile itemPile, Input input, Controller controller) {
		if (entity != null && input.getName().equals("mouse.right") && entity.getWorld() instanceof WorldMaster) {
			Location pos = entity.getLocation();

			// Throw location is the entity location, plus the entity eye level, if
			// applicable
			Location throwLocation = new Location(pos.getWorld(), pos.x(), pos.y(), pos.z());
			entity.components.with(EntityStance.class, stance -> throwLocation.y += stance.get().eyeLevel);

			// Throw force
			EntityRotation entityRotation = entity.components.get(EntityRotation.class);

			// We can't support throwing grenades from something that has no rotation.
			if (entityRotation == null)
				return true;
			Vector3d throwForce = new Vector3d(entityRotation.getDirectionLookingAt())
					.mul(0.2 - Math2.clampd(entityRotation.getVerticalRotation(), -45, 20) / 45f * 0.3f);

			// If the thrower entity has velocity, add it to the initial grenade velocity
			entity.components.with(EntityVelocity.class, ev -> throwForce.add(ev.getVelocity()));

			EntityThrownGrenade grenade = (EntityThrownGrenade) this.getDefinition().store().parent().entities()
					.getEntityDefinition(grenadeEntityName).create(throwLocation);
			grenade.entityLocation.set(throwLocation);
			grenade.entityVelocity.setVelocity(throwForce);

			pos.getWorld().addEntity(grenade);

			if (!entity.components.tryWithBoolean(EntityCreativeMode.class, ecm -> ecm.get())) {
				Inventory inv = itemPile.getInventory();
				if (itemPile.getAmount() == 0 && inv != null)
					inv.setItemPileAt(itemPile.getX(), itemPile.getY(), null);
				else if (itemPile.getAmount() > 0)
					itemPile.setAmount(itemPile.getAmount() - 1);
			}
		}
		return false;
	}
}
