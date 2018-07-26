//
// This file is a part of the XolioZ Mod for Chunk Stories
// Check out README.md for more information
// Website: https://chunkstories.xyz
// Github: https://github.com/Hugobros3/xolioz
//

package io.xol.z.plugin.util

import java.text.SimpleDateFormat
import java.util.Date

// Copyright 2014 XolioWare Interactive

object TimeFormatter {

    val currentDate: String
        get() {
            val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm")
            val date = Date()
            return dateFormat.format(date)
        }

    fun formatTimelapse(time: Long): String {
        val seconds = time % 60
        val minutes = Math.floor((time / 60).toDouble()).toInt() % 60
        val hours = Math.floor((time / (60 * 60)).toDouble()).toInt() % 24
        val days = Math.floor((time / (60 * 60 * 24)).toDouble()).toInt()
        return if (days == 0) {
            if (hours == 0) {
                minutes.toString() + "#{dogez.time.minutesand}" + seconds + "#{dogez.time.seconds}"
            } else hours.toString() + "#{dogez.time.hours}" + minutes + "#{dogez.time.minutesand}" + seconds + "#{dogez.time.seconds}"
        } else days.toString() + "#{dogez.time.days}" + hours + "#{dogez.time.hours}" + minutes + "#{dogez.time.minutesand}" + seconds + "#{dogez.time.seconds}"

    }
}
