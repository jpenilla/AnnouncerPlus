/*
 * This file is part of AnnouncerPlus, licensed under the MIT License.
 *
 * Copyright (c) 2020-2024 Jason Penilla
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
package xyz.jpenilla.announcerplus.command.argument

import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.incendo.cloud.context.CommandContext
import org.incendo.cloud.context.CommandInput
import org.incendo.cloud.kotlin.extension.parserDescriptor
import org.incendo.cloud.parser.ArgumentParseResult
import org.incendo.cloud.parser.ArgumentParser
import org.incendo.cloud.suggestion.BlockingSuggestionProvider
import xyz.jpenilla.announcerplus.command.Commander
import xyz.jpenilla.announcerplus.util.failure

data class WorldPlayers(val players: Collection<Player>)

fun worldPlayersParser() = parserDescriptor(WorldPlayersParser())

class WorldPlayersParser : ArgumentParser<Commander, WorldPlayers>, BlockingSuggestionProvider.Strings<Commander> {
  override fun parse(
    context: CommandContext<Commander>,
    inputQueue: CommandInput
  ): ArgumentParseResult<WorldPlayers> {
    val input = inputQueue.readString()

    if (input == "all") {
      return ArgumentParseResult.success(WorldPlayers(Bukkit.getWorlds().flatMap { it.players }))
    }

    val world = Bukkit.getWorld(input)
      ?: return failure(Component.text("No such world: $input"))

    return ArgumentParseResult.success(WorldPlayers(world.players.toList()))
  }

  override fun stringSuggestions(
    context: CommandContext<Commander>,
    input: CommandInput
  ): List<String> = Bukkit.getWorlds().map { it.name } + "all"
}
