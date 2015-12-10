package io.xol.dogez.plugin.weapon;

import io.xol.dogez.plugin.game.ScheduledEvents;
import io.xol.dogez.plugin.player.PlayerProfile;
import io.xol.dogez.plugin.player.PlayersPackets;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

//Copyright 2015 XolioWare Interactive

public class WeaponRPG7 extends Weapon {

	public WeaponRPG7(int id, int meta, String name, String desc,
			boolean isSniper, boolean isAuto, float damage, float range,
			float soundRange, long reloadTime, long cooldown,
			float imprecision, int nbShots) {
		super(id, meta, name, desc, isSniper, isAuto, damage, range, soundRange,
				reloadTime, cooldown, imprecision, nbShots);
		
	}

	public void clickEvent(Player shooter, boolean button)
	{
		PlayerProfile pp = PlayerProfile.getPlayerProfile(shooter.getUniqueId().toString());
		if(!button)
		{
			if(System.currentTimeMillis() - pp.lastShoot >= cooldown)
			{
				if(shooter.getGameMode().equals(GameMode.SURVIVAL))
				{
					boolean found = false;
					for(int i = 0; i < shooter.getInventory().getSize(); i++)
					{
						ItemStack slot = shooter.getInventory().getItem(i);
						if(slot != null)
						{
							if(slot.getType().equals(Material.SLIME_BALL))
							{
								shooter.getInventory().setItem(i, null);
								shooter.updateInventory();
								found = true;
								i = 400;
							}
						}
					}
					if(!found)
						return;
				}
				launchFireball(shooter);
				pp.lastShoot = System.currentTimeMillis();
			}
			pp.lastTick = ScheduledEvents.ticksCounter;
			//System.out.println(name+":"+pp.lastTick+":"+isAuto);
		}
	}

	private void launchFireball(Player shooter) {
		PlayersPackets.playSound(shooter.getLocation(), "fireworks.launch", 1f, 1f);
		Fireball fireball = (Fireball) shooter.getWorld().spawnEntity(shooter.getLocation().add(0, 1, 0), EntityType.FIREBALL);
		fireball.setShooter(shooter);
		fireball.setVelocity(shooter.getEyeLocation().getDirection().multiply(1.5));
		fireball.setIsIncendiary(false);
		fireball.setFireTicks(0);
		fireball.setYield(0);
	}
}
