package io.xol.dogez.plugin.game;

import io.xol.chunkstories.api.events.Listener;

//(c) 2014 XolioWare Interactive

public class BlockListener implements Listener{
	// u shall not break meh soil
	/*@EventHandler
	void onBlockFade(BlockFadeEvent event) {
		if (!event.getBlock().getWorld().getName().equals(DogeZPlugin.config.activeWorld))
			return;
		if (event.getBlock().getType().equals(Material.SOIL)) {
			event.setCancelled(true);
		}
	}*/
	
	// neither u dirty plyr
    /*@EventHandler
    public void onBlockInteract(PlayerInteractEvent event)
    {
	    if(event.getAction() == Action.PHYSICAL && event.getClickedBlock().getType() == Material.SOIL)
	    	event.setCancelled(true);
    }*/
    
    //Chest placement
    /*@EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
    	Player player = event.getPlayer();
		if(player.getWorld().getName().equals(DogeZPlugin.config.activeWorld) || player.getWorld().getName().equals("namalsk-map"))
		{
			//loot placement and removal
			if(player.hasPermission("dogez.admin"))
			{
				if(event.getBlock().getType().equals(Material.CHEST))
				{
					Location loc = event.getBlock().getLocation();
					PlayerProfile pp = PlayerProfile.getPlayerProfile(player.getUniqueId().toString());
					String coords = loc.getBlockX()+":"+loc.getBlockY()+":"+loc.getBlockZ();
					if(pp.adding && pp.activeCategory != null)
					{
						LootPlace lp = new LootPlace(coords+":"+pp.activeCategory+":"+pp.currentMin+":"+pp.currentMax,player.getWorld());
						if(LootPlaces.add(coords,lp,player.getWorld()))
							player.sendMessage(ChatColor.AQUA+"Point de loot ajouté "+lp.toString());
					}
				}
			}
		}
    }
    
    //chest removal
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
    	Player player = event.getPlayer();
		if(player.getWorld().getName().equals(DogeZPlugin.config.activeWorld) || player.getWorld().getName().equals("namalsk-map"))
		{
			//loot placement and removal
			if(player.hasPermission("dogez.admin"))
			{
				if(event.getBlock().getType().equals(Material.CHEST))
				{
					Location loc = event.getBlock().getLocation();
					String coords = loc.getBlockX()+":"+loc.getBlockY()+":"+loc.getBlockZ();
					if(LootPlaces.removePlace(coords,player.getWorld()))
					{
						Chest c = (Chest) event.getBlock().getState();
						c.getBlockInventory().clear();
						player.sendMessage(ChatColor.AQUA+"Point de loot supprimé ");
					}
				}
			}
		}
    }*/
	
    //
    /*@EventHandler
    public void onChunkUnloaded(ChunkUnloadEvent event) {
    	DogeZPlugin.spawner.cleanChunk(event.getChunk());
    	ChunksCleaner.cleanChunk(event.getChunk());
        //System.out.println("Chunk unloaded: " + event.getChunk());
    }*/
    
    //Chunk biome edit
    /*@EventHandler
    public void onChunkLoaded(ChunkLoadEvent event) {
    	if(event.getWorld().getName().equals(DogeZPlugin.config.activeWorld))
    	{
    		Chunk c = event.getChunk();
    		World w = c.getWorld();
    		for(int i = 0; i < 16*16; i++)
    		{
    			w.setBiome(c.getX()*16+i%16, c.getZ()*16+i/16, Biome.ROOFED_FOREST);
    		}
    	}
    }*/
}
