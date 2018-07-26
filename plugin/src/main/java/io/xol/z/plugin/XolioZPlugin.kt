//
// This file is a part of the XolioZ Mod for Chunk Stories
// Check out README.md for more information
// Website: https://chunkstories.xyz
// Github: https://github.com/Hugobros3/xolioz
//

package io.xol.z.plugin

import java.io.*
import java.util.logging.Logger

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import io.xol.chunkstories.api.content.mods.Mod
import io.xol.chunkstories.api.player.Player
import io.xol.chunkstories.api.plugin.PluginInformation
import io.xol.chunkstories.api.plugin.ServerPlugin
import io.xol.chunkstories.api.server.ServerInterface
import io.xol.chunkstories.api.world.World
import io.xol.chunkstories.api.world.WorldMaster
import io.xol.z.plugin.game.*
import io.xol.z.plugin.loot.LootCategories
import io.xol.z.plugin.loot.LootPlaces
import io.xol.z.plugin.loot.crashes.CrashesHandler
import io.xol.z.plugin.map.PlacesNames
import io.xol.z.plugin.player.PlayerProfiles
import io.xol.z.plugin.zombies.ZombiesPopulation

class XolioZPlugin(pluginInformation: PluginInformation, clientInterface: ServerInterface) : ServerPlugin(pluginInformation, clientInterface) {

    val playerProfiles = PlayerProfiles(this)
    var spawner = ZombiesPopulation(this)

    private var scheduledEvents: ScheduledEvents? = null

    private val entityListener = EntityListener(this)
    private val playerListener = PlayerListener(this)

    val lootTypes = LootCategories(this)
    val lootPlaces = LootPlaces(this)

    val crashesHandler = CrashesHandler(this)

    val talkieWalkiesHandler = TalkieWalkiesHandler(this)

    private val mod_present: Boolean

    var version = "undefined"
    var isGameActive = false

    var config = Config()

    private val gson: Gson

    val gameWorld: WorldMaster
        get() = server.world

    val logger: Logger
        get() = Logger.getLogger("XolioZ")

    init {

        gson = GsonBuilder().setPrettyPrinting().create()

        // Non-presence of the accompanying mod in the server configuration is fatal
        var mod_present = false
        for (mod in this.getPluginExecutionContext().content.modsManager().currentlyLoadedMods) {
            if (mod.modInfo.internalName == "xolioz")
                mod_present = true
        }

        this.mod_present = mod_present
    }

    override fun onEnable() {
        version = "" + this.pluginInformation.pluginVersion

        this.logger.info("Initializing plugin version $version ...")

        if (!mod_present) {
            this.logger.info("'xolioz' mod not found in loaded mods, not enabling the plugin. " + "Please put 'xolioz_content.zip' in your mods/ folder and enable it through the --mods launch argument.")
            return
        }

        checkFolder()
        loadConfigs()

        server.pluginManager.registerEventListener(entityListener, this)
        server.pluginManager.registerEventListener(playerListener, this)
        server.pluginManager.registerEventListener(crashesHandler, this)

        val cliHandler = XolioZCommandsHandler(this)
        this.pluginManager.registerCommandHandler("dz", cliHandler)
        this.pluginManager.registerCommandHandler("r", cliHandler)
        this.pluginManager.registerCommandHandler("m", cliHandler)

        // fix reloading issues
        for (p in gameWorld.players)
            playerProfiles.addPlayerProfile(p.uuid, p.name)

        scheduledEvents = ScheduledEvents(this)

        // done
        isGameActive = true
        this.logger.info("Plugin initialized.")
    }

    private fun checkFolder() {
        val folder = File(pluginFolder)
        if (!folder.exists()) {
            this.logger.info("Couldn't find the plugins/XolioZ/ folder; creating it and filling it using the default settings.")
            folder.mkdir()
            UnpackDefaults(folder)
        }
    }

    fun loadConfigs() {
        // Load configs
        try {
            val configFile = File(pluginFolder + "config.json")
            if (configFile.exists() && configFile.isFile)
                config = gson.fromJson(FileReader(configFile), Config::class.java)
        } catch (e: IOException) {
            e.printStackTrace()
        }

        // lootItems.loadItems();
        lootTypes.loadTypes()

        val world = server.world
        lootPlaces.loadLootFile(world)

        PlacesNames.loadData()
        SpawnPoints.load()
    }

    private fun saveConfigs() {
        // Save configs
        try {
            val writer = FileWriter(File(pluginFolder + "config.json"))
            gson.toJson(config, writer)
            writer.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        val world = server.world
        lootPlaces.saveLootFile(world)
    }

    override fun onDisable() {
        if (!mod_present)
            return

        scheduledEvents!!.unschedule()

        saveConfigs()
        this.logger.info("Plugin disabling... waiting 2s for requests to complete...")
        playerProfiles.saveAll()

        try {
            Thread.sleep(2000L)
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }

        this.logger.info("Done, terminated")
    }

    companion object {

        val pluginFolder = "./plugins/XolioZ/"
    }
}
