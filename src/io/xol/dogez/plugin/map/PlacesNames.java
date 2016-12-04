package io.xol.dogez.plugin.map;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;

import io.xol.chunkstories.api.Location;
import io.xol.chunkstories.api.server.Player;

//(c) 2014 XolioWare Interactive

public class PlacesNames {

	static BufferedImage placesImage;
	static Map<Integer,String> placesNames;
	static boolean initialized = false;
	
	public static void loadData()
	{
		placesNames = new HashMap<Integer,String>();
		try {
			System.out.println("[DogeZ-Plugin] Loading places names...");
			placesImage = ImageIO.read(new File("./plugins/DogeZ/places.png"));
			InputStream ips = new FileInputStream(new File("./plugins/DogeZ/places.dz"));
			InputStreamReader ipsr = new InputStreamReader(ips, "UTF-8");
			BufferedReader br = new BufferedReader(ipsr);
			String ligne;
			while ((ligne = br.readLine()) != null) {
				if(ligne.contains(":"))
				{
					String[] s = ligne.split(":");
					placesNames.put(hexToInt(s[0]), s[1]);
				}
			}
			br.close();
			initialized = true;
			System.out.println("[DogeZ-Plugin] Done !");
		} catch (IOException e) {
			System.out.println("[DogeZ-Plugin] Error while loading places names:");
			e.printStackTrace();
		}
	}
	
	public static String getPlayerPlaceName(Player player)
	{
		//System.out.println("gppn");
		
		//return "nope";
		if(!initialized)
			return "Erreur";
		Location loc = player.getLocation();
		if(isInMap(loc))
		{
			int rgb = placesImage.getRGB((int)(loc.getX())/2, (int)(loc.getZ())/2);
			rgb+=16777216;
			//System.out.println("rgb"+rgb);
			if(rgb == 0)
				return "En pleine nature";
			String name = placesNames.get(rgb);
			if(name == null)
				name = "Dans une zone erronée ( "+rgb+" )";
			return name;
		}
		else
		{
			return "Hors de la map";
		}
	}
	
	public static boolean isInMap(Location loc)
	{
		return true; //(loc.getBlockX() > -2000 && loc.getBlockX() < 1000 && loc.getBlockZ() > -1000 && loc.getBlockZ() < 1000 );
	}
	
	//Hex tools from DoP
	
	static char[] hexTable = {'0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'};
	
	public static int hexToInt(String hex)
	{
		int value = 0;
		for(char c : hex.toCharArray())
		{
			value*=16;
			int index = 0;
			for(char d : hexTable)
			{
				if(c == d)
				{
					value+=index;
				}
				index++;
			}
		}
		return value;
	}
}
