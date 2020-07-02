package xyz.jpenilla.announcerplus.command

import co.aikar.commands.MessageType
import co.aikar.commands.PaperCommandManager
import org.bukkit.ChatColor
import xyz.jpenilla.announcerplus.AnnouncerPlus
import xyz.jpenilla.announcerplus.config.Config


class CommandHelper(private val announcerPlus: AnnouncerPlus) {
    private val commandManager = PaperCommandManager(announcerPlus)

    init {
        commandManager.enableUnstableAPI("help")
        commandManager.defaultHelpPerPage = 5
        commandManager.setFormat(MessageType.ERROR, ChatColor.BLUE, ChatColor.AQUA, ChatColor.WHITE)
        commandManager.setFormat(MessageType.INFO, ChatColor.BLUE, ChatColor.AQUA, ChatColor.WHITE)
        commandManager.setFormat(MessageType.HELP, ChatColor.BLUE, ChatColor.AQUA, ChatColor.WHITE)
        commandManager.setFormat(MessageType.SYNTAX, ChatColor.BLUE, ChatColor.AQUA, ChatColor.WHITE)
        commandManager.registerDependency(Config::class.java, announcerPlus.cfg)
        commandManager.registerCommand(CommandAnnouncerPlus())
        reload()
    }

    fun reload() {
        commandManager.commandCompletions.registerAsyncCompletion("configs") {
            val completion = ArrayList<String>()
            for (messageConfig in announcerPlus.cfg.messageConfigs.values) {
                completion.add(messageConfig.name)
            }
            completion
        }
    }
}