package xyz.jpenilla.announcerplus

import net.milkbowl.vault.permission.Permission
import org.bukkit.plugin.RegisteredServiceProvider
import org.bukkit.plugin.java.JavaPlugin
import xyz.jpenilla.announcerplus.command.CommandHelper
import xyz.jpenilla.announcerplus.config.Config

class AnnouncerPlus : JavaPlugin() {
    var perms: Permission? = null
    var placeholderAPI = false
    lateinit var cfg: Config; private set
    lateinit var commandHelper: CommandHelper

    override fun onEnable() {
        if (!setupPermissions()) {
            logger.warning("Permissions plugin not found. AnnouncerPlus will not work.")
            server.pluginManager.disablePlugin(this)
            return
        }
        placeholderAPI = server.pluginManager.isPluginEnabled("PlaceholderAPI")
        cfg = Config(this)
        commandHelper = CommandHelper(this)
        server.pluginManager.registerEvents(JoinQuitListener(this), this)
        broadcast()
    }

    private fun broadcast() {
        for (c in cfg.messageConfigs.values) {
            c.broadcast()
        }
    }

    fun reload() {
        cfg.reload()
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
}