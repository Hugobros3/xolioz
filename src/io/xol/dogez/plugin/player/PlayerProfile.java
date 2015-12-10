package io.xol.dogez.plugin.player;

//(c) 2014 XolioWare Interactive

import java.util.ArrayList;
import java.util.List;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftEntity;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import io.xol.dogez.plugin.loot.LootItems;
import io.xol.dogez.plugin.misc.HttpRequestThread;
import io.xol.dogez.plugin.misc.HttpRequester;
import io.xol.dogez.plugin.weapon.Weapon;

public class PlayerProfile implements HttpRequester {
	
	public String name;
	public String uuid;
	
	public boolean loadedSuccessfully = false;
	public boolean inGame = false;
	
	//stats
	
	public long dateOfJoin = 0;
	
	public long timeConnected = 0;
	
	public long timeSurvivedTotal = 0;
	public long timeSurvivedLife = 0;
	public long timeAtLastCalc = -1;
	
	public int zombiesKilled = 0;
	public int playersKilled = 0;
	public int deaths = 0;
	
	public int zombiesKilled_thisLife = 0;
	public int playersKilled_thisLife = 0;
	
	//Halloween shit
	
	public int death_level = -1;
	public String deathRequest = "";
	
	public boolean goToHell = false;
	
	//money and donnators features
	
	public double xcBalance = 0;
	public boolean isPlus = false;
	
	// map-building shit
	
	public boolean adding = true;
	public String activeCategory;
	public int currentMin = 1;
	public int currentMax = 5;
	
	// gameplay crap
	
	public String lastPlace = "";
	public boolean isScoping = false;
	public long lastShoot = 0;
	public long lastTick = 0;
	
	public int wpSlot = -1;
	public int ammoSlot = -1;
	public long reloadEndMS = 0;
	
	public Location torchLocation = null;
	
	//public float heat = 0.5f;
	public String talkingTo = "";
	
	//Anti combatlog
	
	public long lastHitTime = 0;
	
	//Admin
	
	public boolean disableSS = false;
	
	public PlayerProfile(String uuid,String name)
	{
		this.name = name;
		this.uuid = uuid;
		reloadProfile();
	}

	public void reloadProfile() {
		new HttpRequestThread(this,"reloadProfile","http://dz.xol.io/api/playerProfile.php","a=load&uuid="+uuid+"&name="+name).run();
	}

	@SuppressWarnings("deprecation")
	public void updateTorch(boolean enabled)
	{
		if(!enabled)
		{
			if(torchLocation != null)
			{
				PlayersPackets.setBlock(torchLocation, 0, (byte)0);
				torchLocation = null;
			}
		}
		else
		{
			Player guy = Bukkit.getPlayer(name);
			
			Location ploc = guy.getLocation();
			
			// Battery condition check
			if(guy.getGameMode().equals(GameMode.SURVIVAL))
			{
				boolean found = false;
				for(int i = 0; i < guy.getInventory().getSize(); i++)
				{
					ItemStack slot = guy.getInventory().getItem(i);
					if(slot != null)
					{
						if(slot.getType().equals(Material.COAL))
						{
							ItemMeta metaTorch = slot.getItemMeta();
							String name = metaTorch.getDisplayName();
							String percentage = "";
							boolean start = false;
							for(char c : name.toCharArray())
							{
								if(c == '[')
									start = true;
								else if(c == '%')
									break;
								else if(start)
									percentage+=c;
							}
							if(percentage.equals(""))
							{
								guy.sendMessage("C'est pas une vraie pile ce truc !");
								break;
							}
							float batteryLeft = Float.parseFloat(percentage);
							if(batteryLeft <= 0f)
							{
								guy.getInventory().setItem(i, null);
								guy.updateInventory();
								return;
							}
							else
								/*if(Math.random() > 0.750)
								{
									batteryLeft -= Math.random();
								}*/
							metaTorch.setDisplayName("Pile alkaline ["+batteryLeft+"%]");
							slot.setItemMeta(metaTorch);
							guy.getInventory().setItem(i, slot);
							guy.updateInventory();
							found = true;
							break;
						}
					}
				}
				if(!found)
				{
					guy.sendMessage(ChatColor.RED+"La torche nécéssite des piles chargées dans votre inventaire !");

					ItemStack torchOff = LootItems.getItem("torchOff").getItem();
					guy.getInventory().setItemInHand(torchOff);
					
					if(torchLocation != null)
						PlayersPackets.setBlock(torchLocation, 0, (byte)0);
					torchLocation = null;
					return;
				}
			}
			
			double angleH = (ploc.getYaw() + 90) % 360;
			double angleV = ploc.getPitch() * -1;
			
			double x = ploc.getX();
			double y = ploc.getY()+1.0;
			double z = ploc.getZ();
			
			double distance_traveled = 0d;
			double range = 45;
			
			double speed = 0.5;
			boolean hit = false;
			int blockID = 0;
			
			while (distance_traveled < range && !hit) {
				x = x + Math.cos(Math.toRadians(angleH)) * speed
						* Math.cos(Math.toRadians(angleV));
				z = z + Math.sin(Math.toRadians(angleH)) * speed
						* Math.cos(Math.toRadians(angleV));
				y = y + Math.sin(Math.toRadians(angleV)) * speed;
				distance_traveled += speed;
				
				//Glass-breaking
				blockID = ploc.getWorld().getBlockAt((int) Math.floor(x), (int) Math.floor(y), (int) Math.floor(z)).getTypeId();
				if(blockID != 0)
				{
					hit = true;
				}
				for(Entity ent : ploc.getWorld().getChunkAt(new Location(ploc.getWorld(),x,y,z)).getEntities())
				{
					CraftEntity ce = (CraftEntity)ent;
					if(x > ce.getHandle().getBoundingBox().a && y > ce.getHandle().getBoundingBox().b && z > ce.getHandle().getBoundingBox().c
							&& x < ce.getHandle().getBoundingBox().d && y < ce.getHandle().getBoundingBox().e && z < ce.getHandle().getBoundingBox().f && !ent.equals(guy))
					{
						hit = true;
					}
				}
				
				if(hit)
				{
					x -= Math.cos(Math.toRadians(angleH)) * speed
							* Math.cos(Math.toRadians(angleV));
					z -= Math.sin(Math.toRadians(angleH)) * speed
							* Math.cos(Math.toRadians(angleV));
					y -= Math.sin(Math.toRadians(angleV)) * speed;
				}
			}
			
			if(hit)
			{
				Location newLocation = new Location(ploc.getWorld(), (int) Math.floor(x), (int) Math.floor(y), (int) Math.floor(z));
				
				if(torchLocation != null && !newLocation.equals(torchLocation))
					PlayersPackets.setBlock(torchLocation, 0, (byte)0);
				if(torchLocation == null || !newLocation.equals(torchLocation))
					PlayersPackets.setBlock(newLocation, 50, (byte)0);
				
				torchLocation = newLocation;
			}
		}
				
	}
	
	public void decreaseBattery() {
		Player guy = Bukkit.getPlayer(name);
		if(guy == null)
			return;
		ItemStack it = guy.getItemInHand();
		if(it != null && it.getType().equals(Material.STICK))
		{
			// Battery condition check
			if(guy.getGameMode().equals(GameMode.SURVIVAL))
			{
				for(int i = 0; i < guy.getInventory().getSize(); i++)
				{
					ItemStack slot = guy.getInventory().getItem(i);
					if(slot != null)
					{
						if(slot.getType().equals(Material.COAL))
						{
							ItemMeta metaTorch = slot.getItemMeta();
							String name = metaTorch.getDisplayName();
							String percentage = "";
							boolean start = false;
							for(char c : name.toCharArray())
							{
								if(c == '[')
									start = true;
								else if(c == '%')
									break;
								else if(start)
									percentage+=c;
							}
							if(percentage.equals(""))
							{
								break;
							}
							float batteryLeft = Float.parseFloat(percentage);
							if(batteryLeft > 0f)
								batteryLeft -= Math.random()*2.5;
							batteryLeft *= 10;
							batteryLeft = (float) Math.floor(batteryLeft);
							batteryLeft /= 10;
							metaTorch.setDisplayName("Pile alkaline ["+batteryLeft+"%]");
							slot.setItemMeta(metaTorch);
							guy.getInventory().setItem(i, slot);
							guy.updateInventory();
							break;
						}
					}
				}
			}
		}
	}
	
	/*@SuppressWarnings("deprecation")
	private boolean isSolid(int blockID) {
		if (blockID == 0 || blockID == 8  || blockID == 9 || blockID == 31 || blockID == 106 || blockID == 63 || blockID == 68 || blockID == 171
				|| blockID == 70 || blockID == 72 || blockID == 147 || blockID == 148 || blockID == 77 || blockID == 18 || blockID == 102
				|| blockID == 64 || blockID == 71)
			return false;
			
		if(blockID == 101 || blockID == 85)
		{
			return Math.random() > 0.5;
		}
		return Material.getMaterial(blockID).isSolid();
	}*/

	public long timeCalc()
	{
		if(timeAtLastCalc == -1)
			timeAtLastCalc = System.currentTimeMillis();
		else
		{
			long timeToAdd = (System.currentTimeMillis() - timeAtLastCalc)/1000;
			timeConnected+=timeToAdd;
			if(inGame)
			{
				timeSurvivedTotal+=timeToAdd;
				timeSurvivedLife+=timeToAdd;
			}
			timeAtLastCalc = System.currentTimeMillis();
			return timeToAdd;
		}
		return 0;
	}
	
	public void saveProfile(){
		timeCalc();
		if(!loadedSuccessfully)
			return;
		new HttpRequestThread(this,"saveProfile","http://dz.xol.io/api/playerProfile.php","a=save&uuid="+uuid+"&name="+name
				+"&tc="+timeConnected
				+"&tst="+timeSurvivedTotal
				+"&tsl="+timeSurvivedLife
				+"&zkt="+zombiesKilled
				+"&zkl="+zombiesKilled_thisLife
				+"&pkt="+playersKilled
				+"&pkl="+playersKilled_thisLife
				+"&d="+deaths
				+"&ig="+(inGame ? "1" : "0")
				+"&death="+death_level
				+"&victim="+this.deathRequest
				).run();
		setBalance();
	}
	
	public void addBalance(float amount)
	{
		xcBalance+=amount;
		new HttpRequestThread(this,"changeBalance","http://dz.xol.io/api/playerProfile.php","a=balance&uuid="+uuid+"&diff="+amount).run();
	}
	
	public void setBalance()
	{
		new HttpRequestThread(this,"changeBalance","http://dz.xol.io/api/playerProfile.php","a=balance&uuid="+uuid+"&bal="+xcBalance).run();
	}
	
	@Override
	public void handleHttpRequest(String info, String result) {
		//System.out.println("[DogeZ][Debug] Request "+info+" answered:"+result);
		if(info.equals("reloadProfile"))
		{
			if(!result.startsWith("p"))
				return;
			
			// 1:uuid 2:name 3:joindate 4:lastlogdate 5:balance 6:timeConnected 7:timeSurvivedTotal 8:timeSurvivedLife 
			// 9:zombiesKilledTotal 10:zombiesKilledLife 11:playersKilledTotal 12:playersKilledLife 13:deaths 14:isIngame
			
			String[] data = result.split(":");
			
			dateOfJoin = Integer.parseInt(data[3]);
			xcBalance = Double.parseDouble(data[5]);
			
			if(timeConnected == 0)
			{
			timeConnected = Long.parseLong(data[6]);
			timeSurvivedTotal = Long.parseLong(data[7]);
			timeSurvivedLife = Long.parseLong(data[8]);
			}
			
			zombiesKilled = Integer.parseInt(data[9]);
			playersKilled = Integer.parseInt(data[11]);
			deaths = Integer.parseInt(data[13]);
			
			zombiesKilled_thisLife = Integer.parseInt(data[10]);
			playersKilled_thisLife = Integer.parseInt(data[12]);
			
			inGame = data[14].equals("1");
			
			death_level = Integer.parseInt(data[15]);
			deathRequest = data[16];
			if(deathRequest.equals("nobody"))
				deathRequest = "";
			timeCalc();
			
			loadedSuccessfully = true;
		}
	}
	
	//Static part
	
	static List<PlayerProfile> playerProfiles = new ArrayList<PlayerProfile>();
	
	public static PlayerProfile getPlayerProfile(String uuid)
	{
		PlayerProfile result = null;
		for(PlayerProfile pp : playerProfiles)
		{
			if(pp.uuid.equals(uuid))
				result = pp;
		}
		if(result == null)
		{
			OfflinePlayer player = Bukkit.getOfflinePlayer(UUID.fromString(uuid));
			if(player != null)
			{
				PlayerProfile pp = new PlayerProfile(uuid, player.getName());
				if(pp != null && pp.loadedSuccessfully)
				{
					//System.out.println("pp loaded already !");
					return pp;
				}
			}
		}
		return result;
	}
	
	public static void removePlayerProfile(String uuid)
	{
		//System.out.println(playerProfiles.size()+"deleting"+uuid);
		List<PlayerProfile> profilesToDelete = new ArrayList<PlayerProfile>();
		for(PlayerProfile pp : playerProfiles)
		{
			//System.out.println("test4"+pp.uuid);
			if(pp.uuid.equals(uuid))
			{
				//System.out.println("match lel");
				pp.saveProfile();
				profilesToDelete.add(pp);
			}
		}

		//System.out.println("deletin"+profilesToDelete.size());
		for(PlayerProfile delete : profilesToDelete)
		{
			playerProfiles.remove(delete);
			//System.out.println("[DogeZ][Debug] Removed player profile ["+delete.uuid+":"+delete.name+"]");
		}
	}
	
	public static void addPlayerProfile(String uuid, String name)
	{
		PlayerProfile add = new PlayerProfile(uuid,name);
		playerProfiles.add(add);
		//System.out.println("[DogeZ][Debug] Added player profile ["+uuid+":"+name+"]");
	}
	
	public static void saveAll()
	{
		for(PlayerProfile pp : playerProfiles)
		{
			pp.saveProfile();
		}
	}

	public void onDeath() {
		new HttpRequestThread(this,"kill","http://dz.xol.io/api/playerProfile.php","a=kill&uuid="+uuid).run();
	}

	@SuppressWarnings("deprecation")
	public String getWeaponString() {
		
		ItemStack it = Bukkit.getPlayer(this.name).getItemInHand();
		if(it != null)
		{
			if(Weapon.isWeapon(it.getTypeId(), 0))
			{
				Weapon wp = Weapon.getWeapon(it.getTypeId(), 0);
				String str = ChatColor.GREEN+wp.name+" // "+wp.getRemainingRounds(Bukkit.getPlayer(this.name), this)+" balles restantes.";
				return str;
			}
		}
		
		return " ";
	}
	
	@SuppressWarnings("deprecation")
	public void updateXPLevel()
	{
		ItemStack it = Bukkit.getPlayer(this.name).getItemInHand();
		if(it != null)
		{
			if(Weapon.isWeapon(it.getTypeId(), 0))
			{
				Weapon wp = Weapon.getWeapon(it.getTypeId(), 0);
				Bukkit.getPlayer(name).setLevel(wp.getRemainingRounds(Bukkit.getPlayer(this.name), this)%31);//.setExp(xpLevels[wp.getRemainingRounds(Bukkit.getPlayer(this.name), this)%31]);
				return;
			}
			
		}
		Bukkit.getPlayer(name).setLevel(0);//.setExp(xpLevels[wp.getRemainingRounds(Bukkit.getPlayer(this.name), this)%31]);
		
	}
	
	public static int[] xpLevels = {0,17,34,51,68,85,102,119,136,153,
		170,187,204,221,238,255,272,292,315,341,370,
		402,437,475,516,560,607,657,710,766,825};
		//TODO : virer poules !

	public double getTimeAlive() {
		return timeSurvivedLife;
	}

}
