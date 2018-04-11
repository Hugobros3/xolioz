package io.xol.dogez.plugin;

//Copyright 2014 XolioWare Interactive

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.logging.Logger;

import io.xol.chunkstories.api.content.mods.Mod;
import io.xol.chunkstories.api.player.Player;
import io.xol.chunkstories.api.plugin.PluginInformation;
import io.xol.chunkstories.api.plugin.ServerPlugin;
import io.xol.chunkstories.api.server.ServerInterface;
import io.xol.chunkstories.api.world.World;
import io.xol.chunkstories.api.world.WorldMaster;
import io.xol.dogez.plugin.game.XolioZCommandsHandler;
import io.xol.dogez.plugin.game.EntityListener;
import io.xol.dogez.plugin.game.PlayerListener;
import io.xol.dogez.plugin.game.ScheduledEvents;
import io.xol.dogez.plugin.game.SpawnPoints;
import io.xol.dogez.plugin.game.TalkieWalkiesHandler;
import io.xol.dogez.plugin.loot.LootPlaces;
import io.xol.dogez.plugin.loot.crashes.CrashesHandler;
import io.xol.dogez.plugin.loot.LootCategories;
import io.xol.dogez.plugin.map.PlacesNames;
import io.xol.dogez.plugin.player.PlayerProfiles;
import io.xol.dogez.plugin.zombies.ZombiesPopulation;

public class XolioZGamemodePlugin extends ServerPlugin {

	private PlayerProfiles playerProfiles = new PlayerProfiles(this);
	private ScheduledEvents scheduledEvents;

	private EntityListener entityListener = new EntityListener(this);
	private PlayerListener playerListener = new PlayerListener(this);

	private LootCategories lootTypes = new LootCategories(this);
	private LootPlaces lootPlaces = new LootPlaces(this);

	private final CrashesHandler crashesHandler = new CrashesHandler(this);

	private TalkieWalkiesHandler talkieWalkiesHandler = new TalkieWalkiesHandler(this);

	private final boolean mod_present;

	public static final String pluginFolder = "./plugins/XolioZ/";

	public XolioZGamemodePlugin(PluginInformation pluginInformation, ServerInterface clientInterface) {
		super(pluginInformation, clientInterface);

		// Non-presence of the accompanying mod in the server configuration is fatal
		boolean mod_present = false;
		for (Mod mod : this.getPluginExecutionContext().getContent().modsManager().getCurrentlyLoadedMods()) {
			if (mod.getModInfo().getInternalName().equals("xolioz"))
				mod_present = true;
		}

		this.mod_present = mod_present;
	}

	public String version = "undefined";
	public boolean isGameActive = false;

	public Config config = new Config();

	public ZombiesPopulation spawner;

	@Override
	public void onEnable() {
		version = "" + this.getPluginInformation().getPluginVersion();

		this.getLogger().info("[XolioZ] Initializing plugin version " + version + " ...");

		if (!mod_present) {
			this.getLogger().info("[XolioZ] 'xolioz' mod not found in loaded mods, not enabling the plugin. "
					+ "Please put 'xolioz_content.zip' in your mods/ folder and enable it through the --mods launch argument.");
			return;
		}

		checkFolder();
		loadConfigs();

		getServer().getPluginManager().registerEventListener(entityListener, this);
		getServer().getPluginManager().registerEventListener(playerListener, this);
		getServer().getPluginManager().registerEventListener(crashesHandler, this);

		XolioZCommandsHandler dogezCmdHandler = new XolioZCommandsHandler(this);
		this.getPluginManager().registerCommandHandler("dz", dogezCmdHandler);
		this.getPluginManager().registerCommandHandler("r", dogezCmdHandler);
		this.getPluginManager().registerCommandHandler("m", dogezCmdHandler);

		// Initlialize custom behaviors
		spawner = new ZombiesPopulation(this);

		// fix reloading issues
		for (Player p : getGameWorld().getPlayers())
			getPlayerProfiles().addPlayerProfile(p.getUUID(), p.getName());

		scheduledEvents = new ScheduledEvents(this);

		// done
		isGameActive = true;
		this.getLogger().info("[XolioZ] Plugin initialized.");
	}

	private void checkFolder() {
		File folder = new File(pluginFolder);
		if (!folder.exists()) {
			this.getLogger().info("Couldn't find the plugins/XolioZ/ folder; creating it and filling it using the default settings.");
			folder.mkdir();
			new UnpackDefaults(folder);
		}
	}

	public void loadConfigs() {
		// Load configs
		try {
			config.load(new FileInputStream(new File(pluginFolder + "config.dz")));
		} catch (IOException e) {
			e.printStackTrace();
		}

		// lootItems.loadItems();
		lootTypes.loadTypes();

		World world = getServer().getWorld();
		lootPlaces.loadLootFile(world);

		PlacesNames.loadData();
		SpawnPoints.load();
	}

	private void saveConfigs() {
		// Save configs
		try {
			config.store(new FileOutputStream(new File(pluginFolder + "config.dz")), null);
		} catch (IOException e) {
			e.printStackTrace();
		}

		World world = getServer().getWorld();
		lootPlaces.saveLootFile(world);
	}

	public WorldMaster getGameWorld() {
		return getServer().getWorld();
	}

	@Override
	public void onDisable() {
		if (!mod_present)
			return;

		scheduledEvents.unschedule();

		saveConfigs();
		this.getLogger().info("[XolioZ] Plugin disabling... waiting 2s for requests to complete...");
		getPlayerProfiles().saveAll();

		try {
			Thread.sleep(2000L);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		this.getLogger().info("[XolioZ] Done, terminated");
	}

	public Logger getLogger() {
		return Logger.getGlobal();
	}

	public boolean isActive() {
		return isGameActive;
	}

	public PlayerProfiles getPlayerProfiles() {
		return playerProfiles;
	}

	public TalkieWalkiesHandler getTalkieWalkiesHandler() {
		return talkieWalkiesHandler;
	}

	public LootCategories getLootTypes() {
		return lootTypes;
	}

	public LootPlaces getLootPlaces() {
		return lootPlaces;
	}

	public CrashesHandler getCrashesHandler() {
		return crashesHandler;
	}
}
