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
import cloud.commandframework.arguments.parser.ArgumentParseResult.failure
import cloud.commandframework.arguments.parser.ArgumentParseResult.success
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

  private fun <C> stringArgument(name: String, builder: StringArgument.Builder<C>.() -> Unit): StringArgument<C> =
    StringArgument.newBuilder<C>(name).apply(builder).build()

  fun helpQuery(name: String) = stringArgument<CommandSender>(name) {
    greedy()
    asOptional()
    withSuggestionsProvider { context, _ ->
      val helpTopic = commandManager.commandHelpHandler.queryHelp(
        context.sender,
        ""
      ) as CommandHelpHandler.IndexHelpTopic<CommandSender>
      helpTopic.entries.map { it.syntaxString }
    }
  }

  private fun <C> integerArgumentBuilder(
    name: String,
    builder: IntegerArgument.Builder<C>.() -> Unit
  ): IntegerArgument.Builder<C> =
    IntegerArgument.newBuilder<C>(name).apply(builder)

  fun positiveInteger(name: String) = integer(name, min = 1)

  fun integer(
    name: String,
    min: Int = Int.MIN_VALUE,
    max: Int = Int.MAX_VALUE
  ) = integerArgumentBuilder<CommandSender>(name) {
    if (min != Int.MIN_VALUE) withMin(min)
    if (max != Int.MAX_VALUE) withMax(max)
  }

  private inline fun <C, reified T> cloud.commandframework.CommandManager<C>.argument(
    name: String,
    builder: CommandArgument.Builder<C, T>.() -> Unit
  ): CommandArgument<C, T> {
    val argumentBuilder = argumentBuilder(T::class.java, name)
    return argumentBuilder.apply(builder).build()
  }

  fun messageConfig(name: String) = commandManager.argument<CommandSender, MessageConfig>(name) {
    withSuggestionsProvider { _, _ -> configManager.messageConfigs.keys.toList() }
    withParser { _, inputQueue ->
      val input = inputQueue.peek()
      val config = configManager.messageConfigs[input]
        ?: return@withParser failure(
          IllegalArgumentException(
            "No message config for name '$input'. Must be one of: ${configManager.messageConfigs.keys.joinToString(", ")}"
          )
        )
      inputQueue.remove()
      success(config)
    }
  }

  fun worldPlayers(name: String) = commandManager.argument<CommandSender, WorldPlayers>(name) {
    withSuggestionsProvider { _, _ ->
      Bukkit.getWorlds().map { it.name }.toMutableList().apply {
        add("all")
      }
    }
    withParser { _, inputQueue ->
      val input = inputQueue.peek()
      if (input.isEmpty()) {
        return@withParser failure(IllegalArgumentException("No input provided."))
      }
      if (input == "all") {
        inputQueue.remove()
        return@withParser success(WorldPlayers(Bukkit.getWorlds().flatMap { it.players }))
      }
      val world = Bukkit.getWorld(input)
        ?: return@withParser failure(IllegalArgumentException("No such world: $input"))
      inputQueue.remove()
      success(WorldPlayers(ImmutableList.copyOf(world.players)))
    }
  }

  data class WorldPlayers(val players: Collection<Player>)
}
