/*
 * This file is part of AnnouncerPlus, licensed under the MIT License.
 *
 * Copyright (c) 2020-2021 Jason Penilla
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
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
        (commandManager.commandHelpHandler.queryHelp(
          context.sender,
          ""
        ) as CommandHelpHandler.IndexHelpTopic<CommandSender>)
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
          ?: return@withParser ArgumentParseResult.failure(
            IllegalArgumentException(
              "No message config for name '$input'. Must be one of: ${configManager.messageConfigs.keys.joinToString(", ")}"
            )
          )
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
        if (input.isEmpty()) {
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
