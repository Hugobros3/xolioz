package io.xol.z.plugin.player;

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

import io.xol.chunkstories.api.player.Player;
import io.xol.z.plugin.XolioZPlugin;
import io.xol.z.plugin.loot.LootCategory;

//(c) 2014 XolioWare Interactive

public class PlayerProfile {

	public String name = null;
	public long uuid;

	public boolean inGame = false;
	// stats

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

	// money
	// public double xcBalance = 0;

	// map-building shit
	public boolean adding = true;
	public LootCategory activeCategory;
	public int currentMin = 1;
	public int currentMax = 5;

	// gameplay crap
	public String lastPlace = "";

	public Player talkingTo = null;

	// Anti combatlog
	public long lastHitTime = 0;

	// Admin
	public boolean disableSS = false;

	public PlayerProfile(long uuid2, String name) {
		this.name = name;
		this.uuid = uuid2;
		reloadProfile();
	}

	public PlayerProfile(long uuid2) {
		this.uuid = uuid2;
		reloadProfile();
	}

	public void reloadProfile() {

		File userProfile = new File(XolioZPlugin.pluginFolder+"users/" + uuid + ".dz");
		userProfile.getParentFile().mkdirs();

		if (userProfile.exists()) {
			String result = "";
			
			//Reads the ordeal
			try {
				InputStream ips = new FileInputStream(userProfile);
				InputStreamReader ipsr = new InputStreamReader(ips, "UTF-8");
				BufferedReader br = new BufferedReader(ipsr);
				String ligne;
				while ((ligne = br.readLine()) != null) {
					result += ligne + "\n";
				}
				br.close();
			} catch (IOException e) {
				e.printStackTrace();
			}

			String[] data = result.split("\n");

			if(name == null)
			{
				name = data[2];
				System.out.println("Loaded name from disk: "+name);
			}
			
			dateOfJoin = Integer.parseInt(data[3]);
			//??? 4

			if (timeConnected == 0) {
				timeConnected = Long.parseLong(data[5]);
				timeSurvivedTotal = Long.parseLong(data[6]);
				timeSurvivedLife = Long.parseLong(data[7]);
			}

			zombiesKilled = Integer.parseInt(data[8]);
			zombiesKilled_thisLife = Integer.parseInt(data[9]);

			playersKilled = Integer.parseInt(data[10]);
			playersKilled_thisLife = Integer.parseInt(data[11]);
			
			deaths = Integer.parseInt(data[12]);

			inGame = data[13].equals("1");

			timeCalc();
		}
	}

	public long timeCalc() {
		if (timeAtLastCalc == -1)
			timeAtLastCalc = System.currentTimeMillis();
		else {
			long timeToAdd = (System.currentTimeMillis() - timeAtLastCalc) / 1000;
			timeConnected += timeToAdd;
			if (inGame) {
				timeSurvivedTotal += timeToAdd;
				timeSurvivedLife += timeToAdd;
			}
			timeAtLastCalc = System.currentTimeMillis();
			return timeToAdd;
		}
		return 0;
	}

	public void saveProfile() {
		timeCalc();

		File userProfile = new File(XolioZPlugin.pluginFolder+"users/" + uuid + ".dz");
		try {
			Writer out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(userProfile), "UTF-8"));
			
			out.write("XolioZ user file format 1.0\n"); //0
			out.write(this.uuid+"\n"); //1
			out.write(this.name+"\n"); //2
			out.write(this.dateOfJoin+"\n"); //3
			out.write(System.currentTimeMillis()+"\n"); //4
			out.write(this.timeConnected+"\n"); //5
			out.write(this.timeSurvivedTotal+"\n"); //6
			out.write(this.timeSurvivedLife+"\n"); //7
			out.write(this.zombiesKilled+"\n"); //8
			out.write(this.zombiesKilled_thisLife+"\n"); //9
			out.write(this.playersKilled+"\n"); //10
			out.write(this.playersKilled_thisLife+"\n"); //11
			out.write(this.deaths+"\n"); //12
			out.write(this.inGame ? "1" : "0" + "\n"); //13
			out.write("\n"); //15
			out.write("\n"); //16
			
			out.close();
		} catch (FileNotFoundException e) {
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public double getTimeAlive() {
		return timeSurvivedLife;
	}
}
