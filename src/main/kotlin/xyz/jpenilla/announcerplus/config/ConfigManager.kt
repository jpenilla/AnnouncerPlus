package xyz.jpenilla.announcerplus.config

import ninja.leaping.configurate.ConfigurationNode
import ninja.leaping.configurate.ConfigurationOptions
import ninja.leaping.configurate.commented.CommentedConfigurationNode
import ninja.leaping.configurate.hocon.HoconConfigurationLoader
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializerCollection
import org.bukkit.command.CommandSender
import org.bukkit.configuration.InvalidConfigurationException
import org.bukkit.entity.Player
import xyz.jpenilla.announcerplus.AnnouncerPlus
import xyz.jpenilla.announcerplus.config.message.MessageConfig
import java.io.File

class ConfigManager(private val announcerPlus: AnnouncerPlus) {
    private val serializers = TypeSerializerCollection.defaults().newChild()
    private val configOptions: ConfigurationOptions

    private val mainConfigFile = File("${announcerPlus.dataFolder}/main.conf")
    private val mainConfigLoader: HoconConfigurationLoader
    lateinit var mainConfig: MainConfig

    private val firstJoinConfigFile = File("${announcerPlus.dataFolder}/first-join.conf")
    private val firstJoinConfigLoader: HoconConfigurationLoader
    lateinit var firstJoinConfig: JoinQuitConfig

    val messageConfigs = hashMapOf<String, MessageConfig>()
    val joinQuitConfigs = hashMapOf<String, JoinQuitConfig>()

    init {
        configOptions = ConfigurationOptions.defaults().withSerializers(serializers)
        mainConfigLoader = HoconConfigurationLoader.builder().setFile(mainConfigFile).build()
        firstJoinConfigLoader = HoconConfigurationLoader.builder().setFile(firstJoinConfigFile).build()

        announcerPlus.dataFolder.mkdir()
        load()
        save()
    }

    fun load() {
        val mainConfigRoot = mainConfigLoader.load(configOptions)
        try {
            mainConfig = MainConfig.loadFrom(mainConfigRoot)
        } catch (e: Exception) {
            throw InvalidConfigurationException("Failed to load the main.conf config file. This is due to misconfiguration", e)
        }

        val firstJoinConfigRoot = firstJoinConfigLoader.load(configOptions)
        try {
            firstJoinConfig = JoinQuitConfig.loadFrom(firstJoinConfigRoot, null)
        } catch (e: Exception) {
            throw InvalidConfigurationException("Failed to load the main.conf config file. This is due to misconfiguration", e)
        }

        loadMessageConfigs()
        loadJoinQuitConfigs()
    }

    fun save() {
        val mainConfigRoot = CommentedConfigurationNode.root(configOptions.withHeader(""" 
     ___                                                 ____  __               __    
    /   |  ____  ____  ____  __  ______  ________  _____/ __ \/ /_  _______  __/ /_
   / /| | / __ \/ __ \/ __ \/ / / / __ \/ ___/ _ \/ ___/ /_/ / / / / / ___/ /_  __/
  / ___ |/ / / / / / / /_/ / /_/ / / / / /__/  __/ /  / ____/ / /_/ (__  )   /_/   
 /_/  |_/_/ /_/_/ /_/\____/\__,_/_/ /_/\___/\___/_/  /_/   /_/\__,_/____/  
 
     v${announcerPlus.description.version}
"""))
        mainConfig.saveTo(mainConfigRoot)
        mainConfigLoader.save(mainConfigRoot)

        val firstJoinConfigRoot = CommentedConfigurationNode.root(configOptions.withHeader(
                "If enabled in main.conf, this join config will be used when players join the server for the first time.\n" +
                        "All other join configs will be skipped for first-join if this is enabled."
        ))
        firstJoinConfig.saveTo(firstJoinConfigRoot)
        firstJoinConfigRoot.removeChild("quit-section")
        firstJoinConfigLoader.save(firstJoinConfigRoot)
    }

    private fun loadJoinQuitConfigs() {
        joinQuitConfigs.clear()
        val path = "${announcerPlus.dataFolder}/join-quit-configs"
        val folder = File(path)
        if (!folder.exists()) {
            if (folder.mkdir()) {
                announcerPlus.logger.info("Creating messages folder")
            }
        }
        if (folder.listFiles().isEmpty()) {
            announcerPlus.logger.info("No join/quit configs found, creating default.conf")

            val defaultConfig = File("$path/default.conf")
            val defaultConfigLoader = HoconConfigurationLoader.builder().setFile(defaultConfig).build()
            val defaultConfigRoot = CommentedConfigurationNode.root(configOptions.withHeader(
                    "To give a player these join/quit messages give them the announcerplus.join.default\n" +
                            "  and announcerplus.quit.default permissions"))
            JoinQuitConfig().saveTo(defaultConfigRoot)
            defaultConfigLoader.save(defaultConfigRoot)
        }
        val joinQuitConfigFiles = File(path).listFiles()
        for (configFile in joinQuitConfigFiles) {
            val configLoader = HoconConfigurationLoader.builder().setFile(configFile).build()
            var root: ConfigurationNode
            val name = configFile.nameWithoutExtension
            try {
                root = configLoader.load(configOptions)
                joinQuitConfigs[name] = JoinQuitConfig.loadFrom(root, name)

                joinQuitConfigs[name]?.saveTo(root)
                configLoader.save(root)
            } catch (e: Exception) {
                throw InvalidConfigurationException("Failed to load message config: ${configFile.name}. This is due to an invalid config file.", e)
            }
        }
    }

    private fun loadMessageConfigs() {
        for (mC in messageConfigs.values) {
            mC.stop()
        }
        messageConfigs.clear()
        val path = "${announcerPlus.dataFolder}/message-configs"
        val folder = File(path)
        if (!folder.exists()) {
            if (folder.mkdir()) {
                announcerPlus.logger.info("Creating messages folder")
            }
        }
        if (folder.listFiles().isEmpty()) {
            announcerPlus.logger.info("No message configs found, creating demo.conf")

            val defaultConfig = File("$path/demo.conf")
            val defaultConfigLoader = HoconConfigurationLoader.builder().setFile(defaultConfig).build()
            val defaultConfigRoot = CommentedConfigurationNode.root(configOptions.withHeader(
                    "For a player to get these messages give them the announcerplus.messages.demo permission\n" +
                            "  If EssentialsX is installed, then giving a player the announcerplus.messages.demo.afk permission\n" +
                            "  will stop them from receiving these messages while afk"))
            MessageConfig().saveTo(defaultConfigRoot)
            defaultConfigLoader.save(defaultConfigRoot)
        }
        val messageConfigFiles = File(path).listFiles()
        for (configFile in messageConfigFiles) {
            val configLoader = HoconConfigurationLoader.builder().setFile(configFile).build()
            val name = configFile.nameWithoutExtension
            var root: ConfigurationNode
            try {
                root = configLoader.load(configOptions)
                messageConfigs[name] = MessageConfig.loadFrom(root, name)

                messageConfigs[name]?.saveTo(root)
                configLoader.save(root)
            } catch (e: Exception) {
                throw InvalidConfigurationException("Failed to load join/quit config: ${configFile.name}. This is due to an invalid config file.", e)
            }
        }
    }

    fun parse(player: CommandSender?, message: String): String {
        val p = if (player is Player) {
            player
        } else {
            null
        }

        var msg = announcerPlus.chat.parse(p, message, mainConfig.placeholders)
        if (msg.startsWith("<center>")) {
            msg = announcerPlus.chat.getCenteredMessage(msg.replace("<center>", ""))
        }

        return msg
    }

    fun parse(player: CommandSender?, messages: List<String>): List<String> {
        val tempMessages = ArrayList<String>()
        for (message in messages) {
            tempMessages.add(parse(player, message))
        }
        return tempMessages
    }
}