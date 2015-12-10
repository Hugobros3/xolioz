package io.xol.dogez.plugin.loot;

import io.xol.dogez.plugin.player.PlayerProfile;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

//Copyright 2014 XolioWare Interactive

public class LootPlaces {
	//This class takes care of holding all the places on the world where the loot do spawns.
	
	static Map<String,LootPlace> placesChernarus = new HashMap<String,LootPlace>();
	static Map<String,LootPlace> placesNamalsk = new HashMap<String,LootPlace>();
	static Map<String,LootPlace> placesOther = new HashMap<String,LootPlace>();
	
	public static Map<String,LootPlace> getData(World w)
	{
		if(w.getName().equals("world"))
			return placesChernarus;
		if(w.getName().equals("namalsk-map"))
			return placesNamalsk;
		return placesOther;
	}
	
	public static int respawnLoot(World w)
	{
		Map<String,LootPlace> places = getData(w);
		int count = 0;
		for(LootPlace lp : places.values())
		{
			lp.lastUpdate = 0;
			count++;
		}
		return count;
	}
	
	public static void loadLootFile(World w)
	{
		Map<String,LootPlace> places = getData(w);
		File file = getFile(w);
		places.clear();
		try {
			InputStream ips = new FileInputStream(file);
			InputStreamReader ipsr = new InputStreamReader(ips, "UTF-8");
			BufferedReader br = new BufferedReader(ipsr);
			String ligne;
			while ((ligne = br.readLine()) != null) {
				LootPlace lp = new LootPlace(ligne,w);
				if(lp.type != null)
				{
					String coords = lp.x+":"+lp.y+":"+lp.z;
					places.put(coords, lp);
					//debug
					//System.out.println("Added LootPlace "+lp.toString());
				}
			}
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private static String getPrefix(World w)
	{
		if(w.getName().equals("world"))
			return "";
		if(w.getName().equals("namalsk-map"))
			return "-namalsk";
		return "-other";
	}
	
	
	private static File getFile(World w) {
		File file = new File("./plugins/DogeZ/lootPlaces"+getPrefix(w)+".dz");
		if(!file.exists())
			try {
				file.createNewFile();
			} 
			catch (IOException e1) {
			}
		return file;
	}

	public static void saveLootFile(World w)
	{
		File file = getFile(w);
		Map<String,LootPlace> places = getData(w);
		try {
			Writer out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "UTF-8"));
			for(LootPlace lp : places.values())
			{
				out.write(lp.save()+"\n");
			}
			//out.write(key + "=" + props.get(key)+"\n");
			out.close();
		} catch (FileNotFoundException e) {
		} catch (IOException e) {
			e.printStackTrace();
		}
		//SECURITY
		File folder = new File("./plugins/DogeZ/backups");
		if(!folder.exists())
			folder.mkdir();
		file = new File("./plugins/DogeZ/backups/lootPlaces"+getPrefix(w)+"-"+System.currentTimeMillis()+".dz");
		try {
			file.createNewFile();
			Writer out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "UTF-8"));
			for(LootPlace lp : places.values())
			{
				out.write(lp.save()+"\n");
			}
			//out.write(key + "=" + props.get(key)+"\n");
			out.close();
		} catch (FileNotFoundException e) {
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static boolean removePlace(String coords, World w) {
		Map<String,LootPlace> places = getData(w);
		return places.remove(coords) != null;
	}

	public static boolean add(String coords, LootPlace lp, World w) {
		Map<String,LootPlace> places = getData(w);
		if(places.containsKey(coords))
			return false;
		places.put(coords, lp);
		return true;
	}

	public static int count(World w) {
		Map<String,LootPlace> places = getData(w);
		return places.size();
	}

	public static void update(String coords, World w) {
		Map<String,LootPlace> places = getData(w);
		if(places.containsKey(coords))
			places.get(coords).update();
	}

	public static int lootArroundPlayer(Player player, int radius, boolean forceReplace) {
		Map<String,LootPlace> places = getData(player.getWorld());
		int count = 0;
		PlayerProfile pp = PlayerProfile.getPlayerProfile(player.getUniqueId().toString());
		Location loc = player.getLocation();
		for(int x = loc.getBlockX()-radius; x < loc.getBlockX()+radius; x++ )
		{
			for(int z = loc.getBlockZ()-radius; z < loc.getBlockZ()+radius; z++ )
			{
				for(int y = 0; y < 255; y ++)
				{
					Block b = player.getWorld().getBlockAt(x, y, z);
					if(b != null && b.getType().equals(Material.CHEST) && pp.activeCategory != null)
					{
						String coords = x+":"+y+":"+z;
						LootPlace lp = new LootPlace(coords+":"+pp.activeCategory+":"+pp.currentMin+":"+pp.currentMax,player.getWorld());
						if(forceReplace || !places.containsKey(coords))
						{
							if(places.containsKey(coords))
								places.remove(coords);
							places.put(coords, lp);
							count++;
						}
					}
				}
			}
		}
		return count;
	}
}
