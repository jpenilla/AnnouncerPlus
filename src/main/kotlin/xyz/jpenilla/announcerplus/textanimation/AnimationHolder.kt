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
package xyz.jpenilla.announcerplus.textanimation

import org.bukkit.command.CommandSender
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import xyz.jpenilla.announcerplus.command.BukkitCommander
import xyz.jpenilla.announcerplus.command.Commander
import xyz.jpenilla.announcerplus.config.ConfigManager

class AnimationHolder(
  private val message: String,
  private val stringProcessor: (String) -> String
) : KoinComponent {
  companion object : KoinComponent {
    fun create(sender: Commander?, message: String): AnimationHolder =
      create((sender as BukkitCommander?)?.commandSender, message)

    fun create(sender: CommandSender?, message: String): AnimationHolder {
      val configManager: ConfigManager = get()
      return AnimationHolder(message) { configManager.parse(sender, it) }
    }

    private val pattern = "\\{animate:/?([a-z][^}]*)/?}?".toPattern()
  }

  private val animations: Map<String, TextAnimation> = findAnimations()

  private fun findAnimations(): Map<String, TextAnimation> {
    val map: MutableMap<String, TextAnimation> = HashMap()

    val matcher = pattern.matcher(message)
    while (matcher.find()) {
      val split = matcher.group(1).split(":")
      val typeName = split.getOrNull(0)
        ?: continue
      val tokens = split.subList(1, split.size).toMutableList()
      val animation = TextAnimation.types[typeName.lowercase()]?.create(stringProcessor, tokens)
        ?: continue
      map[matcher.group()] = animation
    }

    return map
  }

  fun parseNext(text: String = message): String {
    var msg = text
    animations.forEach { animation ->
      msg = msg.replace(animation.key, animation.value.nextValue())
    }
    return stringProcessor(msg)
  }

  fun parseCurrent(text: String = message): String {
    var msg = text
    animations.forEach { animation ->
      msg = msg.replace(animation.key, animation.value.getValue())
    }
    return stringProcessor(msg)
  }
}
