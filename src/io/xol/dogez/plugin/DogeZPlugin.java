package io.xol.dogez.plugin;

//Copyright 2014 XolioWare Interactive

import java.io.File;
import java.util.logging.Logger;

import io.xol.chunkstories.api.plugin.PluginInformation;
import io.xol.chunkstories.api.plugin.ServerPlugin;
import io.xol.chunkstories.api.server.Player;
import io.xol.chunkstories.api.server.ServerInterface;
import io.xol.chunkstories.api.world.World;
import io.xol.chunkstories.api.world.WorldMaster;
import io.xol.dogez.plugin.economy.SignsShopsHandlers;
import io.xol.dogez.plugin.game.DogeZPluginCommandsHandler;
import io.xol.dogez.plugin.game.EntityListener;
import io.xol.dogez.plugin.game.PlayerListener;
import io.xol.dogez.plugin.game.ScheduledEvents;
import io.xol.dogez.plugin.game.SpawnPoints;
import io.xol.dogez.plugin.game.TalkieWalkiesHandler;
import io.xol.dogez.plugin.loot.LootItems;
import io.xol.dogez.plugin.loot.LootPlaces;
import io.xol.dogez.plugin.loot.LootTypes;
import io.xol.dogez.plugin.map.PlacesNames;
import io.xol.dogez.plugin.player.PlayerProfiles;
import io.xol.dogez.plugin.weapon.ChunksCleaner;
import io.xol.dogez.plugin.zombies.ZombieSpawner;

public class DogeZPlugin extends ServerPlugin {

	private PlayerProfiles playerProfiles = new PlayerProfiles(this);
	private ScheduledEvents scheduledEvents;
	private SignsShopsHandlers signShop = new SignsShopsHandlers(this);

	private EntityListener entityListener = new EntityListener(this);
	private PlayerListener playerListener = new PlayerListener(this);
	
	private LootItems lootItems = new LootItems(this);
	private LootTypes lootTypes = new LootTypes(this);
	private LootPlaces lootPlaces = new LootPlaces(this);
	
	private TalkieWalkiesHandler talkieWalkiesHandler = new TalkieWalkiesHandler(this);
	
	public DogeZPlugin(PluginInformation pluginInformation, ServerInterface clientInterface) {
		super(pluginInformation, clientInterface);
	}

	public String version = "undefined";
	public boolean isGameActive = false;

	public Config config = new Config();

	public ZombieSpawner spawner;

	@Override
	public void onEnable() {
		// Splash screen !
		version = "" + this.getPluginInformation().getPluginVersion();
		
		this.getLogger().info("[DogeZ] Initializing plugin version " + version + " ...");
		// Plugin initialisation !
		checkFolder();
		loadConfigs();
		// initialize handlers

		getServer().getPluginManager().registerEventListener(entityListener, this);
		getServer().getPluginManager().registerEventListener(playerListener, this);

		DogeZPluginCommandsHandler dogezCmdHandler = new DogeZPluginCommandsHandler(this);
		this.getPluginManager().registerCommandHandler("dz", dogezCmdHandler);
		this.getPluginManager().registerCommandHandler("r", dogezCmdHandler);
		this.getPluginManager().registerCommandHandler("m", dogezCmdHandler);

		// Initlialize custom behaviors
		spawner = new ZombieSpawner(this);

		// fix reloading issues
		for (Player p : getGameWorld().getPlayers())
			getPlayerProfiles().addPlayerProfile(p.getUUID(), p.getName());

		scheduledEvents = new ScheduledEvents(this);
		
		// done
		isGameActive = true;
		this.getLogger().info("[DogeZ] Plugin initialized.");
	}

	private void checkFolder() {
		File folder = new File("./plugins/DogeZ");
		if (folder.exists())
			folder.mkdir();
	}

	public void loadConfigs() {
		// Load configs
		config.load();
		
		lootItems.loadItems();
		lootTypes.loadTypes();
		
		World world = getServer().getWorld();
		lootPlaces.loadLootFile(world);
		
		PlacesNames.loadData();
		SpawnPoints.load();
	}

	private void saveConfigs() {
		// Save configs
		config.save();
		
		World w = getServer().getWorld();
		lootPlaces.saveLootFile(w);
	}

	public WorldMaster getGameWorld()
	{
		return getServer().getWorld();
	}
	
	@Override
	public void onDisable() {
		scheduledEvents.unschedule();
		
		saveConfigs();
		ChunksCleaner.cleanAllChunks(this.getGameWorld());
		this.getLogger().info("[DogeZ] Plugin disabling... waiting 2s for requests to complete...");
		getPlayerProfiles().saveAll();
		
		try {
			Thread.sleep(2000L);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		this.getLogger().info("[DogeZ] Done, terminated");
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
	
	public SignsShopsHandlers getSignShopsHandler()
	{
		return signShop;
	}
	
	public TalkieWalkiesHandler getTalkieWalkiesHandler()
	{
		return talkieWalkiesHandler;
	}
	
	public LootItems getLootItems()
	{
		return lootItems;
	}
	
	public LootTypes getLootTypes()
	{
		return lootTypes;
	}
	
	public LootPlaces getLootPlaces()
	{
		return lootPlaces;
	}
}
