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

import cloud.commandframework.context.CommandContext
import cloud.commandframework.minecraft.extras.MinecraftHelp
import net.kyori.adventure.extra.kotlin.style
import net.kyori.adventure.extra.kotlin.text
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.space
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.event.ClickEvent.copyToClipboard
import net.kyori.adventure.text.event.ClickEvent.openUrl
import net.kyori.adventure.text.feature.pagination.Pagination
import net.kyori.adventure.text.format.NamedTextColor.GRAY
import net.kyori.adventure.text.format.NamedTextColor.GREEN
import net.kyori.adventure.text.format.NamedTextColor.RED
import net.kyori.adventure.text.format.NamedTextColor.WHITE
import net.kyori.adventure.text.format.Style
import net.kyori.adventure.text.format.TextDecoration.BOLD
import net.kyori.adventure.text.format.TextDecoration.ITALIC
import net.kyori.adventure.text.format.TextDecoration.STRIKETHROUGH
import net.kyori.adventure.text.serializer.plain.PlainComponentSerializer
import org.bukkit.command.CommandSender
import org.koin.core.inject
import xyz.jpenilla.announcerplus.AnnouncerPlus
import xyz.jpenilla.announcerplus.config.ConfigManager
import xyz.jpenilla.announcerplus.config.message.MessageConfig
import xyz.jpenilla.announcerplus.util.center
import xyz.jpenilla.announcerplus.util.description
import xyz.jpenilla.announcerplus.util.miniMessage
import xyz.jpenilla.announcerplus.util.modifyHSV
import xyz.jpenilla.announcerplus.util.randomColor
import xyz.jpenilla.jmplib.Chat
import java.util.Collections
import kotlin.math.roundToInt

class CommandAnnouncerPlus : BaseCommand {
  private val announcerPlus: AnnouncerPlus by inject()
  private val commandManager: CommandManager by inject()
  private val configManager: ConfigManager by inject()
  private val minecraftHelp: MinecraftHelp<CommandSender> by inject()
  private val argumentFactory: ArgumentFactory by inject()
  private val chat: Chat by inject()

  override fun register() {
    commandManager.registerSubcommand("help") {
      permission = "announcerplus.command.help"
      commandDescription("Shows help for AnnouncerPlus commands.")
      argument(description("Help Query")) {
        argumentFactory.helpQuery("query")
      }
      handler(::executeHelp)
    }
    commandManager.registerSubcommand("about") {
      permission = "announcerplus.command.about"
      commandDescription("Prints some information about AnnouncerPlus.")
      handler(::executeAbout)
    }
    commandManager.registerSubcommand("reload") {
      permission = "announcerplus.command.reload"
      commandDescription("Reloads AnnouncerPlus configs.")
      handler(::executeReload)
    }
    commandManager.registerSubcommand("list") {
      permission = "announcerplus.command.list"
      commandDescription("Displays the chat messages of a message config to the command sender.")
      argument(argumentFactory.messageConfig("config"))
      argument(argumentFactory.positiveInteger("page").asOptional())
      handler(::executeList)
    }
  }

  private fun executeHelp(ctx: CommandContext<CommandSender>) {
    minecraftHelp.queryCommands(ctx.getOrDefault("query", "")!!, ctx.sender)
  }

  private fun executeAbout(ctx: CommandContext<CommandSender>) {
    val audience = announcerPlus.audiences().sender(ctx.sender)
    val color = randomColor()
    val nameAndVersion = text {
      hoverEvent(miniMessage("<rainbow>click me!"))
      clickEvent(openUrl(announcerPlus.description.website!!))
      append(text(announcerPlus.description.name))
      append(space())
      val lightenedColor = color.modifyHSV(sRatio = 0.3f, vRatio = 2.0f)
      append(miniMessage("<gradient:$color:$lightenedColor>${announcerPlus.description.version}"))
    }
    val spaces = " ".repeat((PlainComponentSerializer.plain().serialize(nameAndVersion).length * 1.5).roundToInt())
    val header = miniMessage("<gradient:$color:white:$color><strikethrough>$spaces").center()
    sequenceOf(
      header,
      nameAndVersion.center(),
      text {
        content("By ")
        append(text("jmp", color))
      }.center(),
      header
    ).forEach(audience::sendMessage)
  }

  private fun executeReload(ctx: CommandContext<CommandSender>) {
    chat.send(
      ctx.sender,
      chat.getCenteredMessage("<italic><gradient:${randomColor()}:${randomColor()}>Reloading ${announcerPlus.name} config...")
    )
    try {
      announcerPlus.reload()
      chat.send(ctx.sender, chat.getCenteredMessage("<green>Done."))
    } catch (e: Exception) {
      chat.send(
        ctx.sender,
        "<gradient:red:gold>I'm sorry, but there was an error reloading the plugin. This is most likely due to misconfiguration. Check console for more info."
      )
      e.printStackTrace()
    }
  }

  private fun executeList(ctx: CommandContext<CommandSender>) {
    val color = randomColor()
    val config = ctx.get<MessageConfig>("config")
    val page = ctx.getOrDefault("page", 1) ?: 1
    val pagination = Pagination.builder().apply {
      resultsPerPage(17)
      width(53)
      line { characterAndStyle ->
        characterAndStyle.character('-')
        characterAndStyle.style(style {
          color(color)
          decorate(STRIKETHROUGH)
        })
      }
      renderer(object : Pagination.Renderer {
        private fun renderButton(character: Char, style: Style, clickEvent: ClickEvent): Component =
          TextComponent.ofChildren(
            space(),
            text("[", WHITE),
            text(character, style.clickEvent(clickEvent)),
            text("]", WHITE),
            space()
          )

        override fun renderPreviousPageButton(character: Char, style: Style, clickEvent: ClickEvent): Component =
          renderButton(character, style, clickEvent)

        override fun renderNextPageButton(character: Char, style: Style, clickEvent: ClickEvent) =
          renderButton(character, style, clickEvent)
      })
      nextButton { characterAndStyle ->
        characterAndStyle.style(style {
          decorate(BOLD)
          color(color)
          hoverEvent(text("Next Page", GREEN))
        })
      }
      previousButton { characterAndStyle ->
        characterAndStyle.style(style {
          decorate(BOLD)
          color(color)
          hoverEvent(text("Previous Page", RED))
        })
      }
    }.build<Component>(
      text("Messages"),
      { value, _ -> Collections.singleton(value) },
      { "/announcerplus list ${config.name} $it" }
    )

    val messages = arrayListOf<Component>()
    for (msg in config.messages) {
      for (line in msg.messageText) {
        messages.add(text {
          if (msg.messageText.indexOf(line) != 0) {
            append(text("  "))
          }
          append(text(" - ", color))
          append(text('"', WHITE))
          append(miniMessage(configManager.parse(ctx.sender, line)))
          append(text('"', WHITE))
        })
      }
    }
    val header = text {
      content("Config")
      append(text(":", GRAY))
      append(space())
      append(text(config.name, color))
      append(space())
      append(text("(", WHITE))
      append(text {
        content("announcerplus.messages.${config.name}")
        color(GRAY)
        hoverEvent(text("Click to copy", WHITE, ITALIC))
        clickEvent(copyToClipboard("announcerplus.messages.${config.name}"))
      })
      append(text(")", WHITE))
    }
    val l = arrayListOf<Component>(header)
    l.addAll(pagination.render(messages, page))
    chat.send(ctx.sender, l)
  }
}
