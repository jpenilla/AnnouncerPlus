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
package xyz.jpenilla.announcerplus.command

import org.incendo.cloud.SenderMapper
import org.incendo.cloud.bukkit.CloudBukkitCapabilities
import org.incendo.cloud.execution.ExecutionCoordinator
import org.incendo.cloud.kotlin.MutableCommandBuilder
import org.incendo.cloud.kotlin.extension.commandBuilder
import org.incendo.cloud.minecraft.extras.MinecraftExceptionHandler
import org.incendo.cloud.paper.LegacyPaperCommandManager
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
  val commandManager: LegacyPaperCommandManager<Commander> = LegacyPaperCommandManager(
    plugin,
    ExecutionCoordinator.simpleCoordinator(),
    SenderMapper.create(
      { commandSender -> BukkitCommander.create(plugin.audiences(), commandSender) },
      { commander -> (commander as BukkitCommander).commandSender }
    )
  )

  init {
    MinecraftExceptionHandler.createNative<Commander>()
      .defaultHandlers()
      .decorator { _, _, msg -> ofChildren(Constants.CHAT_PREFIX, msg) }
      .registerTo(commandManager)

    if (commandManager.hasCapability(CloudBukkitCapabilities.NATIVE_BRIGADIER)) {
      commandManager.registerBrigadier()
    } else if (commandManager.hasCapability(CloudBukkitCapabilities.ASYNCHRONOUS_COMPLETION)) {
      commandManager.registerAsynchronousCompletions()
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
