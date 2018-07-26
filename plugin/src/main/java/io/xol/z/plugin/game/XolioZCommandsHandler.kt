//
// This file is a part of the XolioZ Mod for Chunk Stories
// Check out README.md for more information
// Website: https://chunkstories.xyz
// Github: https://github.com/Hugobros3/xolioz
//

package io.xol.z.plugin.game

import io.xol.chunkstories.api.Location
import io.xol.chunkstories.api.entity.traits.serializable.TraitCreativeMode
import io.xol.chunkstories.api.entity.traits.serializable.TraitHealth
import io.xol.chunkstories.api.entity.traits.serializable.TraitInventory
import io.xol.chunkstories.api.player.Player
import io.xol.chunkstories.api.plugin.commands.Command
import io.xol.chunkstories.api.plugin.commands.CommandEmitter
import io.xol.chunkstories.api.plugin.commands.CommandHandler
import io.xol.chunkstories.api.util.compatibility.ChatColor
import io.xol.chunkstories.core.entity.EntityZombie
import io.xol.z.plugin.XolioZPlugin
import io.xol.z.plugin.misc.TimeFormatter
import io.xol.z.plugin.player.PlayerProfile

/**
* Monolithic class that handles all the command-line stuff.
* Written in 2014, wouldn't hurt to see a bigger cleanup
*/
class XolioZCommandsHandler(private val plugin: XolioZPlugin) : CommandHandler {

	override fun handleCommand(sender: CommandEmitter, command: Command, args: Array<String>): Boolean {
		try {
			when (command.name) {
				"m" -> privateMessageCommand(sender, args)

				"r" -> privateMessageReplyCommand(sender, args)

				"dz" -> {
					val subcommand = args.getOrNull(0)
					val args = args.sliceArray(1 until args.size)

					handleGeneralCommand(sender, subcommand, args)
				}

				else -> return false
			}


		} catch (e: Exception) {
			sender.sendMessage(ChatColor.RED.toString() + e.message)
		}

		return true
	}

	private fun privateMessageReplyCommand(sender: CommandEmitter, args: Array<String>) {
		val sender = sender.asPlayerSafe()
		if (args.isEmpty()) throw Exception("#{dogez.goodsyntaxr}")
		val message = args.joinToString(separator = " ")

		val recipient = sender.profile.talkingTo ?: throw Exception("#{dogez.pmnotinaconversation}")
		if (!recipient.isConnected) throw Exception("{dogez.pmnotavailable}")

		if (!sender.canUseTalkieWalkie() && !recipient.hasPermission("dogez.talkie.receiveAnyway")) throw Exception("#{dogez.pmrequiretw}")
		if (!recipient.canUseTalkieWalkie()) sender.sendMessage(ChatColor.RED.toString() + "#{dogez.pmnoreplywarn}")

		recipient.sendMessage(ChatColor.GRAY.toString() + "[#{dogez.pmfrom}" + ChatColor.AQUA + sender.name + ChatColor.GRAY + "]:" + message)
		sender.sendMessage(ChatColor.GRAY.toString() + "[#{dogez.pmto}" + ChatColor.AQUA + recipient + ChatColor.GRAY + "]:" + message)
	}

	private fun privateMessageCommand(sender: CommandEmitter, args: Array<String>) {
		val sender = sender.asPlayerSafe()
		if (args.size < 2) throw Exception("#{dogez.goodsyntaxm}")
		val message = args.slice(1..args.size).joinToString(separator = " ")

		val recipient = plugin.server.getPlayerByName(args[0]) ?: throw Exception("{dogez.pmnotavailable}")

		if (!sender.canUseTalkieWalkie() && !recipient.hasPermission("dogez.talkie.receiveAnyway")) throw Exception("#{dogez.pmrequiretw}")
		if (!recipient.canUseTalkieWalkie()) sender.sendMessage(ChatColor.RED.toString() + "#{dogez.pmnoreplywarn}")

		recipient.sendMessage(ChatColor.GRAY.toString() + "[#{dogez.pmfrom}" + ChatColor.AQUA + sender.name + ChatColor.GRAY + "]:" + message)
		sender.sendMessage(ChatColor.GRAY.toString() + "[#{dogez.pmto}" + ChatColor.AQUA + recipient + ChatColor.GRAY + "]:" + message)

		//Set the talkingTo variable so we remember who they were talking with
		sender.profile.talkingTo = recipient
		recipient.profile.talkingTo = sender
	}

	private fun handleGeneralCommand(sender: CommandEmitter, subcommand: String?, args: Array<String>) {
		when (subcommand) {
			null, "help" -> sender.sendHelpMessage()

			"spawn", "play" -> {
				val player =
						if (args.isEmpty())
							sender.asPlayerSafe()
						else {
							sender.assertHasPermission("dogez.spawn.as")
							plugin.server.getPlayerByName(args[0])
									?: throw Exception("This player isn't logged in")
						}

				if (player.profile.inGame) throw Exception("#{dogez.alreadyig}")

				val entity = player.controlledEntity ?: throw Exception("This player has no entity")
				val spawnPoint = SpawnPoints.getRandomSpawn()

				entity.traits.with<TraitCreativeMode>(TraitCreativeMode::class.java) { ecm -> ecm.set(false) }

				val spawnGear = plugin.lootTypes.getCategory("spawn")
				entity.traits.with<TraitInventory>(TraitInventory::class.java) { ei ->
					ei.clear()
					for (i in spawnGear.allItems) {
						ei.addItemPile(i)
					}
				}

				entity.traits.with<TraitHealth>(TraitHealth::class.java) { eh -> eh.health = eh.maxHealth }

				player.location = Location(plugin.gameWorld, spawnPoint[0].toDouble(), (spawnPoint[1] + 2).toDouble(), spawnPoint[2].toDouble())

				player.sendMessage(ChatColor.DARK_AQUA.toString() + "#{dogez.goodluck}")
				player.profile.inGame = true
			}

			"suicide", "kill" -> {
				val player = sender.asPlayerSafe()
				player.controlledEntity?.let {
					it.traits.get(TraitHealth::class.java)?.health = -1f
				}
			}

			"stats" -> {
				val player =
						if (args.isEmpty())
							sender.asPlayerSafe()
						else {
							sender.assertHasPermission("dogez.viewotherstats")
							plugin.server.getPlayerByName(args[0])
									?: throw Exception("This player isn't logged in")
						}
				sender.sendStats(player.profile)
			}

			"reload" -> {
				sender.assertHasPermission("dogez.admin.reload")
				plugin.loadConfigs()
				sender.sendMessage(ChatColor.AQUA.toString() + "Configuration reloaded")
			}

			"butcher" -> {
				sender.assertHasPermission("dogez.admin.butcher")
				var count = 0
				plugin.gameWorld.allLoadedEntities.forEach {
					if (it is EntityZombie) {
						plugin.gameWorld.removeEntity(it)
						count++
					}
				}

				sender.sendMessage("$count zombies removed from the map")
			}

			"spawnZombie" -> {
				sender.assertHasPermission("dogez.admin.debug")
				plugin.spawner.spawnZombie(sender.asPlayerSafe().location)
				sender.sendMessage("Zombie spawned at your position")
			}

			"togglesynch" -> {
				sender.assertHasPermission("dogez.admin")
				plugin.config.irlTimeSync = plugin.config.irlTimeSync
				sender.sendMessage("Toggling IRL time synchronisation :" + plugin.config.irlTimeSync)
			}

			"loot" -> {
				sender.assertHasPermission("dogez.admin.loot")

				val subcommand = args.getOrNull(0)
				val args = args.sliceArray(1 until args.size)

				handleLootCommand(sender, subcommand, args)
			}
		}
	}

	private fun handleLootCommand(sender: CommandEmitter, subcommand: String?, args: Array<String>) {
		when (subcommand) {
			null, "help" -> sender.sendLootHelp()

			"add" -> {
				if (args.isEmpty()) throw Exception("Missing category")

				val playerProfile = sender.asPlayerSafe().profile

				val categoryName = args[0]
				val category = plugin.lootTypes.getCategory(categoryName)

				// Check the supplied one exist
				if (category != null) {

					playerProfile.activeCategory = category
					playerProfile.adding = true

					if (args.size >= 2)
						playerProfile.currentMin = Integer.parseInt(args[1])
					if (args.size >= 3)
						playerProfile.currentMax = Integer.parseInt(args[2])

					sender.sendMessage(ChatColor.AQUA.toString() + "You are now placed loot points of parameters [" + categoryName + ":" + playerProfile.currentMin + "-"
							+ playerProfile.currentMax + "]")
				} else {
					sender.sendMessage(ChatColor.RED.toString() + "The loot type \"" + categoryName + "\" doesn't exist.")
				}
			}

			"remove" -> {
				val playerProfile = sender.asPlayerSafe().profile
				playerProfile.adding = false
				sender.sendMessage(ChatColor.AQUA.toString() + "You are now removing loot points.")
			}

			"stats" -> {
				sender.sendMessage(ChatColor.AQUA.toString() + "There are " + plugin.lootPlaces.count(plugin.gameWorld) + " in the current loot file.")
			}

			"list" -> {
				sender.sendMessage(ChatColor.AQUA.toString() + "Available loot categories :" +
						plugin.lootTypes.categories.keys.joinToString())
			}

			"reload" -> {
				plugin.lootPlaces.loadLootFile(plugin.gameWorld)
				sender.sendMessage(ChatColor.AQUA.toString() + "Loot points file (re)loaded.")
			}

			"save" -> {
				plugin.lootPlaces.saveLootFile(plugin.gameWorld)
				sender.sendMessage(ChatColor.AQUA.toString() + "Loot points file saved.")
			}

			"reloot", "respawnLoot" -> {
				sender.sendMessage(ChatColor.AQUA.toString() + "" + plugin.lootPlaces.respawnLoot(plugin.gameWorld) + " loot points have been refreshed.")
			}

			"arround" -> {
				var radius: Int = args.getOrNull(0)?.toInt() ?: throw Exception("Missing radius")
				val force = args.contains("force")

				if (radius > 50) {
					radius = 50
					sender.sendMessage(ChatColor.AQUA.toString() + "Radius too wide, capping to 50")
				}

				val addedCount = plugin.lootPlaces.generateLootPointsArroundPlayer(sender.asPlayerSafe(), radius, force)
				sender.sendMessage(ChatColor.AQUA.toString() + "$addedCount new loot points added")
			}
		}
	}

	public val Player.profile: PlayerProfile
		get() = plugin.playerProfiles.getPlayerProfile(this.uuid)

	fun CommandEmitter.assertHasPermission(permissionNode: String) {
		if (!this.hasPermission(permissionNode)) throw Exception("$this lacks permission $permissionNode")
	}

	fun CommandEmitter.asPlayerSafe() = this as? Player ?: throw Exception("#{dogez.mustbeplayer}")

	/** Sends a long-ass message showing the different top-level commands */
	private fun CommandEmitter.sendHelpMessage() {
		this.sendMessage(ChatColor.BLUE.toString() + "#{dogez.pluginname}" + ChatColor.DARK_GREEN + plugin.version)
		this.sendMessage(ChatColor.DARK_GRAY.toString() + "=========[" + ChatColor.BLUE + "#{dogez.avaiablecmds}" + ChatColor.DARK_GRAY + "]=========")
		this.sendMessage(ChatColor.BLUE.toString() + "/dz" + ChatColor.DARK_GRAY + " - " + ChatColor.GRAY + ChatColor.ITALIC + "#{dogez.seethishelp}")
		this.sendMessage(
				ChatColor.BLUE.toString() + "/dz " + ChatColor.DARK_AQUA + "play" + ChatColor.DARK_GRAY + " - " + ChatColor.GRAY + ChatColor.ITALIC + "#{dogez.play}")
		this.sendMessage(ChatColor.BLUE.toString() + "/dz " + ChatColor.DARK_AQUA + "stats" + ChatColor.DARK_GRAY + " - " + ChatColor.GRAY + ChatColor.ITALIC
				+ "#{dogez.stats}")
		this.sendMessage(ChatColor.BLUE.toString() + "/dz " + ChatColor.DARK_AQUA + "suicide" + ChatColor.DARK_GRAY + " - " + ChatColor.GRAY + ChatColor.ITALIC
				+ "#{dogez.kys}")

		if (this.hasPermission("dogez.admin")) {
			this.sendMessage(ChatColor.RED.toString() + "#{dogez.operatoronly}")
			this.sendMessage(ChatColor.BLUE.toString() + "/dz " + ChatColor.DARK_AQUA + "loot" + ChatColor.DARK_GRAY + " - " + ChatColor.GRAY + ChatColor.ITALIC
					+ "#{dogez.loot}")
			this.sendMessage(ChatColor.BLUE.toString() + "/dz " + ChatColor.DARK_AQUA + "reload" + ChatColor.DARK_GRAY + " - " + ChatColor.GRAY + ChatColor.ITALIC
					+ "#{dogez.reload}")
			this.sendMessage(ChatColor.BLUE.toString() + "/dz " + ChatColor.DARK_AQUA + "togglesynch" + ChatColor.DARK_GRAY + " - " + ChatColor.GRAY
					+ ChatColor.ITALIC + "#{dogez.togglesynch}")
			this.sendMessage(ChatColor.BLUE.toString() + "/dz " + ChatColor.DARK_AQUA + "togglelocalizedchat" + ChatColor.DARK_GRAY + " - " + ChatColor.GRAY
					+ ChatColor.ITALIC + "#{dogez.togglelocalizedchat}")
		}
	}

	private fun CommandEmitter.sendStats(playerProfile: PlayerProfile) {
		playerProfile.timeCalc()
		this.sendMessage(ChatColor.DARK_GRAY.toString() + "=========[" + ChatColor.BLUE + "#{dogez.accountstats}" + ChatColor.RED + playerProfile.name + " "
				+ ChatColor.DARK_GRAY + "]=========")
		this.sendMessage(ChatColor.AQUA.toString() + if (playerProfile.inGame) "#{dogez.ig}" else "#{dogez.og}")
		this.sendMessage(ChatColor.DARK_AQUA.toString() + "#{dogez.spenttime1} " + TimeFormatter.formatTimelapse(playerProfile.timeConnected) + "#{dogez.spenttime2}")
		this.sendMessage(ChatColor.DARK_AQUA.toString() + "#{dogez.alivesince}" + TimeFormatter.formatTimelapse(playerProfile.timeSurvivedLife) + " !")
		this.sendMessage(ChatColor.DARK_AQUA.toString() + "#{dogez.killed}" + playerProfile.zombiesKilled + "#{dogez.kzombies}" + playerProfile.zombiesKilled_thisLife
				+ "#{dogez.thisgame}")
		this.sendMessage(ChatColor.DARK_AQUA.toString() + "#{dogez.killed}" + playerProfile.playersKilled + " #{dogez.kplayers}" + playerProfile.playersKilled_thisLife
				+ "#{dogez.thisgame}")
		this.sendMessage(ChatColor.DARK_AQUA.toString() + "#{dogez.deaths1}" + playerProfile.deaths + "#{dogez.deaths2}")
	}

	/** Tells the user about all the loot configuration commands */
	private fun CommandEmitter.sendLootHelp() {
		this.sendMessage(ChatColor.DARK_GRAY.toString() + "======[" + ChatColor.BLUE + "Loot configuration commands " + ChatColor.DARK_GRAY + "]======")
		this.sendMessage(ChatColor.BLUE.toString() + "/dz " + ChatColor.DARK_AQUA + "loot" + ChatColor.DARK_GRAY + " - " + ChatColor.GRAY
				+ ChatColor.ITALIC + "See this help message.")
		this.sendMessage(ChatColor.BLUE.toString() + "/dz " + ChatColor.DARK_AQUA + "loot stats" + ChatColor.DARK_GRAY + " - " + ChatColor.GRAY
				+ ChatColor.ITALIC + "See stats on the loot points currently on the map")
		this.sendMessage(ChatColor.BLUE.toString() + "/dz " + ChatColor.DARK_AQUA + "loot add <type> [min] [max]" + ChatColor.DARK_GRAY + " - "
				+ ChatColor.GRAY + ChatColor.ITALIC + "Add loot points")
		this.sendMessage(ChatColor.BLUE.toString() + "/dz " + ChatColor.DARK_AQUA + "loot remove" + ChatColor.DARK_GRAY + " - " + ChatColor.GRAY
				+ ChatColor.ITALIC + "Remove loot points")
		this.sendMessage(ChatColor.BLUE.toString() + "/dz " + ChatColor.DARK_AQUA + "loot reload" + ChatColor.DARK_GRAY + " - " + ChatColor.GRAY
				+ ChatColor.ITALIC + "Reload the loot points file")
		this.sendMessage(ChatColor.BLUE.toString() + "/dz " + ChatColor.DARK_AQUA + "loot save" + ChatColor.DARK_GRAY + " - " + ChatColor.GRAY
				+ ChatColor.ITALIC + "Save the loot points file")
		this.sendMessage(ChatColor.BLUE.toString() + "/dz " + ChatColor.DARK_AQUA + "loot reloot" + ChatColor.DARK_GRAY + " - " + ChatColor.GRAY
				+ ChatColor.ITALIC + "Refreshes the loot points")
		this.sendMessage(ChatColor.BLUE.toString() + "/dz " + ChatColor.DARK_AQUA + "loot list" + ChatColor.DARK_GRAY + " - " + ChatColor.GRAY
				+ ChatColor.ITALIC + "List available loot types")
		this.sendMessage(ChatColor.BLUE.toString() + "/dz " + ChatColor.DARK_AQUA + "loot arround <radius> [force]" + ChatColor.DARK_GRAY + " - "
				+ ChatColor.GRAY + ChatColor.ITALIC + "Broadly assign loot points with a radius")
	}
}
