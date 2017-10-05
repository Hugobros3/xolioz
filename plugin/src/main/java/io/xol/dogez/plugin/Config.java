package io.xol.dogez.plugin;

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
	
	//misc-related crap
	public boolean isTimeLinked = false;
	public int timeLinkDelayInMinutes = 0;
	
	//economy-related crap
	
	//loot-related shit
	public int timeBetweenReloots = 600; // in seconds
	public boolean synchTime = true;
	
	public void load() {
		// TODO Auto-generated method stub
		
	}

	public void save() {
		// TODO Auto-generated method stub
		
	}
	
}
