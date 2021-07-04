/*
 * This file is part of AnnouncerPlus, licensed under the MIT License.
 *
 * Copyright (c) 2020-2021 Jason Penilla
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package xyz.jpenilla.announcerplus.config

import net.kyori.adventure.serializer.configurate4.ConfigurateComponentSerializer
import net.kyori.adventure.sound.Sound
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.ConfigurationOptions
import org.spongepowered.configurate.hocon.HoconConfigurationLoader
import org.spongepowered.configurate.objectmapping.ObjectMapper
import org.spongepowered.configurate.objectmapping.meta.NodeResolver
import xyz.jpenilla.announcerplus.AnnouncerPlus
import xyz.jpenilla.announcerplus.compatibility.PlaceholderAPIMiniMessagePreprocessor
import xyz.jpenilla.announcerplus.config.message.MessageConfig
import xyz.jpenilla.announcerplus.config.serializer.SoundSerializer
import xyz.jpenilla.announcerplus.util.LegacyFormatting
import xyz.jpenilla.announcerplus.util.dataPath
import xyz.jpenilla.announcerplus.util.miniMessage
import xyz.jpenilla.jmplib.ChatCentering
import java.nio.file.Path
import kotlin.collections.set
import kotlin.io.path.createDirectories
import kotlin.io.path.isRegularFile
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.name
import kotlin.io.path.notExists

class ConfigManager(private val announcerPlus: AnnouncerPlus) {
  private val mapperFactory = ObjectMapper.factoryBuilder()
    .addNodeResolver { name, _ ->
      /* We don't want to attempt serializing delegated properties, and they can't be @Transient */
      if (name.endsWith("\$delegate")) {
        NodeResolver.SKIP_FIELD
      } else {
        null
      }
    }
    .build()

  private val mainConfigPath = announcerPlus.dataPath.resolve("main.conf")
  private val mainConfigLoader: HoconConfigurationLoader = createLoader(mainConfigPath) {
    it.header(
      """ 
          |     ___                                                 ____  __               __    
          |    /   |  ____  ____  ____  __  ______  ________  _____/ __ \/ /_  _______  __/ /_
          |   / /| | / __ \/ __ \/ __ \/ / / / __ \/ ___/ _ \/ ___/ /_/ / / / / / ___/ /_  __/
          |  / ___ |/ / / / / / / /_/ / /_/ / / / / /__/  __/ /  / ____/ / /_/ (__  )   /_/   
          | /_/  |_/_/ /_/_/ /_/\____/\__,_/_/ /_/\___/\___/_/  /_/   /_/\__,_/____/  
          | 
          |     v${announcerPlus.description.version}
          """.trimMargin()
    )
  }
  lateinit var mainConfig: MainConfig

  private val firstJoinConfigPath = announcerPlus.dataPath.resolve("first-join.conf")
  private val firstJoinConfigLoader: HoconConfigurationLoader = createLoader(firstJoinConfigPath) {
    it.header(
      "If enabled in main.conf, this join config will be used when players join the server for the first time.\n" +
        "All other join configs will be skipped for first-join if this is enabled."
    )
  }
  lateinit var firstJoinConfig: JoinQuitConfig

  val messageConfigs = hashMapOf<String, MessageConfig>()
  val joinQuitConfigs = hashMapOf<String, JoinQuitConfig>()

  private fun createLoader(
    path: Path,
    modifier: (ConfigurationOptions) -> ConfigurationOptions = { it }
  ): HoconConfigurationLoader =
    HoconConfigurationLoader.builder()
      .path(path)
      .defaultOptions { options ->
        options.serializers {
          it.register(Sound::class.java, SoundSerializer)
            .registerAll(ConfigurateComponentSerializer.configurate().serializers())
            .registerAnnotatedObjects(mapperFactory)
        }.run(modifier)
      }
      .build()

  init {
    reload()
  }

  fun reload() {
    load()
    save()
  }

  private fun load() {
    announcerPlus.dataPath.createDirectories()

    try {
      val mainConfigRoot = mainConfigLoader.load()
      mainConfig = MainConfig.loadFrom(mainConfigRoot)
    } catch (e: Exception) {
      throw IllegalArgumentException("Failed to load the 'main.conf' config file. This is likely due to misconfiguration.", e)
    }

    try {
      val node = firstJoinConfigLoader.load()
      if (firstJoinConfigPath.isRegularFile()) {
        upgradeNode(Transformations.JoinQuitConfig, node, "first join", firstJoinConfigPath.name)
      }
      firstJoinConfig = JoinQuitConfig.loadFrom(node, null)
    } catch (e: Exception) {
      throw IllegalArgumentException("Failed to load the 'first-join.conf' config file. This is likely due to misconfiguration.", e)
    }

    loadMessageConfigs()
    loadJoinQuitConfigs()
  }

  private fun save() {
    val mainConfigRoot = mainConfigLoader.createNode()
    mainConfig.saveTo(mainConfigRoot)
    mainConfigLoader.save(mainConfigRoot)

    val firstJoinConfigRoot = firstJoinConfigLoader.createNode()
    firstJoinConfig.saveTo(firstJoinConfigRoot)
    firstJoinConfigRoot.removeChild("quit-section")
    firstJoinConfigLoader.save(firstJoinConfigRoot)
  }

  private fun loadJoinQuitConfigs() {
    joinQuitConfigs.clear()
    val path = announcerPlus.dataPath.resolve("join-quit-configs")
    if (path.notExists()) {
      announcerPlus.logger.info("Creating join quit config folder")
      path.createDirectories()
    }
    if (path.listDirectoryEntries().isEmpty()) {
      announcerPlus.logger.info("No join/quit configs found, creating default.conf")

      val defaultConfig = path.resolve("default.conf")
      val defaultConfigLoader = createLoader(defaultConfig) {
        it.header(
          "To give a player these join/quit messages give them the announcerplus.join.default\n" +
            "  and announcerplus.quit.default permissions"
        )
      }
      val defaultConfigRoot = defaultConfigLoader.createNode()
      JoinQuitConfig().saveTo(defaultConfigRoot)
      defaultConfigLoader.save(defaultConfigRoot)
    }
    path.listDirectoryEntries("*.conf").forEach {
      val configLoader = createLoader(it)
      val name = it.toFile().nameWithoutExtension
      try {
        val node = configLoader.load()
        upgradeNode(Transformations.JoinQuitConfig, node, "join quit", it.name).node
        joinQuitConfigs[name] = JoinQuitConfig.loadFrom(node, name)

        joinQuitConfigs[name]?.saveTo(node)
        configLoader.save(node)
      } catch (e: Exception) {
        throw IllegalArgumentException("Failed to load message config: ${it.name}. This is likely due to an invalid config file.", e)
      }
    }
  }

  private fun loadMessageConfigs() {
    messageConfigs.values.forEach(MessageConfig::stop)
    messageConfigs.clear()
    val path = announcerPlus.dataPath.resolve("message-configs")
    if (path.notExists()) {
      announcerPlus.logger.info("Creating message config folder")
      path.createDirectories()
    }
    if (path.listDirectoryEntries().isEmpty()) {
      announcerPlus.logger.info("No message configs found, creating demo.conf")

      val defaultConfig = path.resolve("demo.conf")
      val defaultConfigLoader = createLoader(defaultConfig) {
        it.header(
          """For a player to get these messages give them the announcerplus.messages.demo permission
                      |  If EssentialsX is installed, then giving a player the announcerplus.messages.demo.afk permission
                      |  will stop them from receiving these messages while afk""".trimMargin()
        )
      }
      val defaultConfigRoot = defaultConfigLoader.createNode()
      MessageConfig().saveTo(defaultConfigRoot)
      defaultConfigLoader.save(defaultConfigRoot)
    }
    path.listDirectoryEntries("*.conf").forEach {
      val configLoader = createLoader(it)
      val name = it.toFile().nameWithoutExtension
      try {
        val node = configLoader.load()
        upgradeNode(Transformations.MessageConfig, node, "message", it.name)
        messageConfigs[name] = MessageConfig.loadFrom(node, name)

        messageConfigs[name]?.saveTo(node)
        configLoader.save(node)
      } catch (e: Exception) {
        throw IllegalArgumentException("Failed to load join/quit config: ${it.name}. This is likely due to an invalid config file.", e)
      }
    }
  }

  private fun <N : ConfigurationNode> upgradeNode(upgrader: Transformations.ConfigurationUpgrader, node: N, configType: String, fileName: String): UpgradeResult<N> {
    val result = upgrader.upgrade(node)
    val (old, new, _, didUpgrade) = result
    if (didUpgrade) {
      announcerPlus.logger.info("Upgraded $configType config '$fileName' from ${if (old == -1) "un-versioned" else "v$old"} to v$new")
    }
    return result
  }

  private val preprocessor: PlaceholderAPIMiniMessagePreprocessor? by lazy {
    if (Bukkit.getServer().pluginManager.isPluginEnabled("PlaceholderAPI")) {
      PlaceholderAPIMiniMessagePreprocessor(miniMessage())
    } else {
      null
    }
  }

  private val legacyChecker: LegacyFormatting = LegacyFormatting(announcerPlus)

  fun parse(commandSender: CommandSender?, message: String): String {
    var msg = message
    mainConfig.customPlaceholders.forEach { (token, replacement) ->
      msg = msg.replace("{$token}", replacement)
    }
    msg = legacyChecker.check(msg)
    if (commandSender is Player) {
      preprocessor?.apply {
        msg = process(commandSender, msg)
      }
    }

    if (msg.startsWith("<center>")) {
      msg = msg.replaceFirst("<center>", "")
      val spaces = ChatCentering.spacePrefix(miniMessage(msg))
      return spaces + msg
    }
    return msg
  }

  fun parse(player: CommandSender?, messages: List<String>): List<String> =
    messages.map { parse(player, it) }
}
