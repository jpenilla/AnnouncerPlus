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
package xyz.jpenilla.announcerplus.compatibility

import me.clip.placeholderapi.PlaceholderAPI
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import org.bukkit.entity.Player
import java.util.regex.Matcher
import java.util.regex.Pattern

class PlaceholderAPIMiniMessagePreprocessor(private val miniMessage: MiniMessage) {
  fun process(player: Player, input: String): String =
    this.process(
      PlaceholderAPI.getPlaceholderPattern(),
      input
    ) { PlaceholderAPI.setPlaceholders(player, it) }

  fun process(one: Player, two: Player, input: String): String =
    this.process(
      PlaceholderAPI.getPlaceholderPattern(),
      input
    ) { PlaceholderAPI.setPlaceholders(one, PlaceholderAPI.setRelationalPlaceholders(one, two, it)) }

  private fun process(
    pattern: Pattern,
    input: String,
    placeholderResolver: (String) -> String,
  ): String {
    val matcher = pattern.matcher(input)
    val buffer = StringBuffer()
    while (matcher.find()) {
      val match = matcher.group()
      val replaced = placeholderResolver(match)
      if (match == replaced || !replaced.contains(LegacyComponentSerializer.SECTION_CHAR)) {
        matcher.appendReplacement(buffer, Matcher.quoteReplacement(replaced))
      } else {
        matcher.appendReplacement(buffer, Matcher.quoteReplacement(miniMessage.serialize(LegacyComponentSerializer.legacySection().deserialize(replaced))))
      }
    }
    matcher.appendTail(buffer)
    return buffer.toString()
  }
}
