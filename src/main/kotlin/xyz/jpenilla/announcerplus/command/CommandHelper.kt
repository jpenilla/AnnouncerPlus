package xyz.jpenilla.announcerplus.command

import co.aikar.commands.ACFUtil
import co.aikar.commands.InvalidCommandArgument
import co.aikar.commands.MessageKeys
import co.aikar.commands.PaperCommandManager
import co.aikar.commands.bukkit.contexts.OnlinePlayer
import com.google.common.collect.ImmutableList
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.koin.core.KoinComponent
import org.koin.core.inject
import xyz.jpenilla.announcerplus.AnnouncerPlus
import xyz.jpenilla.announcerplus.config.message.MessageConfig
import java.util.regex.Pattern
import kotlin.math.ceil

class CommandHelper : KoinComponent {
    private val announcerPlus: AnnouncerPlus by inject()
    private val commandManager = PaperCommandManager(announcerPlus)

    init {
        commandManager.enableUnstableAPI("help")
        commandManager.defaultHelpPerPage = 4
        commandManager.helpFormatter = HelpFormatter(commandManager)
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

        contexts.registerContext(QuotedString::class.java) {
            val args = it.args.joinToString(" ")
            val matcher = quotesPattern.matcher(args)
            if (matcher.find() && args.startsWith(matcher.group())) {
                it.args.clear()
                it.args.addAll(args.replaceFirst(matcher.group(), "").trim().split(" "))

                val string = StringBuilder(matcher.group())
                string.deleteCharAt(string.lastIndex)
                string.deleteCharAt(0)

                QuotedString(string.toString().replace("\\\"", "\"").replace("\\ ", " "))
            } else {
                QuotedString(it.popFirstArg())
            }
        }

        contexts.registerContext(WorldPlayers::class.java) {
            val input = it.popFirstArg()
            val world = Bukkit.getWorld(input)
            when {
                world != null -> {
                    WorldPlayers(ImmutableList.copyOf(world.players))
                }
                input == "*" -> {
                    WorldPlayers(ImmutableList.copyOf(Bukkit.getOnlinePlayers()))
                }
                input.contains(",") -> {
                    val inputs = input.split(",")
                    val players = arrayListOf<Player>()
                    val valid = ArrayList(Bukkit.getWorlds().map { w -> w.name })
                    for (string in inputs) {
                        val w = Bukkit.getWorld(string)
                                ?: throw InvalidCommandArgument(MessageKeys.PLEASE_SPECIFY_ONE_OF, "{valid}", ACFUtil.join(valid, ", "))
                        players.addAll(w.players)
                    }
                    WorldPlayers(players.distinct())
                }
                else -> {
                    val valid = ArrayList(Bukkit.getWorlds().map { w -> w.name })
                    valid.add("*")
                    throw InvalidCommandArgument(MessageKeys.PLEASE_SPECIFY_ONE_OF, "{valid}", ACFUtil.join(valid, ", "))
                }
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

        completions.registerAsyncCompletion("quoted_string") {
            val input = it.input
            if (input.length > 1 && input.startsWith("\"") && input.endsWith("\"") && !input.endsWith("\\\"")) {
                emptyList()
            } else {
                ImmutableList.of("$input\"")
            }
        }
        completions.setDefaultCompletion("quoted_string", QuotedString::class.java)

        completions.registerCompletion("world_audience") {
            val completion = ArrayList(getCommaSeparatedCompletion(it.input, Bukkit.getWorlds().map { w -> w.name }))
            completion.add("*")
            completion
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

        completions.registerCompletion("player_array") {
            val names = ImmutableList.copyOf(Bukkit.getOnlinePlayers()).map { player -> player.name }
            getCommaSeparatedCompletion(it.input, names)
        }
        completions.setDefaultCompletion("player_array", Array<OnlinePlayer>::class.java)
    }

    private fun getCommaSeparatedCompletion(input: String, names: List<String>): List<String> {
        val completion = arrayListOf<String>()
        completion.addAll(names)
        if (input.isNotEmpty()) {
            val i = input.split(",")
            i.forEachIndexed { index, s ->
                if (i.lastIndex != index && !names.contains(s)) {
                    return completion
                }
            }
            if (names.contains(i.last())) {
                completion.add("$input,")
            }
            if (i.size > 1) {
                val trimmed = input.substringBeforeLast(",")
                completion.addAll(names.map { name -> "$trimmed,$name" })
            }
        }
        for (string in ImmutableList.copyOf(completion)) {
            val splitC = string.split(",")
            if (splitC.distinct().size != splitC.size) {
                completion.remove(string)
            }
        }
        return completion
    }

    fun reload() {
        // register text replacements which need to be reloaded here
    }

    companion object {
        private val quotesPattern = Pattern.compile("\"(\\\\\\\\|\\\\\"|[^\"])*\"")
    }

    data class QuotedString(val string: String)
    data class WorldPlayers(val players: List<Player>)
}