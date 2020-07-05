package xyz.jpenilla.announcerplus.command

import co.aikar.commands.MessageType
import co.aikar.commands.PaperCommandManager
import org.bukkit.ChatColor
import xyz.jpenilla.announcerplus.AnnouncerPlus
import xyz.jpenilla.announcerplus.config.Config
import xyz.jpenilla.jmplib.Chat


class CommandHelper(private val announcerPlus: AnnouncerPlus) {
    private val commandManager = PaperCommandManager(announcerPlus)

    init {
        commandManager.enableUnstableAPI("help")
        commandManager.defaultHelpPerPage = 4
        /**commandManager.setFormat(MessageType.ERROR, ChatColor.AQUA, ChatColor.BLUE, ChatColor.WHITE)
        commandManager.setFormat(MessageType.INFO, ChatColor.AQUA, ChatColor.BLUE, ChatColor.WHITE)
        commandManager.setFormat(MessageType.HELP, ChatColor.AQUA, ChatColor.BLUE, ChatColor.WHITE)
        commandManager.setFormat(MessageType.SYNTAX, ChatColor.AQUA, ChatColor.BLUE, ChatColor.WHITE)**/
        commandManager.registerDependency(Config::class.java, announcerPlus.cfg)
        commandManager.registerDependency(Chat::class.java, announcerPlus.chat)
        commandManager.helpFormatter = HelpFormatter(announcerPlus, commandManager)
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