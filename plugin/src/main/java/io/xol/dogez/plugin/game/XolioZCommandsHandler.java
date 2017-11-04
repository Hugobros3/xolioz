package io.xol.dogez.plugin.game;

//Copyright 2014 XolioWare Interactive

import io.xol.chunkstories.api.Location;
import io.xol.chunkstories.api.entity.Entity;
import io.xol.chunkstories.api.entity.EntityLiving;
import io.xol.chunkstories.api.entity.interfaces.EntityCreative;
import io.xol.chunkstories.api.entity.interfaces.EntityWithInventory;
import io.xol.chunkstories.api.item.inventory.ItemPile;
import io.xol.chunkstories.api.player.Player;
import io.xol.chunkstories.api.plugin.commands.Command;
import io.xol.chunkstories.api.plugin.commands.CommandEmitter;
import io.xol.chunkstories.api.plugin.commands.CommandHandler;
import io.xol.chunkstories.api.sound.SoundSource.Mode;
import io.xol.chunkstories.api.util.compatibility.ChatColor;
import io.xol.chunkstories.core.entity.EntityZombie;
import io.xol.dogez.plugin.XolioZGamemodePlugin;
import io.xol.dogez.plugin.loot.LootCategory;
import io.xol.dogez.plugin.loot.LootType;
import io.xol.dogez.plugin.map.PlacesNames;
import io.xol.dogez.plugin.misc.TimeFormatter;
import io.xol.dogez.plugin.player.PlayerProfile;

public class XolioZCommandsHandler implements CommandHandler {

	private final XolioZGamemodePlugin plugin;

	public XolioZCommandsHandler(XolioZGamemodePlugin dogeZPlugin) {
		plugin = dogeZPlugin;
	}

	@Override
	public boolean handleCommand(CommandEmitter sender, Command cmd, String[] args) {

		if (cmd.getName().equals("m")) {
			if (!(sender instanceof Player)) {
				sender.sendMessage("#{dogez.mustbeplayer}");
				return true;
			}
			Player player = (Player) sender;
			PlayerProfile pp = plugin.getPlayerProfiles().getPlayerProfile(player.getUUID());
			if (args.length < 2) {
				sender.sendMessage(ChatColor.RED + "#{dogez.goodsyntaxm}");
			} else {
				String toSend = "";
				String destination = args[0];
				Player destPlayer = plugin.getServer().getPlayerByName(destination);// Bukkit.getPlayer(destination);
				if (plugin.getTalkieWalkiesHandler().canPlayerUse(player)
						|| (destPlayer != null && destPlayer.hasPermission("dogez.talkie.receiveAnyway"))) {
					if (destPlayer != null) {
						PlayerProfile pp2 = plugin.getPlayerProfiles().getPlayerProfile(destPlayer.getUUID());
						boolean canReply = plugin.getTalkieWalkiesHandler().canPlayerUse(destPlayer);
						for (int i = 1; i < args.length - 1; i++)
							toSend += args[i] + " ";
						toSend += args[args.length - 1];
						destPlayer.sendMessage(ChatColor.GRAY + "[#{dogez.pmfrom}" + ChatColor.AQUA + player.getName()
								+ ChatColor.GRAY + "]:" + toSend);
						player.sendMessage(ChatColor.GRAY + "[#{dogez.pmto}" + ChatColor.AQUA + destination
								+ ChatColor.GRAY + "]:" + toSend);
						if (!canReply)
							player.sendMessage(ChatColor.RED
									+ "#{dogez.pmnoreplywarn}");
						pp.talkingTo = destination;
						pp2.talkingTo = player.getName();
					} else {
						sender.sendMessage(
								ChatColor.RED + "#{dogez.pmnotdispo}");
					}
				} else {
					sender.sendMessage(ChatColor.RED + "#{dogez.pmrequiretw}");
				}
			}
			return true;
		}
		if (cmd.getName().equals("r")) {
			if (!(sender instanceof Player)) {
				sender.sendMessage("#{dogez.mustbeplayer}");
				return true;
			}
			Player player = (Player) sender;
			PlayerProfile pp = plugin.getPlayerProfiles().getPlayerProfile(player.getUUID());
			if (args.length < 1) {
				sender.sendMessage(ChatColor.RED + "#{dogez.goodsyntaxr}");
			} else {
				String toSend = "";
				if (plugin.getTalkieWalkiesHandler().canPlayerUse(player)) {
					if (pp.talkingTo.equals("")) {
						sender.sendMessage(ChatColor.RED + "#{dogez.pmnoreply}");
						return true;
					}
					String destination = pp.talkingTo;
					Player destPlayer = plugin.getServer().getPlayerByName(destination);// Bukkit.getPlayer(destination);
					if (destPlayer != null) {
						PlayerProfile pp2 = plugin.getPlayerProfiles().getPlayerProfile(destPlayer.getUUID());
						boolean canReply = plugin.getTalkieWalkiesHandler().canPlayerUse(destPlayer);
						for (int i = 0; i < args.length - 1; i++)
							toSend += args[i] + " ";
						toSend += args[args.length - 1];
						destPlayer.sendMessage(ChatColor.GRAY + "[#{dogez.pmfrom}" + ChatColor.AQUA + player.getName()
								+ ChatColor.GRAY + "]:" + toSend);
						player.sendMessage(ChatColor.GRAY + "[#{dogez.pmto}" + ChatColor.AQUA + destination
								+ ChatColor.GRAY + "]:" + toSend);
						if (!canReply)
							player.sendMessage(ChatColor.RED
									+ "#{dogez.pmnoreplywarn}");
						pp.talkingTo = destination;
						pp2.talkingTo = player.getName();
						// TalkieWalkie.notifyListenersAdmins(player.getName(),
						// destination, toSend);
					} else {
						sender.sendMessage(
								ChatColor.RED + "#{dogez.pmnotdispo}");
					}
				} else {
					sender.sendMessage(ChatColor.RED + "#{dogez.pmrequiretw}");
				}
			}
			return true;
		}

		if (args.length == 0) {
			sender.sendMessage(ChatColor.BLUE + "#{dogez.pluginname}" + ChatColor.DARK_GREEN + plugin.version);
			sender.sendMessage(ChatColor.DARK_GRAY + "=========[" + ChatColor.BLUE + "#{dogez.avaiablecmds}"
					+ ChatColor.DARK_GRAY + "]=========");
			sender.sendMessage(ChatColor.BLUE + "/dz" + ChatColor.DARK_GRAY + " - " + ChatColor.GRAY + ChatColor.ITALIC
					+ "#{dogez.seethishelp}");
			sender.sendMessage(ChatColor.BLUE + "/dz " + ChatColor.DARK_AQUA + "play" + ChatColor.DARK_GRAY + " - "
					+ ChatColor.GRAY + ChatColor.ITALIC + "#{dogez.play}");
			sender.sendMessage(ChatColor.BLUE + "/dz " + ChatColor.DARK_AQUA + "stats" + ChatColor.DARK_GRAY + " - "
					+ ChatColor.GRAY + ChatColor.ITALIC + "#{dogez.stats}");
			sender.sendMessage(ChatColor.BLUE + "/dz " + ChatColor.DARK_AQUA + "money" + ChatColor.DARK_GRAY + " - "
					+ ChatColor.GRAY + ChatColor.ITALIC + "#{dogez.money}");
			sender.sendMessage(ChatColor.BLUE + "/dz " + ChatColor.DARK_AQUA + "#{dogez.paysyntax}"
					+ ChatColor.DARK_GRAY + " - " + ChatColor.GRAY + ChatColor.ITALIC + "#{dogez.pay}");
			sender.sendMessage(ChatColor.BLUE + "/dz " + ChatColor.DARK_AQUA + "suicide"
					+ ChatColor.DARK_GRAY + " - " + ChatColor.GRAY + ChatColor.ITALIC + "#{dogez.kys}");
			
			//sender.sendMessage(ChatColor.BLUE + "/dz " + ChatColor.DARK_AQUA + "setpw <mdp> <mdp>" + ChatColor.DARK_GRAY
			//		+ " - " + ChatColor.GRAY + ChatColor.ITALIC + "Définir votre mdp sur le site");
			
			if (sender.hasPermission("dogez.admin")) {
				sender.sendMessage(ChatColor.RED + "#{dogez.operatoronly}");
				sender.sendMessage(ChatColor.BLUE + "/dz " + ChatColor.DARK_AQUA + "loot" + ChatColor.DARK_GRAY + " - "
						+ ChatColor.GRAY + ChatColor.ITALIC + "#{dogez.loot}");
				sender.sendMessage(ChatColor.BLUE + "/dz " + ChatColor.DARK_AQUA + "reload" + ChatColor.DARK_GRAY
						+ " - " + ChatColor.GRAY + ChatColor.ITALIC + "#{dogez.reload}");
				sender.sendMessage(ChatColor.BLUE + "/dz " + ChatColor.DARK_AQUA + "togglesynch" + ChatColor.DARK_GRAY
						+ " - " + ChatColor.GRAY + ChatColor.ITALIC
						+ "#{dogez.togglesynch}");
				sender.sendMessage(
						ChatColor.BLUE + "/dz " + ChatColor.DARK_AQUA + "togglelocalizedchat" + ChatColor.DARK_GRAY
								+ " - " + ChatColor.GRAY + ChatColor.ITALIC + "#{dogez.togglelocalizedchat}");
			}
			return true;
		}
		if (!(sender instanceof Player)) {
			sender.sendMessage("#{dogez.mustbeplayer}");
			return false;
		}
		if (args.length > 0) {
			Player player = (Player) sender;
			if (args[0].equals("spawn") || args[0].equals("play")) {
				Player player2 = player;
				if (args.length > 1 && sender.hasPermission("dogez.spawn.as"))
					player2 = plugin.getServer().getPlayerByName(args[1]); // Bukkit.getPlayer(args[1]);
				if (player2 == null)
					return false;
				PlayerProfile profile = plugin.getPlayerProfiles().getPlayerProfile(player2.getUUID());
				if (profile.inGame)
					player2.sendMessage(ChatColor.RED + "#{dogez.alreadyig}");
				else {
					int[] coords = SpawnPoints.getRandomSpawn();
					((EntityWithInventory) player2.getControlledEntity()).getInventory().clear();

					((EntityCreative) player2.getControlledEntity()).getCreativeModeComponent().set(false);
					LootCategory spawnGear = plugin.getLootTypes().getCategory("spawn");

					for (ItemPile i : spawnGear.getAllItems()) {
						((EntityWithInventory) player2.getControlledEntity()).getInventory().addItemPile(i);
					}

					profile.inGame = true;
					((EntityLiving) player2.getControlledEntity()).setHealth(100f);
					player2.sendMessage(ChatColor.DARK_AQUA + "#{dogez.goodluck}");

					player2.setLocation(new Location(plugin.getGameWorld(), coords[0], coords[1] + 2, coords[2]));
				}
				return true;
			}
			if (args[0].equals("suicide")) {
				((EntityLiving) player.getControlledEntity()).setHealth(-10f);
				return true;
			}
			if (args[0].equals("stats")) {
				PlayerProfile profile = plugin.getPlayerProfiles().getPlayerProfile(player.getUUID());
				if (args.length == 2 && sender.hasPermission("dogez.viewotherstats")) {
					String requestedPlayerName = args[1];

					// OfflinePlayer requestedPlayer =
					// Bukkit.getOfflinePlayer(requestedPlayerName);
					profile = plugin.getPlayerProfiles().getPlayerProfile(requestedPlayerName.hashCode());
				}
				if (profile == null) {
					sender.sendMessage(ChatColor.RED
							+ "#{dogez.errorloadingprofile}");
					return true;
				}

				profile.timeCalc();
				sender.sendMessage(ChatColor.DARK_GRAY + "=========[" + ChatColor.BLUE + "#{dogez.accountstats}"
						+ ChatColor.RED + profile.name + " " + ChatColor.DARK_GRAY + "]=========");
				// en jeu ?
				sender.sendMessage(ChatColor.AQUA
						+ (profile.inGame ? "#{dogez.ig}" : "#{dogez.og}"));
				// Temps passé
				sender.sendMessage(ChatColor.DARK_AQUA + "#{dogez.spenttime1} "
						+ TimeFormatter.formatTimelapse(profile.timeConnected) + "#{dogez.spenttime2}");
				sender.sendMessage(ChatColor.DARK_AQUA + "#{dogez.alivesince}"
						+ TimeFormatter.formatTimelapse(profile.timeSurvivedLife) + " !");
				// stats kill
				sender.sendMessage(ChatColor.DARK_AQUA + "#{dogez.killed}" + profile.zombiesKilled + "#{dogez.kzombies}"
						+ profile.zombiesKilled_thisLife + "#{dogez.thisgame}");
				sender.sendMessage(ChatColor.DARK_AQUA + "#{dogez.killed}" + profile.playersKilled + " #{dogez.kplayers}"
						+ profile.playersKilled_thisLife + "#{dogez.thisgame}");
				sender.sendMessage(ChatColor.DARK_AQUA + "#{dogez.deaths1}" + profile.deaths + "#{dogez.deaths2}");

				return true;
			}
			if (args[0].equals("money") && args.length == 1) {
				double xc = plugin.getPlayerProfiles().getPlayerProfile(player.getUUID()).xcBalance;
				xc *= 100;
				xc = Math.floor(xc);
				xc /= 100;
				sender.sendMessage(
						ChatColor.DARK_AQUA + "#{dogez.money1}" + xc + " XolioCoins.");
				return true;
			}
			if (args[0].equals("money") && args.length == 2 && sender.hasPermission("dogez.seeothersmoney")) {
				
				Player target = plugin.getServer().getPlayerByName(args[1]);
				if (player != null) {
					double xc = plugin.getPlayerProfiles().getPlayerProfile(args[1].hashCode()).xcBalance;
					xc *= 100;
					xc = Math.floor(xc);
					xc /= 100;
					sender.sendMessage(ChatColor.DARK_AQUA + "#{dogez.money2}" + target.getName()
							+ "#{dogez.money3}" + xc + " XolioCoins.");
				}
				return true;
			}
			if (args[0].equals("pay")) {
				if (args.length == 3) {
					double amount = Double.parseDouble(args[2]);
					
					if (amount < 0.00) {
						player.sendMessage(ChatColor.RED + "#{dogez.nominus}");
						return true;
					}
					if (amount == 0.0) {
						player.sendMessage(ChatColor.RED + "#{dogez.nozero}");
						return true;
					}
					if (amount < 0.01) {
						player.sendMessage(ChatColor.RED + "#{dogez.nolessthan1ct}");
						return true;
					}

					PlayerProfile senderProfile = plugin.getPlayerProfiles().getPlayerProfile(player.getUUID());
					
					PlayerProfile receiverProfile = plugin.getPlayerProfiles().getPlayerProfile(args[1].hashCode());
					if(player.getName().equals(receiverProfile.name)) {
						player.sendMessage(ChatColor.RED + "#{dogez.noself}");
						return true;
					}
					
					if(senderProfile.xcBalance < amount) {
						player.sendMessage(ChatColor.RED + "#{dogez.nofunds}");
						return true;
					}
					
					senderProfile.xcBalance -= amount;
					receiverProfile.xcBalance += amount;
					
					receiverProfile.saveProfile();
					senderProfile.saveProfile();
					
					player.sendMessage(ChatColor.DARK_AQUA + "#{dogez.sentok}" + ChatColor.AQUA
							+ ChatColor.BOLD + amount + ChatColor.DARK_AQUA + "#{dogez.sentok2}" + ChatColor.AQUA
							+ ChatColor.BOLD + receiverProfile);
					
					//If the receiver is online
					Player receiver = plugin.getServer().getPlayerByName(args[1]);
					if(receiver != null)
					{
						receiver.sendMessage(ChatColor.DARK_AQUA + "#{dogez.received}" + ChatColor.AQUA + ChatColor.BOLD
								+ amount + ChatColor.DARK_AQUA + "#{dogez.receivedfrom}" + ChatColor.AQUA
								+ ChatColor.BOLD + player.getName());
					}
					
					return true;
				} else {
					player.sendMessage(ChatColor.RED + "#{dogez.paysyntax}");
					return true;
				}
			}
			
			// admin
			if (args[0].equals("reload")) {
				if (player.hasPermission("dogez.admin")) {
					plugin.loadConfigs();
					player.sendMessage(ChatColor.AQUA + "Configuration rechargée");
				} else
					player.sendMessage(ChatColor.RED + "Vous n'avez pas la permission.");
				return true;
			}
			if (args[0].equals("butcher")) {
				if (player.hasPermission("dogez.admin")) {
					for (Entity entity : player.getWorld().getAllLoadedEntities()) {
						if (entity instanceof EntityZombie) {
							entity.getWorld().removeEntity(entity);
						}
					}
				} else
					player.sendMessage(ChatColor.RED + "Vous n'avez pas la permission.");
				return true;
			}
			if (args[0].equals("loot")) {
				if (player.hasPermission("dogez.admin")) {
					if (args.length == 1) {
						sender.sendMessage(ChatColor.DARK_GRAY + "======[" + ChatColor.BLUE
								+ "Commandes à propos du loot" + ChatColor.DARK_GRAY + "]======");
						sender.sendMessage(ChatColor.BLUE + "/dz " + ChatColor.DARK_AQUA + "loot" + ChatColor.DARK_GRAY
								+ " - " + ChatColor.GRAY + ChatColor.ITALIC + "Voir cette aide.");
						sender.sendMessage(
								ChatColor.BLUE + "/dz " + ChatColor.DARK_AQUA + "loot stats" + ChatColor.DARK_GRAY
										+ " - " + ChatColor.GRAY + ChatColor.ITALIC + "Voir des stats sur le loot");
						sender.sendMessage(ChatColor.BLUE + "/dz " + ChatColor.DARK_AQUA + "loot add <type> [min] [max]"
								+ ChatColor.DARK_GRAY + " - " + ChatColor.GRAY + ChatColor.ITALIC + "Ajouter un point");
						sender.sendMessage(
								ChatColor.BLUE + "/dz " + ChatColor.DARK_AQUA + "loot remove" + ChatColor.DARK_GRAY
										+ " - " + ChatColor.GRAY + ChatColor.ITALIC + "Supprimmer un point");
						sender.sendMessage(
								ChatColor.BLUE + "/dz " + ChatColor.DARK_AQUA + "loot reload" + ChatColor.DARK_GRAY
										+ " - " + ChatColor.GRAY + ChatColor.ITALIC + "Recharger le fichier");
						sender.sendMessage(
								ChatColor.BLUE + "/dz " + ChatColor.DARK_AQUA + "loot save" + ChatColor.DARK_GRAY
										+ " - " + ChatColor.GRAY + ChatColor.ITALIC + "Sauvegarder le fichier");
						sender.sendMessage(
								ChatColor.BLUE + "/dz " + ChatColor.DARK_AQUA + "loot reloot" + ChatColor.DARK_GRAY
										+ " - " + ChatColor.GRAY + ChatColor.ITALIC + "Ré-initialise chaque point");
						sender.sendMessage(
								ChatColor.BLUE + "/dz " + ChatColor.DARK_AQUA + "loot list" + ChatColor.DARK_GRAY
										+ " - " + ChatColor.GRAY + ChatColor.ITALIC + "Liste les types de loot");
						sender.sendMessage(ChatColor.BLUE + "/dz " + ChatColor.DARK_AQUA
								+ "loot arround <rayon> [force]" + ChatColor.DARK_GRAY + " - " + ChatColor.GRAY
								+ ChatColor.ITALIC + "set les loots à x blocs à la ronde");
						return true;
					} else {
						//Add loot spawn points mode
						if (args[1].equals("add") && args.length >= 3) {
							
							//Parse the category name supplied
							String categoryName = args[2];
							LootCategory category = plugin.getLootTypes().getCategory(categoryName);
							
							//Check the supplied one exist
							if (category != null) {
								//Grab his profile & configure it like so
								PlayerProfile profile = plugin.getPlayerProfiles().getPlayerProfile(player.getUUID());
								
								profile.activeCategory = category;
								profile.adding = true;
								
								if (args.length >= 4)
									profile.currentMin = Integer.parseInt(args[3]);
								if (args.length >= 5)
									profile.currentMax = Integer.parseInt(args[4]);
								
								player.sendMessage(ChatColor.AQUA + "Vous placez désormais des points de loot ["
										+ categoryName + ":" + profile.currentMin + "-" + profile.currentMax + "]");
							} else {
								player.sendMessage(
										ChatColor.RED + "La catégorie de loot \"" + categoryName + "\" n'éxiste pas.");
							}
							return true;
						} else if (args[1].equals("remove")) {
							PlayerProfile pp = plugin.getPlayerProfiles().getPlayerProfile(player.getUUID());
							pp.adding = false;
							player.sendMessage(ChatColor.AQUA + "Vous supprimmez désormais des points de loot");
							return true;
						} else if (args[1].equals("stats")) {
							player.sendMessage(ChatColor.AQUA + "Il existe "
									+ plugin.getLootPlaces().count(player.getControlledEntity().getWorld())
									+ " points de loot dans le fichier.");
							return true;
						} else if (args[1].equals("list")) {
							String list = "";
							for (String c : plugin.getLootTypes().categories.keySet()) {
								list += c + ",";
							}
							list = list.substring(0, list.length() - 1);
							player.sendMessage(ChatColor.AQUA + "Types de loot :" + list);
							return true;
						} else if (args[1].equals("reload")) {
							plugin.getLootPlaces().loadLootFile(player.getControlledEntity().getWorld());
							player.sendMessage(ChatColor.AQUA + "Fichier de loot rechargé.");
							return true;
						} else if (args[1].equals("save")) {
							plugin.getLootPlaces().saveLootFile(player.getControlledEntity().getWorld());
							player.sendMessage(ChatColor.AQUA + "Fichier de loot sauvegardé.");
							return true;
						} else if (args[1].equals("reloot")) {
							player.sendMessage(ChatColor.AQUA + ""
									+ plugin.getLootPlaces().respawnLoot(player.getControlledEntity().getWorld())
									+ " points de loot respawnés.");
							return true;
						} else if (args[1].equals("arround")) {
							if (args.length >= 3) {
								boolean force = (args.length == 4 && args[3].endsWith("force"));
								int radius = Integer.parseInt(args[2]);
								if (radius > 50) {
									player.sendMessage(ChatColor.AQUA + "Rayon trop élevé, cappé à 50");
									radius = 50;
								}
								int amount = plugin.getLootPlaces().generateLootPointsArroundPlayer(player, radius, force);
								player.sendMessage(ChatColor.AQUA + "" + amount
										+ " nouveaux points de loot ajoutés ( f=" + force + " )");
							} else
								player.sendMessage(ChatColor.AQUA + "Syntaxe : /dz loot arround <rayon> [force]");
							return true;
						}
					}
				} else {
					player.sendMessage(ChatColor.RED + "Vous n'avez pas la permission.");
					return true;
				}
			}
			// debug
			if (args[0].equals("cc") && player.hasPermission("dogez.admin.cc")) {
				sender.sendMessage(ChatColor.AQUA + "Toutes les chunks ont étés nettoyés");
				return true;
			}
			if (args[0].equals("snd") && player.hasPermission("dogez.admin.snd")) {
				if (args.length == 2)
					player.getControlledEntity().getWorld().getSoundManager().playSoundEffect(args[1], Mode.NORMAL, 
							player.getLocation(), 1f, 1f, 5f, 15f);
				// PlayersPackets.playSound(player.getLocation(),args[1],1f,1f);
				else if (args.length == 3)
					player.getControlledEntity().getWorld().getSoundManager().playSoundEffect(args[1],Mode.NORMAL, 
							player.getLocation(), Float.parseFloat(args[2]), 1f, 5f, 15f);
				// PlayersPackets.playSound(player.getLocation(),args[1],1f,Float.parseFloat(args[2]));
				else if (args.length == 4)
					player.getControlledEntity().getWorld().getSoundManager().playSoundEffect(args[1],Mode.NORMAL, 
							player.getLocation(), Float.parseFloat(args[3]), Float.parseFloat(args[2]), 5f, 15f);
				// PlayersPackets.playSound(player.getLocation(),args[1],Float.parseFloat(args[3]),Float.parseFloat(args[2]));
				else
					sender.sendMessage(ChatColor.RED + "Syntaxe : /dz snd node.du.son [pitch] [volume]");
				return true;
			}
			if (args[0].equals("zmb") && player.hasPermission("dogez.admin.debug")) {
				plugin.spawner.spawnZombie(player.getLocation());
				return true;
			}
			if (args[0].equals("fli") && player.hasPermission("dogez.admin")) {
				/*
				 * PermissionUser user = PermissionsEx.getUser(player); String
				 * prefix = user.getPrefix();
				 */
				plugin.getServer()
						.broadcastMessage(ChatColor.DARK_GRAY + "[" + ChatColor.GREEN + "+" + ChatColor.DARK_GRAY + "] "
								+ player.getDisplayName() + ChatColor.GRAY + " vient de se connecter.");
				return true;
			}
			if (args[0].equals("flo") && player.hasPermission("dogez.admin")) {
				/*
				 * PermissionUser user = PermissionsEx.getUser(player); String
				 * prefix = user.getPrefix();
				 */
				plugin.getServer()
						.broadcastMessage(ChatColor.DARK_GRAY + "[" + ChatColor.RED + "-" + ChatColor.DARK_GRAY + "] "
								+ player.getDisplayName() + ChatColor.GRAY + " vient de se déconnecter.");
				return true;
			}
			if (args[0].equals("pn") && player.hasPermission("dogez.admin.debug")) {
				sender.sendMessage("PlaceNAme=" + PlacesNames.getPlayerPlaceName(player));
				return true;
			}
			if (args[0].equals("testloot") && args.length == 2 && player.hasPermission("dogez.admin.testloot")) {
				String category = args[1];
				if (plugin.getLootTypes().categories.keySet().contains(category)) {
					LootType lt = plugin.getLootTypes().getCategory(category).getRandomSpawn();
					sender.sendMessage("Giving random loot in category " + category + " : " + lt.toString());
				} else {
					player.sendMessage(ChatColor.RED + "Wrong cat, fuck you");
				}
				return true;
			}
			if (args[0].equals("load") && player.hasPermission("dogez.admin.debug")) {
				sender.sendMessage("Forcing reload of profile");
				plugin.getPlayerProfiles().getPlayerProfile(player.getUUID()).reloadProfile();
				return true;
			}
			if (args[0].equals("save") && player.hasPermission("dogez.admin.debug")) {
				sender.sendMessage("Forcing save of profile");
				plugin.getPlayerProfiles().getPlayerProfile(player.getUUID()).saveProfile();
				return true;
			}

			if (args[0].equals("togglesynch") && player.hasPermission("dogez.admin")) {
				
				plugin.config.setProperty("irlTimeCycleSync", ""+(!plugin.config.getBoolean("irlTimeCycleSync", true)));
				//plugin.config.setProperty("irlTimeCycleSync", plugin.config.getProperty("irlTimeCycleSync", "true").equals("true") ? "false" : "true");// = !plugin.config.synchTime;
				sender.sendMessage("Synchronization heure IRL : " + plugin.config.getBoolean("irlTimeCycleSync"));
				return true;
			}
		}
		sender.sendMessage(ChatColor.RED + "Commande inconnue");
		return true;
	}

}
