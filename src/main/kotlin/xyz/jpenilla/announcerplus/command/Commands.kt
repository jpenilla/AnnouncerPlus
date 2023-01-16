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
package xyz.jpenilla.announcerplus.command

import cloud.commandframework.bukkit.CloudBukkitCapabilities
import cloud.commandframework.execution.CommandExecutionCoordinator
import cloud.commandframework.execution.FilteringCommandSuggestionProcessor
import cloud.commandframework.kotlin.MutableCommandBuilder
import cloud.commandframework.kotlin.extension.commandBuilder
import cloud.commandframework.minecraft.extras.AudienceProvider.nativeAudience
import cloud.commandframework.minecraft.extras.MinecraftExceptionHandler
import cloud.commandframework.paper.PaperCommandManager
import org.koin.core.context.loadKoinModules
import org.koin.dsl.module
import xyz.jpenilla.announcerplus.AnnouncerPlus
import xyz.jpenilla.announcerplus.command.commands.AboutCommand
import xyz.jpenilla.announcerplus.command.commands.HelpCommand
import xyz.jpenilla.announcerplus.command.commands.MessageCommands
import xyz.jpenilla.announcerplus.command.commands.MessageConfigCommands
import xyz.jpenilla.announcerplus.command.commands.ReloadCommand
import xyz.jpenilla.announcerplus.util.Constants
import xyz.jpenilla.announcerplus.util.ofChildren

class Commands(plugin: AnnouncerPlus) {
  val commandManager: PaperCommandManager<Commander> = PaperCommandManager(
    plugin,
    CommandExecutionCoordinator.simpleCoordinator(),
    { commandSender -> BukkitCommander.create(plugin.audiences(), commandSender) },
    { commander -> (commander as BukkitCommander).commandSender }
  )

  init {
    commandManager.commandSuggestionProcessor(
      FilteringCommandSuggestionProcessor(
        FilteringCommandSuggestionProcessor.Filter.contains<Commander>(true).andTrimBeforeLastSpace()
      )
    )

    MinecraftExceptionHandler<Commander>()
      .withDefaultHandlers()
      .withDecorator { ofChildren(Constants.CHAT_PREFIX, it) }
      .apply(commandManager, nativeAudience())

    if (commandManager.hasCapability(CloudBukkitCapabilities.NATIVE_BRIGADIER)) {
      commandManager.registerBrigadier()
      commandManager.brigadierManager()?.setNativeNumberSuggestions(false)
    }

    loadKoinModules(
      module {
        single { this@Commands }
      }
    )

    registerCommands()
  }

  private fun registerCommands() {
    setOf(
      AboutCommand(),
      HelpCommand(),
      MessageConfigCommands(),
      ReloadCommand(),
      MessageCommands(),
    ).forEach(RegistrableCommand::register)
  }

  private fun rootBuilder(
    lambda: MutableCommandBuilder<Commander>.() -> Unit = {}
  ) = commandManager.commandBuilder("ap", aliases = arrayOf("announcerplus", "announcer"), lambda = lambda)

  fun registerSubcommand(
    literal: String,
    registrationPredicate: Boolean = true,
    lambda: MutableCommandBuilder<Commander>.() -> Unit
  ) {
    if (registrationPredicate) {
      rootBuilder().literal(literal).apply(lambda).register()
    }
  }
}
