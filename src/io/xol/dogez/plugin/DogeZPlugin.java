package io.xol.dogez.plugin;

//Copyright 2014 XolioWare Interactive

import java.io.File;
import java.util.logging.Logger;

import io.xol.chunkstories.api.plugin.ChunkStoriesPlugin;
import io.xol.chunkstories.api.server.Player;
import io.xol.chunkstories.api.world.World;
import io.xol.chunkstories.server.Server;
import io.xol.dogez.plugin.game.BlockListener;
import io.xol.dogez.plugin.game.DogeZPluginCommandsHandler;
import io.xol.dogez.plugin.game.EntityListener;
import io.xol.dogez.plugin.game.PlayerListener;
import io.xol.dogez.plugin.game.ScheduledEvents;
import io.xol.dogez.plugin.game.SpawnPoints;
import io.xol.dogez.plugin.loot.LootItems;
import io.xol.dogez.plugin.loot.LootPlaces;
import io.xol.dogez.plugin.loot.LootTypes;
import io.xol.dogez.plugin.map.PlacesNames;
//import io.xol.dogez.plugin.misc.PlayersLister;
import io.xol.dogez.plugin.player.PlayerProfile;
import io.xol.dogez.plugin.weapon.ChunksCleaner;
import io.xol.dogez.plugin.zombies.ZombieSpawner;

public class DogeZPlugin extends ChunkStoriesPlugin {
	
	public static String version = "undefined";
	public static boolean isGameActive = false;
	
	public static Config config = new Config();
	public static DogeZPlugin access;
	
	//listeners
	BlockListener blockListener = new BlockListener();
	EntityListener entityListener = new EntityListener();
	PlayerListener playerListener = new PlayerListener();
	
	public static ZombieSpawner spawner;
	
	@Override
	public void onEnable() {
		access = this;
		//Splash screen !
		DogeZPlugin.version = ""+this.getPluginInformation().getPluginVersion();
		this.getLogger().info("[DogeZ] Initializing plugin version "+DogeZPlugin.version+" ...");
		//Plugin initialisation !
			checkFolder();
			loadConfigs();
			//initialize handlers
			
			getServer().getPluginsManager().registerEventListener(blockListener, this);
			getServer().getPluginsManager().registerEventListener(entityListener, this);
			entityListener.plugin = this;
			getServer().getPluginsManager().registerEventListener(playerListener, this);
			
			DogeZPluginCommandsHandler dogezCmdHandler = new DogeZPluginCommandsHandler(this);
			this.getPluginsManager().registerCommandHandler("dz", dogezCmdHandler);
			this.getPluginsManager().registerCommandHandler("r", dogezCmdHandler);
			this.getPluginsManager().registerCommandHandler("m", dogezCmdHandler);
			
			//Initlialize custom behaviors
			spawner = new ZombieSpawner(this);
			
			/*Weapon.init(this);
			Ammo.init(this);
			NMSTools.setMaxStackSize((332), 1);// Snowball/grenade nostack
			NMSTools.setMaxStackSize((344), 1);// egg/flash nostack
			NMSTools.setMaxStackSize((405), 1);// talkie-walkie
			NMSTools.setMaxStackSize((294), 1);// essence
			NMSTools.setMaxStackSize((341), 1);// roquettes*/
			//fix reloading issues
			for(Player p : config.getWorld().getPlayers())
				PlayerProfile.addPlayerProfile(p.getUUID(), p.getName());
			//start threads
			
			//PlayersLister.setup(this);
			ScheduledEvents.startEvents(this);
		//done
		isGameActive = true;
		this.getLogger().info("[DogeZ] Plugin initialized.");
	}

	private void checkFolder() {
		File folder = new File("./plugins/DogeZ");
		if(folder.exists())
			folder.mkdir();
	}

	public void loadConfigs() {
		//Load configs
		DogeZPlugin.config.load();
		LootItems.loadItems();
		LootTypes.loadTypes();
		//for(World w : getServer().getWorlds())
		//{
		World w = Server.getInstance().getWorld();
			LootPlaces.loadLootFile(w);
		//}
		PlacesNames.loadData();
		SpawnPoints.load();
	}
	
	private void saveConfigs() {
		//Save configs
		DogeZPlugin.config.save();
		//for(World w : getServer().getWorlds())
		//{
			World w = Server.getInstance().getWorld();
			LootPlaces.saveLootFile(w);
		//}
	}

	@Override
	public void onDisable() {
		saveConfigs();
		ChunksCleaner.cleanAllChunks(config.getWorld());
		this.getLogger().info("[DogeZ] Plugin disabling... waiting 2s for requests to complete...");
		PlayerProfile.saveAll();
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

	public static boolean isActive()
	{
		return isGameActive;
	}
}
