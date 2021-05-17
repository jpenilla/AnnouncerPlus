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

import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.spongepowered.configurate.ConfigurationOptions
import org.spongepowered.configurate.hocon.HoconConfigurationLoader
import org.spongepowered.configurate.objectmapping.ObjectMapper
import org.spongepowered.configurate.objectmapping.meta.NodeResolver
import org.spongepowered.configurate.serialize.TypeSerializerCollection
import xyz.jpenilla.announcerplus.AnnouncerPlus
import xyz.jpenilla.announcerplus.compatibility.PlaceholderAPIMiniMessagePreprocessor
import xyz.jpenilla.announcerplus.config.message.MessageConfig
import xyz.jpenilla.announcerplus.util.LegacyFormatting
import xyz.jpenilla.announcerplus.util.miniMessage
import xyz.jpenilla.jmplib.ChatCentering
import java.nio.file.Files
import kotlin.collections.set
import kotlin.streams.toList

class ConfigManager(private val announcerPlus: AnnouncerPlus) {
  private val serializers: TypeSerializerCollection
  private val configOptions: ConfigurationOptions

  private val mainConfigPath = announcerPlus.dataFolder.toPath().resolve("main.conf")
  private val mainConfigLoader: HoconConfigurationLoader
  lateinit var mainConfig: MainConfig

  private val firstJoinConfigPath = announcerPlus.dataFolder.toPath().resolve("first-join.conf")
  private val firstJoinConfigLoader: HoconConfigurationLoader
  lateinit var firstJoinConfig: JoinQuitConfig

  private val mapperFactory = ObjectMapper.factoryBuilder()
    .addNodeResolver { name, _ ->
      /* We don't want to attempt serializing delegated properties, and they can't be @Transient */
      if (name.endsWith("delegate")) {
        NodeResolver.SKIP_FIELD
      } else {
        null
      }
    }.build()

  val messageConfigs = hashMapOf<String, MessageConfig>()
  val joinQuitConfigs = hashMapOf<String, JoinQuitConfig>()

  init {
    serializers = TypeSerializerCollection.defaults().childBuilder()
      .registerAnnotatedObjects(mapperFactory)
      .build()

    configOptions = ConfigurationOptions.defaults().serializers(serializers)

    mainConfigLoader = HoconConfigurationLoader.builder().path(mainConfigPath).build()
    firstJoinConfigLoader = HoconConfigurationLoader.builder().path(firstJoinConfigPath).build()

    reload()
  }

  fun reload() {
    load()
    save()
  }

  private fun load() {
    Files.createDirectories(announcerPlus.dataFolder.toPath())

    val mainConfigRoot = mainConfigLoader.load(configOptions)
    try {
      mainConfig = MainConfig.loadFrom(mainConfigRoot)
    } catch (e: Exception) {
      throw IllegalArgumentException("Failed to load the main.conf config file. This is due to misconfiguration", e)
    }

    val firstJoinConfigRoot = firstJoinConfigLoader.load(configOptions)
    try {
      firstJoinConfig = JoinQuitConfig.loadFrom(firstJoinConfigRoot, null)
    } catch (e: Exception) {
      throw IllegalArgumentException("Failed to load the main.conf config file. This is due to misconfiguration", e)
    }

    loadMessageConfigs()
    loadJoinQuitConfigs()
  }

  private fun save() {
    val mainConfigRoot = mainConfigLoader.createNode(
      configOptions.header(
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
    )
    mainConfig.saveTo(mainConfigRoot)
    mainConfigLoader.save(mainConfigRoot)

    val firstJoinConfigRoot = firstJoinConfigLoader.createNode(
      configOptions.header(
        "If enabled in main.conf, this join config will be used when players join the server for the first time.\n" +
          "All other join configs will be skipped for first-join if this is enabled."
      )
    )
    firstJoinConfig.saveTo(firstJoinConfigRoot)
    firstJoinConfigRoot.removeChild("quit-section")
    firstJoinConfigLoader.save(firstJoinConfigRoot)
  }

  private fun loadJoinQuitConfigs() {
    joinQuitConfigs.clear()
    val path = announcerPlus.dataFolder.toPath().resolve("join-quit-configs")
    if (!Files.exists(path)) {
      announcerPlus.logger.info("Creating join quit config folder")
      Files.createDirectories(path)
    }
    if (Files.list(path).toList().isEmpty()) {
      announcerPlus.logger.info("No join/quit configs found, creating default.conf")

      val defaultConfig = path.resolve("default.conf")
      val defaultConfigLoader = HoconConfigurationLoader.builder().path(defaultConfig).build()
      val defaultConfigRoot = defaultConfigLoader.createNode(
        configOptions.header(
          "To give a player these join/quit messages give them the announcerplus.join.default\n" +
            "  and announcerplus.quit.default permissions"
        )
      )
      JoinQuitConfig().saveTo(defaultConfigRoot)
      defaultConfigLoader.save(defaultConfigRoot)
    }
    Files.list(path).forEach {
      val configLoader = HoconConfigurationLoader.builder().path(it).build()
      val name = it.toFile().nameWithoutExtension
      try {
        val root = configLoader.load(configOptions)
        joinQuitConfigs[name] = JoinQuitConfig.loadFrom(root, name)

        joinQuitConfigs[name]?.saveTo(root)
        configLoader.save(root)
      } catch (e: Exception) {
        throw IllegalArgumentException(
          "Failed to load message config: ${it.fileName}. This is due to an invalid config file.",
          e
        )
      }
    }
  }

  private fun loadMessageConfigs() {
    messageConfigs.values.forEach(MessageConfig::stop)
    messageConfigs.clear()
    val path = announcerPlus.dataFolder.toPath().resolve("message-configs")
    if (!Files.exists(path)) {
      announcerPlus.logger.info("Creating message config folder")
      Files.createDirectories(path)
    }
    if (Files.list(path).toList().isEmpty()) {
      announcerPlus.logger.info("No message configs found, creating demo.conf")

      val defaultConfig = path.resolve("demo.conf")
      val defaultConfigLoader = HoconConfigurationLoader.builder().path(defaultConfig).build()
      val defaultConfigRoot = defaultConfigLoader.createNode(
        configOptions.header(
          """For a player to get these messages give them the announcerplus.messages.demo permission
                      |  If EssentialsX is installed, then giving a player the announcerplus.messages.demo.afk permission
                      |  will stop them from receiving these messages while afk""".trimMargin()
        )
      )
      MessageConfig().saveTo(defaultConfigRoot)
      defaultConfigLoader.save(defaultConfigRoot)
    }
    Files.list(path).forEach {
      val configLoader = HoconConfigurationLoader.builder().path(it).build()
      val name = it.toFile().nameWithoutExtension
      try {
        val root = configLoader.load(configOptions)
        messageConfigs[name] = MessageConfig.loadFrom(root, name)

        messageConfigs[name]?.saveTo(root)
        configLoader.save(root)
      } catch (e: Exception) {
        throw IllegalArgumentException(
          "Failed to load join/quit config: ${it.fileName}. This is due to an invalid config file.",
          e
        )
      }
    }
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
