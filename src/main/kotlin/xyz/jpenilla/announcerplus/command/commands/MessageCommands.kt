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

import net.kyori.adventure.bossbar.BossBar
import net.kyori.adventure.platform.bukkit.BukkitAudiences
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.incendo.cloud.bukkit.data.MultiplePlayerSelector
import org.incendo.cloud.bukkit.parser.MaterialParser.materialParser
import org.incendo.cloud.bukkit.parser.selector.MultiplePlayerSelectorParser.multiplePlayerSelectorParser
import org.incendo.cloud.component.CommandComponent
import org.incendo.cloud.context.CommandContext
import org.incendo.cloud.description.CommandDescription.commandDescription
import org.incendo.cloud.description.Description
import org.incendo.cloud.description.Description.description
import org.incendo.cloud.execution.CommandExecutionHandler
import org.incendo.cloud.kotlin.MutableCommandBuilder
import org.incendo.cloud.parser.standard.IntegerParser.integerParser
import org.incendo.cloud.parser.standard.StringParser.greedyStringParser
import org.incendo.cloud.parser.standard.StringParser.quotedStringParser
import org.incendo.cloud.suggestion.SuggestionProvider
import org.koin.core.component.inject
import xyz.jpenilla.announcerplus.command.BaseCommand
import xyz.jpenilla.announcerplus.command.BukkitCommander
import xyz.jpenilla.announcerplus.command.Commander
import xyz.jpenilla.announcerplus.command.Commands
import xyz.jpenilla.announcerplus.command.argument.WorldPlayers
import xyz.jpenilla.announcerplus.command.argument.enum
import xyz.jpenilla.announcerplus.command.argument.positiveInteger
import xyz.jpenilla.announcerplus.command.argument.worldPlayersParser
import xyz.jpenilla.announcerplus.config.message.ToastSettings
import xyz.jpenilla.announcerplus.task.ActionBarUpdateTask
import xyz.jpenilla.announcerplus.task.BossBarUpdateTask
import xyz.jpenilla.announcerplus.task.TitleUpdateTask
import xyz.jpenilla.announcerplus.util.DisplayTracker
import xyz.jpenilla.announcerplus.util.miniMessage
import xyz.jpenilla.announcerplus.util.scheduleAsync
import kotlin.reflect.KClass

class MessageCommands : BaseCommand() {
  private val displayTracker: DisplayTracker by inject()
  private val audiences: BukkitAudiences by inject()

  override fun register() {
    val parse = Category(
      prefix = "parse",
      targetExtractor = TargetExtractor.Single { (it.sender() as BukkitCommander).commandSender },
      senderType = Commander.Player::class
    )
    commands.chatCommand(parse.copy(senderType = null), "Parses a chat message and echoes it back.")
    commands.toastCommand(parse, "Helps to test a toast by displaying it to yourself.")
    commands.titleCommand(parse, "Helps to test a title by displaying it to yourself.")
    commands.actionBarCommand(parse, "Helps to test an action bar by displaying it to yourself.")
    commands.bossBarCommand(parse, "Helps to test a boss bar by displaying it to yourself.")

    commands.registerSubcommand("parseanimation") {
      permission = "announcerplus.command.parse.animation"
      senderType<Commander.Player>()
      commandDescription(commandDescription("Helps to test a message with animations."))
      required("seconds", positiveInteger())
      required("message", greedyStringParser())
      handler(::executeParseAnimation)
    }

    val send = Category(
      prefix = "send",
      targetArgument = { CommandComponent.builder("players", multiplePlayerSelectorParser()) },
      targetExtractor = { it.get<MultiplePlayerSelector>("players").values() }
    )
    commands.chatCommand(send, "Broadcasts a chat message to the specified players.")
    commands.toastCommand(send, "Broadcasts a toast to the specified players.")
    commands.titleCommand(send, "Broadcasts a title to the specified players.")
    commands.actionBarCommand(send, "Broadcasts an action bar to the specified players.")
    commands.bossBarCommand(send, "Broadcasts a boss bar to the specified players.")

    val broadcast = Category(
      prefix = "broadcast",
      targetArgument = { CommandComponent.builder("world", worldPlayersParser()) },
      targetExtractor = { it.get<WorldPlayers>("world").players }
    )
    commands.chatCommand(broadcast, "Broadcasts a chat message to players in the specified world or all worlds.")
    commands.toastCommand(broadcast, "Broadcasts a toast to players in the specified world or all worlds.")
    commands.titleCommand(broadcast, "Broadcasts a title to players in the specified world or all worlds.")
    commands.actionBarCommand(broadcast, "Broadcasts an action bar to players in the specified world or all worlds.")
    commands.bossBarCommand(broadcast, "Broadcasts a boss bar to players in the specified world or all worlds.")
  }

  private fun executeParseAnimation(ctx: CommandContext<Commander>) {
    val player = (ctx.sender() as BukkitCommander.Player).player
    val seconds = ctx.get<Int>("seconds")
    val message = ctx.get<String>("message")
    val titleTask = TitleUpdateTask(player, 0, seconds, 0, message, message)
    val actionBarTask = ActionBarUpdateTask(player, seconds * 20L, false, message)
    displayTracker.startAndTrack(player.uniqueId, titleTask)
    displayTracker.startAndTrack(player.uniqueId, actionBarTask)
  }

  private fun Commands.chatCommand(category: Category, description: String): Unit = registerSubcommand(category.prefix) {
    category.applyFirst(this, "chat")
    commandDescription(description)
    required("message", greedyStringParser())
    handler(executeChat(category.targetExtractor))
  }

  private fun executeChat(targets: TargetExtractor): CommandExecutionHandler<Commander> = CommandExecutionHandler { ctx ->
    announcerPlus.scheduleAsync {
      for (target in targets.extract(ctx)) {
        audiences.sender(target).sendMessage(miniMessage(configManager.parse(target, ctx.get<String>("message"))))
      }
    }
  }

  private fun Commands.toastCommand(category: Category, description: String): Unit = registerSubcommand("${category.prefix}toast", announcerPlus.toastTask != null) {
    category.applyFirst(this, "toast")
    commandDescription(description)
    required("icon", materialParser())
    required("frame", enum<ToastSettings.FrameType>())
    required("header", quotedStringParser()) {
      description(quotedStringDescription())
    }
    required("body", quotedStringParser()) {
      description(quotedStringDescription())
    }
    flag("enchant", arrayOf("e"))
    flag("custom-model-data", arrayOf("m")) {
      withComponent(CommandComponent.builder("value", integerParser()))
    }
    handler(executeToast(category.targetExtractor))
  }

  private fun executeToast(targets: TargetExtractor): CommandExecutionHandler<Commander> = CommandExecutionHandler { ctx ->
    val customModelData = ctx.flags().getValue<Int>("custom-model-data").orElse(-1)
    val toast = ToastSettings(ctx.get("icon"), ctx.get("frame"), ctx.get("header"), ctx.get("body"), ctx.flags().isPresent("enchant"), customModelData)
    for (target in targets.extractPlayers(ctx)) {
      toast.displayIfEnabled(target)
    }
  }

  private fun Commands.titleCommand(category: Category, description: String): Unit = registerSubcommand("${category.prefix}title") {
    category.applyFirst(this, "title")
    commandDescription(description)
    required("fade_in", integerParser(0))
    required("stay", integerParser(0))
    required("fade_out", integerParser(0))
    required("title", quotedStringParser()) {
      description(quotedStringDescription())
    }
    required("subtitle", quotedStringParser()) {
      description(quotedStringDescription())
    }
    handler(executeTitle(category.targetExtractor))
  }

  private fun executeTitle(targets: TargetExtractor): CommandExecutionHandler<Commander> = CommandExecutionHandler { ctx ->
    for (target in targets.extractPlayers(ctx)) {
      val task = TitleUpdateTask(
        target,
        ctx.get("fade_in"),
        ctx.get("stay"),
        ctx.get("fade_out"),
        ctx.get("title"),
        ctx.get("subtitle")
      )
      displayTracker.startAndTrack(target.uniqueId, task)
    }
  }

  private fun Commands.actionBarCommand(category: Category, description: String): Unit = registerSubcommand("${category.prefix}actionbar") {
    category.applyFirst(this, "actionbar")
    commandDescription(description)
    required("seconds", positiveInteger())
    required("text", greedyStringParser())
    handler(executeActionBar(category.targetExtractor))
  }

  private fun executeActionBar(targets: TargetExtractor): CommandExecutionHandler<Commander> = CommandExecutionHandler { ctx ->
    for (target in targets.extractPlayers(ctx)) {
      val task = ActionBarUpdateTask(target, ctx.get<Int>("seconds") * 20L, true, ctx.get("text"))
      displayTracker.startAndTrack(target.uniqueId, task)
    }
  }

  private fun Commands.bossBarCommand(category: Category, description: String): Unit = registerSubcommand("${category.prefix}bossbar") {
    category.applyFirst(this, "bossbar")
    commandDescription(description)
    required("seconds", positiveInteger())
    required("overlay", enum<BossBar.Overlay>())
    required("fillmode", enum<BossBarUpdateTask.FillMode>())
    required("color", quotedStringParser()) {
      suggestionProvider(SuggestionProvider.suggestingStrings(BossBar.Color.NAMES.keys().toList()))
      description(quotedStringDescription())
    }
    required("text", greedyStringParser())
    handler(executeBossBar(category.targetExtractor))
  }

  private fun executeBossBar(targets: TargetExtractor): CommandExecutionHandler<Commander> = CommandExecutionHandler { ctx ->
    for (target in targets.extractPlayers(ctx)) {
      val task = BossBarUpdateTask(
        target,
        ctx.get("seconds"),
        ctx.get("overlay"),
        ctx.get("fillmode"),
        ctx.get("color"),
        ctx.get("text")
      )
      displayTracker.startAndTrack(target.uniqueId, task)
    }
  }

  private fun quotedStringDescription(): Description =
    description("Can be quoted to allow for spaces")

  data class Category(
    val prefix: String,
    val targetExtractor: TargetExtractor,
    val targetArgument: (() -> CommandComponent.Builder<Commander, *>)? = null,
    val senderType: KClass<out Commander>? = null
  ) {
    fun applyFirst(builder: MutableCommandBuilder<Commander>, type: String) {
      builder.permission = "announcerplus.command.$prefix.$type"
      targetArgument?.let { builder.argument(it().build()) }
      senderType?.let { builder.senderType(it) }
    }
  }

  fun interface TargetExtractor {
    fun extract(ctx: CommandContext<Commander>): Collection<CommandSender>

    fun extractPlayers(ctx: CommandContext<Commander>): Collection<Player> = extract(ctx).map { it as Player }

    fun interface Single : TargetExtractor {
      fun extractSingle(ctx: CommandContext<Commander>): CommandSender

      override fun extract(ctx: CommandContext<Commander>): Collection<CommandSender> = listOf(extractSingle(ctx))
    }
  }
}
