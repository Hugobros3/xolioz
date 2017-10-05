package io.xol.dogez.plugin;

import java.util.Properties;

//Copyright 2014 XolioWare Interactive

public class Config extends Properties {
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
	
	public int getInt(String property) {
		try {
			return Integer.parseInt(this.getProperty(property));
		} catch(NumberFormatException e) {
			return 0;
		}
	}
	
	public float getFloat(String property) {
		try {
			return Float.parseFloat(this.getProperty(property));
		} catch(NumberFormatException e) {
			return 0.0f;
		}
	}
	
	public boolean getBoolean(String property) {
		return Boolean.parseBoolean(this.getProperty(property));
	}
}
