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

import cloud.commandframework.arguments.standard.EnumArgument
import cloud.commandframework.arguments.standard.StringArgument
import cloud.commandframework.bukkit.parsers.MaterialArgument
import cloud.commandframework.context.CommandContext
import net.kyori.adventure.bossbar.BossBar
import net.kyori.adventure.platform.bukkit.BukkitAudiences
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.koin.core.inject
import xyz.jpenilla.announcerplus.AnnouncerPlus
import xyz.jpenilla.announcerplus.config.ConfigManager
import xyz.jpenilla.announcerplus.config.message.ToastSettings
import xyz.jpenilla.announcerplus.task.ActionBarUpdateTask
import xyz.jpenilla.announcerplus.task.BossBarUpdateTask
import xyz.jpenilla.announcerplus.task.TitleUpdateTask
import xyz.jpenilla.announcerplus.util.description
import xyz.jpenilla.announcerplus.util.miniMessage

class CommandParse : BaseCommand {
  private val commandManager: CommandManager by inject()
  private val configManager: ConfigManager by inject()
  private val argumentFactory: ArgumentFactory by inject()
  private val announcerPlus: AnnouncerPlus by inject()
  private val audiences: BukkitAudiences by inject()

  override fun register() {
    commandManager.registerSubcommand("parse") {
      permission = "announcerplus.command.parse.chat"
      commandDescription("Parses a message and echoes it back.")
      argument(StringArgument.greedy("message"))
      handler(::executeParse)
    }
    commandManager.registerSubcommand("parsetoast", announcerPlus.toastTask != null) {
      permission = "announcerplus.command.parse.toast"
      senderType<Player>()
      commandDescription("Parses a Toast style message and displays it to the command sender.")
      argument(MaterialArgument.of("icon"))
      argument(EnumArgument.of(ToastSettings.FrameType::class.java, "frame"))
      argument(StringArgument.quoted("header"), description("Quoted String"))
      argument(StringArgument.quoted("body"), description("Quoted String"))
      handler(::executeParseToast)
    }
    commandManager.registerSubcommand("parsetitle") {
      permission = "announcerplus.command.parse.title"
      senderType<Player>()
      commandDescription("Parses a Title and Subtitle style message and displays it to the command sender.")
      argument(argumentFactory.integer("fade_in", min = 0))
      argument(argumentFactory.integer("stay", min = 0))
      argument(argumentFactory.integer("fade_out", min = 0))
      argument(StringArgument.quoted("title"), description("Quoted String"))
      argument(StringArgument.quoted("subtitle"), description("Quoted String"))
      handler(::executeParseTitle)
    }
    commandManager.registerSubcommand("parseactionbar") {
      permission = "announcerplus.command.parse.actionbar"
      senderType<Player>()
      commandDescription("Parses an Action Bar style message and displays it to the command sender.")
      argument(argumentFactory.positiveInteger("seconds"))
      argument(StringArgument.greedy("text"))
      handler(::executeParseActionBar)
    }
    commandManager.registerSubcommand("parsebossbar") {
      permission = "announcerplus.command.parse.bossbar"
      senderType<Player>()
      commandDescription("Parses a Boss Bar style message and displays it to the command sender.")
      argument(argumentFactory.positiveInteger("seconds"))
      argument(EnumArgument.of(BossBar.Overlay::class.java, "overlay"))
      argument(EnumArgument.of(BossBarUpdateTask.FillMode::class.java, "fillmode"))
      argument(EnumArgument.of(BossBar.Color::class.java, "color"))
      argument(StringArgument.greedy("text"))
      handler(::executeParseBossBar)
    }
    commandManager.registerSubcommand("parseanimation") {
      permission = "announcerplus.command.parse.animation"
      senderType<Player>()
      commandDescription("Parses a message with an animation and displays it to the command sender.")
      argument(argumentFactory.positiveInteger("seconds"))
      argument(StringArgument.greedy("message"))
      handler(::executeParseAnimation)
    }
  }

  private fun executeParse(ctx: CommandContext<CommandSender>) {
    val audience = audiences.sender(ctx.sender)
    audience.sendMessage(miniMessage(configManager.parse(ctx.sender, ctx.get<String>("message"))))
  }

  private fun executeParseToast(ctx: CommandContext<CommandSender>) {
    val toast = ToastSettings(ctx.get("icon"), ctx.get("frame"), ctx.get("header"), ctx.get("body"))
    toast.displayIfEnabled(ctx.sender as Player)
  }

  private fun executeParseTitle(ctx: CommandContext<CommandSender>) {
    TitleUpdateTask(
      ctx.sender as Player,
      ctx.get("fade_in"),
      ctx.get("stay"),
      ctx.get("fade_out"),
      ctx.get("title"),
      ctx.get("subtitle")
    ).start()
  }

  private fun executeParseActionBar(ctx: CommandContext<CommandSender>) {
    ActionBarUpdateTask(ctx.sender as Player, ctx.get<Int>("seconds") * 20L, true, ctx.get("text")).start()
  }

  private fun executeParseBossBar(ctx: CommandContext<CommandSender>) {
    BossBarUpdateTask(
      ctx.sender as Player,
      ctx.get("seconds"),
      ctx.get("overlay"),
      ctx.get("fillmode"),
      ctx.get<BossBar.Color>("color").toString(),
      ctx.get("text")
    ).start()
  }

  private fun executeParseAnimation(ctx: CommandContext<CommandSender>) {
    val player = ctx.sender as Player
    val seconds = ctx.get<Int>("seconds")
    val message = ctx.get<String>("message")
    TitleUpdateTask(player, 0, seconds, 0, message, message).start()
    ActionBarUpdateTask(player, seconds * 20L, false, message).start()
  }
}
