package io.xol.dogez.plugin.player;

import java.util.ArrayList;
import java.util.List;

import io.xol.chunkstories.api.server.Player;
import io.xol.dogez.plugin.DogeZPlugin;

public class PlayerProfiles {

	private final DogeZPlugin plugin;

	public PlayerProfiles(DogeZPlugin plugin) {
		this.plugin = plugin;
	}

	List<PlayerProfile> playerProfiles = new ArrayList<PlayerProfile>();

	public PlayerProfile getPlayerProfile(long uuid) {
		
		PlayerProfile result = null;
		for (PlayerProfile pp : playerProfiles) {
			if (pp.uuid == uuid)
			{
				result = pp;
				break;
			}
		}
		
		if (result == null) {
			
			//Look if the player is already logged in
			Player player = plugin.getServer().getPlayerByUUID(uuid);
			
			//If it is load it
			if (player != null) {
				PlayerProfile pp = new PlayerProfile(uuid, player.getName());
				playerProfiles.add(pp);
				return pp;
			}
			//If it isn't, load it but don't assume the name
			else
			{
				PlayerProfile pp = new PlayerProfile(uuid);
				playerProfiles.add(pp);
				return pp;
			}
		}
		
		//Should never happen
		if(result == null)
			System.out.println("FUCK OUT 666");
		return result;
	}

	public void removePlayerProfile(long uuid) {
		
		List<PlayerProfile> profilesToDelete = new ArrayList<PlayerProfile>();
		for (PlayerProfile pp : playerProfiles) {

			if (pp.uuid == uuid)
			{
				pp.saveProfile();
				profilesToDelete.add(pp);
			}
		}

		for (PlayerProfile delete : profilesToDelete) {
			playerProfiles.remove(delete);
		}
	}

	public void addPlayerProfile(long uuid, String name) {
		PlayerProfile add = new PlayerProfile(uuid, name);
		playerProfiles.add(add);
	}

	public void saveAll() {
		for (PlayerProfile pp : playerProfiles) {
			pp.saveProfile();
		}
	}
}
