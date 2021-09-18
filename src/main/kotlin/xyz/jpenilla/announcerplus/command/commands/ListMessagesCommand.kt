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
package xyz.jpenilla.announcerplus.command.commands

import cloud.commandframework.context.CommandContext
import net.kyori.adventure.extra.kotlin.style
import net.kyori.adventure.extra.kotlin.text
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.space
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.event.ClickEvent.copyToClipboard
import net.kyori.adventure.text.feature.pagination.Pagination
import net.kyori.adventure.text.feature.pagination.Pagination.Renderer
import net.kyori.adventure.text.format.NamedTextColor.GRAY
import net.kyori.adventure.text.format.NamedTextColor.GREEN
import net.kyori.adventure.text.format.NamedTextColor.RED
import net.kyori.adventure.text.format.NamedTextColor.WHITE
import net.kyori.adventure.text.format.Style
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration.BOLD
import net.kyori.adventure.text.format.TextDecoration.ITALIC
import net.kyori.adventure.text.format.TextDecoration.STRIKETHROUGH
import org.koin.core.inject
import xyz.jpenilla.announcerplus.command.BaseCommand
import xyz.jpenilla.announcerplus.command.Commander
import xyz.jpenilla.announcerplus.command.Commands
import xyz.jpenilla.announcerplus.command.argument.MessageConfigArgument
import xyz.jpenilla.announcerplus.command.argument.positiveInteger
import xyz.jpenilla.announcerplus.config.ConfigManager
import xyz.jpenilla.announcerplus.config.message.MessageConfig
import xyz.jpenilla.announcerplus.util.miniMessage
import xyz.jpenilla.announcerplus.util.ofChildren
import xyz.jpenilla.announcerplus.util.randomColor

class ListMessagesCommand : BaseCommand {
  private val commands: Commands by inject()
  private val configManager: ConfigManager by inject()

  override fun register() {
    commands.registerSubcommand("list") {
      permission = "announcerplus.command.list"
      commandDescription("Displays the chat messages of a message config to the command sender.")
      argument(MessageConfigArgument("config"))
      argument(positiveInteger("page").asOptional())
      handler(::execute)
    }
  }

  private fun execute(ctx: CommandContext<Commander>) {
    val audience = ctx.sender
    val color = randomColor()
    val config = ctx.get<MessageConfig>("config")
    val page = ctx.getOrDefault("page", 1) ?: 1

    val header = text {
      content("Config")
      append(text(":", GRAY))
      append(space())
      append(text(config.name, color))
      append(space())
      append(text("(", WHITE))
      append(
        text {
          content("announcerplus.messages.${config.name}")
          color(GRAY)
          hoverEvent(text("Click to copy", WHITE, ITALIC))
          clickEvent(copyToClipboard("announcerplus.messages.${config.name}"))
        }
      )
      append(text(")", WHITE))
    }
    audience.sendMessage(header)

    val messages = arrayListOf<Component>()
    for (msg in config.messages) {
      for (line in msg.messageText) {
        messages.add(
          text {
            if (msg.messageText.indexOf(line) != 0) {
              append(text("  "))
            }
            append(text(" - ", color))
            append(text('"', WHITE))
            append(miniMessage(configManager.parse(ctx.sender, line)))
            append(text('"', WHITE))
          }
        )
      }
    }
    buildPagination(color, config)
      .render(messages, page)
      .forEach(audience::sendMessage)
  }

  private fun buildPagination(color: TextColor, config: MessageConfig): Pagination<Component> = Pagination.builder()
    .resultsPerPage(17)
    .width(53)
    .line { characterAndStyle ->
      characterAndStyle.character('-')
      characterAndStyle.style(
        style {
          color(color)
          decorate(STRIKETHROUGH)
        }
      )
    }
    .renderer(object : Renderer {
      private fun renderButton(character: Char, style: Style, clickEvent: ClickEvent): Component = ofChildren(
        space(),
        text('[', WHITE),
        text(character, style.clickEvent(clickEvent)),
        text(']', WHITE),
        space()
      )

      override fun renderPreviousPageButton(character: Char, style: Style, clickEvent: ClickEvent): Component =
        renderButton(character, style, clickEvent)

      override fun renderNextPageButton(character: Char, style: Style, clickEvent: ClickEvent) =
        renderButton(character, style, clickEvent)
    })
    .nextButton { characterAndStyle ->
      characterAndStyle.style(
        style {
          decorate(BOLD)
          color(color)
          hoverEvent(text("Next Page", GREEN))
        }
      )
    }
    .previousButton { characterAndStyle ->
      characterAndStyle.style(
        style {
          decorate(BOLD)
          color(color)
          hoverEvent(text("Previous Page", RED))
        }
      )
    }
    .build(
      text("Messages"),
      { value, _ -> setOf(value) },
      { "/announcerplus list ${config.name} $it" }
    )
}
