//
// This file is a part of the XolioZ Mod for Chunk Stories
// Check out README.md for more information
// Website: https://chunkstories.xyz
// Github: https://github.com/Hugobros3/xolioz
//

package io.xol.z.plugin.loot;

// Copyright 2014 XolioWare Interactive

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import io.xol.chunkstories.api.item.ItemDefinition;
import io.xol.z.plugin.XolioZPlugin;

public class LootCategories {

	private final XolioZPlugin plugin;

	public LootCategories(XolioZPlugin dogeZPlugin) {
		plugin = dogeZPlugin;
	}

	public Map<String, LootCategory> categories = new HashMap<String, LootCategory>();

	public void loadTypes() {
		File file = new File(XolioZPlugin.pluginFolder + "lootCategories.dz");
		if (!file.exists())
			try {
				file.createNewFile();
			} catch (IOException e1) {
			}
		categories.clear();
		LootCategory currentCategory = null;
		try {
			InputStream ips = new FileInputStream(file);
			InputStreamReader ipsr = new InputStreamReader(ips, "UTF-8");
			BufferedReader br = new BufferedReader(ipsr);
			String ligne;
			while ((ligne = br.readLine()) != null) {
				if (ligne.startsWith("=")) {
					String line = ligne.replace("=", "");
					String name = line;
					float gsp = 1f;
					if (line.contains(":")) {
						String[] data = line.split(":");
						name = data[0];
						gsp = Float.parseFloat(data[1]);
					}
					currentCategory = new LootCategory(name, gsp);
					categories.put(name, currentCategory);
				} else {
					if (!ligne.startsWith("//")) {
						ItemDefinition ItemDefinition = plugin.getPluginExecutionContext().getContent().items().getItemDefinition(ligne.split(":")[0]);

						LootType addmeh = new LootType(ItemDefinition, ligne);
						if (currentCategory != null && addmeh.lootItem != null)
							currentCategory.add(addmeh);
						else
							System.out.println("Curent category was null.");
					}
				}
			}
			for (String cat : categories.keySet()) {
				categories.get(cat).computeProbs();
			}
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public LootCategory getCategory(String currentCategory) {
		LootCategory cg = categories.get(currentCategory);
		return cg;
	}
}
