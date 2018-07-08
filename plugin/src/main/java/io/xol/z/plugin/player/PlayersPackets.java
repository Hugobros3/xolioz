package io.xol.z.plugin.player;

public class PlayersPackets {
	
	/*public static void playSound(Location loc, String soundName, float soundVolume, float soundPitch)
	{
		if(soundVolume >= 0)
		{
			PacketPlayOutNamedSoundEffect packet = new PacketPlayOutNamedSoundEffect(soundName, loc.getX(), loc.getY(), loc.getZ(), soundVolume, soundPitch);
			for(Player p : loc.getWorld().getPlayers())
			{
				if(loc.getWorld().equals(p.getLocation().getWorld()) && loc.distance(p.getLocation()) < 500f)
				{
					((CraftPlayer) p).getHandle().playerConnection.sendPacket(packet);
				}
			}
		}
		else
		{
			// Negative volume for %playall
			for(Player p : loc.getWorld().getPlayers())
			{
				Location ploc = p.getLocation();
				PacketPlayOutNamedSoundEffect packet = new PacketPlayOutNamedSoundEffect(soundName, ploc.getX(), ploc.getY(), ploc.getZ(), 1000, soundPitch);
				((CraftPlayer) p).getHandle().playerConnection.sendPacket(packet);
			}
		}
	}

	@SuppressWarnings("deprecation")
	public static void setBlock(Location loc, int i, byte b) {
		for(Player p : loc.getWorld().getPlayers())
		{
			if(loc.getWorld().equals(p.getLocation().getWorld()) && loc.distance(p.getLocation()) < 444f)
			{
				p.sendBlockChange(loc, i, b);
			}
		}
	}*/
}
