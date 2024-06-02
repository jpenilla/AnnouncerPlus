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
package xyz.jpenilla.announcerplus.command.commands

import net.kyori.adventure.text.format.NamedTextColor.DARK_GRAY
import net.kyori.adventure.text.format.NamedTextColor.GRAY
import net.kyori.adventure.text.format.NamedTextColor.WHITE
import net.kyori.adventure.text.format.TextColor.color
import org.incendo.cloud.component.CommandComponent
import org.incendo.cloud.component.DefaultValue
import org.incendo.cloud.context.CommandContext
import org.incendo.cloud.context.CommandInput
import org.incendo.cloud.description.CommandDescription.commandDescription
import org.incendo.cloud.description.Description.description
import org.incendo.cloud.minecraft.extras.AudienceProvider
import org.incendo.cloud.minecraft.extras.MinecraftHelp
import org.incendo.cloud.minecraft.extras.MinecraftHelp.captionMessageProvider
import org.incendo.cloud.minecraft.extras.MinecraftHelp.helpColors
import org.incendo.cloud.minecraft.extras.caption.ComponentCaptionFormatter
import org.incendo.cloud.parser.standard.StringParser.greedyStringParser
import org.incendo.cloud.suggestion.SuggestionProvider
import xyz.jpenilla.announcerplus.command.BaseCommand
import xyz.jpenilla.announcerplus.command.Commander

class HelpCommand : BaseCommand() {
  private val help = createHelp()

  override fun register() {
    commands.registerSubcommand("help") {
      permission = "announcerplus.command.help"
      commandDescription(commandDescription("Shows help for AnnouncerPlus commands."))
      argument(helpQueryArgument("query"))
      handler(::execute)
    }
  }

  private fun execute(ctx: CommandContext<Commander>) {
    help.queryCommands(ctx.get("query"), ctx.sender())
  }

  private fun createHelp(): MinecraftHelp<Commander> = MinecraftHelp.builder<Commander>()
    .commandManager(commands.commandManager)
    .audienceProvider(AudienceProvider.nativeAudience())
    .commandPrefix("/announcerplus help")
    .messageProvider(
      captionMessageProvider(
        commands.commandManager.captionRegistry(),
        ComponentCaptionFormatter.placeholderReplacing()
      )
    )
    .colors(
      helpColors(
        color(0x00a3ff),
        WHITE,
        color(0x284fff),
        GRAY,
        DARK_GRAY
      )
    )
    .build()

  private fun helpQueryArgument(name: String) =
    CommandComponent.builder<Commander, String>(name, greedyStringParser())
      .optional()
      .defaultValue(DefaultValue.constant(""))
      .suggestionProvider(SuggestionProvider.blockingStrings(::suggestHelpQueries))
      .description(description("Help Query"))
      .build()

  private fun suggestHelpQueries(context: CommandContext<Commander>, input: CommandInput): List<String> {
    val helpTopic = commands.commandManager.createHelpHandler().queryRootIndex(context.sender())
    return helpTopic.entries().map { it.syntax() }
  }
}
