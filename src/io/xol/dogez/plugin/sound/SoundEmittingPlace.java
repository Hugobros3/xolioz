package io.xol.dogez.plugin.sound;

import org.bukkit.Bukkit;
import org.bukkit.Location;

public class SoundEmittingPlace {

	Location loc;
	String soundName = "";
	int soundRepeatDelay = 5;
	long lastPlay = 0;
	
	public void update()
	{
		Bukkit.dispatchCommand(new FakeCommandBlock(),"");
	}
}
