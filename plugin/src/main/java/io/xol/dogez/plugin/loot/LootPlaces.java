package io.xol.dogez.plugin.loot;

import io.xol.chunkstories.api.Location;
import io.xol.chunkstories.api.player.Player;
import io.xol.chunkstories.api.voxel.Voxel;
import io.xol.chunkstories.api.world.World;
import io.xol.chunkstories.core.voxel.VoxelChest;
import io.xol.dogez.plugin.DogeZPlugin;
import io.xol.dogez.plugin.player.PlayerProfile;

import java.awt.image.BufferedImage;
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

import javax.imageio.ImageIO;

//Copyright 2014 XolioWare Interactive

public class LootPlaces {
	
	public static void main(String[] a)
	{
		int scale = 4;
		BufferedImage img = new BufferedImage(4096 / scale, 4096 / scale, BufferedImage.TYPE_INT_RGB);
		
		LootPlaces lp = new LootPlaces(null);
		lp.loadLootFile(null);
		
		int count = 0;
		for(LootPlace pl : lp.places.values())
		{
			int px = pl.x / scale;
			int py = pl.z / scale;
			
			img.setRGB(px, py, 255 << 16);
			
			count++;
		}
		
		try {
			ImageIO.write(img, "PNG", new File("loot-density.png"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		System.out.println("Found "+count+" lootplaces.");
	}
	
	private final DogeZPlugin plugin;

	public LootPlaces(DogeZPlugin dogeZPlugin) {
		plugin = dogeZPlugin;
	}
	
	public DogeZPlugin getPlugin()
	{
		return plugin;
	}
	
	//This class takes care of holding all the places on the world where the loot do spawns.
	Map<String,LootPlace> places = new HashMap<String,LootPlace>();
	
	public Map<String,LootPlace> getData(World w)
	{
		return places;
	}
	
	public int respawnLoot(World w)
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
	
	public void loadLootFile(World w)
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
				LootPlace lp = new LootPlace(this, ligne,w);
				if(lp.type != null)
				{
					String coords = lp.x+":"+lp.y+":"+lp.z;
					places.put(coords, lp);
				}
			}
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private File getFile(World w) {
		File file = new File("./plugins/DogeZ/lootPlaces-namalsk.dz");
		if(!file.exists())
			try {
				file.createNewFile();
			} 
			catch (IOException e1) {
			}
		return file;
	}

	public void saveLootFile(World w)
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
		file = new File("./plugins/DogeZ/backups/lootPlaces-"+System.currentTimeMillis()+".dz");
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

	public boolean removePlace(String coords, World w) {
		Map<String,LootPlace> places = getData(w);
		return places.remove(coords) != null;
	}

	public boolean add(String coords, LootPlace lp, World w) {
		Map<String,LootPlace> places = getData(w);
		if(places.containsKey(coords))
			return false;
		places.put(coords, lp);
		return true;
	}

	public int count(World w) {
		Map<String,LootPlace> places = getData(w);
		return places.size();
	}

	public void update(String coords, World w) {
		Map<String,LootPlace> places = getData(w);
		
		if(places.containsKey(coords))
			places.get(coords).update();
	}

	public int lootArroundPlayer(Player player, int radius, boolean forceReplace) {
		Map<String,LootPlace> places = getData(player.getControlledEntity().getWorld());
		int count = 0;
		PlayerProfile pp = plugin.getPlayerProfiles().getPlayerProfile(player.getUUID());
		Location loc = player.getLocation();
		for(int x = (int) (loc.x()-radius); x < loc.x()+radius; x++ )
		{
			for(int z = (int) (loc.z()-radius); z < loc.z()+radius; z++ )
			{
				for(int y = 0; y < 255; y ++)
				{
					Voxel v = plugin.getServer().getContent().voxels().getVoxelById(player.getControlledEntity().getWorld().getVoxelData(x, y, z));
					
					if(v != null && v instanceof VoxelChest && pp.activeCategory != null)
					{
						String coords = x+":"+y+":"+z;
						LootPlace lp = new LootPlace(this, coords+":"+pp.activeCategory+":"+pp.currentMin+":"+pp.currentMax,player.getControlledEntity().getWorld());
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
