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
package xyz.jpenilla.announcerplus.command.argument

import cloud.commandframework.arguments.CommandArgument
import cloud.commandframework.arguments.parser.ArgumentParseResult
import cloud.commandframework.arguments.parser.ArgumentParser
import cloud.commandframework.context.CommandContext
import net.kyori.adventure.text.Component
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import xyz.jpenilla.announcerplus.command.Commander
import xyz.jpenilla.announcerplus.config.ConfigManager
import xyz.jpenilla.announcerplus.config.message.MessageConfig
import xyz.jpenilla.announcerplus.util.failure
import java.util.Queue

class MessageConfigArgument(
  name: String,
  required: Boolean = true
) : CommandArgument<Commander, MessageConfig>(
  required,
  name,
  Parser(),
  MessageConfig::class.java
) {
  companion object {
    fun optional(name: String) = MessageConfigArgument(name, false)
  }

  class Parser : ArgumentParser<Commander, MessageConfig>, KoinComponent {
    private val configManager: ConfigManager by inject()

    override fun parse(
      context: CommandContext<Commander>,
      inputQueue: Queue<String>
    ): ArgumentParseResult<MessageConfig> {
      val input = inputQueue.peek()

      val config = configManager.messageConfigs[input]
        ?: return failure(Component.text("No message config with name '$input'. Known message configs: ${configManager.messageConfigs.keys.joinToString(", ")}"))

      inputQueue.remove()
      return ArgumentParseResult.success(config)
    }

    override fun suggestions(
      context: CommandContext<Commander>,
      input: String
    ): List<String> = configManager.messageConfigs.keys.toList()
  }
}
