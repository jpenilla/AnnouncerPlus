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

import cloud.commandframework.arguments.standard.StringArgument
import cloud.commandframework.bukkit.arguments.selector.MultiplePlayerSelector
import cloud.commandframework.bukkit.parsers.MaterialArgument
import cloud.commandframework.bukkit.parsers.selector.MultiplePlayerSelectorArgument
import cloud.commandframework.context.CommandContext
import net.kyori.adventure.bossbar.BossBar
import net.kyori.adventure.platform.bukkit.BukkitAudiences
import org.koin.core.inject
import xyz.jpenilla.announcerplus.AnnouncerPlus
import xyz.jpenilla.announcerplus.command.BaseCommand
import xyz.jpenilla.announcerplus.command.Commander
import xyz.jpenilla.announcerplus.command.Commands
import xyz.jpenilla.announcerplus.command.argument.enum
import xyz.jpenilla.announcerplus.command.argument.integer
import xyz.jpenilla.announcerplus.command.argument.positiveInteger
import xyz.jpenilla.announcerplus.config.ConfigManager
import xyz.jpenilla.announcerplus.config.message.ToastSettings
import xyz.jpenilla.announcerplus.task.ActionBarUpdateTask
import xyz.jpenilla.announcerplus.task.BossBarUpdateTask
import xyz.jpenilla.announcerplus.task.TitleUpdateTask
import xyz.jpenilla.announcerplus.util.description
import xyz.jpenilla.announcerplus.util.miniMessage

class SendCommands : BaseCommand {
  private val commands: Commands by inject()
  private val configManager: ConfigManager by inject()
  private val announcerPlus: AnnouncerPlus by inject()
  private val audiences: BukkitAudiences by inject()

  override fun register() {
    commands.registerSubcommand("send") {
      permission = "announcerplus.command.send.chat"
      commandDescription("Parses a message and sends it to the specified players.")
      argument(MultiplePlayerSelectorArgument.of("players"))
      argument(StringArgument.greedy("message"))
      handler(::executeSend)
    }
    commands.registerSubcommand("sendtoast", announcerPlus.toastTask != null) {
      permission = "announcerplus.command.send.toast"
      commandDescription("Parses and sends a Toast style message to the specified players.")
      argument(MultiplePlayerSelectorArgument.of("players"))
      argument(MaterialArgument.of("icon"))
      argument(enum<ToastSettings.FrameType>("frame"))
      argument(StringArgument.quoted("header"), description("Quoted String"))
      argument(StringArgument.quoted("body"), description("Quoted String"))
      flag("enchant", arrayOf("e"))
      flag("custom-model-data", arrayOf("m")) {
        integer("value").build()
      }
      handler(::executeSendToast)
    }
    commands.registerSubcommand("sendtitle") {
      permission = "announcerplus.command.send.title"
      commandDescription("Parses and sends a Title and Subtitle style message to the specified players.")
      argument(MultiplePlayerSelectorArgument.of("players"))
      argument(integer("fade_in", min = 0))
      argument(integer("stay", min = 0))
      argument(integer("fade_out", min = 0))
      argument(StringArgument.quoted("title"), description("Quoted String"))
      argument(StringArgument.quoted("subtitle"), description("Quoted String"))
      handler(::executeSendTitle)
    }
    commands.registerSubcommand("sendactionbar") {
      permission = "announcerplus.command.send.actionbar"
      commandDescription("Parses and sends an Action Bar style message to the specified players.")
      argument(MultiplePlayerSelectorArgument.of("players"))
      argument(positiveInteger("seconds"))
      argument(StringArgument.greedy("text"))
      handler(::executeSendActionBar)
    }
    commands.registerSubcommand("sendbossbar") {
      permission = "announcerplus.command.send.bossbar"
      commandDescription("Parses and sends a Boss Bar style message to the specified players.")
      argument(MultiplePlayerSelectorArgument.of("players"))
      argument(positiveInteger("seconds"))
      argument(enum<BossBar.Overlay>("overlay"))
      argument(enum<BossBarUpdateTask.FillMode>("fillmode"))
      argument(enum<BossBar.Color>("color"))
      argument(StringArgument.greedy("text"))
      handler(::executeSendBossBar)
    }
  }

  private fun executeSend(ctx: CommandContext<Commander>) {
    for (player in ctx.get<MultiplePlayerSelector>("players").players) {
      val audience = audiences.player(player)
      audience.sendMessage(miniMessage(configManager.parse(player, ctx.get<String>("message"))))
    }
  }

  private fun executeSendToast(ctx: CommandContext<Commander>) {
    val customModelData = ctx.flags().getValue<Int>("custom-model-data").orElse(-1)
    val toast = ToastSettings(ctx.get("icon"), ctx.get("frame"), ctx.get("header"), ctx.get("body"), ctx.flags().isPresent("enchant"), customModelData)
    for (player in ctx.get<MultiplePlayerSelector>("players").players) {
      toast.displayIfEnabled(player)
    }
  }

  private fun executeSendTitle(ctx: CommandContext<Commander>) {
    for (player in ctx.get<MultiplePlayerSelector>("players").players) {
      TitleUpdateTask(
        player,
        ctx.get("fade_in"),
        ctx.get("stay"),
        ctx.get("fade_out"),
        ctx.get("title"),
        ctx.get("subtitle")
      ).start()
    }
  }

  private fun executeSendActionBar(ctx: CommandContext<Commander>) {
    for (player in ctx.get<MultiplePlayerSelector>("players").players) {
      ActionBarUpdateTask(player, ctx.get<Int>("seconds") * 20L, true, ctx.get("text")).start()
    }
  }

  private fun executeSendBossBar(ctx: CommandContext<Commander>) {
    for (player in ctx.get<MultiplePlayerSelector>("players").players) {
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
