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
import org.bukkit.command.CommandSender
import org.koin.core.inject
import xyz.jpenilla.announcerplus.AnnouncerPlus
import xyz.jpenilla.announcerplus.config.ConfigManager
import xyz.jpenilla.announcerplus.config.message.ToastSettings
import xyz.jpenilla.announcerplus.task.ActionBarUpdateTask
import xyz.jpenilla.announcerplus.task.BossBarUpdateTask
import xyz.jpenilla.announcerplus.task.TitleUpdateTask
import xyz.jpenilla.announcerplus.util.description
import xyz.jpenilla.jmplib.Chat

class CommandBroadcast : BaseCommand {
  private val commandManager: CommandManager by inject()
  private val configManager: ConfigManager by inject()
  private val argumentFactory: ArgumentFactory by inject()
  private val announcerPlus: AnnouncerPlus by inject()
  private val chat: Chat by inject()

  override fun register() {
    commandManager.registerSubcommand("broadcast") {
      permission = "announcerplus.command.broadcast.chat"
      commandDescription("Parses and broadcasts a message to chat in the specified world or all worlds.")
      argument(argumentFactory.worldPlayers("world"))
      argument(StringArgument.greedy("message"))
      handler(::executeBroadcast)
    }
    commandManager.registerSubcommand("broadcasttoast", announcerPlus.toastTask != null) {
      permission = "announcerplus.command.broadcast.toast"
      commandDescription("Parses and broadcasts a Toast style message to the specified world or all worlds.")
      argument(argumentFactory.worldPlayers("world"))
      argument(MaterialArgument.of("icon"))
      argument(EnumArgument.of(ToastSettings.FrameType::class.java, "frame"))
      argument(StringArgument.quoted("header"), description("Quoted String"))
      argument(StringArgument.quoted("body"), description("Quoted String"))
      handler(::executeBroadcastToast)
    }
    commandManager.registerSubcommand("broadcasttitle") {
      permission = "announcerplus.command.broadcast.title"
      commandDescription("Parses and broadcasts a Title and Subtitle style message to the specified world or all worlds.")
      argument(argumentFactory.worldPlayers("world"))
      argument(argumentFactory.positiveInteger("seconds"))
      argument(StringArgument.quoted("title"), description("Quoted String"))
      argument(StringArgument.quoted("subtitle"), description("Quoted String"))
      handler(::executeBroadcastTitle)
    }
    commandManager.registerSubcommand("broadcastactionbar") {
      permission = "announcerplus.command.broadcast.actionbar"
      commandDescription("Parses and broadcasts an Action Bar style message to the specified world or all worlds.")
      argument(argumentFactory.worldPlayers("world"))
      argument(argumentFactory.positiveInteger("seconds"))
      argument(StringArgument.greedy("text"))
      handler(::executeBroadcastActionBar)
    }
    commandManager.registerSubcommand("broadcastbossbar") {
      permission = "announcerplus.command.broadcast.bossbar"
      commandDescription("Parses and broadcasts a Boss Bar style message to the specified world or all worlds.")
      argument(argumentFactory.worldPlayers("world"))
      argument(argumentFactory.positiveInteger("seconds"))
      argument(EnumArgument.of(BossBar.Overlay::class.java, "overlay"))
      argument(EnumArgument.of(BossBarUpdateTask.FillMode::class.java, "fillmode"))
      argument(EnumArgument.of(BossBar.Color::class.java, "color"))
      argument(StringArgument.greedy("text"))
      handler(::executeBroadcastBossBar)
    }
  }

  private fun executeBroadcast(ctx: CommandContext<CommandSender>) {
    for (player in ctx.get<ArgumentFactory.WorldPlayers>("world").players) {
      chat.send(player, configManager.parse(player, ctx.get<String>("message")))
    }
  }

  private fun executeBroadcastToast(ctx: CommandContext<CommandSender>) {
    val toast = ToastSettings(ctx.get("icon"), ctx.get("frame"), ctx.get("header"), ctx.get("body"))
    for (player in ctx.get<ArgumentFactory.WorldPlayers>("world").players) {
      toast.displayIfEnabled(player)
    }
  }

  private fun executeBroadcastTitle(ctx: CommandContext<CommandSender>) {
    for (player in ctx.get<ArgumentFactory.WorldPlayers>("world").players) {
      TitleUpdateTask(player, 0, ctx.get("seconds"), 1, ctx.get("title"), ctx.get("subtitle")).start()
    }
  }

  private fun executeBroadcastActionBar(ctx: CommandContext<CommandSender>) {
    for (player in ctx.get<ArgumentFactory.WorldPlayers>("world").players) {
      ActionBarUpdateTask(player, ctx.get<Int>("seconds") * 20L, true, ctx.get("text")).start()
    }
  }

  private fun executeBroadcastBossBar(ctx: CommandContext<CommandSender>) {
    for (player in ctx.get<ArgumentFactory.WorldPlayers>("world").players) {
      BossBarUpdateTask(
        player,
        ctx.get("seconds"),
        ctx.get("overlay"),
        ctx.get("fillmode"),
        ctx.get<BossBar.Color>("color").toString(),
        ctx.get("text")
      ).start()
    }
  }
}
