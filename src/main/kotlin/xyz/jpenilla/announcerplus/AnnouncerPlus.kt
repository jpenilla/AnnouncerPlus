package xyz.jpenilla.announcerplus

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonParser
import kr.entree.spigradle.annotations.PluginMain
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer
import net.milkbowl.vault.permission.Permission
import org.bstats.bukkit.Metrics
import xyz.jpenilla.announcerplus.command.CommandHelper
import xyz.jpenilla.announcerplus.compatability.EssentialsHook
import xyz.jpenilla.announcerplus.config.ConfigManager
import xyz.jpenilla.announcerplus.task.ToastTask
import xyz.jpenilla.announcerplus.util.UpdateChecker
import xyz.jpenilla.jmplib.BasePlugin

@PluginMain
class AnnouncerPlus : BasePlugin() {
    val gson: Gson = GsonBuilder().create()
    val jsonParser = JsonParser()
    val gsonComponentSerializer = GsonComponentSerializer.gson()
    val downsamplingGsonComponentSerializer = GsonComponentSerializer.colorDownsamplingGson()
    var perms: Permission? = null
    var essentials: EssentialsHook? = null
    var toastTask: ToastTask? = null
    lateinit var configManager: ConfigManager; private set
    lateinit var commandHelper: CommandHelper

    override fun onPluginEnable() {
        instance = this
        if (!setupPermissions()) {
            logger.warning("Permissions plugin not found. AnnouncerPlus will not work.")
            server.pluginManager.disablePlugin(this)
            return
        }
        if (server.pluginManager.isPluginEnabled("Essentials")) {
            essentials = EssentialsHook(this)
        }
        configManager = ConfigManager(this)
        commandHelper = CommandHelper(this)
        if (majorMinecraftVersion > 12) {
            toastTask = ToastTask(this)
        } else {
            logger.info("Sorry, but Toast/Achievement style messages do not work on this version. Update to 1.13 or newer to use this feature.")
        }
        server.pluginManager.registerEvents(JoinQuitListener(this), this)
        broadcast()
        UpdateChecker(this, "jmanpenilla/AnnouncerPlus").updateCheck()
        val metrics = Metrics(this, 8067)
        metrics.addCustomChart(Metrics.SimplePie("join_quit_configs", configManager.joinQuitConfigs.size::toString))
        metrics.addCustomChart(Metrics.SimplePie("message_configs", configManager.messageConfigs.size::toString))
    }

    private fun broadcast() {
        for (c in configManager.messageConfigs.values) {
            c.broadcast()
        }
    }

    fun reload() {
        configManager.load()
        commandHelper.reload()
        broadcast()
    }

    override fun onDisable() {
        toastTask?.cancel()
    }

    private fun setupPermissions(): Boolean {
        val rsp = server.servicesManager.getRegistration(Permission::class.java)
        if (rsp != null) {
            perms = rsp.provider
        }
        return perms != null
    }

    companion object {
        lateinit var instance: AnnouncerPlus
    }
}
