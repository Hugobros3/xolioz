//
// This file is a part of the XolioZ Mod for Chunk Stories
// Check out README.md for more information
// Website: https://chunkstories.xyz
// Github: https://github.com/Hugobros3/xolioz
//

package io.xol.z.plugin.misc;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

// Copyright 2014 XolioWare Interactive

public class TimeFormatter {
	public static String formatTimelapse(long time) {
		long seconds = time % 60;
		int minutes = (int) Math.floor(time / 60) % 60;
		int hours = (int) Math.floor(time / (60 * 60)) % 24;
		int days = (int) Math.floor(time / (60 * 60 * 24));
		if (days == 0) {
			if (hours == 0) {
				return minutes + "#{dogez.time.minutesand}" + seconds + "#{dogez.time.seconds}";
			}
			return hours + "#{dogez.time.hours}" + minutes + "#{dogez.time.minutesand}" + seconds + "#{dogez.time.seconds}";
		}

		return days + "#{dogez.time.days}" + hours + "#{dogez.time.hours}" + minutes + "#{dogez.time.minutesand}" + seconds + "#{dogez.time.seconds}";
	}

	public static String getCurrentDate() {
		DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");
		Date date = new Date();
		return dateFormat.format(date);
	}
}
