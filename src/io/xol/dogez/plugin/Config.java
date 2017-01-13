package io.xol.dogez.plugin;

import io.xol.chunkstories.server.Server;
import io.xol.chunkstories.world.WorldServer;

//Copyright 2014 XolioWare Interactive

public class Config {
	//This class holds the configuration of the plugin.
	
	//general bullshit
	public String activeWorld = "world";
	public boolean showUpConnectionMessages = true;
	
	//zombie-related crap
	public float zombiesSpeedMultiplier = 1f;
	public float zombiesHealth = 10;
	public float zombiesDamageMultipler = 1f;
	public int maxZombies = 450;
	public String[] toleratedMobs = {"ZOMBIE","PIG","SHEEP","COW","PLAYER","BAT"};
	
	//misc-related crap
	public boolean isTimeLinked = false;
	public int timeLinkDelayInMinutes = 0;
	
	//economy-related crap
	
	//loot-related shit
	public int timeBetweenReloots = 600; // in seconds
	public boolean synchTime = true;

	public String apiHttpAccess = "http://51.254.129.247/dogez/chunkstories-port/api/";//"http://dz.xol.io/chunkstories-port/api";
	
	public void load() {
		// TODO Auto-generated method stub
		
	}

	public WorldServer getWorld() {
		//TODO make WorldMaster handle players etc
		return (WorldServer)DogeZPlugin.access.getServer().getWorld();
	}

	public void save() {
		// TODO Auto-generated method stub
		
	}
	
}
