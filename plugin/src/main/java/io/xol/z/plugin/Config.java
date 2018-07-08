package io.xol.z.plugin;

import java.util.Properties;

//Copyright 2014 XolioWare Interactive

/** 
 * This class holds the configuration of the plugin in a Properties file, but provides some helper methods
 */
public class Config extends Properties {
	private static final long serialVersionUID = -8929515019749259838L;

	public int getInt(String property) {
		return getInt(property, 0);
	}
	
	public int getInt(String property, int defaultValue) {
		try {
			return Integer.parseInt(this.getProperty(property));
		} catch(NumberFormatException e) {
			this.setProperty(property, ""+defaultValue);
			return defaultValue;
		}
	}
	
	public float getFloat(String property) {
		return getFloat(property, 0.0f);
	}
	
	public float getFloat(String property, float defaultValue) {
		try {
			return Float.parseFloat(this.getProperty(property));
		} catch(NumberFormatException e) {
			this.setProperty(property, ""+defaultValue);
			return defaultValue;
		}
	}

	public boolean getBoolean(String property) {
		return getBoolean(property, false);
	}
	
	public boolean getBoolean(String property, boolean defaultValue) {
		String isSet = this.getProperty(property);
		if(isSet != null) {
			return Boolean.parseBoolean(isSet);
		} else {
			this.setProperty(property, defaultValue ? "true" : "false");
			return defaultValue;
		}
	}
}
