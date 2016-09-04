package io.xol.dogez.plugin.game.special;

import io.xol.chunkstories.api.Location;
import io.xol.chunkstories.api.server.Player;

//Copyright 2014 XolioWare Interactive

public class SafeTeleporter {
	
	public static void safeTeleport(Player player, Location location)
	{
		//ChunkStories _should_ be somewhat safe in terms of teleportation so i'm dropping this for now
		
		//SafeTPTask safeTPTask = new SafeTPTask(location, player);
		
		//int taskID = Bukkit.getScheduler().scheduleSyncRepeatingTask(DogeZPlugin.access, safeTPTask, 0L, 20L);
		//safeTPTask.id = taskID;
	}
	
	/*static class SafeTPTask implements Runnable{
		Location loc;
		Player player;
		
		int id = -1;
		
		SafeTPTask(Location loc, Player player)
		{
			this.player = player;
			this.loc = loc;
		}
		
		@Override
        public void run() {
            if(loc.getWorld().isChunkLoaded(loc.getBlockX(), loc.getBlockZ()))
            {
            	player.teleport(loc);
            	Bukkit.getScheduler().cancelTask(id);
            	//System.out.println("Done, cancelling task !");
            }
            else
            {
            	loc.getWorld().loadChunk(loc.getBlockX(), loc.getBlockZ());
            	//System.out.println("Chunk not loaded, waiting !");
            }
        }
	}*/
}
