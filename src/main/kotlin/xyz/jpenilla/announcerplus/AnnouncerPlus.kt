package xyz.jpenilla.announcerplus

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonParser
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer
import net.milkbowl.vault.permission.Permission
import org.bstats.bukkit.Metrics
import org.bukkit.plugin.RegisteredServiceProvider
import xyz.jpenilla.announcerplus.command.CommandHelper
import xyz.jpenilla.announcerplus.compatability.EssentialsHook
import xyz.jpenilla.announcerplus.config.ConfigManager
import xyz.jpenilla.announcerplus.util.UpdateChecker
import xyz.jpenilla.jmplib.BasePlugin
import java.util.concurrent.Callable

class AnnouncerPlus : BasePlugin() {
    val gson: Gson = GsonBuilder().create()
    val jsonParser = JsonParser()
    val gsonComponentSerializer = GsonComponentSerializer.gson()
    var perms: Permission? = null
    var essentials: EssentialsHook? = null
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
        server.pluginManager.registerEvents(JoinQuitListener(this), this)
        broadcast()
        UpdateChecker(this, 81005).updateCheck()
        val metrics = Metrics(this, 8067)
        metrics.addCustomChart(Metrics.SimplePie("join_quit_configs", Callable { configManager.joinQuitConfigs.size.toString() }))
        metrics.addCustomChart(Metrics.SimplePie("message_configs", Callable { configManager.messageConfigs.size.toString() }))
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
    }

    private fun setupPermissions(): Boolean {
        val rsp: RegisteredServiceProvider<Permission>? = server.servicesManager.getRegistration(Permission::class.java)
        if (rsp != null) {
            perms = rsp.provider
        }
        return perms != null
    }

    companion object {
        lateinit var instance: AnnouncerPlus
    }
}
