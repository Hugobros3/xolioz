package io.xol.z.plugin.game;

import io.xol.chunkstories.api.Location;
import io.xol.chunkstories.api.entity.Entity;
import io.xol.chunkstories.api.entity.traits.serializable.TraitCreativeMode;
import io.xol.chunkstories.api.entity.traits.serializable.TraitHealth;
import io.xol.chunkstories.api.entity.traits.serializable.TraitInventory;
import io.xol.chunkstories.api.item.inventory.ItemPile;
import io.xol.chunkstories.api.player.Player;
import io.xol.chunkstories.api.plugin.commands.Command;
import io.xol.chunkstories.api.plugin.commands.CommandEmitter;
import io.xol.chunkstories.api.plugin.commands.CommandHandler;
import io.xol.chunkstories.api.sound.SoundSource.Mode;
import io.xol.chunkstories.api.util.compatibility.ChatColor;
import io.xol.chunkstories.core.entity.EntityZombie;
import io.xol.z.plugin.XolioZPlugin;
import io.xol.z.plugin.loot.LootCategory;
import io.xol.z.plugin.loot.LootType;
import io.xol.z.plugin.map.PlacesNames;
import io.xol.z.plugin.misc.TimeFormatter;
import io.xol.z.plugin.player.PlayerProfile;

/** 
 * Monolithic class that handles all the command-line stuff.
 * Written in 2014, wouldn't hurt to see a bigger cleanup
 */
public class XolioZCommandsHandler implements CommandHandler {

	private final XolioZPlugin plugin;

	public XolioZCommandsHandler(XolioZPlugin dogeZPlugin) {
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
				if (plugin.getTalkieWalkiesHandler().canPlayerUseTW(player) || (destPlayer != null && destPlayer.hasPermission("dogez.talkie.receiveAnyway"))) {
					if (destPlayer != null) {
						PlayerProfile pp2 = plugin.getPlayerProfiles().getPlayerProfile(destPlayer.getUUID());
						boolean canReply = plugin.getTalkieWalkiesHandler().canPlayerUseTW(destPlayer);
						for (int i = 1; i < args.length - 1; i++)
							toSend += args[i] + " ";
						toSend += args[args.length - 1];
						destPlayer.sendMessage(ChatColor.GRAY + "[#{dogez.pmfrom}" + ChatColor.AQUA + player.getName() + ChatColor.GRAY + "]:" + toSend);
						player.sendMessage(ChatColor.GRAY + "[#{dogez.pmto}" + ChatColor.AQUA + destination + ChatColor.GRAY + "]:" + toSend);
						if (!canReply)
							player.sendMessage(ChatColor.RED + "#{dogez.pmnoreplywarn}");
						pp.talkingTo = destination;
						pp2.talkingTo = player.getName();
					} else {
						sender.sendMessage(ChatColor.RED + "#{dogez.pmnotdispo}");
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
				if (plugin.getTalkieWalkiesHandler().canPlayerUseTW(player)) {
					if (pp.talkingTo.equals("")) {
						sender.sendMessage(ChatColor.RED + "#{dogez.pmnoreply}");
						return true;
					}
					String destination = pp.talkingTo;
					Player destPlayer = plugin.getServer().getPlayerByName(destination);
					if (destPlayer != null) {
						PlayerProfile pp2 = plugin.getPlayerProfiles().getPlayerProfile(destPlayer.getUUID());
						boolean canReply = plugin.getTalkieWalkiesHandler().canPlayerUseTW(destPlayer);
						for (int i = 0; i < args.length - 1; i++)
							toSend += args[i] + " ";
						toSend += args[args.length - 1];
						destPlayer.sendMessage(ChatColor.GRAY + "[#{dogez.pmfrom}" + ChatColor.AQUA + player.getName() + ChatColor.GRAY + "]:" + toSend);
						player.sendMessage(ChatColor.GRAY + "[#{dogez.pmto}" + ChatColor.AQUA + destination + ChatColor.GRAY + "]:" + toSend);
						if (!canReply)
							player.sendMessage(ChatColor.RED + "#{dogez.pmnoreplywarn}");
						pp.talkingTo = destination;
						pp2.talkingTo = player.getName();

					} else {
						sender.sendMessage(ChatColor.RED + "#{dogez.pmnotdispo}");
					}
				} else {
					sender.sendMessage(ChatColor.RED + "#{dogez.pmrequiretw}");
				}
			}
			return true;
		}

		if (args.length == 0) {
			sender.sendMessage(ChatColor.BLUE + "#{dogez.pluginname}" + ChatColor.DARK_GREEN + plugin.version);
			sender.sendMessage(ChatColor.DARK_GRAY + "=========[" + ChatColor.BLUE + "#{dogez.avaiablecmds}" + ChatColor.DARK_GRAY + "]=========");
			sender.sendMessage(ChatColor.BLUE + "/dz" + ChatColor.DARK_GRAY + " - " + ChatColor.GRAY + ChatColor.ITALIC + "#{dogez.seethishelp}");
			sender.sendMessage(
					ChatColor.BLUE + "/dz " + ChatColor.DARK_AQUA + "play" + ChatColor.DARK_GRAY + " - " + ChatColor.GRAY + ChatColor.ITALIC + "#{dogez.play}");
			sender.sendMessage(ChatColor.BLUE + "/dz " + ChatColor.DARK_AQUA + "stats" + ChatColor.DARK_GRAY + " - " + ChatColor.GRAY + ChatColor.ITALIC
					+ "#{dogez.stats}");
			sender.sendMessage(ChatColor.BLUE + "/dz " + ChatColor.DARK_AQUA + "suicide" + ChatColor.DARK_GRAY + " - " + ChatColor.GRAY + ChatColor.ITALIC
					+ "#{dogez.kys}");

			if (sender.hasPermission("dogez.admin")) {
				sender.sendMessage(ChatColor.RED + "#{dogez.operatoronly}");
				sender.sendMessage(ChatColor.BLUE + "/dz " + ChatColor.DARK_AQUA + "loot" + ChatColor.DARK_GRAY + " - " + ChatColor.GRAY + ChatColor.ITALIC
						+ "#{dogez.loot}");
				sender.sendMessage(ChatColor.BLUE + "/dz " + ChatColor.DARK_AQUA + "reload" + ChatColor.DARK_GRAY + " - " + ChatColor.GRAY + ChatColor.ITALIC
						+ "#{dogez.reload}");
				sender.sendMessage(ChatColor.BLUE + "/dz " + ChatColor.DARK_AQUA + "togglesynch" + ChatColor.DARK_GRAY + " - " + ChatColor.GRAY
						+ ChatColor.ITALIC + "#{dogez.togglesynch}");
				sender.sendMessage(ChatColor.BLUE + "/dz " + ChatColor.DARK_AQUA + "togglelocalizedchat" + ChatColor.DARK_GRAY + " - " + ChatColor.GRAY
						+ ChatColor.ITALIC + "#{dogez.togglelocalizedchat}");
			}
			return true;
		}
		if (!(sender instanceof Player)) {
			sender.sendMessage("#{dogez.mustbeplayer}");
			return false;
		}
		if (args.length > 0) {
			Player player = (Player) sender;
			Entity playerEntity = player.getControlledEntity();
			PlayerProfile profile = plugin.getPlayerProfiles().getPlayerProfile(player.getUUID());

			if (args[0].equals("spawn") || args[0].equals("play")) {

				if (args.length > 1 && sender.hasPermission("dogez.spawn.as")) {
					player = plugin.getServer().getPlayerByName(args[1]);
					playerEntity = player.getControlledEntity();
					profile = plugin.getPlayerProfiles().getPlayerProfile(player.getUUID());
				}

				if (player == null)
					return false;

				if (profile.inGame) {
					player.sendMessage(ChatColor.RED + "#{dogez.alreadyig}");
				} else {
					int[] spawnPoint = SpawnPoints.getRandomSpawn();

					playerEntity.traits.with(TraitCreativeMode.class, ecm -> ecm.set(false));
					
					LootCategory spawnGear = plugin.getLootTypes().getCategory("spawn");
					playerEntity.traits.with(TraitInventory.class, ei -> {
						ei.clear();
						for(ItemPile i : spawnGear.getAllItems()) {
							ei.addItemPile(i);
						}
					});

					profile.inGame = true;
					playerEntity.traits.with(TraitHealth.class, eh -> eh.setHealth(eh.getMaxHealth()));
					
					player.sendMessage(ChatColor.DARK_AQUA + "#{dogez.goodluck}");
					player.setLocation(new Location(plugin.getGameWorld(), spawnPoint[0], spawnPoint[1] + 2, spawnPoint[2]));
				}
				return true;
			}
			if (args[0].equals("suicide") && playerEntity != null) {
				playerEntity.traits.with(TraitHealth.class, eh -> eh.setHealth(-10));
				return true;
			}
			if (args[0].equals("stats")) {
				if (args.length == 2 && sender.hasPermission("dogez.viewotherstats")) {
					String requestedPlayerName = args[1];
					profile = plugin.getPlayerProfiles().getPlayerProfile(requestedPlayerName.hashCode());
				}
				if (profile == null) {
					sender.sendMessage(ChatColor.RED + "#{dogez.errorloadingprofile}");
					return true;
				}

				profile.timeCalc();
				sender.sendMessage(ChatColor.DARK_GRAY + "=========[" + ChatColor.BLUE + "#{dogez.accountstats}" + ChatColor.RED + profile.name + " "
						+ ChatColor.DARK_GRAY + "]=========");
				// en jeu ?
				sender.sendMessage(ChatColor.AQUA + (profile.inGame ? "#{dogez.ig}" : "#{dogez.og}"));
				// Temps passé
				sender.sendMessage(ChatColor.DARK_AQUA + "#{dogez.spenttime1} " + TimeFormatter.formatTimelapse(profile.timeConnected) + "#{dogez.spenttime2}");
				sender.sendMessage(ChatColor.DARK_AQUA + "#{dogez.alivesince}" + TimeFormatter.formatTimelapse(profile.timeSurvivedLife) + " !");
				// stats kill
				sender.sendMessage(ChatColor.DARK_AQUA + "#{dogez.killed}" + profile.zombiesKilled + "#{dogez.kzombies}" + profile.zombiesKilled_thisLife
						+ "#{dogez.thisgame}");
				sender.sendMessage(ChatColor.DARK_AQUA + "#{dogez.killed}" + profile.playersKilled + " #{dogez.kplayers}" + profile.playersKilled_thisLife
						+ "#{dogez.thisgame}");
				sender.sendMessage(ChatColor.DARK_AQUA + "#{dogez.deaths1}" + profile.deaths + "#{dogez.deaths2}");

				return true;
			}

			// ADMINISTRATIVE COMMANDS
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
					player.sendMessage(ChatColor.RED + "You don't have permission.");
				return true;
			}
			if (args[0].equals("loot")) {
				if (player.hasPermission("dogez.admin")) {
					if (args.length == 1) {
						sender.sendMessage(ChatColor.DARK_GRAY + "======[" + ChatColor.BLUE + "Loot configuration commands " + ChatColor.DARK_GRAY + "]======");
						sender.sendMessage(ChatColor.BLUE + "/dz " + ChatColor.DARK_AQUA + "loot" + ChatColor.DARK_GRAY + " - " + ChatColor.GRAY
								+ ChatColor.ITALIC + "See this help message.");
						sender.sendMessage(ChatColor.BLUE + "/dz " + ChatColor.DARK_AQUA + "loot stats" + ChatColor.DARK_GRAY + " - " + ChatColor.GRAY
								+ ChatColor.ITALIC + "See stats on the loot points currently on the map");
						sender.sendMessage(ChatColor.BLUE + "/dz " + ChatColor.DARK_AQUA + "loot add <type> [min] [max]" + ChatColor.DARK_GRAY + " - "
								+ ChatColor.GRAY + ChatColor.ITALIC + "Add loot points");
						sender.sendMessage(ChatColor.BLUE + "/dz " + ChatColor.DARK_AQUA + "loot remove" + ChatColor.DARK_GRAY + " - " + ChatColor.GRAY
								+ ChatColor.ITALIC + "Remove loot points");
						sender.sendMessage(ChatColor.BLUE + "/dz " + ChatColor.DARK_AQUA + "loot reload" + ChatColor.DARK_GRAY + " - " + ChatColor.GRAY
								+ ChatColor.ITALIC + "Reload the loot points file");
						sender.sendMessage(ChatColor.BLUE + "/dz " + ChatColor.DARK_AQUA + "loot save" + ChatColor.DARK_GRAY + " - " + ChatColor.GRAY
								+ ChatColor.ITALIC + "Save the loot points file");
						sender.sendMessage(ChatColor.BLUE + "/dz " + ChatColor.DARK_AQUA + "loot reloot" + ChatColor.DARK_GRAY + " - " + ChatColor.GRAY
								+ ChatColor.ITALIC + "Refreshes the loot points");
						sender.sendMessage(ChatColor.BLUE + "/dz " + ChatColor.DARK_AQUA + "loot list" + ChatColor.DARK_GRAY + " - " + ChatColor.GRAY
								+ ChatColor.ITALIC + "List available loot types");
						sender.sendMessage(ChatColor.BLUE + "/dz " + ChatColor.DARK_AQUA + "loot arround <rayon> [force]" + ChatColor.DARK_GRAY + " - "
								+ ChatColor.GRAY + ChatColor.ITALIC + "Broadly assign loot points with a radius");
						return true;
					} else {
						// Add loot spawn points mode
						if (args[1].equals("add") && args.length >= 3) {

							// Parse the category name supplied
							String categoryName = args[2];
							LootCategory category = plugin.getLootTypes().getCategory(categoryName);

							// Check the supplied one exist
							if (category != null) {
								
								profile.activeCategory = category;
								profile.adding = true;

								if (args.length >= 4)
									profile.currentMin = Integer.parseInt(args[3]);
								if (args.length >= 5)
									profile.currentMax = Integer.parseInt(args[4]);

								player.sendMessage(ChatColor.AQUA + "You are now placed loot points of parameters [" + categoryName + ":" + profile.currentMin + "-"
										+ profile.currentMax + "]");
							} else {
								player.sendMessage(ChatColor.RED + "The loot type \"" + categoryName + "\" doesn't exist.");
							}
							return true;
						} else if (args[1].equals("remove")) {
							PlayerProfile pp = plugin.getPlayerProfiles().getPlayerProfile(player.getUUID());
							pp.adding = false;
							player.sendMessage(ChatColor.AQUA + "You are now removing loot points.");
							return true;
						} else if (args[1].equals("stats")) {
							player.sendMessage(ChatColor.AQUA + "There are " + plugin.getLootPlaces().count(player.getControlledEntity().getWorld())
									+ " in the current loot file.");
							return true;
						} else if (args[1].equals("list")) {
							String list = "";
							for (String c : plugin.getLootTypes().categories.keySet()) {
								list += c + ",";
							}
							list = list.substring(0, list.length() - 1);
							player.sendMessage(ChatColor.AQUA + "Listing loot types :" + list);
							return true;
						} else if (args[1].equals("reload")) {
							plugin.getLootPlaces().loadLootFile(player.getControlledEntity().getWorld());
							player.sendMessage(ChatColor.AQUA + "Loot points file (re)loaded.");
							return true;
						} else if (args[1].equals("save")) {
							plugin.getLootPlaces().saveLootFile(player.getControlledEntity().getWorld());
							player.sendMessage(ChatColor.AQUA + "Loot points file saved.");
							return true;
						} else if (args[1].equals("reloot")) {
							player.sendMessage(ChatColor.AQUA + "" + plugin.getLootPlaces().respawnLoot(player.getControlledEntity().getWorld())
									+ " loot points have been refreshed.");
							return true;
						} else if (args[1].equals("arround")) {
							if (args.length >= 3) {
								boolean force = (args.length == 4 && args[3].endsWith("force"));
								int radius = Integer.parseInt(args[2]);
								if (radius > 50) {
									player.sendMessage(ChatColor.AQUA + "Radius too wide, capping to 50");
									radius = 50;
								}
								int amount = plugin.getLootPlaces().generateLootPointsArroundPlayer(player, radius, force);
								player.sendMessage(ChatColor.AQUA + "" + amount + " new loot points added ( f=" + force + " )");
							} else
								player.sendMessage(ChatColor.AQUA + "Syntax : /dz loot arround <rayon> [force]");
							return true;
						}
					}
				} else {
					player.sendMessage(ChatColor.RED + "You don't have permission.");
					return true;
				}
			}
			// debug
			if (args[0].equals("cc") && player.hasPermission("dogez.admin.cc")) {
				sender.sendMessage(ChatColor.AQUA + "All chunks have been cleaned");
				return true;
			}
			if (args[0].equals("snd") && player.hasPermission("dogez.admin.snd")) {
				if (args.length == 2)
					player.getControlledEntity().getWorld().getSoundManager().playSoundEffect(args[1], Mode.NORMAL, player.getLocation(), 1f, 1f, 5f, 15f);
				// PlayersPackets.playSound(player.getLocation(),args[1],1f,1f);
				else if (args.length == 3)
					player.getControlledEntity().getWorld().getSoundManager().playSoundEffect(args[1], Mode.NORMAL, player.getLocation(),
							Float.parseFloat(args[2]), 1f, 5f, 15f);
				// PlayersPackets.playSound(player.getLocation(),args[1],1f,Float.parseFloat(args[2]));
				else if (args.length == 4)
					player.getControlledEntity().getWorld().getSoundManager().playSoundEffect(args[1], Mode.NORMAL, player.getLocation(),
							Float.parseFloat(args[3]), Float.parseFloat(args[2]), 5f, 15f);
				// PlayersPackets.playSound(player.getLocation(),args[1],Float.parseFloat(args[3]),Float.parseFloat(args[2]));
				else
					sender.sendMessage(ChatColor.RED + "Syntax : /dz snd [sound path] [pitch] [volume]");
				return true;
			}
			
			// DEBUG FEATURES
			if (args[0].equals("spawnZombie") && player.hasPermission("dogez.admin.debug")) {
				plugin.spawner.spawnZombie(player.getLocation());
				return true;
			}

			if (args[0].equals("getPlaceName") && player.hasPermission("dogez.admin.debug")) {
				sender.sendMessage("PlaceName=" + PlacesNames.getPlayerPlaceName(player));
				return true;
			}
			if (args[0].equals("testloot") && args.length == 2 && player.hasPermission("dogez.admin.testloot")) {
				String category = args[1];
				if (plugin.getLootTypes().categories.keySet().contains(category)) {
					LootType lt = plugin.getLootTypes().getCategory(category).getRandomSpawn();
					sender.sendMessage("Giving random loot in category " + category + " : " + lt.toString());
				} else {
					player.sendMessage(ChatColor.RED + "Wrong category.");
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

				plugin.config.setProperty("irlTimeCycleSync", "" + (!plugin.config.getBoolean("irlTimeCycleSync", true)));
				sender.sendMessage("Toggling IRL time synchronisation :" + plugin.config.getBoolean("irlTimeCycleSync"));
				return true;
			}
		}
		sender.sendMessage(ChatColor.RED + "Unknown command.");
		return true;
	}

}
