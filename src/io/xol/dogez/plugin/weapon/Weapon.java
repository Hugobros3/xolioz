package io.xol.dogez.plugin.weapon;

//Copyright 2015 XolioWare Interactive

import java.util.HashMap;
import java.util.Map;

import io.xol.dogez.plugin.DogeZPlugin;
import io.xol.dogez.plugin.game.ScheduledEvents;
import io.xol.dogez.plugin.player.PlayerProfile;
import io.xol.dogez.plugin.player.PlayersPackets;
import net.minecraft.server.v1_8_R3.BlockPosition;
import net.minecraft.server.v1_8_R3.EntityZombie;
import net.minecraft.server.v1_8_R3.EnumParticle;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntityEquipment;
import net.minecraft.server.v1_8_R3.PacketPlayOutWorldEvent;

import org.bukkit.Effect;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class Weapon {
	
	public static void init(DogeZPlugin dogeZPlugin) {
		//Armes "de base"
		Weapon.addWeapon( new Weapon(256,0,"shotgun","Il déboite ta mère",false, false, 5.5f, 90,80,1800L,600L,12f,8));
		Weapon.addWeapon( new Weapon(273,0,"svd","Sniper russe peu cher",true, false, 16.5f, 120,130,2500L,120L,0.5f,1));
		Weapon.addWeapon( new Weapon(277,0,"ak74","Arme illégale importée",false, true, 10, 70,70,1500L,95L,1.9f,1));
		Weapon.addWeapon( new Weapon(284,0,"m4","Matériel américain",false, true, 12, 90,85,1300L,90L,1.5f,1));
		Weapon.addWeapon( new Weapon(293,0,"m1911","Pistolet semi-auto",false, false, 8, 45,60,1800L,50L,2f,1));
		Weapon.addWeapon( new Weapon(269,0,"makarov","Pistolet russe",false, false, 7, 40,50,1500L,45L,3f,1));
		Weapon.addWeapon( new Weapon(270,0,"akm","Arme de milice",false, true, 12, 70,90,1500L,110L,1.6f,1));
		Weapon.addWeapon( new Weapon(274,0,"dmr","Sniper américain",true, false, 18, 130,200,2500L,120L,0.45f,1));

		//Armes ISIS
		Weapon.addWeapon( new Weapon(258,0,"aa12","Fusil à pompe automatique",false, true, 6.5f, 60,80,2100L,300L,8f,5));
		Weapon.addWeapon( new Weapon(285,0,"uzi","Pistolet mitrailleur",false, true, 6.5f, 60,120,2100L,10L,2.5f,1));
		Weapon.addWeapon( new Weapon(278,0,"pkm","Mitrailleuse lourde",false, true, 6f, 160,180,2100L,200L,4f,1));

		//Armes spéciales
		Weapon.addWeapon( new Weapon(291,0,"m249","M249", false, true, 5, 250,500,250L,0L,3.45f,2));
		Weapon.addWeapon( new Weapon(292,0,"m9sd","M9SD", false, false, 8, 45,7,1800L,40L,2f,1));
		Weapon.addWeapon( new WeaponFirethrower(290,0,"firethrower","Lance-flammes, cachez-vous !",false, true, 16, 12,200,2500L,0L,20f,5));
		Weapon.addWeapon( new WeaponRPG7(279, 0, "rpg7", "RPG-7", false, false, 0, 25, 25, 3500L, 0L, 0f, 0));
		
		System.out.println("[DogeZ-Plugin] "+weaponsC+" weapons initialized.");
	}
	
	public static Weapon[]weaponsId = new Weapon[512*16];
	public static Map<String,Integer> weaponsNames = new HashMap<String,Integer>();
	public static int weaponsC = 0;
	
	public static Weapon addWeapon(Weapon wp)
	{
		weaponsId[wp.id*16+wp.meta] = wp;
		weaponsC++;
		weaponsNames.put(wp.name, wp.id*16+wp.meta);
		return wp;
	}
	
	public static boolean isWeapon(int id, int meta)
	{
		meta = 0;
		id = id%512;
		if(id <= 0)
			return false;
		return !(weaponsId[id*16+meta%16] == null);
	}
	
	public static Weapon getWeapon(int id, int meta)
	{
		meta = 0;
		id = id%512;
		return weaponsId[id*16+meta%16];
	}
	
	public static Weapon getWeapon(String name)
	{
		int lol = 0;
		if(weaponsNames.containsKey(name))
			lol = weaponsNames.get(name);
		return weaponsId[lol];
	}
	
	public int id;
	public int meta;
	public String name;
	public String desc;
	
	public float soundVolume = 1.0f;
	public float soundFade = 1.0f;
	
	public boolean isSniper = false;
	public boolean isAuto = false;
	
	public float damage = 0;
	public float range = 0;
	public float soundRange = 0;
	
	public long reloadTime = 0;
	public long cooldown;
	
	public float imprecision = 0;
	public int nbShots = 1;
	
	public Ammo[] acceptableAmmo = new Ammo[0];
	
	public Weapon(int id,int meta,String name,String desc,boolean isSniper, boolean isAuto, float damage, float range, float soundRange, long reloadTime, long cooldown, float imprecision, int nbShots)
	{
		this.id = id;
		this.meta = meta;
		this.name = name;
		this.desc = desc;
		
		this.isSniper = isSniper;
		this.isAuto = isAuto;
		
		this.damage = damage;
		this.range = range;
		this.soundRange = soundRange;
		
		this.reloadTime = reloadTime;
		this.cooldown = cooldown;
		
		this.imprecision = imprecision;
		this.nbShots = nbShots;
	}
	
	public ItemStack getItem()
	{
		return null;
	}

	public void addSuitableAmmo(Ammo ammo) {
		Ammo[] copy = acceptableAmmo;
		acceptableAmmo = new Ammo[copy.length+1];
		for(int i = 0;i < copy.length; i++)
		{
			acceptableAmmo[i] = copy[i];
		}
		acceptableAmmo[copy.length] = ammo;
		//System.out.println("debug: added ammo "+ammo.toString()+" to weapon "+name+" "+toString()+" - "+acceptableAmmo.length);
	}
	
	public boolean isAcceptableAmmo(Ammo ammo)
	{
		for(Ammo a : acceptableAmmo)
		{
			if(ammo.equals(a))
				return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public int getRemainingRounds(Player p, PlayerProfile pp)
	{
		if(pp.ammoSlot >= 0)
		{
			ItemStack s = p.getInventory().getItem(pp.ammoSlot);
			if(s != null)
			{
				if(Ammo.isAmmo(s.getTypeId(),s.getDurability()) && isAcceptableAmmo(Ammo.getAmmo(s.getTypeId(),s.getDurability())))
					return s.getAmount();
			}
		}
		return 0;
	}
	
	public void clickEvent(Player shooter, boolean button)
	{
		PlayerProfile pp = PlayerProfile.getPlayerProfile(shooter.getUniqueId().toString());
		if(button)
		{
			if(isSniper)
			{
				//shooter.updateInventory();
				pp.isScoping = !pp.isScoping;
			}
		}
		else
		{
			if(System.currentTimeMillis() - pp.lastShoot >= cooldown)
			{
				if(isAuto)
				{
					shoot(shooter,pp);
					pp.lastShoot = System.currentTimeMillis();
				}
				else
				{
					if(ScheduledEvents.ticksCounter - pp.lastTick > 5)
					{
						shoot(shooter,pp);
						pp.lastShoot = System.currentTimeMillis();
					}
				}
			}
			pp.lastTick = ScheduledEvents.ticksCounter;
			//System.out.println(name+":"+pp.lastTick+":"+isAuto);
		}
		shooter.updateInventory();
		Weapon.refreshEffects(shooter);
	}
	
	@SuppressWarnings("deprecation")
	private void shoot(Player shooter, PlayerProfile pp) {

		Location ploc = shooter.getEyeLocation();
		//System.out.println("debug: shooting"+pp.wpSlot+":"+pp.ammoSlot+":"+pp.reloadEndMS+":"+getRemainingRounds(shooter,pp));
		//First check if we got the ammo to do so
		if(!shooter.getGameMode().equals(GameMode.CREATIVE))
		{
			//If he's not in creative we'll have to check on his ammo
			boolean slotChanged = false;
			//First, we need to check if the weapon was switched recently, as we don't want having to reload our guns
			//everytime we switch them.
			if(pp.wpSlot < 0)
			{
				slotChanged = true;
				pp.wpSlot = shooter.getInventory().getHeldItemSlot();
			}
			ItemStack s = shooter.getInventory().getItem(pp.wpSlot);
			if(s == null || !Weapon.isWeapon(s.getTypeId(), s.getDurability()) || !Weapon.getWeapon(s.getTypeId(), s.getDurability()).equals(this))
			{
				slotChanged = true;
				pp.wpSlot = shooter.getInventory().getHeldItemSlot();
			}
			//Is our current stack of ammo still containing some stuff ?
			if(getRemainingRounds(shooter,pp) <= 0)
			{
				//Seek for another one
				//System.out.println("debug: seeking for ammo");
				pp.ammoSlot = -1;
				for(int i = 0; i < shooter.getInventory().getSize(); i++)
				{
					ItemStack slot = shooter.getInventory().getItem(i);
					if(slot != null)
					{
						if(Ammo.isAmmo(slot.getTypeId(),slot.getDurability()) && isAcceptableAmmo(Ammo.getAmmo(slot.getTypeId(),slot.getDurability())))
						{
							//System.out.println("debug: ammo found");
							pp.ammoSlot = i;
							i = shooter.getInventory().getSize();
							if(!slotChanged)
							{
								pp.reloadEndMS = System.currentTimeMillis() + reloadTime;
								PlayersPackets.playSound(ploc, "dogez.weapon."+name+".reload", 1, 1f);
								//System.out.println("debug: reloading"+reloadTime+":"+pp.reloadEndMS);
							}
						}
					}
				}
			}
			if(pp.ammoSlot >= 0)
			{
				//System.out.println("debug: shootin "+System.currentTimeMillis());
				if(System.currentTimeMillis() > pp.reloadEndMS)
				{
					//System.out.println("debug: "+System.currentTimeMillis()+" > "+pp.reloadEndMS);
					ItemStack ammoStack = shooter.getInventory().getItem(pp.ammoSlot);
					ammoStack.setAmount(ammoStack.getAmount()-1);
					if(ammoStack.getAmount() <= 0)
						ammoStack = null;
					shooter.getInventory().setItem(pp.ammoSlot, ammoStack);
				}
				else
					return;
			}
			else
			{
				//dogez.weapon.default.dry
				PlayersPackets.playSound(ploc, "dogez.weapon.default.dry", 1f, 1f);
				//System.out.println("debug: no ammo found, returning");
				return;
			}
		}
		double angleH = (ploc.getYaw() + 90) % 360;
		double angleV = ploc.getPitch() * -1;
		//raycast shots
		for(int i = 0; i < nbShots; i++)
		{
			raycastBullet(shooter,angleH+imprecision*(Math.random()-0.5),angleV+imprecision*(Math.random()-0.5)*0.3f);
		}
		//call nearby zombies
		for(Entity ent : shooter.getWorld().getEntities())
		{
			if(ent instanceof EntityZombie && ent.getLocation().distance(shooter.getLocation()) < soundRange/1.8f)
			{
				EntityZombie zombie = (EntityZombie) ent;
				zombie.setGoalTarget(((CraftPlayer) shooter).getHandle());
			}
		}
		//System.out.println("Shooting' "+System.currentTimeMillis());
		PlayersPackets.playSound(ploc, "dogez.weapon."+name+".shoot", soundRange/16f, 1f);
	}
	
	@SuppressWarnings("deprecation")
	private void raycastBullet(Player shooter, double angleH, double angleV) {

		Location ploc = shooter.getEyeLocation();
		double x = ploc.getX();
		double y = ploc.getY();
		double z = ploc.getZ();
		CraftPlayer craftPlayer = (CraftPlayer)shooter;
		
		double distance_traveled = 0d;
		double speed = 0.3f;
		boolean hit = false;
		boolean underwater = false;
		int blockID = 0;
		while (!isSolid(shooter.getWorld().getBlockAt((int) Math.floor(x), (int) Math.floor(y), (int) Math.floor(z)),x,y,z) && distance_traveled < range && !hit) {
			x = x + Math.cos(Math.toRadians(angleH)) * speed
					* Math.cos(Math.toRadians(angleV));
			z = z + Math.sin(Math.toRadians(angleH)) * speed
					* Math.cos(Math.toRadians(angleV));
			y = y + Math.sin(Math.toRadians(angleV)) * speed;
			distance_traveled += speed;
			if(underwater)
			{
				distance_traveled += speed*10;
				//Add bubbles in water !
				//shooter.getWorld().spigot().playEffect(new Location(shooter.getWorld(), x, y, z), Effect.);
				((CraftWorld)shooter.getWorld()).getHandle().addParticle(EnumParticle.WATER_BUBBLE, x, y, z, 0, 0, 0);
			}
			//Glass-breaking
			blockID = shooter.getWorld().getBlockAt((int) Math.floor(x), (int) Math.floor(y), (int) Math.floor(z)).getTypeId();
			if(blockID == 8 || blockID == 9)
			{
				double lx = x - Math.cos(Math.toRadians(angleH)) * speed
						* Math.cos(Math.toRadians(angleV));
				double lz = z - Math.sin(Math.toRadians(angleH)) * speed
						* Math.cos(Math.toRadians(angleV));
				double ly = y - Math.sin(Math.toRadians(angleV)) * speed;
				if(underwater == false)
				{
					//Particules pour la flotte
					shooter.getWorld().spigot().playEffect(new Location(shooter.getWorld(), x, y, z), Effect.SPLASH);
					PlayersPackets.playSound(new Location(shooter.getWorld(),lx,ly,lz), "game.neutral.swim.splash", 1.5f, 1.8f+(float)Math.random()*0.2f);
					//((CraftWorld)shooter.getWorld()).getHandle().addParticle(EnumParticle.WATER_SPLASH, lx, ly+1, lz, 0, 0, 0);
				}
				underwater = true;
			}
			if(blockID == 102 || blockID == 20 || blockID == 95 || blockID == 160)
			{
				//System.out.println("glass should break mdr");
				if(shooter.hasPermission("dogez.breakglass"))
				{
					spillparticle(new Location(shooter.getWorld(),x,y,z),shooter.getWorld().getBlockAt((int) Math.floor(x), (int) Math.floor(y), (int) Math.floor(z)).getTypeId());
					ChunksCleaner.breakGlass(shooter.getWorld(), (int) Math.floor(x), (int) Math.floor(y), (int) Math.floor(z));
					if(Math.random() > 0.8)
						PlayersPackets.playSound(new Location(shooter.getWorld(),x,y,z), "dogez.weapon.hit.glass", 1.5f, 0.9f+(float)Math.random()*0.2f);
				}
			}
			
			for(Entity ent : shooter.getWorld().getChunkAt(new Location(shooter.getWorld(),x,y,z)).getEntities())
			{
				CraftEntity ce = (CraftEntity)ent;
				if(x > ce.getHandle().getBoundingBox().a && y > ce.getHandle().getBoundingBox().b && z > ce.getHandle().getBoundingBox().c
						&& x < ce.getHandle().getBoundingBox().d && y < ce.getHandle().getBoundingBox().e && z < ce.getHandle().getBoundingBox().f && !ent.equals(shooter))
				{
					if (ent instanceof LivingEntity) {
						LivingEntity victim = (LivingEntity) ent;
						if(victim.getHealth() > 0)
						{
							float damageToDeal = damage;
							//water check
							if(underwater)
								damageToDeal/=3;
							//headshot check
							if(y >= victim.getEyeHeight()+ent.getLocation().getY())
							{
								damageToDeal*=1.5f; // 1.5X MORE DAMAGE
								//shooter.sendMessage(ChatColor.RED+"HEADSHOT - 1.5X MORE DMG");
							}
							victim.damage(damageToDeal,craftPlayer);
							
							victim.setNoDamageTicks(0);
							spillparticle(new Location(shooter.getWorld(),x,y,z),87);
							hit = true;
						}
					}
				}
			}
		}
		if(!hit)
		{
			PlayersPackets.playSound(new Location(shooter.getWorld(),x,y,z), "dogez.weapon.hit."+getClacSoundForBlock(blockID), 1.5f, 0.5f+(float)Math.random());
			spillparticle(new Location(shooter.getWorld(),x,y,z),shooter.getWorld().getBlockAt((int) Math.floor(x), (int) Math.floor(y), (int) Math.floor(z)).getTypeId());
		}
	}

	String getClacSoundForBlock(int blockID)
	{
		if(blockID == 2 || blockID == 3)
			return "ground";
		if(blockID == 5 || blockID == 17 || blockID == 126 || blockID == 85 || blockID == 65 || blockID == 146 || blockID ==  54
				|| blockID == 64 || blockID == 107)
		{
			if(Math.random() > 0.5d)
				return "concrete";
			else
				return "wood";
		}
		if(blockID == 101)
			return "fence";
		if(blockID == 20 || blockID == 102)
			return "glass";
		if(blockID == 71 || blockID == 61 || blockID == 42)
			return "car";
		if(blockID == 98 || blockID == 35)
			return "wall";
		return "clac";
	}
	
	@SuppressWarnings("deprecation")
	protected boolean isSolid(Block block, double x, double y, double z) {
		int blockID = block.getTypeId();
		if (blockID == 0 || blockID == 8  || blockID == 9 || blockID == 31 || blockID == 106 || blockID == 63 || blockID == 68 || blockID == 171
				|| blockID == 70 || blockID == 72 || blockID == 147 || blockID == 148 || blockID == 77 || blockID == 18 || blockID == 102
				|| blockID == 64 || blockID == 71)
			return false;
			
		if(/*blockID == 102 ||*/ blockID == 101 || blockID == 85)
		{
			//System.out.println("debugblockid"+blockID);
			/*CraftWorld world = (CraftWorld) block.getWorld();
			net.minecraft.server.v1_8_R2.Block b2 = (net.minecraft.server.v1_8_R2.Block) net.minecraft.server.v1_8_R2.Block.REGISTRY.a(blockID);
			if(b2 != null)
			{
				AxisAlignedBB bb = b2.a(world.getHandle(), new BlockPosition((int) Math.floor(x), (int) Math.floor(y), (int) Math.floor(z)));
				//System.out.println("debugblock"+bb.a+":"+bb.b+":"+bb.c+":"+bb.d+":"+bb.e+":"+bb.f+":");
				return (x > bb.a && y > bb.b && z > bb.c
						&& x < bb.d && y < bb.e && z < bb.f);
			}*/
			return Math.random() > 0.5;
			//Block b2 = world.getHandle().
			//getgetBlockAt((int) Math.floor(x), (int) Math.floor(y), (int) Math.floor(z));
		}
		return Material.getMaterial(blockID).isSolid();
	}
	
	public static void spillparticle(Location loc, int id)
	{
		PacketPlayOutWorldEvent  packet = new PacketPlayOutWorldEvent(2001, new BlockPosition(loc.getBlockX(),loc.getBlockY(),loc.getBlockZ()), id, false);//new PacketPlayOutWorldEvent (2001, loc.getBlockX(),loc.getBlockY(),loc.getBlockZ(), id, false);
		for(Player p : loc.getWorld().getPlayers())
		{
			if(p.getLocation().distance(loc) < 32)
				 ((CraftPlayer)p).getHandle().playerConnection.sendPacket(packet);
		}
	}
	
	public static void refreshEffects(Player player)
	{
		player.removePotionEffect(PotionEffectType.SLOW);
		PlayerProfile pp = PlayerProfile.getPlayerProfile(player.getUniqueId().toString());
		if(pp.isScoping)
		{
			PotionEffect zoom = new PotionEffect(PotionEffectType.SLOW, 200000, 6);
			//player.getActivePotionEffects().add(zoom);
			player.addPotionEffect(zoom);
			//System.out.println("scoping");
			if(pp.isScoping)
			{
				PacketPlayOutEntityEquipment packet = new PacketPlayOutEntityEquipment(player.getEntityId(), 3, CraftItemStack.asNMSCopy(new ItemStack(Material.PUMPKIN, 1)));
                ((CraftPlayer)player).getHandle().playerConnection.sendPacket(packet);
			}
		}
	}
}
