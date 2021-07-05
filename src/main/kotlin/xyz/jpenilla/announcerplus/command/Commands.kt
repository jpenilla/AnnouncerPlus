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

import cloud.commandframework.bukkit.CloudBukkitCapabilities
import cloud.commandframework.execution.AsynchronousCommandExecutionCoordinator
import cloud.commandframework.kotlin.MutableCommandBuilder
import cloud.commandframework.kotlin.extension.commandBuilder
import cloud.commandframework.minecraft.extras.AudienceProvider.nativeAudience
import cloud.commandframework.minecraft.extras.MinecraftExceptionHandler
import cloud.commandframework.minecraft.extras.MinecraftHelp
import cloud.commandframework.paper.PaperCommandManager
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor
import org.bukkit.entity.Player
import org.koin.core.context.loadKoinModules
import org.koin.dsl.module
import xyz.jpenilla.announcerplus.AnnouncerPlus
import xyz.jpenilla.announcerplus.command.commands.AnnouncerPlusCommands
import xyz.jpenilla.announcerplus.command.commands.BroadcastCommands
import xyz.jpenilla.announcerplus.command.commands.ParseCommands
import xyz.jpenilla.announcerplus.command.commands.SendCommands
import xyz.jpenilla.announcerplus.util.Constants

class Commands(plugin: AnnouncerPlus) {
  val commandManager = PaperCommandManager(
    plugin,
    AsynchronousCommandExecutionCoordinator.newBuilder<Commander>().build(),
    { commandSender ->
      when (commandSender) {
        is Player -> BukkitPlayerCommander(commandSender, plugin.audiences().player(commandSender))
        else -> BukkitCommander(commandSender, plugin.audiences().sender(commandSender))
      }
    },
    { (it as BukkitCommander).commandSender }
  )

  private val minecraftHelp = MinecraftHelp(
    "/announcerplus help",
    nativeAudience(),
    commandManager
  ).apply {
    helpColors = MinecraftHelp.HelpColors.of(
      TextColor.color(0x00a3ff),
      NamedTextColor.WHITE,
      TextColor.color(0x284fff),
      NamedTextColor.GRAY,
      NamedTextColor.DARK_GRAY
    )
    setMessage(MinecraftHelp.MESSAGE_HELP_TITLE, "AnnouncerPlus Help")
  }

  init {
    MinecraftExceptionHandler<Commander>()
      .withDefaultHandlers()
      .withDecorator {
        TextComponent.ofChildren(Constants.CHAT_PREFIX, it)
      }
      .apply(commandManager, nativeAudience())

    if (commandManager.queryCapability(CloudBukkitCapabilities.NATIVE_BRIGADIER)) {
      commandManager.registerBrigadier()
      commandManager.brigadierManager()?.setNativeNumberSuggestions(false)
      plugin.logger.info("Successfully registered Mojang Brigadier support for commands.")
    }

    if (commandManager.queryCapability(CloudBukkitCapabilities.ASYNCHRONOUS_COMPLETION)) {
      commandManager.registerAsynchronousCompletions()
      plugin.logger.info("Successfully registered asynchronous command completion listener.")
    }

    loadKoinModules(module {
      single { this@Commands }
      single { minecraftHelp }
      single { ArgumentFactory() }
    })

    listOf(
      AnnouncerPlusCommands(),
      BroadcastCommands(),
      SendCommands(),
      ParseCommands()
    ).forEach(BaseCommand::register)
  }

  fun rootBuilder(
    lambda: MutableCommandBuilder<Commander>.() -> Unit = {}
  ) = commandManager.commandBuilder("ap", aliases = arrayOf("announcerplus", "announcer"), lambda = lambda)

  fun registerSubcommand(
    literal: String,
    lambda: MutableCommandBuilder<Commander>.() -> Unit
  ) = this.rootBuilder().literal(literal).apply(lambda).register()

  fun registerSubcommand(
    literal: String,
    registrationPredicate: Boolean,
    lambda: MutableCommandBuilder<Commander>.() -> Unit
  ) {
    if (registrationPredicate) {
      this.registerSubcommand(literal, lambda)
    }
  }
}
