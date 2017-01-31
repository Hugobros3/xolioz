package io.xol.dogez.plugin.game;

//Copyright 2014 XolioWare Interactive

import io.xol.chunkstories.api.Location;
import io.xol.chunkstories.api.compatibility.ChatColor;
import io.xol.chunkstories.api.entity.Entity;
import io.xol.chunkstories.api.entity.EntityLiving;
import io.xol.chunkstories.api.entity.interfaces.EntityCreative;
import io.xol.chunkstories.api.entity.interfaces.EntityWithInventory;
import io.xol.chunkstories.api.item.ItemPile;
import io.xol.chunkstories.api.plugin.commands.Command;
import io.xol.chunkstories.api.plugin.commands.CommandEmitter;
import io.xol.chunkstories.api.plugin.commands.CommandHandler;
import io.xol.chunkstories.api.server.Player;
import io.xol.chunkstories.core.entity.EntityZombie;
import io.xol.dogez.plugin.DogeZPlugin;
import io.xol.dogez.plugin.loot.LootCategory;
import io.xol.dogez.plugin.loot.LootItem;
import io.xol.dogez.plugin.loot.LootType;
import io.xol.dogez.plugin.map.PlacesNames;
import io.xol.dogez.plugin.misc.HttpRequestThread;
import io.xol.dogez.plugin.misc.HttpRequester;
import io.xol.dogez.plugin.misc.TimeFormatter;
import io.xol.dogez.plugin.player.PlayerProfile;
import io.xol.dogez.plugin.weapon.ChunksCleaner;
import io.xol.dogez.plugin.zombies.ZombieType;

public class DogeZPluginCommandsHandler implements CommandHandler, HttpRequester {

	private final DogeZPlugin plugin;

	public DogeZPluginCommandsHandler(DogeZPlugin dogeZPlugin) {
		plugin = dogeZPlugin;
	}

	@Override
	public boolean handleCommand(CommandEmitter sender, Command cmd, String[] args) {

		if (cmd.getName().equals("m")) {
			if (!(sender instanceof Player)) {
				sender.sendMessage("You must be a player to use this.");
				return true;
			}
			Player player = (Player) sender;
			PlayerProfile pp = plugin.getPlayerProfiles().getPlayerProfile(player.getUUID());
			if (args.length < 2) {
				sender.sendMessage(ChatColor.RED + "Syntaxe correcte : /m <joueur> <message>");
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
						destPlayer.sendMessage(ChatColor.GRAY + "[MP de la part de " + ChatColor.AQUA + player.getName()
								+ ChatColor.GRAY + "]:" + toSend);
						player.sendMessage(ChatColor.GRAY + "[MP envoyé à " + ChatColor.AQUA + destination
								+ ChatColor.GRAY + "]:" + toSend);
						if (!canReply)
							player.sendMessage(ChatColor.RED
									+ "Attention, le joueur auquel vous parlez ne peut pas vous répondre, ");
						pp.talkingTo = destination;
						pp2.talkingTo = player.getName();
					} else {
						sender.sendMessage(
								ChatColor.RED + "Le joueur auquel vous vous addressez n'est pas disponible.");
					}
				} else {
					sender.sendMessage(ChatColor.RED + "Il vous faut un talkie-walkie pour faire ça !");
				}
			}
			return true;
		}
		if (cmd.getName().equals("r")) {
			if (!(sender instanceof Player)) {
				sender.sendMessage("You must be a player to use this.");
				return true;
			}
			Player player = (Player) sender;
			PlayerProfile pp = plugin.getPlayerProfiles().getPlayerProfile(player.getUUID());
			if (args.length < 1) {
				sender.sendMessage(ChatColor.RED + "Syntaxe correcte : /r <message>");
			} else {
				String toSend = "";
				if (plugin.getTalkieWalkiesHandler().canPlayerUse(player)) {
					if (pp.talkingTo.equals("")) {
						sender.sendMessage(ChatColor.RED + "Vous n'avez personne à qui répondre :foreveralone:");
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
						destPlayer.sendMessage(ChatColor.GRAY + "[MP de la part de " + ChatColor.AQUA + player.getName()
								+ ChatColor.GRAY + "]:" + toSend);
						player.sendMessage(ChatColor.GRAY + "[MP envoyé à " + ChatColor.AQUA + destination
								+ ChatColor.GRAY + "]:" + toSend);
						if (!canReply)
							player.sendMessage(ChatColor.RED
									+ "Attention, le joueur auquel vous parlez ne peut pas vous répondre, ");
						pp.talkingTo = destination;
						pp2.talkingTo = player.getName();
						// TalkieWalkie.notifyListenersAdmins(player.getName(),
						// destination, toSend);
					} else {
						sender.sendMessage(
								ChatColor.RED + "Le joueur auquel vous vous addressez n'est pas disponible.");
					}
				} else {
					sender.sendMessage(ChatColor.RED + "Il vous faut un talkie-walkie pour faire ça !");
				}
			}
			return true;
		}

		if (args.length == 0) {
			sender.sendMessage(ChatColor.BLUE + "Plugin DogeZ " + ChatColor.DARK_GREEN + "(v " + plugin.version + ")");
			sender.sendMessage(ChatColor.DARK_GRAY + "=========[" + ChatColor.BLUE + "Commandes disponibles"
					+ ChatColor.DARK_GRAY + "]=========");
			sender.sendMessage(ChatColor.BLUE + "/dz" + ChatColor.DARK_GRAY + " - " + ChatColor.GRAY + ChatColor.ITALIC
					+ "Voir cette aide.");
			sender.sendMessage(ChatColor.BLUE + "/dz " + ChatColor.DARK_AQUA + "play" + ChatColor.DARK_GRAY + " - "
					+ ChatColor.GRAY + ChatColor.ITALIC + "Commencer une nouvelle partie");
			sender.sendMessage(ChatColor.BLUE + "/dz " + ChatColor.DARK_AQUA + "stats" + ChatColor.DARK_GRAY + " - "
					+ ChatColor.GRAY + ChatColor.ITALIC + "Obtenir des statistiques sur votre partie");
			sender.sendMessage(ChatColor.BLUE + "/dz " + ChatColor.DARK_AQUA + "money" + ChatColor.DARK_GRAY + " - "
					+ ChatColor.GRAY + ChatColor.ITALIC + "Connaitre votre argent");
			sender.sendMessage(ChatColor.BLUE + "/dz " + ChatColor.DARK_AQUA + "pay <joueur> <montant>"
					+ ChatColor.DARK_GRAY + " - " + ChatColor.GRAY + ChatColor.ITALIC + "Envoyer de l'argent");
			// sender.sendMessage(ChatColor.BLUE+"/dz
			// "+ChatColor.DARK_AQUA+"report <joueur>
			// <raison>"+ChatColor.DARK_GRAY+" -
			// "+ChatColor.GRAY+ChatColor.ITALIC+"Signaler un joueur");
			sender.sendMessage(ChatColor.BLUE + "/dz " + ChatColor.DARK_AQUA + "setpw <mdp> <mdp>" + ChatColor.DARK_GRAY
					+ " - " + ChatColor.GRAY + ChatColor.ITALIC + "Définir votre mdp sur le site");
			if (sender.hasPermission("dogez.admin")) {
				sender.sendMessage(ChatColor.RED + "Les commandes ci-dessous sont réservées aux opérateurs.");
				sender.sendMessage(ChatColor.BLUE + "/dz " + ChatColor.DARK_AQUA + "loot" + ChatColor.DARK_GRAY + " - "
						+ ChatColor.GRAY + ChatColor.ITALIC + "Commandes de gestion du loot");
				sender.sendMessage(ChatColor.BLUE + "/dz " + ChatColor.DARK_AQUA + "reload" + ChatColor.DARK_GRAY
						+ " - " + ChatColor.GRAY + ChatColor.ITALIC + "Recharge les fichiers de configuration");
				sender.sendMessage(ChatColor.BLUE + "/dz " + ChatColor.DARK_AQUA + "togglesynch" + ChatColor.DARK_GRAY
						+ " - " + ChatColor.GRAY + ChatColor.ITALIC
						+ "Active où désactive la synchrinisation heure IRL");
				sender.sendMessage(
						ChatColor.BLUE + "/dz " + ChatColor.DARK_AQUA + "togglelocalizedchat" + ChatColor.DARK_GRAY
								+ " - " + ChatColor.GRAY + ChatColor.ITALIC + "Active ou désactive le chat localisé");
			}
			return true;
		}
		if (!(sender instanceof Player)) {
			sender.sendMessage("You must be a player to use this.");
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
					player2.sendMessage(ChatColor.RED + "Vous êtes déjà en jeu !");
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
					player2.sendMessage(ChatColor.DARK_AQUA + "Vous venez de commencer une partie, bonne chance !");

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
				if (args.length == 2) {
					String requestedPlayerName = args[1];

					Player requestedPlayer = plugin.getServer().getPlayerByName(requestedPlayerName);
					// OfflinePlayer requestedPlayer =
					// Bukkit.getOfflinePlayer(requestedPlayerName);
					profile = plugin.getPlayerProfiles().getPlayerProfile(requestedPlayer.getUUID());
				}
				if (profile == null) {
					sender.sendMessage(ChatColor.RED
							+ "Impossible d'afficher ce profil. ( Erreur de chargement depuis la BDD ?) ");
					return true;
				}

				profile.timeCalc();
				sender.sendMessage(ChatColor.DARK_GRAY + "=========[" + ChatColor.BLUE + "Statistiques du compte "
						+ ChatColor.RED + profile.name + " " + ChatColor.DARK_GRAY + "]=========");
				// en jeu ?
				sender.sendMessage(ChatColor.AQUA
						+ (profile.inGame ? "Actuellement dans une partie" : "Hors-jeu ( spawn où minijeu )"));
				// Temps passé
				sender.sendMessage(ChatColor.DARK_AQUA + "Vous avez passé "
						+ TimeFormatter.formatTimelapse(profile.timeConnected) + " sur ce serveur !");
				sender.sendMessage(ChatColor.DARK_AQUA + "Vous êtes en vie depuis "
						+ TimeFormatter.formatTimelapse(profile.timeSurvivedLife) + " !");
				// stats kill
				sender.sendMessage(ChatColor.DARK_AQUA + "Vous avez tué " + profile.zombiesKilled + " zombies, dont "
						+ profile.zombiesKilled_thisLife + " sur cette partie.");
				sender.sendMessage(ChatColor.DARK_AQUA + "Vous avez tué " + profile.playersKilled + " joueurs, dont "
						+ profile.playersKilled_thisLife + " sur cette partie.");
				sender.sendMessage(ChatColor.DARK_AQUA + "Vous avez été tué " + profile.deaths + " fois");

				return true;
			}
			if (args[0].equals("money") && args.length == 1) {
				double xc = plugin.getPlayerProfiles().getPlayerProfile(player.getUUID()).xcBalance;
				xc *= 100;
				xc = Math.floor(xc);
				xc /= 100;
				sender.sendMessage(
						ChatColor.DARK_AQUA + "Votre compte est actuellement crédité de " + xc + " XolioCoins.");
				return true;
			}
			if (args[0].equals("money") && args.length == 2) {
				// OfflinePlayer target =
				// plugin.getServer().getOfflinePlayer(args[1]);
				Player target = plugin.getServer().getPlayerByName(args[1]);
				if (player != null) {
					double xc = plugin.getPlayerProfiles().getPlayerProfile(target.getUUID()).xcBalance;
					xc *= 100;
					xc = Math.floor(xc);
					xc /= 100;
					sender.sendMessage(ChatColor.DARK_AQUA + "Le compte de " + target.getName()
							+ " est actuellement crédité de " + xc + " XolioCoins.");
				}
				return true;
			}
			if (args[0].equals("pay")) {
				if (args.length == 3) {
					double amount = Double.parseDouble(args[2]);
					if (amount < 0.01) {
						player.sendMessage(ChatColor.RED + "Vous ne pouvez pas envoyer moins de 1 centime.");
						return true;
					}
					new HttpRequestThread(this, "sendMoney:" + player.getUUID(),
							"http://dz.xol.io/api/playerProfile.php",
							"a=pay&uuid=" + player.getUUID() + "&amount=" + amount + "&who=" + args[1]).run();
					return true;
				} else {
					player.sendMessage(ChatColor.RED + "Syntaxe correcte : /dz pay <joueur> <montant>");
					return true;
				}
			}
			if (args[0].equals("setpw")) {
				if (args.length == 3) {
					String password = args[1];
					if (args[1].equals(args[2])) {
						if (password.length() >= 6) {
							new HttpRequestThread(this, "setPW:" + player.getUUID(),
									"http://dz.xol.io/api/playerProfile.php",
									"a=setpw&uuid=" + player.getUUID() + "&pw=" + password).run();
							return true;
						} else {
							player.sendMessage(ChatColor.RED + "Votre mot de passe fait moins de 6 caractères !");
							return true;
						}
					} else {
						player.sendMessage(ChatColor.RED + "Les deux mots de passe ne correspondent pas.");
						return true;
					}
				} else {
					player.sendMessage(ChatColor.RED + "Syntaxe correcte : /dz setpw <mdp> <mdp>");
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
						if (args[1].equals("add") && args.length >= 3) {
							String category = args[2];
							if (plugin.getLootTypes().categories.keySet().contains(category)) {
								PlayerProfile pp = plugin.getPlayerProfiles().getPlayerProfile(player.getUUID());
								pp.activeCategory = category;
								pp.adding = true;
								if (args.length >= 4)
									pp.currentMin = Integer.parseInt(args[3]);
								if (args.length >= 5)
									pp.currentMax = Integer.parseInt(args[4]);
								player.sendMessage(ChatColor.AQUA + "Vous placez désormais des points de loot ["
										+ category + ":" + pp.currentMin + "-" + pp.currentMax + "]");
							} else {
								player.sendMessage(
										ChatColor.RED + "La catégorie de loot \"" + category + "\" n'éxiste pas.");
							}
							return true;
						}
						if (args[1].equals("remove")) {
							PlayerProfile pp = plugin.getPlayerProfiles().getPlayerProfile(player.getUUID());
							pp.adding = false;
							player.sendMessage(ChatColor.AQUA + "Vous supprimmez désormais des points de loot");
							return true;
						}
						if (args[1].equals("stats")) {
							player.sendMessage(ChatColor.AQUA + "Il existe "
									+ plugin.getLootPlaces().count(player.getControlledEntity().getWorld())
									+ " points de loot dans le fichier.");
							return true;
						}
						if (args[1].equals("list")) {
							String list = "";
							for (String c : plugin.getLootTypes().categories.keySet()) {
								list += c + ",";
							}
							list = list.substring(0, list.length() - 1);
							player.sendMessage(ChatColor.AQUA + "Types de loot :" + list);
							return true;
						}
						if (args[1].equals("reload")) {
							plugin.getLootPlaces().loadLootFile(player.getControlledEntity().getWorld());
							player.sendMessage(ChatColor.AQUA + "Fichier de loot rechargé.");
							return true;
						}
						if (args[1].equals("save")) {
							plugin.getLootPlaces().saveLootFile(player.getControlledEntity().getWorld());
							player.sendMessage(ChatColor.AQUA + "Fichier de loot sauvegardé.");
							return true;
						}
						if (args[1].equals("reloot")) {
							player.sendMessage(ChatColor.AQUA + ""
									+ plugin.getLootPlaces().respawnLoot(player.getControlledEntity().getWorld())
									+ " points de loot respawnés.");
							return true;
						}
						if (args[1].equals("arround")) {
							if (args.length >= 3) {
								boolean force = (args.length == 4 && args[3].endsWith("force"));
								int radius = Integer.parseInt(args[2]);
								if (radius > 50) {
									player.sendMessage(ChatColor.AQUA + "Rayon trop élevé, cappé à 50");
									radius = 50;
								}
								int amount = plugin.getLootPlaces().lootArroundPlayer(player, radius, force);
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
			if (args[0].equals("item")) {
				if (player.hasPermission("dogez.admin")) {
					if (args.length == 1) {
						sender.sendMessage(ChatColor.DARK_GRAY + "======[" + ChatColor.BLUE
								+ "Commandes à propos des items" + ChatColor.DARK_GRAY + "]======");
						sender.sendMessage(ChatColor.BLUE + "/dz " + ChatColor.DARK_AQUA + "item" + ChatColor.DARK_GRAY
								+ " - " + ChatColor.GRAY + ChatColor.ITALIC + "Voir cette aide.");
						sender.sendMessage(
								ChatColor.BLUE + "/dz " + ChatColor.DARK_AQUA + "item list [page]" + ChatColor.DARK_GRAY
										+ " - " + ChatColor.GRAY + ChatColor.ITALIC + "Voire une liste des items");
						sender.sendMessage(ChatColor.BLUE + "/dz " + ChatColor.DARK_AQUA + "item give <name> [amount]"
								+ ChatColor.DARK_GRAY + " - " + ChatColor.GRAY + ChatColor.ITALIC + "Donne un item");
						return true;
					} else {
						if (args[1].equals("list")) {
							int page = 0;
							if (args.length == 3)
								page = Integer.parseInt(args[2]);
							/*
							 * while(page*10 > LootItems.lootItems.size())
							 * page--;
							 */
							Object[] keys = plugin.getLootItems().lootItems.keySet().toArray();
							int lol = 0;
							for (int i = 0; i < 10; i++) {
								if (page * 10 + i < keys.length) {
									LootItem li = plugin.getLootItems().getItem((String) keys[page * 10 + i]);
									player.sendMessage(
											ChatColor.AQUA + li.internalName + " " + li.type + " => " + li.name);
									lol++;
								}
							}
							sender.sendMessage(ChatColor.BLUE + "" + lol + " items sur " + keys.length
									+ " affichés. ( page " + page + " sur " + keys.length / 10 + ")");
						}
						if (args[1].equals("give")) {
							try {
								int amount = 1;
								if (args.length == 4)
									amount = Integer.parseInt(args[3]);
								ItemPile gimme = plugin.getLootItems().getItem(args[2]).getItem();
								gimme.setAmount(amount);
								((EntityWithInventory) player.getControlledEntity()).getInventory().addItemPile(gimme);
								// player.updateInventory();
								sender.sendMessage(
										ChatColor.AQUA + "Item " + args[2] + " donné en " + amount + " exemplaire(s).");

							} catch (Exception e) {
								player.sendMessage(ChatColor.RED + "Erreur. " + e.getLocalizedMessage());
							}
						}
					}
					return true;
				}
			}
			// debug
			if (args[0].equals("cc") && player.hasPermission("dogez.admin.cc")) {
				ChunksCleaner.cleanAllChunks(player.getControlledEntity().getWorld());
				sender.sendMessage(ChatColor.AQUA + "Toutes les chunks ont étés nettoyés");
				return true;
			}
			if (args[0].equals("snd") && player.hasPermission("dogez.admin.snd")) {
				if (args.length == 2)
					player.getControlledEntity().getWorld().getSoundManager().playSoundEffect(args[1],
							player.getLocation(), 1f, 1f);
				// PlayersPackets.playSound(player.getLocation(),args[1],1f,1f);
				else if (args.length == 3)
					player.getControlledEntity().getWorld().getSoundManager().playSoundEffect(args[1],
							player.getLocation(), Float.parseFloat(args[2]), 1f);
				// PlayersPackets.playSound(player.getLocation(),args[1],1f,Float.parseFloat(args[2]));
				else if (args.length == 4)
					player.getControlledEntity().getWorld().getSoundManager().playSoundEffect(args[1],
							player.getLocation(), Float.parseFloat(args[3]), Float.parseFloat(args[2]));
				// PlayersPackets.playSound(player.getLocation(),args[1],Float.parseFloat(args[3]),Float.parseFloat(args[2]));
				else
					sender.sendMessage(ChatColor.RED + "Syntaxe : /dz snd node.du.son [pitch] [volume]");
				return true;
			}
			if (args[0].equals("zmb") && player.hasPermission("dogez.admin.debug")) {
				plugin.spawner.spawnZombie(player.getLocation(), ZombieType.values()[Integer.parseInt(args[1])]);
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
				plugin.config.synchTime = !plugin.config.synchTime;
				sender.sendMessage("Synchronization heure IRL : " + plugin.config.synchTime);
				return true;
			}
		}
		sender.sendMessage(ChatColor.RED + "Commande inconnue");
		return true;
	}

	@Override
	public void handleHttpRequest(String info, String result) {
		System.out.println("[DogeZ][Debug] Request answered:" + result);
		if (info.startsWith("sendMoney")) {
			String uuid = info.split(":")[1];
			Player player = plugin.getServer().getPlayerByUUID(Long.parseLong(uuid));// Bukkit.getPlayer(UUID.fromString(uuid));
			if (player != null) {
				if (result.startsWith("success")) {
					String amount = result.split(":")[2];
					String receiver = result.split(":")[1];

					plugin.getPlayerProfiles().getPlayerProfile(player.getUUID()).reloadProfile();
					Player receiverP = plugin.getServer().getPlayerByName(receiver);// Bukkit.getPlayer(receiver);
					if (receiverP != null) {
						plugin.getPlayerProfiles().getPlayerProfile(receiverP.getUUID()).reloadProfile();
						receiverP.sendMessage(ChatColor.DARK_AQUA + "Vous avez reçu " + ChatColor.AQUA + ChatColor.BOLD
								+ amount + ChatColor.DARK_AQUA + " XolioCoins de la part de " + ChatColor.AQUA
								+ ChatColor.BOLD + player.getName());
					}

					player.sendMessage(ChatColor.DARK_AQUA + "Vous avez envoyé avec succès " + ChatColor.AQUA
							+ ChatColor.BOLD + amount + ChatColor.DARK_AQUA + " XolioCoins à " + ChatColor.AQUA
							+ ChatColor.BOLD + receiver);
				} else if (result.startsWith("nominus")) {
					player.sendMessage(ChatColor.RED + "Bien tenté ;)");
				} else if (result.startsWith("nozero")) {
					player.sendMessage(ChatColor.RED + "Envoyer rien du tout ça sert à [GOTO 8]");
				} else if (result.startsWith("nofunds")) {
					player.sendMessage(ChatColor.RED + "Vous ne disposez pas de l'argent nécéssaire... :/");
				} else if (result.startsWith("noself")) {
					player.sendMessage(ChatColor.RED + "C'est pas un peu débile de s'envoyer du fric ?");
				} else if (result.startsWith("noplayer")) {
					player.sendMessage(ChatColor.RED + "Ce joueur n'existe pas dans la base de données... :/");
				} else
					player.sendMessage(ChatColor.RED + "Erreur inconnue survenue lors de l'envoi d'argent :/");
			} else
				System.out.println("[DogeZ][Debug] null player :/ " + uuid);

			// player.sendMessage(ChatColor.DARK_AQUA+"Votre compte est
			// actuellement crédité de
			// "+plugin.getPlayerProfiles().getPlayerProfile(player.getUUID()).xcBalance+"
			// XolioCoins.");
		}
		if (info.startsWith("setPW")) {
			String uuid = info.split(":")[1];
			Player player = plugin.getServer().getPlayerByUUID(Long.parseLong(uuid));
			if (player != null) {
				if (result.equals("password set"))
					player.sendMessage(ChatColor.DARK_AQUA + "Votre mot de passe à bien été modifié.");
				else
					player.sendMessage(ChatColor.RED
							+ "Pour une raison non-gérée par ce plugin votre mot de passe n'a pas été modifié :/");
			}
		}
	}

}
