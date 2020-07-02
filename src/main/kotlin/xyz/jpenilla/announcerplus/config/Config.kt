package xyz.jpenilla.announcerplus.config

import me.clip.placeholderapi.PlaceholderAPI
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Player
import xyz.jpenilla.announcerplus.AnnouncerPlus
import java.io.File

class Config(private val announcerPlus: AnnouncerPlus) {
    val messageConfigs: HashMap<String, MessageConfig> = HashMap()
    val placeholders = HashMap<String, String>()

    init {
        announcerPlus.saveDefaultConfig()
        reload()
    }

    fun reload() {
        announcerPlus.reloadConfig()
        val config = announcerPlus.config

        placeholders.clear()
        for (key in config.getConfigurationSection("placeholders")!!.getKeys(false)) {
            placeholders[key] = config.getString("placeholders.$key")!!
        }
        loadMessageConfigs()
    }

    private fun loadMessageConfigs() {
        for (mC in messageConfigs.values) {
            mC.stop()
        }
        messageConfigs.clear()
        val path = "${announcerPlus.dataFolder}/messages"
        val folder = File(path)
        if (!folder.exists()) {
            if (folder.mkdir()) {
                announcerPlus.logger.info("Creating messages folder")
            }
        }
        if (folder.listFiles().isEmpty()) {
            announcerPlus.logger.info("No message configs found, copying demo.yml")
            announcerPlus.saveResource("messages/demo.yml", false)
        }
        val messageConfigFiles = File(path).listFiles()
        for (configFile in messageConfigFiles) {
            val data = YamlConfiguration.loadConfiguration(configFile)
            val name = configFile.name.split(".").toTypedArray()[0]
            messageConfigs[name] = (MessageConfig(announcerPlus, name, data))
        }
    }

    fun replacePlaceholders(player: Player?, message: String): String {
        var m = message
        for (placeholder in placeholders.entries) {
            m = m.replace("{${placeholder.key}}", placeholder.value)
        }
        return if (player != null && announcerPlus.placeholderAPI) {
            PlaceholderAPI.setPlaceholders(player, m)
        } else {
            m
        }
    }
}