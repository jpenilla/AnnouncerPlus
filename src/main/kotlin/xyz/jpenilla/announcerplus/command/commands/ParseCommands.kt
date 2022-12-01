/*
 * This file is part of AnnouncerPlus, licensed under the MIT License.
 *
 * Copyright (c) 2020-2022 Jason Penilla
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
import cloud.commandframework.bukkit.parsers.MaterialArgument
import cloud.commandframework.context.CommandContext
import net.kyori.adventure.bossbar.BossBar
import xyz.jpenilla.announcerplus.command.BaseCommand
import xyz.jpenilla.announcerplus.command.BukkitCommander
import xyz.jpenilla.announcerplus.command.Commander
import xyz.jpenilla.announcerplus.command.PlayerCommander
import xyz.jpenilla.announcerplus.command.argument.enum
import xyz.jpenilla.announcerplus.command.argument.integer
import xyz.jpenilla.announcerplus.command.argument.positiveInteger
import xyz.jpenilla.announcerplus.config.message.ToastSettings
import xyz.jpenilla.announcerplus.task.ActionBarUpdateTask
import xyz.jpenilla.announcerplus.task.BossBarUpdateTask
import xyz.jpenilla.announcerplus.task.TitleUpdateTask
import xyz.jpenilla.announcerplus.util.description
import xyz.jpenilla.announcerplus.util.miniMessage

class ParseCommands : BaseCommand() {
  override fun register() {
    commands.registerSubcommand("parse") {
      permission = "announcerplus.command.parse.chat"
      commandDescription("Parses a message and echoes it back.")
      argument(StringArgument.greedy("message"))
      handler(::executeParse)
    }
    commands.registerSubcommand("parsetoast", announcerPlus.toastTask != null) {
      permission = "announcerplus.command.parse.toast"
      senderType<PlayerCommander>()
      commandDescription("Parses a Toast style message and displays it to the command sender.")
      argument(MaterialArgument.of("icon"))
      argument(enum<ToastSettings.FrameType>("frame"))
      argument(StringArgument.quoted("header"), description("Quoted String"))
      argument(StringArgument.quoted("body"), description("Quoted String"))
      flag("enchant", arrayOf("e"))
      flag("custom-model-data", arrayOf("m")) {
        integer("value").build()
      }
      handler(::executeParseToast)
    }
    commands.registerSubcommand("parsetitle") {
      permission = "announcerplus.command.parse.title"
      senderType<PlayerCommander>()
      commandDescription("Parses a Title and Subtitle style message and displays it to the command sender.")
      argument(integer("fade_in", min = 0))
      argument(integer("stay", min = 0))
      argument(integer("fade_out", min = 0))
      argument(StringArgument.quoted("title"), description("Quoted String"))
      argument(StringArgument.quoted("subtitle"), description("Quoted String"))
      handler(::executeParseTitle)
    }
    commands.registerSubcommand("parseactionbar") {
      permission = "announcerplus.command.parse.actionbar"
      senderType<PlayerCommander>()
      commandDescription("Parses an Action Bar style message and displays it to the command sender.")
      argument(positiveInteger("seconds"))
      argument(StringArgument.greedy("text"))
      handler(::executeParseActionBar)
    }
    commands.registerSubcommand("parsebossbar") {
      permission = "announcerplus.command.parse.bossbar"
      senderType<PlayerCommander>()
      commandDescription("Parses a Boss Bar style message and displays it to the command sender.")
      argument(positiveInteger("seconds"))
      argument(enum<BossBar.Overlay>("overlay"))
      argument(enum<BossBarUpdateTask.FillMode>("fillmode"))
      argument(enum<BossBar.Color>("color"))
      argument(StringArgument.greedy("text"))
      handler(::executeParseBossBar)
    }
    commands.registerSubcommand("parseanimation") {
      permission = "announcerplus.command.parse.animation"
      senderType<PlayerCommander>()
      commandDescription("Parses a message with an animation and displays it to the command sender.")
      argument(positiveInteger("seconds"))
      argument(StringArgument.greedy("message"))
      handler(::executeParseAnimation)
    }
  }

  private fun executeParse(ctx: CommandContext<Commander>) {
    ctx.sender.sendMessage(miniMessage(configManager.parse(ctx.sender, ctx.get<String>("message"))))
  }

  private fun executeParseToast(ctx: CommandContext<Commander>) {
    val customModelData = ctx.flags().getValue<Int>("custom-model-data").orElse(-1)
    val toast = ToastSettings(ctx.get("icon"), ctx.get("frame"), ctx.get("header"), ctx.get("body"), ctx.flags().isPresent("enchant"), customModelData)
    toast.displayIfEnabled((ctx.sender as BukkitCommander.Player).player)
  }

  private fun executeParseTitle(ctx: CommandContext<Commander>) {
    TitleUpdateTask(
      (ctx.sender as BukkitCommander.Player).player,
      ctx.get("fade_in"),
      ctx.get("stay"),
      ctx.get("fade_out"),
      ctx.get("title"),
      ctx.get("subtitle")
    ).start()
  }

  private fun executeParseActionBar(ctx: CommandContext<Commander>) {
    ActionBarUpdateTask((ctx.sender as BukkitCommander.Player).player, ctx.get<Int>("seconds") * 20L, true, ctx.get("text")).start()
  }

  private fun executeParseBossBar(ctx: CommandContext<Commander>) {
    BossBarUpdateTask(
      (ctx.sender as BukkitCommander.Player).player,
      ctx.get("seconds"),
      ctx.get("overlay"),
      ctx.get("fillmode"),
      ctx.get<BossBar.Color>("color").toString(),
      ctx.get("text")
    ).start()
  }

  private fun executeParseAnimation(ctx: CommandContext<Commander>) {
    val player = (ctx.sender as BukkitCommander.Player).player
    val seconds = ctx.get<Int>("seconds")
    val message = ctx.get<String>("message")
    TitleUpdateTask(player, 0, seconds, 0, message, message).start()
    ActionBarUpdateTask(player, seconds * 20L, false, message).start()
  }
}
