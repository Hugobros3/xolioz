package io.xol.dogez.plugin.game;

//Copyright 2014 XolioWare Interactive

import java.util.UUID;

import io.xol.dogez.plugin.DogeZPlugin;
import io.xol.dogez.plugin.game.special.SafeTeleporter;
import io.xol.dogez.plugin.loot.LootCategory;
import io.xol.dogez.plugin.loot.LootItem;
import io.xol.dogez.plugin.loot.LootItems;
import io.xol.dogez.plugin.loot.LootPlaces;
import io.xol.dogez.plugin.loot.LootType;
import io.xol.dogez.plugin.loot.LootTypes;
import io.xol.dogez.plugin.map.PlacesNames;
//import io.xol.dogez.plugin.misc.ChatFormatter;
import io.xol.dogez.plugin.misc.HttpRequestThread;
import io.xol.dogez.plugin.misc.HttpRequester;
import io.xol.dogez.plugin.misc.TimeFormatter;
import io.xol.dogez.plugin.player.PlayerProfile;
import io.xol.dogez.plugin.player.PlayersPackets;
import io.xol.dogez.plugin.weapon.ChunksCleaner;
import io.xol.dogez.plugin.zombies.ZombieType;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

//import ru.tehkode.permissions.PermissionUser;
//import ru.tehkode.permissions.bukkit.PermissionsEx;

public class CommandsHandler implements CommandExecutor, HttpRequester{

	DogeZPlugin plugin;
	
	public CommandsHandler(DogeZPlugin dogeZPlugin) {
		plugin = dogeZPlugin;
	}

	@SuppressWarnings("deprecation")
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String arg2,String[] args) {
		//System.out.println(arg2);
		if(cmd.getLabel().equals("m") || cmd.getName().equals("m"))
		{
			if(!(sender instanceof Player))
			{
				sender.sendMessage("You must be a player to use this.");
				return true;
			}
			Player player = (Player)sender;
			PlayerProfile pp = PlayerProfile.getPlayerProfile(player.getUniqueId().toString());
			if(args.length < 2)
			{
				sender.sendMessage(ChatColor.RED+"Syntaxe correcte : /m <joueur> <message>");
			}
			else
			{
				String toSend = "";
				String destination = args[0];
				Player destPlayer = Bukkit.getPlayer(destination);
				if(TalkieWalkie.canPlayerUse(player) || (destPlayer != null && destPlayer.isOp()))
				{
					if(destPlayer != null)
					{
						PlayerProfile pp2 = PlayerProfile.getPlayerProfile(destPlayer.getUniqueId().toString());
						boolean canReply = TalkieWalkie.canPlayerUse(destPlayer);
						for(int i = 1; i < args.length-1; i++)
							toSend+=args[i]+" ";
						toSend+=args[args.length-1];
						destPlayer.sendMessage(ChatColor.GRAY+"[MP de la part de "+ChatColor.AQUA+player.getName()+ChatColor.GRAY+"]:"+toSend);
						player.sendMessage(ChatColor.GRAY+"[MP envoyé à "+ChatColor.AQUA+destination+ChatColor.GRAY+"]:"+toSend);
						if(!canReply)
							player.sendMessage(ChatColor.RED+"Attention, le joueur auquel vous parlez ne peut pas vous répondre, ");
						pp.talkingTo = destination;
						pp2.talkingTo = player.getName();
						//TalkieWalkie.notifyListenersAdmins(player.getName(), destination, toSend);
					}
					else
					{
						sender.sendMessage(ChatColor.RED+"Le joueur auquel vous vous addressez n'est pas disponible.");
					}
				}
				else
				{
					sender.sendMessage(ChatColor.RED+"Il vous faut un talkie-walkie pour faire ça !");
				}
			}
			return true;
		}
		if(cmd.getName().equals("r") || cmd.getLabel().equals("r"))
		{
			if(!(sender instanceof Player))
			{
				sender.sendMessage("You must be a player to use this.");
				return true;
			}
			Player player = (Player)sender;
			PlayerProfile pp = PlayerProfile.getPlayerProfile(player.getUniqueId().toString());
			if(args.length < 1)
			{
				sender.sendMessage(ChatColor.RED+"Syntaxe correcte : /r <message>");
			}
			else
			{
				String toSend = "";
				if(TalkieWalkie.canPlayerUse(player))
				{
					if(pp.talkingTo.equals(""))
					{
						sender.sendMessage(ChatColor.RED+"Vous n'avez personne à qui répondre :foreveralone:");
						return true;
					}
					String destination = pp.talkingTo;
					Player destPlayer = Bukkit.getPlayer(destination);
					if(destPlayer != null)
					{
						PlayerProfile pp2 = PlayerProfile.getPlayerProfile(destPlayer.getUniqueId().toString());
						boolean canReply = TalkieWalkie.canPlayerUse(destPlayer);
						for(int i = 0; i < args.length-1; i++)
							toSend+=args[i]+" ";
						toSend+=args[args.length-1];
						destPlayer.sendMessage(ChatColor.GRAY+"[MP de la part de "+ChatColor.AQUA+player.getName()+ChatColor.GRAY+"]:"+toSend);
						player.sendMessage(ChatColor.GRAY+"[MP envoyé à "+ChatColor.AQUA+destination+ChatColor.GRAY+"]:"+toSend);
						if(!canReply)
							player.sendMessage(ChatColor.RED+"Attention, le joueur auquel vous parlez ne peut pas vous répondre, ");
						pp.talkingTo = destination;
						pp2.talkingTo = player.getName();
						//TalkieWalkie.notifyListenersAdmins(player.getName(), destination, toSend);
					}
					else
					{
						sender.sendMessage(ChatColor.RED+"Le joueur auquel vous vous addressez n'est pas disponible.");
					}
				}
				else
				{
					sender.sendMessage(ChatColor.RED+"Il vous faut un talkie-walkie pour faire ça !");
				}
			}
			return true;
		}
		
		if(args.length == 0)
		{
			sender.sendMessage(ChatColor.BLUE+"Plugin DogeZ "+ChatColor.DARK_GREEN+"(v "+DogeZPlugin.version+")");
			sender.sendMessage(ChatColor.DARK_GRAY+"=========["+ChatColor.BLUE+"Commandes disponibles"+ChatColor.DARK_GRAY+"]=========");
			sender.sendMessage(ChatColor.BLUE+"/dz"+ChatColor.DARK_GRAY+" - "+ChatColor.GRAY+ChatColor.ITALIC+"Voir cette aide.");
			sender.sendMessage(ChatColor.BLUE+"/dz "+ChatColor.DARK_AQUA+"play"+ChatColor.DARK_GRAY+" - "+ChatColor.GRAY+ChatColor.ITALIC+"Commencer une nouvelle partie");
			sender.sendMessage(ChatColor.BLUE+"/dz "+ChatColor.DARK_AQUA+"stats"+ChatColor.DARK_GRAY+" - "+ChatColor.GRAY+ChatColor.ITALIC+"Obtenir des statistiques sur votre partie");
			sender.sendMessage(ChatColor.BLUE+"/dz "+ChatColor.DARK_AQUA+"money"+ChatColor.DARK_GRAY+" - "+ChatColor.GRAY+ChatColor.ITALIC+"Connaitre votre argent");
			sender.sendMessage(ChatColor.BLUE+"/dz "+ChatColor.DARK_AQUA+"pay <joueur> <montant>"+ChatColor.DARK_GRAY+" - "+ChatColor.GRAY+ChatColor.ITALIC+"Envoyer de l'argent");
			//sender.sendMessage(ChatColor.BLUE+"/dz "+ChatColor.DARK_AQUA+"report <joueur> <raison>"+ChatColor.DARK_GRAY+" - "+ChatColor.GRAY+ChatColor.ITALIC+"Signaler un joueur");
			sender.sendMessage(ChatColor.BLUE+"/dz "+ChatColor.DARK_AQUA+"setpw <mdp> <mdp>"+ChatColor.DARK_GRAY+" - "+ChatColor.GRAY+ChatColor.ITALIC+"Définir votre mdp sur le site");
			if(sender.hasPermission("dogez.admin"))
			{
				sender.sendMessage(ChatColor.RED+"Les commandes ci-dessous sont réservées aux opérateurs.");
				sender.sendMessage(ChatColor.BLUE+"/dz "+ChatColor.DARK_AQUA+"loot"+ChatColor.DARK_GRAY+" - "+ChatColor.GRAY+ChatColor.ITALIC+"Commandes de gestion du loot");
				sender.sendMessage(ChatColor.BLUE+"/dz "+ChatColor.DARK_AQUA+"reload"+ChatColor.DARK_GRAY+" - "+ChatColor.GRAY+ChatColor.ITALIC+"Recharge les fichiers de configuration");
				sender.sendMessage(ChatColor.BLUE+"/dz "+ChatColor.DARK_AQUA+"togglesynch"+ChatColor.DARK_GRAY+" - "+ChatColor.GRAY+ChatColor.ITALIC+"Active où désactive la synchrinisation heure IRL");
				sender.sendMessage(ChatColor.BLUE+"/dz "+ChatColor.DARK_AQUA+"togglelocalizedchat"+ChatColor.DARK_GRAY+" - "+ChatColor.GRAY+ChatColor.ITALIC+"Active ou désactive le chat localisé");
			}
			return true;
		}
		if(!(sender instanceof Player))
		{
			sender.sendMessage("You must be a player to use this.");
			return false;
		}
		if(args.length > 0)
		{
			Player player = (Player)sender;
			if(args[0].equals("spawn") || args[0].equals("play"))
			{
				Player player2 = player;
				if(args.length > 1 && sender.hasPermission("dogez.spawn.as"))
					player2 = Bukkit.getPlayer(args[1]);
				if(player2 == null)
					return false;
				PlayerProfile profile = PlayerProfile.getPlayerProfile(player2.getUniqueId().toString());
				if(profile.inGame)
					player2.sendMessage(ChatColor.RED+"Vous êtes déjà en jeu !");
				else
				{
					int[] coords = SpawnPoints.getRandomSpawn();
					player2.getInventory().clear();
					player2.setGameMode(GameMode.SURVIVAL);
					LootCategory spawnGear = LootTypes.getCategory("spawn");
					for(ItemStack i : spawnGear.getAllItems())
					{
						player2.getInventory().addItem(i);
					}
					player2.updateInventory();
					profile.inGame=true;
					player2.setHealth(20);
					player2.setFoodLevel(20);
					player2.sendMessage(ChatColor.DARK_AQUA+"Vous venez de commencer une partie, bonne chance !");
					
					SafeTeleporter.safeTeleport(player2, new Location(DogeZPlugin.config.getWorld(),coords[0],coords[1]+2,coords[2]));
					//player2.teleport(new Location(DogeZPlugin.config.getWorld(),coords[0],coords[1]+2,coords[2]));
				}
				return true;
			}
			if(args[0].equals("suicide"))
			{
				player.setHealth(0d);
			}
			if(args[0].equals("stats"))
			{
				PlayerProfile profile = PlayerProfile.getPlayerProfile(player.getUniqueId().toString());
				if(args.length == 2)
				{
					String requestedPlayerName = args[1];
					OfflinePlayer requestedPlayer = Bukkit.getOfflinePlayer(requestedPlayerName);
					profile = PlayerProfile.getPlayerProfile(requestedPlayer.getUniqueId().toString());
				}
				if(profile == null)
				{
					sender.sendMessage(ChatColor.RED+"Impossible d'afficher ce profil. ( Erreur de chargement depuis la BDD ?) ");
					return true;
				}
					
				profile.timeCalc();
				sender.sendMessage(ChatColor.DARK_GRAY+"=========["+ChatColor.BLUE+"Statistiques du compte "+ChatColor.RED+profile.name+" "+ChatColor.DARK_GRAY+"]=========");
				//en jeu ?
				sender.sendMessage(ChatColor.AQUA+(profile.inGame ? "Actuellement dans une partie" : "Hors-jeu ( spawn où minijeu )"));
				//Temps passé
				sender.sendMessage(ChatColor.DARK_AQUA+"Vous avez passé "+TimeFormatter.formatTimelapse(profile.timeConnected)+" sur ce serveur !");
				sender.sendMessage(ChatColor.DARK_AQUA+"Vous êtes en vie depuis "+TimeFormatter.formatTimelapse(profile.timeSurvivedLife)+" !");
				//stats kill
				sender.sendMessage(ChatColor.DARK_AQUA+"Vous avez tué "+profile.zombiesKilled+" zombies, dont "+profile.zombiesKilled_thisLife+" sur cette partie.");
				sender.sendMessage(ChatColor.DARK_AQUA+"Vous avez tué "+profile.playersKilled+" joueurs, dont "+profile.playersKilled_thisLife+" sur cette partie.");
				sender.sendMessage(ChatColor.DARK_AQUA+"Vous avez été tué "+profile.deaths+" fois");
				
				return true;
			}
			if(args[0].equals("money") && args.length == 1)
			{
				double xc = PlayerProfile.getPlayerProfile(player.getUniqueId().toString()).xcBalance;
				xc*=100;
				xc = Math.floor(xc);
				xc/=100;
				sender.sendMessage(ChatColor.DARK_AQUA+"Votre compte est actuellement crédité de "+xc+" XolioCoins.");
				return true;
			}
			if(args[0].equals("money") && args.length == 2)
			{
				OfflinePlayer target = plugin.getServer().getOfflinePlayer(args[1]);
				if(player != null)
				{
					double xc = PlayerProfile.getPlayerProfile(target.getUniqueId().toString()).xcBalance;
					xc*=100;
					xc = Math.floor(xc);
					xc/=100;
					sender.sendMessage(ChatColor.DARK_AQUA+"Le compte de "+target.getName()+" est actuellement crédité de "+xc+" XolioCoins.");
				}
				return true;
			}
			if(args[0].equals("pay"))
			{
				if(args.length == 3)
				{
					double amount = Double.parseDouble(args[2]);
					if(amount < 0.01)
					{
						player.sendMessage(ChatColor.RED+"Vous ne pouvez pas envoyer moins de 1 centime.");
						return true;
					}
					new HttpRequestThread(this,"sendMoney:"+player.getUniqueId().toString(),"http://dz.xol.io/api/playerProfile.php","a=pay&uuid="+player.getUniqueId().toString()+"&amount="+amount+"&who="+args[1]).run();
					return true;
				}
				else
				{
					player.sendMessage(ChatColor.RED+"Syntaxe correcte : /dz pay <joueur> <montant>");
					return true;
				}
			}
			if(args[0].equals("setpw"))
			{
				if(args.length == 3)
				{
					String password = args[1];
					if(args[1].equals(args[2]))
					{
						if(password.length() >= 6)
						{
							new HttpRequestThread(this,"setPW:"+player.getUniqueId().toString(),"http://dz.xol.io/api/playerProfile.php","a=setpw&uuid="+player.getUniqueId().toString()+"&pw="+password).run();
							return true;
						}
						else
						{
							player.sendMessage(ChatColor.RED+"Votre mot de passe fait moins de 6 caractères !");
							return true;
						}
					}
					else
					{
						player.sendMessage(ChatColor.RED+"Les deux mots de passe ne correspondent pas.");
						return true;
					}
				}
				else
				{
					player.sendMessage(ChatColor.RED+"Syntaxe correcte : /dz setpw <mdp> <mdp>");
					return true;
				}
			}
			//admin
			if(args[0].equals("reload"))
			{
				if(player.hasPermission("dogez.admin"))
				{
					plugin.loadConfigs();
					player.sendMessage(ChatColor.AQUA+"Configuration rechargée");
				}
				else
					player.sendMessage(ChatColor.RED+"Vous n'avez pas la permission.");
				return true;
			}
			if(args[0].equals("loot"))
			{
				if(player.hasPermission("dogez.admin"))
				{
					if(args.length == 1)
					{
						sender.sendMessage(ChatColor.DARK_GRAY+"======["+ChatColor.BLUE+"Commandes à propos du loot"+ChatColor.DARK_GRAY+"]======");
						sender.sendMessage(ChatColor.BLUE+"/dz "+ChatColor.DARK_AQUA+"loot"+ChatColor.DARK_GRAY+" - "+ChatColor.GRAY+ChatColor.ITALIC+"Voir cette aide.");
						sender.sendMessage(ChatColor.BLUE+"/dz "+ChatColor.DARK_AQUA+"loot stats"+ChatColor.DARK_GRAY+" - "+ChatColor.GRAY+ChatColor.ITALIC+"Voir des stats sur le loot");
						sender.sendMessage(ChatColor.BLUE+"/dz "+ChatColor.DARK_AQUA+"loot add <type> [min] [max]"+ChatColor.DARK_GRAY+" - "+ChatColor.GRAY+ChatColor.ITALIC+"Ajouter un point");
						sender.sendMessage(ChatColor.BLUE+"/dz "+ChatColor.DARK_AQUA+"loot remove"+ChatColor.DARK_GRAY+" - "+ChatColor.GRAY+ChatColor.ITALIC+"Supprimmer un point");
						sender.sendMessage(ChatColor.BLUE+"/dz "+ChatColor.DARK_AQUA+"loot reload"+ChatColor.DARK_GRAY+" - "+ChatColor.GRAY+ChatColor.ITALIC+"Recharger le fichier");
						sender.sendMessage(ChatColor.BLUE+"/dz "+ChatColor.DARK_AQUA+"loot save"+ChatColor.DARK_GRAY+" - "+ChatColor.GRAY+ChatColor.ITALIC+"Sauvegarder le fichier");
						sender.sendMessage(ChatColor.BLUE+"/dz "+ChatColor.DARK_AQUA+"loot reloot"+ChatColor.DARK_GRAY+" - "+ChatColor.GRAY+ChatColor.ITALIC+"Ré-initialise chaque point");
						sender.sendMessage(ChatColor.BLUE+"/dz "+ChatColor.DARK_AQUA+"loot list"+ChatColor.DARK_GRAY+" - "+ChatColor.GRAY+ChatColor.ITALIC+"Liste les types de loot");
						sender.sendMessage(ChatColor.BLUE+"/dz "+ChatColor.DARK_AQUA+"loot arround <rayon> [force]"+ChatColor.DARK_GRAY+" - "+ChatColor.GRAY+ChatColor.ITALIC+"set les loots à x blocs à la ronde");
						return true;
					}
					else
					{
						if(args[1].equals("add") && args.length >= 3)
						{
							String category = args[2];
							if(LootTypes.categories.keySet().contains(category))
							{
								PlayerProfile pp = PlayerProfile.getPlayerProfile(player.getUniqueId().toString());
								pp.activeCategory = category;
								pp.adding = true;
								if(args.length >= 4)
									pp.currentMin = Integer.parseInt(args[3]);
								if(args.length >= 5)
									pp.currentMax = Integer.parseInt(args[4]);
								player.sendMessage(ChatColor.AQUA+"Vous placez désormais des points de loot ["+category+":"+pp.currentMin+"-"+pp.currentMax+"]");
							}
							else
							{
								player.sendMessage(ChatColor.RED+"La catégorie de loot \""+category+"\" n'éxiste pas.");
							}
							return true;
						}
						if(args[1].equals("remove"))
						{
							PlayerProfile pp = PlayerProfile.getPlayerProfile(player.getUniqueId().toString());
							pp.adding = false;
							player.sendMessage(ChatColor.AQUA+"Vous supprimmez désormais des points de loot");
							return true;
						}
						if(args[1].equals("stats"))
						{
							player.sendMessage(ChatColor.AQUA+"Il existe "+LootPlaces.count(player.getWorld())+" points de loot dans le fichier.");
							return true;
						}
						if(args[1].equals("list"))
						{
							String list = "";
							for(String c : LootTypes.categories.keySet())
							{
								list+=c+",";
							}
							list = list.substring(0, list.length()-1);
							player.sendMessage(ChatColor.AQUA+"Types de loot :"+list);
							return true;
						}
						if(args[1].equals("reload"))
						{
							LootPlaces.loadLootFile(player.getWorld());
							player.sendMessage(ChatColor.AQUA+"Fichier de loot rechargé.");
							return true;
						}
						if(args[1].equals("save"))
						{
							LootPlaces.saveLootFile(player.getWorld());
							player.sendMessage(ChatColor.AQUA+"Fichier de loot sauvegardé.");
							return true;
						}
						if(args[1].equals("reloot"))
						{
							player.sendMessage(ChatColor.AQUA+""+LootPlaces.respawnLoot(player.getWorld())+" points de loot respawnés.");
							return true;
						}
						if(args[1].equals("arround"))
						{
							if(args.length >= 3)
							{
								boolean force = (args.length == 4 && args[3].endsWith("force"));
								int radius = Integer.parseInt(args[2]);
								if(radius > 50)
								{
									player.sendMessage(ChatColor.AQUA+"Rayon trop élevé, cappé à 50");
									radius = 50;
								}
								int amount = LootPlaces.lootArroundPlayer(player,radius,force);
								player.sendMessage(ChatColor.AQUA+""+amount+" nouveaux points de loot ajoutés ( f="+force+" )");
							}
							else
								player.sendMessage(ChatColor.AQUA+"Syntaxe : /dz loot arround <rayon> [force]");
							return true;
						}
					}
				}
				else
				{
					player.sendMessage(ChatColor.RED+"Vous n'avez pas la permission.");
					return true;
				}
			}
			if(args[0].equals("item"))
			{
				if(player.hasPermission("dogez.admin"))
				{
					if(args.length == 1)
					{
						sender.sendMessage(ChatColor.DARK_GRAY+"======["+ChatColor.BLUE+"Commandes à propos des items"+ChatColor.DARK_GRAY+"]======");
						sender.sendMessage(ChatColor.BLUE+"/dz "+ChatColor.DARK_AQUA+"item"+ChatColor.DARK_GRAY+" - "+ChatColor.GRAY+ChatColor.ITALIC+"Voir cette aide.");
						sender.sendMessage(ChatColor.BLUE+"/dz "+ChatColor.DARK_AQUA+"item list [page]"+ChatColor.DARK_GRAY+" - "+ChatColor.GRAY+ChatColor.ITALIC+"Voire une liste des items");
						sender.sendMessage(ChatColor.BLUE+"/dz "+ChatColor.DARK_AQUA+"item give <name> [amount]"+ChatColor.DARK_GRAY+" - "+ChatColor.GRAY+ChatColor.ITALIC+"Donne un item");
						return true;
					}
					else
					{
						if(args[1].equals("list"))
						{
							int page = 0;
							if(args.length == 3)
								page = Integer.parseInt(args[2]);
							/*while(page*10 > LootItems.lootItems.size())
								page--;*/
							Object[] keys = LootItems.lootItems.keySet().toArray();
							int lol = 0;
							for(int i = 0 ; i < 10; i++)
							{
								if(page*10+i < keys.length)
								{
									LootItem li = LootItems.getItem((String)keys[page*10+i]);
									player.sendMessage(ChatColor.AQUA+li.internalName+" "+li.typeId+":"+li.metaData+" => "+li.name);
									lol++;
								}
							}
							sender.sendMessage(ChatColor.BLUE+""+lol+" items sur "+keys.length+" affichés. ( page "+page+" sur "+keys.length/10+")");
						}
						if(args[1].equals("give"))
						{
							try{
								int amount = 1;
								if(args.length == 4)
									amount = Integer.parseInt(args[3]);
								ItemStack gimme = LootItems.getItem(args[2]).getItem();
								gimme.setAmount(amount);
								player.getInventory().addItem(gimme);
								player.updateInventory();
								sender.sendMessage(ChatColor.AQUA+"Item "+args[2]+" donné en "+amount+" exemplaire(s).");
									
							}
							catch(Exception e)
							{
								player.sendMessage(ChatColor.RED+"Erreur. "+e.getLocalizedMessage());
							}
						}
					}
					return true;
				}
			}
			//debug
			if(args[0].equals("cc") && player.hasPermission("dogez.admin.cc"))
			{
				ChunksCleaner.cleanAllChunks(player.getWorld());
				sender.sendMessage(ChatColor.AQUA+"Toutes les chunks ont étés nettoyés");
				return true;
			}
			if(args[0].equals("snd") && player.hasPermission("dogez.admin.snd"))
			{
				if(args.length == 2)
					PlayersPackets.playSound(player.getLocation(),args[1],1f,1f);
				else if(args.length == 3)
					PlayersPackets.playSound(player.getLocation(),args[1],1f,Float.parseFloat(args[2]));
				else if(args.length == 4)
					PlayersPackets.playSound(player.getLocation(),args[1],Float.parseFloat(args[3]),Float.parseFloat(args[2]));
				else
					sender.sendMessage(ChatColor.RED+"Syntaxe : /dz snd node.du.son [pitch] [volume]");
				return true;
			}
			if(args[0].equals("zmb")  && player.hasPermission("dogez.admin.debug"))
			{
				DogeZPlugin.spawner.spawnZombie(player.getLocation(), ZombieType.values()[Integer.parseInt(args[1])]);
				return true;
			}
			if(args[0].equals("torch")  && player.hasPermission("dogez.admin.debug"))
			{
				player.sendBlockChange(player.getLocation(), Material.TORCH, Byte.parseByte(args[1]));
				//HeadsUpDisplay.displayTextBar("mdr", player);
				//HeadsUpDisplay.displayLoadingBar("gdf", "gfdgdf", player, 5, 500l, false);
				//StatusBarAPI.removeStatusBar(player);
				//StatusBarAPI.setStatusBar(player, ChatColor.GREEN+"ntm :D"+args[2], Integer.parseInt(args[1])/100f);
				return true;
			}
			if(args[0].equals("fli") && player.hasPermission("dogez.admin"))
			{
				/*PermissionUser user = PermissionsEx.getUser(player);
				String prefix = user.getPrefix();*/
				Bukkit.getServer().broadcastMessage(ChatColor.DARK_GRAY+"["+ChatColor.GREEN+"+"+ChatColor.DARK_GRAY+"] "+player.getDisplayName()+ChatColor.GRAY+" vient de se connecter.");
				return true;
			}
			if(args[0].equals("flo") && player.hasPermission("dogez.admin"))
			{
				/*PermissionUser user = PermissionsEx.getUser(player);
				String prefix = user.getPrefix();*/
				Bukkit.getServer().broadcastMessage(ChatColor.DARK_GRAY+"["+ChatColor.RED+"-"+ChatColor.DARK_GRAY+"] "+player.getDisplayName()+ChatColor.GRAY+" vient de se déconnecter.");
				return true;
			}
			if(args[0].equals("pn") && player.hasPermission("dogez.admin.debug"))
			{
				sender.sendMessage("PlaceNAme="+PlacesNames.getPlayerPlaceName(player));
				return true;
			}
			if(args[0].equals("testloot") && args.length == 2 && player.hasPermission("dogez.admin.testloot"))
			{
				String category = args[1];
				if(LootTypes.categories.keySet().contains(category))
				{
					LootType lt = LootTypes.getCategory(category).getRandomSpawn();
					sender.sendMessage("Giving random loot in category "+category+" : "+lt.toString());
				}
				else
				{
					player.sendMessage(ChatColor.RED+"Wrong cat, fuck you");
				}
				return true;
			}
			if(args[0].equals("load")  && player.hasPermission("dogez.admin.debug"))
			{
				sender.sendMessage("Forcing reload of profile");
				PlayerProfile.getPlayerProfile(player.getUniqueId().toString()).reloadProfile();
				return true;
			}
			if(args[0].equals("save")  && player.hasPermission("dogez.admin.debug"))
			{
				sender.sendMessage("Forcing save of profile");
				PlayerProfile.getPlayerProfile(player.getUniqueId().toString()).saveProfile();
				return true;
			}
			/*if(args[0].equals("timediff")  && player.hasPermission("dogez.admin.debug"))
			{
				sender.sendMessage(PlayerProfile.getPlayerProfile(player.getUniqueId().toString()).timeCalc()+" seconds since last time :)");
				return true;
			}
			if(args[0].equals("http")  && player.hasPermission("dogez.admin.debug"))
			{
				sender.sendMessage("mdr");
				new HttpRequestThread(this,"reloadProfile","http://dz.xol.io/api/playerProfile.php","a=load&uuid="+player.getUniqueId().toString()+"&name="+player.getName()).run();
				return true;
			}*/
			
			if(args[0].equals("togglesynch")  && player.hasPermission("dogez.admin"))
			{
				DogeZPlugin.config.synchTime = !DogeZPlugin.config.synchTime;
				sender.sendMessage("Synchronization heure IRL : "+DogeZPlugin.config.synchTime);
				return true;
			}
		}
		//Bukkit.dispatchCommand(new FakeCommandBlock(),"say coucou");
		sender.sendMessage(ChatColor.RED+"Commande inconnue");
		return true;
	}

	@Override
	public void handleHttpRequest(String info, String result) {
		System.out.println("[DogeZ][Debug] Request answered:"+result);
		if(info.startsWith("sendMoney"))
		{
			String uuid = info.split(":")[1];
			Player player = Bukkit.getPlayer(UUID.fromString(uuid));
			if(player != null)
			{
				if(result.startsWith("success"))
				{
					String amount = result.split(":")[2];
					String receiver = result.split(":")[1];
					
					PlayerProfile.getPlayerProfile(player.getUniqueId().toString()).reloadProfile();
					Player receiverP = Bukkit.getPlayer(receiver);
					if(receiverP != null)
					{
						PlayerProfile.getPlayerProfile(receiverP.getUniqueId().toString()).reloadProfile();
						receiverP.sendMessage(ChatColor.DARK_AQUA+"Vous avez reçu "+ChatColor.AQUA+ChatColor.BOLD+amount+ChatColor.DARK_AQUA+" XolioCoins de la part de "+ChatColor.AQUA+ChatColor.BOLD+player.getName());
					}
					
					player.sendMessage(ChatColor.DARK_AQUA+"Vous avez envoyé avec succès "+ChatColor.AQUA+ChatColor.BOLD+amount+ChatColor.DARK_AQUA+" XolioCoins à "+ChatColor.AQUA+ChatColor.BOLD+receiver);
					}
				else if(result.startsWith("nominus"))
				{
					player.sendMessage(ChatColor.RED+"Bien tenté ;)");
				}
				else if(result.startsWith("nozero"))
				{
					player.sendMessage(ChatColor.RED+"Envoyer rien du tout ça sert à [GOTO 8]");
				}
				else if(result.startsWith("nofunds"))
				{
					player.sendMessage(ChatColor.RED+"Vous ne disposez pas de l'argent nécéssaire... :/");
				}
				else if(result.startsWith("noself"))
				{
					player.sendMessage(ChatColor.RED+"C'est pas un peu débile de s'envoyer du fric ?");
				}
				else if(result.startsWith("noplayer"))
				{
					player.sendMessage(ChatColor.RED+"Ce joueur n'existe pas dans la base de données... :/");
				}
				else
					player.sendMessage(ChatColor.RED+"Erreur inconnue survenue lors de l'envoi d'argent :/");
			}
			else
				System.out.println("[DogeZ][Debug] null player :/ "+uuid);
			
			//player.sendMessage(ChatColor.DARK_AQUA+"Votre compte est actuellement crédité de "+PlayerProfile.getPlayerProfile(player.getUniqueId().toString()).xcBalance+" XolioCoins.");
		}
		if(info.startsWith("setPW"))
		{
			String uuid = info.split(":")[1];
			Player player = Bukkit.getPlayer(UUID.fromString(uuid));
			if(player != null)
			{
				if(result.equals("password set"))
					player.sendMessage(ChatColor.DARK_AQUA+"Votre mot de passe à bien été modifié.");
				else
					player.sendMessage(ChatColor.RED+"Pour une raison non-gérée par ce plugin votre mot de passe n'a pas été modifié :/");
			}
		}
	}

}
