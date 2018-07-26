//
// This file is a part of the XolioZ Mod for Chunk Stories
// Check out README.md for more information
// Website: https://chunkstories.xyz
// Github: https://github.com/Hugobros3/xolioz
//

package io.xol.z.plugin.util

import java.io.File
import java.io.FileOutputStream
import java.io.IOException

//TODO .use
class UnpackDefaults(folder: File) {
    init {
        try {
            val files = this.javaClass.getResourceAsStream("/default/defaultFiles.txt").bufferedReader().use { it.readText() }

            for (line in files.lines()) {
                this.javaClass.getResourceAsStream("/default/$line").copyTo(FileOutputStream(File(folder.absolutePath + "/$line")))
                println("Unpacked $line")
            }

        } catch (e: IOException) {

        }

    }
}