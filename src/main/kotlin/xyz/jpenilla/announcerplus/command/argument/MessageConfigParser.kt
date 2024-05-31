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
package xyz.jpenilla.announcerplus.command.argument

import net.kyori.adventure.text.Component
import org.incendo.cloud.context.CommandContext
import org.incendo.cloud.context.CommandInput
import org.incendo.cloud.kotlin.extension.parserDescriptor
import org.incendo.cloud.parser.ArgumentParseResult
import org.incendo.cloud.parser.ArgumentParser
import org.incendo.cloud.parser.ParserDescriptor
import org.incendo.cloud.suggestion.BlockingSuggestionProvider
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import xyz.jpenilla.announcerplus.command.Commander
import xyz.jpenilla.announcerplus.config.ConfigManager
import xyz.jpenilla.announcerplus.config.message.MessageConfig
import xyz.jpenilla.announcerplus.util.failure

fun messageConfigParser(): ParserDescriptor<Commander, MessageConfig> {
  return parserDescriptor(MessageConfigParser())
}

class MessageConfigParser :
  ArgumentParser<Commander, MessageConfig>,
  BlockingSuggestionProvider.Strings<Commander>,
  KoinComponent {

  private val configManager: ConfigManager by inject()

  override fun parse(
    context: CommandContext<Commander>,
    inputQueue: CommandInput
  ): ArgumentParseResult<MessageConfig> {
    val input = inputQueue.readString()

    val config = configManager.messageConfigs[input]
      ?: return failure(Component.text("No message config with name '$input'. Known message configs: ${configManager.messageConfigs.keys.joinToString(", ")}"))

    return ArgumentParseResult.success(config)
  }

  override fun stringSuggestions(
    context: CommandContext<Commander>,
    input: CommandInput
  ): List<String> = configManager.messageConfigs.keys.toList()
}
