package io.xol.dogez.plugin.loot;

import io.xol.dogez.plugin.misc.ChatFormatter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LootItems {

	public static Map<String,LootItem> lootItems = new HashMap<String,LootItem>();
	public static LootItem failItem = new LootItem("&cI'm a failed loot entry !","failedentry",1,0);
	
	public static LootItem getItem(String name) {
		LootItem li = lootItems.get(name);
		if(li == null)
			li = failItem;
		return li;
	}
	
	public static void loadItems()
	{
		File file = new File("./plugins/DogeZ/lootItems.dz");
		if(!file.exists())
			try {
				file.createNewFile();
			} catch (IOException e1) {
			}
		lootItems.clear();
		try {
			InputStream ips = new FileInputStream(file);
			InputStreamReader ipsr = new InputStreamReader(ips, "UTF-8");
			BufferedReader br = new BufferedReader(ipsr);
			String ligne;
			while ((ligne = br.readLine()) != null) {
				
					if(!ligne.startsWith("//"))
					{
						String[] data = ligne.split(":");
						if(data.length >= 4)
						{
							// 0:techname 1:typeId 2:metaData 3:realName
							LootItem li = new LootItem(data[3],data[0],Integer.parseInt(data[1]),Integer.parseInt(data[2]));
							if(data.length >= 5)
							{
								List<String> descLines = new ArrayList<String>();
								for( String line : data[4].split(";;"))
								{
									descLines.add(ChatFormatter.convertString(line));
								}
								li.description = descLines;
							}
							lootItems.put(data[0], li);
						}
					}
			}
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static boolean contains(String line) {
		return lootItems.containsKey(line);
	}
}
