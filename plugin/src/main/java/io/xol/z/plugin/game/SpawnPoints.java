//
// This file is a part of the XolioZ Mod for Chunk Stories
// Check out README.md for more information
// Website: https://chunkstories.xyz
// Github: https://github.com/Hugobros3/xolioz
//

package io.xol.z.plugin.game;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import io.xol.z.plugin.XolioZPlugin;

// Copyright 2014 XolioWare Interactive

public class SpawnPoints {

	static List<int[]> points = new ArrayList<int[]>();

	public static void load() {
		points.clear();
		File file = getFile();
		try {
			InputStream ips = new FileInputStream(file);
			InputStreamReader ipsr = new InputStreamReader(ips, "UTF-8");
			BufferedReader br = new BufferedReader(ipsr);
			String ligne;
			while ((ligne = br.readLine()) != null) {
				String[] data = ligne.split(" ");
				if (data.length >= 3) {
					int[] coords = { Integer.parseInt(data[0]), Integer.parseInt(data[1]), Integer.parseInt(data[2]) };
					points.add(coords);
				}
			}
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("[DogeZ]" + points.size() + " spawn points loaded.");
	}

	private static File getFile() {
		File file = new File(XolioZPlugin.pluginFolder + "spawnPoints.dz");
		if (!file.exists())
			try {
				file.createNewFile();
			} catch (IOException e1) {
			}
		return file;
	}

	public static int[] getRandomSpawn() {
		Random random = new Random();
		return points.get(random.nextInt(points.size()));
	}
}
