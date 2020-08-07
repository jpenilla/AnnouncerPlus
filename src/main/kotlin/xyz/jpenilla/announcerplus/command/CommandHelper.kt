package xyz.jpenilla.announcerplus.command

import co.aikar.commands.ACFUtil
import co.aikar.commands.InvalidCommandArgument
import co.aikar.commands.MessageKeys
import co.aikar.commands.PaperCommandManager
import xyz.jpenilla.announcerplus.AnnouncerPlus
import xyz.jpenilla.announcerplus.config.ConfigManager
import xyz.jpenilla.announcerplus.config.message.MessageConfig
import xyz.jpenilla.jmplib.Chat
import kotlin.math.ceil


class CommandHelper(private val announcerPlus: AnnouncerPlus) {
    private val commandManager = PaperCommandManager(announcerPlus)

    init {
        commandManager.enableUnstableAPI("help")
        commandManager.defaultHelpPerPage = 4
        commandManager.registerDependency(ConfigManager::class.java, announcerPlus.configManager)
        commandManager.registerDependency(Chat::class.java, announcerPlus.chat)
        commandManager.helpFormatter = HelpFormatter(announcerPlus, commandManager)
        registerContexts()
        registerCompletions()
        reload()
        commandManager.registerCommand(CommandAnnouncerPlus())
    }

    private fun registerContexts() {
        commandManager.commandContexts.registerContext(MessageConfig::class.java) {
            val name: String = it.popFirstArg()
            val valid = announcerPlus.configManager.messageConfigs.map { config -> config.key }
            announcerPlus.configManager.messageConfigs[name]
                    ?: throw InvalidCommandArgument(MessageKeys.PLEASE_SPECIFY_ONE_OF, "{valid}", ACFUtil.join(valid, ", "))
        }
    }

    private fun registerCompletions() {
        commandManager.commandCompletions.registerAsyncCompletion("message_configs") {
            val completion = ArrayList<String>()
            for (messageConfig in announcerPlus.configManager.messageConfigs.values) {
                completion.add(messageConfig.name)
            }
            completion
        }
        commandManager.commandCompletions.setDefaultCompletion("message_configs", MessageConfig::class.java)

        commandManager.commandCompletions.registerAsyncCompletion("message_config_pages") {
            val config = it.getContextValue(MessageConfig::class.java)
            var lines = 0
            for (msg in config.messages) {
                for (line in msg.text) {
                    lines++
                }
            }
            val pages = ceil(lines / 17.00).toInt()
            val completion = ArrayList<String>()
            for (i in 1..pages) {
                completion.add(i.toString())
            }
            completion
        }
    }

    fun reload() {
        // register text replacements which need to be reloaded here
    }
}