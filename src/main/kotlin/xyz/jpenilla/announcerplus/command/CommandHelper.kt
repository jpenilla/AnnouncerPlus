package xyz.jpenilla.announcerplus.command

import co.aikar.commands.ACFUtil
import co.aikar.commands.InvalidCommandArgument
import co.aikar.commands.MessageKeys
import co.aikar.commands.PaperCommandManager
import com.google.common.collect.ImmutableList
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import xyz.jpenilla.announcerplus.AnnouncerPlus
import xyz.jpenilla.announcerplus.config.ConfigManager
import xyz.jpenilla.announcerplus.config.message.MessageConfig
import xyz.jpenilla.jmplib.Chat
import java.util.regex.Pattern
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
        val contexts = commandManager.commandContexts
        contexts.registerContext(MessageConfig::class.java) {
            val name = it.popFirstArg()
            val valid = announcerPlus.configManager.messageConfigs.map { config -> config.key }
            announcerPlus.configManager.messageConfigs[name]
                    ?: throw InvalidCommandArgument(MessageKeys.PLEASE_SPECIFY_ONE_OF, "{valid}", ACFUtil.join(valid, ", "))
        }

        contexts.registerContext(StringPair::class.java) {
            val input = if (it.isLastArg) {
                it.joinArgs()
            } else {
                it.popFirstArg()
            }
            val matcher = quotesPattern.matcher(input)
            val strings = arrayListOf<String>()
            while (matcher.find()) {
                val string = StringBuilder(matcher.group(0))
                string.deleteCharAt(string.lastIndex)
                string.deleteCharAt(0)
                strings.add(string.toString().replace("\\\"", "\""))
            }
            if (strings.isEmpty()) {
                strings.add(input)
            }
            StringPair(strings[0], strings.getOrNull(1) ?: "")
        }

        contexts.registerContext(WorldPlayers::class.java) {
            val input = it.popFirstArg()
            val world = Bukkit.getWorld(input)
            if (world != null) {
                WorldPlayers(ImmutableList.copyOf(world.players))
            } else if (input == "*") {
                WorldPlayers(ImmutableList.copyOf(Bukkit.getOnlinePlayers()))
            } else {
                val valid = ArrayList(Bukkit.getWorlds().map { w -> w.name })
                valid.add("*")
                throw InvalidCommandArgument(MessageKeys.PLEASE_SPECIFY_ONE_OF, "{valid}", ACFUtil.join(valid, ", "))
            }
        }
    }

    private fun registerCompletions() {
        val completions = commandManager.commandCompletions
        completions.registerAsyncCompletion("message_configs") {
            val completion = ArrayList<String>()
            for (messageConfig in announcerPlus.configManager.messageConfigs.values) {
                completion.add(messageConfig.name)
            }
            completion
        }
        completions.setDefaultCompletion("message_configs", MessageConfig::class.java)

        completions.registerAsyncCompletion("message_config_pages") {
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

        completions.registerAsyncCompletion("string_pair") {
            ImmutableList.of(
                    "A single unquoted text",
                    "\"Text 1\" \"Text 2\"",
                    "\"Some Text\" \"Some \\\"quoted\\\" text!\"",
                    "\"A single text\""
            )
        }
        completions.setDefaultCompletion("string_pair", StringPair::class.java)

        completions.registerCompletion("world_audience") {
            val valid = ArrayList(Bukkit.getWorlds().map { w -> w.name })
            valid.add("*")
            valid
        }
        completions.setDefaultCompletion("world_audience", WorldPlayers::class.java)

        completions.registerAsyncCompletion("numbers_by_5") {
            val completion = arrayListOf<String>()
            for (i in 0..60) {
                if (i % 5 == 0) {
                    completion.add(i.toString())
                }
            }
            completion
        }
    }

    fun reload() {
        // register text replacements which need to be reloaded here
    }

    companion object {
        private val quotesPattern = Pattern.compile("\"(\\\\\\\\|\\\\\"|[^\"])*\"")
    }

    data class StringPair(val first: String, val second: String)
    data class WorldPlayers(val players: List<Player>)
}