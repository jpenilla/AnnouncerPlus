package xyz.jpenilla.announcerplus.command

import cloud.commandframework.CommandHelpHandler
import cloud.commandframework.arguments.CommandArgument
import cloud.commandframework.arguments.parser.ArgumentParseResult
import cloud.commandframework.arguments.standard.IntegerArgument
import cloud.commandframework.arguments.standard.StringArgument
import com.google.common.collect.ImmutableList
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.koin.core.KoinComponent
import org.koin.core.inject
import xyz.jpenilla.announcerplus.config.ConfigManager
import xyz.jpenilla.announcerplus.config.message.MessageConfig

class ArgumentFactory : KoinComponent {
    private val commandManager: CommandManager by inject()
    private val configManager: ConfigManager by inject()

    fun helpQuery(name: String): CommandArgument<CommandSender, String> {
        return StringArgument.newBuilder<CommandSender>(name)
                .greedy()
                .asOptional()
                .withSuggestionsProvider { context, _ ->
                    (commandManager.commandHelpHandler.queryHelp(context.sender, "") as CommandHelpHandler.IndexHelpTopic<CommandSender>)
                            .entries.map { it.syntaxString }
                }
                .build()
    }

    fun positiveInteger(name: String): IntegerArgument.Builder<CommandSender> {
        return IntegerArgument.newBuilder<CommandSender>(name)
                .withMin(1)
    }

    fun messageConfig(name: String): CommandArgument<CommandSender, MessageConfig> {
        return commandManager.argumentBuilder(MessageConfig::class.java, name)
                .withSuggestionsProvider { _, _ -> configManager.messageConfigs.keys.toList() }
                .withParser { _, inputQueue ->
                    val input = inputQueue.peek()
                    val config = configManager.messageConfigs[input]
                            ?: return@withParser ArgumentParseResult.failure(IllegalArgumentException(
                                    "No message config for name '$input'. Must be one of: ${configManager.messageConfigs.keys.joinToString(", ")}"
                            ))
                    inputQueue.remove()
                    ArgumentParseResult.success(config)
                }
                .build()
    }

    fun worldPlayers(name: String): CommandArgument<CommandSender, WorldPlayers> {
        return commandManager.argumentBuilder(WorldPlayers::class.java, name)
                .withSuggestionsProvider { _, _ ->
                    val suggestions = Bukkit.getWorlds().map { it.name }.toMutableList()
                    suggestions.add("all")
                    suggestions
                }
                .withParser { _, inputQueue ->
                    val input = inputQueue.peek()
                    if (input == null || input.isEmpty()) {
                        return@withParser ArgumentParseResult.failure(
                                IllegalArgumentException("No input provided.")
                        )
                    }
                    if (input == "all") {
                        inputQueue.remove()
                        return@withParser ArgumentParseResult.success(WorldPlayers(
                                Bukkit.getWorlds().flatMap { it.players }
                        ))
                    }
                    val world = Bukkit.getWorld(input)
                            ?: return@withParser ArgumentParseResult.failure(
                                    IllegalArgumentException("No such world: $input")
                            )
                    inputQueue.remove()
                    ArgumentParseResult.success(WorldPlayers(ImmutableList.copyOf(world.players)))
                }
                .build()
    }

    data class WorldPlayers(val players: Collection<Player>)
}