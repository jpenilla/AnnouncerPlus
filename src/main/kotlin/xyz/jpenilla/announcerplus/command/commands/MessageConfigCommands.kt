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

import cloud.commandframework.context.CommandContext
import net.kyori.adventure.extra.kotlin.style
import net.kyori.adventure.extra.kotlin.text
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.space
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.event.ClickEvent.copyToClipboard
import net.kyori.adventure.text.event.ClickEvent.runCommand
import net.kyori.adventure.text.feature.pagination.Pagination
import net.kyori.adventure.text.feature.pagination.Pagination.Renderer
import net.kyori.adventure.text.format.NamedTextColor.GOLD
import net.kyori.adventure.text.format.NamedTextColor.GRAY
import net.kyori.adventure.text.format.NamedTextColor.GREEN
import net.kyori.adventure.text.format.NamedTextColor.WHITE
import net.kyori.adventure.text.format.Style
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration.BOLD
import net.kyori.adventure.text.format.TextDecoration.ITALIC
import net.kyori.adventure.text.format.TextDecoration.STRIKETHROUGH
import xyz.jpenilla.announcerplus.command.BaseCommand
import xyz.jpenilla.announcerplus.command.Commander
import xyz.jpenilla.announcerplus.command.argument.MessageConfigArgument
import xyz.jpenilla.announcerplus.command.argument.positiveInteger
import xyz.jpenilla.announcerplus.config.message.Message
import xyz.jpenilla.announcerplus.config.message.MessageConfig
import xyz.jpenilla.announcerplus.textanimation.AnimationHolder
import xyz.jpenilla.announcerplus.util.Constants
import xyz.jpenilla.announcerplus.util.description
import xyz.jpenilla.announcerplus.util.miniMessage
import xyz.jpenilla.announcerplus.util.ofChildren
import xyz.jpenilla.announcerplus.util.randomColor

class MessageConfigCommands : BaseCommand() {
  override fun register() {
    commands.registerSubcommand("messageconfig") {
      permission = "announcerplus.command.messageconfig"
      commandDescription("Shows information about a message config.")
      argument(MessageConfigArgument("config"))
      handler(::executeInfo)
      registerCopy {
        commandDescription("Shows the messages in a message config.")
        literal("messages")
        argument(positiveInteger("message_number").asOptional(), description("Index of the message to show (1-indexed)"))
        handler(::executeMessages)
      }
    }
  }

  private fun executeInfo(ctx: CommandContext<Commander>) {
    val config = ctx.get<MessageConfig>("config")
    val color = randomColor()

    val out = mutableListOf<Component>()
    out += text {
      append(Constants.CHAT_PREFIX)
      append(header("Message config", color))
      append(text(config.name))
    }
    out += text {
      append(header("  Permission", WHITE))
      append(
        text {
          val permissionText = "announcerplus.messages.${config.name}"
          content(permissionText)
          decorate(ITALIC)
          hoverEvent(text("Click to copy permission", WHITE))
          clickEvent(copyToClipboard(permissionText))
        }
      )
    }
    out += text {
      append(header("  Message count", WHITE))
      append(
        text {
          content(config.messages.size.toString())
          decorate(ITALIC)
          hoverEvent(text("Click to list messages", WHITE))
          clickEvent(runCommand("/announcerplus messageconfig ${config.name} messages"))
        }
      )
    }

    out.forEach(ctx.sender::sendMessage)
  }

  private fun executeMessages(ctx: CommandContext<Commander>) {
    val config = ctx.get<MessageConfig>("config")
    messagesPagination(ctx.sender, randomColor(), config)
      .render(config.messages, ctx.getOrDefault("message_number", 1)!!)
      .forEach(ctx.sender::sendMessage)
  }

  private fun messagesPagination(sender: Commander, color: TextColor, config: MessageConfig): Pagination<Message> = Pagination.builder()
    .resultsPerPage(1)
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
          hoverEvent(text("Previous Page", GOLD))
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

      override fun renderNextPageButton(character: Char, style: Style, clickEvent: ClickEvent): Component =
        renderButton(character, style, clickEvent)

      override fun renderUnknownPage(page: Int, pages: Int): Component =
        text("Unknown message selected. $pages total messages.", GRAY)
    })
    .build(
      text {
        append(Constants.CHAT_PREFIX)
        append(
          text {
            content("${config.name} config messages")
            clickEvent(runCommand("/announcerplus messageconfig ${config.name}"))
            hoverEvent(text("Click for info about this message config", WHITE))
          }
        )
      },
      { value, _ -> messageComponents(value!!, sender, color) },
      { "/announcerplus messageconfig ${config.name} messages $it" }
    )

  private fun messageComponents(message: Message, sender: Commander, color: TextColor): List<Component> {
    val out = mutableListOf<Component>()
    out += chatMessages(message, color, sender)
    out += actionBar(message, color, sender)
    out += bossBar(message, color, sender)
    out += title(message, color, sender)
    out += toast(message, color, sender)
    out += sounds(message, color)
    out += commands(color, "Commands", message.commands)
    out += commands(color, "Run for each player commands", message.perPlayerCommands)
    out += commands(color, "Run as each player commands", message.asPlayerCommands)
    if (out.isEmpty()) {
      out += text("Empty message.")
    }
    return out
  }

  private fun chatMessages(message: Message, color: TextColor, sender: Commander): List<Component> {
    if (message.messageText.size <= 0) {
      return emptyList()
    }
    val out = mutableListOf<Component>()
    out += header("Chat messages", color)
    out += message.messageText.map { line ->
      text {
        append(text("  - ", GRAY))
        append(text('"', WHITE))
        append(miniMessage(configManager.parse(sender, line)))
        append(text('"', WHITE))
      }
    }
    return out
  }

  private fun actionBar(message: Message, color: TextColor, sender: Commander): List<Component> {
    if (!message.actionBar.isEnabled()) {
      return emptyList()
    }
    val out = mutableListOf<Component>()
    out += header("Action bar", color)
    out += text {
      content("  Text content")
      append(colonSpace())
      append(miniMessage(AnimationHolder.create(sender, message.actionBar.text).parseCurrent()))
    }
    out += text {
      content("  Duration")
      append(colonSpace())
      append(text("${message.actionBar.durationSeconds} seconds"))
    }
    out += text {
      content("  Fade out enabled")
      append(colonSpace())
      append(text(message.actionBar.enableFadeOut))
    }
    return out
  }

  private fun bossBar(message: Message, color: TextColor, sender: Commander): List<Component> {
    if (!message.bossBar.isEnabled()) {
      return emptyList()
    }
    val out = mutableListOf<Component>()
    out += header("Boss bar", color)
    out += text {
      content("  Text content")
      append(colonSpace())
      append(miniMessage(AnimationHolder.create(sender, message.bossBar.text).parseCurrent()))
    }
    out += text {
      content("  Duration")
      append(colonSpace())
      append(text("${message.bossBar.durationSeconds} seconds"))
    }
    out += text {
      content("  Color")
      append(colonSpace())
      append(text(message.bossBar.color))
    }
    out += text {
      content("  Overlay")
      append(colonSpace())
      append(text(message.bossBar.overlay.name))
    }
    out += text {
      content("  Fill mode")
      append(colonSpace())
      append(text(message.bossBar.fillMode.name))
    }
    return out
  }

  private fun title(message: Message, color: TextColor, sender: Commander): List<Component> {
    if (!message.title.isEnabled()) {
      return emptyList()
    }
    val out = mutableListOf<Component>()
    out += header("Title", color)
    out += text {
      content("  Title text")
      append(colonSpace())
      append(miniMessage(AnimationHolder.create(sender, message.title.title).parseCurrent()))
    }
    out += text {
      content("  Subtitle text")
      append(colonSpace())
      append(miniMessage(AnimationHolder.create(sender, message.title.subtitle).parseCurrent()))
    }
    out += text {
      content("  Fade in duration")
      append(colonSpace())
      append(text("${message.title.fadeInSeconds} seconds"))
    }
    out += text {
      content("  Stay duration")
      append(colonSpace())
      append(text("${message.title.durationSeconds} seconds"))
    }
    out += text {
      content("  Fade out duration")
      append(colonSpace())
      append(text("${message.title.fadeOutSeconds} seconds"))
    }
    return out
  }

  private fun toast(message: Message, color: TextColor, sender: Commander): List<Component> {
    if (!message.toast.isEnabled()) {
      return emptyList()
    }
    val out = mutableListOf<Component>()
    out += header("Toast", color)
    out += text {
      content("  Icon material")
      append(colonSpace())
      append(text(message.toast.icon.name))
    }
    out += text {
      content("  Icon enchanted")
      append(colonSpace())
      append(text(message.toast.iconEnchanted))
    }
    out += text {
      content("  Icon custom model data")
      append(colonSpace())
      append(text(message.toast.iconCustomModelData))
    }
    out += text {
      content("  Header text")
      append(colonSpace())
      append(miniMessage(configManager.parse(sender, message.toast.header)))
    }
    out += text {
      content("  Footer text")
      append(colonSpace())
      append(miniMessage(configManager.parse(sender, message.toast.footer)))
    }
    out += text {
      content("  Frame type")
      append(colonSpace())
      append(text(message.toast.frame.name))
    }
    return out
  }

  private fun sounds(message: Message, color: TextColor): List<Component> {
    if (message.sounds.isEmpty()) {
      return emptyList()
    }
    val out = mutableListOf<Component>()
    out += header("Sounds", color)
    out += text {
      content("  Randomized")
      append(colonSpace())
      append(text(message.soundsRandomized))
    }
    out += text {
      content("  List")
      append(colonSpace())
    }
    for (sound in message.sounds) {
      out += text().append(text("    - ", color)).append(text("key")).append(colonSpace()).append(text(sound.name().toString())).build()
      out += text().append(text("      source")).append(colonSpace()).append(text(sound.source().name)).build()
      out += text().append(text("      volume")).append(colonSpace()).append(text(sound.volume())).build()
      out += text().append(text("      pitch")).append(colonSpace()).append(text(sound.pitch())).build()
      out += text().append(text("      seed")).append(colonSpace()).append(text(if (sound.seed().isPresent) sound.seed().asLong.toString() else "random")).build()
    }
    return out
  }

  private fun commands(color: TextColor, header: String, commands: List<String>): List<Component> {
    if (commands.isEmpty()) {
      return emptyList()
    }
    val out = mutableListOf<Component>()
    out += header(header, color)
    out += commands.map { command ->
      text {
        content("  - ")
        color(GRAY)
        append(text('"', WHITE))
        append(text(command))
        append(text('"', WHITE))
      }
    }
    return out
  }

  private fun header(sectionName: String, color: TextColor): Component = text {
    content(sectionName)
    color(color)
    append(colonSpace())
  }

  private fun colonSpace(color: TextColor = GRAY): Component = text(": ", color)
}
