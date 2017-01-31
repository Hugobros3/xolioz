package io.xol.dogez.plugin.player;

//(c) 2014 XolioWare Interactive

import io.xol.chunkstories.api.Location;
import io.xol.dogez.plugin.misc.HttpRequestThread;
import io.xol.dogez.plugin.misc.HttpRequester;

public class PlayerProfile implements HttpRequester {

	public String name;
	public long uuid;

	public boolean loadedSuccessfully = false;
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

	// Halloween shit

	public int death_level = -1;
	public String deathRequest = "";

	public boolean goToHell = false;

	// money and donnators features

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

	// public float heat = 0.5f;
	public String talkingTo = "";

	// Anti combatlog

	public long lastHitTime = 0;

	// Admin

	public boolean disableSS = false;

	static String apiHttpAccess = "http://37.187.125.96/dogez/chunkstories-port/api/";

	public PlayerProfile(long uuid2, String name) {
		this.name = name;
		this.uuid = uuid2;
		reloadProfile();
	}

	public void reloadProfile() {
		new HttpRequestThread(this, "reloadProfile", apiHttpAccess + "playerProfile.php",
				"a=load&uuid=" + uuid + "&name=" + name).start();
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
		if (!loadedSuccessfully)
			return;
		new HttpRequestThread(this, "saveProfile", apiHttpAccess + "playerProfile.php",
				"a=save&uuid=" + uuid + "&name=" + name + "&tc=" + timeConnected + "&tst=" + timeSurvivedTotal + "&tsl="
						+ timeSurvivedLife + "&zkt=" + zombiesKilled + "&zkl=" + zombiesKilled_thisLife + "&pkt="
						+ playersKilled + "&pkl=" + playersKilled_thisLife + "&d=" + deaths + "&ig="
						+ (inGame ? "1" : "0") + "&death=" + death_level + "&victim=" + this.deathRequest).start();
		setBalance();
	}

	public void addBalance(float amount) {
		xcBalance += amount;
		new HttpRequestThread(this, "changeBalance", apiHttpAccess + "playerProfile.php",
				"a=balance&uuid=" + uuid + "&diff=" + amount).start();
	}

	public void setBalance() {
		new HttpRequestThread(this, "changeBalance", apiHttpAccess + "playerProfile.php",
				"a=balance&uuid=" + uuid + "&bal=" + xcBalance).start();
	}

	@Override
	public void handleHttpRequest(String info, String result) {
		// System.out.println("[DogeZ][Debug] Request "+info+"
		// answered:"+result);
		if (info.equals("reloadProfile")) {
			if (!result.startsWith("p"))
				return;

			// 1:uuid 2:name 3:joindate 4:lastlogdate 5:balance 6:timeConnected
			// 7:timeSurvivedTotal 8:timeSurvivedLife
			// 9:zombiesKilledTotal 10:zombiesKilledLife 11:playersKilledTotal
			// 12:playersKilledLife 13:deaths 14:isIngame

			String[] data = result.split(":");

			dateOfJoin = Integer.parseInt(data[3]);
			xcBalance = Double.parseDouble(data[5]);

			if (timeConnected == 0) {
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
			if (deathRequest.equals("nobody"))
				deathRequest = "";
			timeCalc();

			loadedSuccessfully = true;
		}
	}

	public void onDeath() {
		new HttpRequestThread(this, "kill", apiHttpAccess + "api/playerProfile.php",
				"a=kill&uuid=" + uuid).start();
	}

	public double getTimeAlive() {
		return timeSurvivedLife;
	}
}
