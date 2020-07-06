package xyz.jpenilla.announcerplus.command

import co.aikar.commands.PaperCommandManager
import com.okkero.skedule.schedule
import xyz.jpenilla.announcerplus.AnnouncerPlus
import xyz.jpenilla.announcerplus.config.Config
import xyz.jpenilla.jmplib.Chat


class CommandHelper(private val announcerPlus: AnnouncerPlus) {
    private val commandManager = PaperCommandManager(announcerPlus)
    private val helpFormatter = HelpFormatter(announcerPlus, commandManager)

    init {
        commandManager.enableUnstableAPI("help")
        commandManager.defaultHelpPerPage = 4
        commandManager.registerDependency(Config::class.java, announcerPlus.cfg)
        commandManager.registerDependency(Chat::class.java, announcerPlus.chat)
        commandManager.helpFormatter = helpFormatter
        commandManager.registerCommand(CommandAnnouncerPlus())
        //TODO: Find a better fix for stopping the console getting spammed with help messages on load
        announcerPlus.schedule {
            waitFor(1L)
            announcerPlus.commandHelper.helpFormatter.loaded = true
        }
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