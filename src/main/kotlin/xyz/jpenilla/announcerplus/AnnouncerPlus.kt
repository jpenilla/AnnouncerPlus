package xyz.jpenilla.announcerplus

import net.milkbowl.vault.permission.Permission
import org.bstats.bukkit.Metrics
import org.bukkit.plugin.RegisteredServiceProvider
import org.bukkit.plugin.java.JavaPlugin
import xyz.jpenilla.announcerplus.command.CommandHelper
import xyz.jpenilla.announcerplus.compatability.PrismaHook
import xyz.jpenilla.announcerplus.config.Config
import xyz.jpenilla.jmplib.Chat

class AnnouncerPlus : JavaPlugin() {
    var perms: Permission? = null
    var prismaCompat: PrismaHook? = null
    lateinit var cfg: Config; private set
    lateinit var commandHelper: CommandHelper
    lateinit var chat: Chat

    override fun onEnable() {
        if (!setupPermissions()) {
            logger.warning("Permissions plugin not found. AnnouncerPlus will not work.")
            server.pluginManager.disablePlugin(this)
            return
        }
        chat = Chat(this)
        if (server.pluginManager.isPluginEnabled("Prisma")) {
            prismaCompat = PrismaHook()
        }
        cfg = Config(this)
        commandHelper = CommandHelper(this)
        server.pluginManager.registerEvents(JoinQuitListener(this), this)
        broadcast()
        UpdateChecker(this, 81005).updateCheck()
        val metrics = Metrics(this, 8067)
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
