package io.xol.dogez.plugin.misc;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

//(c) 2014 XolioWare Interactive

public class PlayersLister {

	/*
	 *	/!\ This is a direct and very crappy port from old XolioHacks plugin.
	 * 	
	 * 	Code looks like crap ... who cares it just writes a few files...
	 */
	public static void setup(JavaPlugin plugin)
	{
		plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
			public void run() {
				   	try {
						FileOutputStream out = new FileOutputStream(new File("plugins/DogeZ/connected"));
						writeln("Joueurs connectes : "+Bukkit.getServer().getOnlinePlayers().size()+"/"+Bukkit.getServer().getMaxPlayers(),out);
						out.close();
						FileOutputStream num = new FileOutputStream(new File("plugins/DogeZ/number"));
						writeln(Bukkit.getServer().getOnlinePlayers().size()+"",num);
						num.close();
						FileOutputStream con = new FileOutputStream(new File("plugins/DogeZ/list"));
						//Player[] players = (Player[]) Bukkit.getServer().getOnlinePlayers().toArray();
						String listeDesJoueurs = "";
						int i = 0;
						for(Object o : Bukkit.getServer().getOnlinePlayers())
						{
							if(o instanceof Player)
							{
								i++;
								Player player = (Player)o;
								if(i > 0)
									listeDesJoueurs = listeDesJoueurs + ", ";
								listeDesJoueurs = listeDesJoueurs + player.getDisplayName();
								
							}
						}
						writeln(listeDesJoueurs,con);
						con.close();
						FileOutputStream ban = new FileOutputStream(new File("plugins/DogeZ/public_banlist"));
						for(OfflinePlayer banned : Bukkit.getServer().getBannedPlayers())
						{
							writeln("Le joueur "+banned.getName()+" est banni sur DogeZ\n", ban);
						}
					} catch (Exception e) {
						e.printStackTrace();
						System.out.println("FATAL ERROR : CAN'T OUTPUT");
					}
					//Bukkit.getServer().broadcastMessage("wtf");

				   	
			   }
			}, 60L, 20L);
	}
	
	static void writeln(String txt, FileOutputStream fos)
	{
		int x,z = 0;
		x = txt.length();
		while(z < x)
		{
			try {
				fos.write(txt.charAt(z));
			} catch (IOException e) {
			}
			z++;
		}
		try {
			fos.write('\n');
		} catch (IOException e) {
		}
	}
}
