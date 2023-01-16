/*
 * This file is part of AnnouncerPlus, licensed under the MIT License.
 *
 * Copyright (c) 2020-2023 Jason Penilla
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
package xyz.jpenilla.announcerplus.command.commands

import cloud.commandframework.CommandHelpHandler
import cloud.commandframework.arguments.standard.StringArgument
import cloud.commandframework.context.CommandContext
import cloud.commandframework.minecraft.extras.AudienceProvider
import cloud.commandframework.minecraft.extras.MinecraftHelp
import net.kyori.adventure.text.format.NamedTextColor.DARK_GRAY
import net.kyori.adventure.text.format.NamedTextColor.GRAY
import net.kyori.adventure.text.format.NamedTextColor.WHITE
import net.kyori.adventure.text.format.TextColor.color
import xyz.jpenilla.announcerplus.command.BaseCommand
import xyz.jpenilla.announcerplus.command.Commander
import xyz.jpenilla.announcerplus.util.description

class HelpCommand : BaseCommand() {
  private val help = createHelp()

  override fun register() {
    commands.registerSubcommand("help") {
      permission = "announcerplus.command.help"
      commandDescription("Shows help for AnnouncerPlus commands.")
      argument(helpQueryArgument("query"), description("Help Query"))
      handler(::execute)
    }
  }

  private fun execute(ctx: CommandContext<Commander>) {
    help.queryCommands(ctx.getOrDefault("query", "")!!, ctx.sender)
  }

  private fun createHelp(): MinecraftHelp<Commander> {
    val minecraftHelp = MinecraftHelp(
      "/announcerplus help",
      AudienceProvider.nativeAudience(),
      commands.commandManager
    )

    minecraftHelp.helpColors = MinecraftHelp.HelpColors.of(
      color(0x00a3ff),
      WHITE,
      color(0x284fff),
      GRAY,
      DARK_GRAY
    )

    minecraftHelp.setMessage(MinecraftHelp.MESSAGE_HELP_TITLE, "AnnouncerPlus Help")

    return minecraftHelp
  }

  private fun helpQueryArgument(name: String) =
    StringArgument.builder<Commander>(name)
      .greedy()
      .asOptional()
      .withSuggestionsProvider(::suggestHelpQueries)
      .build()

  private fun suggestHelpQueries(context: CommandContext<Commander>, input: String): List<String> {
    val helpTopic = commands.commandManager.createCommandHelpHandler()
      .queryHelp(context.sender, "") as CommandHelpHandler.IndexHelpTopic<Commander>

    return helpTopic.entries.map { it.syntaxString }
  }
}
