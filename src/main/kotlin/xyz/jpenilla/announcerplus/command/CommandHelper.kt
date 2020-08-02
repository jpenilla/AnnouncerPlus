package xyz.jpenilla.announcerplus.command

import co.aikar.commands.PaperCommandManager
import xyz.jpenilla.announcerplus.AnnouncerPlus
import xyz.jpenilla.announcerplus.config.ConfigManager
import xyz.jpenilla.jmplib.Chat


class CommandHelper(private val announcerPlus: AnnouncerPlus) {
    private val commandManager = PaperCommandManager(announcerPlus)

    init {
        commandManager.enableUnstableAPI("help")
        commandManager.defaultHelpPerPage = 4
        commandManager.registerDependency(ConfigManager::class.java, announcerPlus.configManager)
        commandManager.registerDependency(Chat::class.java, announcerPlus.chat)
        commandManager.helpFormatter = HelpFormatter(announcerPlus, commandManager)
        commandManager.registerCommand(CommandAnnouncerPlus())
        reload()
    }

    fun reload() {
        commandManager.commandCompletions.registerAsyncCompletion("configs") {
            val completion = ArrayList<String>()
            for (messageConfig in announcerPlus.configManager.messageConfigs.values) {
                completion.add(messageConfig.name)
            }
            completion
        }
    }
}