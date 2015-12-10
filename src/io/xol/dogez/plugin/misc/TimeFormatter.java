package io.xol.dogez.plugin.misc;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

//Copyright 2014 XolioWare Interactive

public class TimeFormatter {
	public static String formatTimelapse(long time)
	{
		long seconds = time % 60;
		int minutes = (int) Math.floor(time / 60) % 60;
		int hours = (int) Math.floor(time / (60*60)) % 24;
		int days = (int) Math.floor(time / (60*60*24));
		if(days == 0)
		{
			if(hours == 0)
			{
				return minutes+" minutes et "+seconds+" secondes";
			}
			return hours+" heures, "+minutes+" minutes et "+seconds+" secondes";
		}
		
		return days+" jours, "+hours+" heures, "+minutes+" minutes et "+seconds+" secondes";
	}
	
	public static String getCurrentDate()
	{
		DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");
		Date date = new Date();
		return dateFormat.format(date);
	}
}
