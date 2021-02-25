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
package xyz.jpenilla.announcerplus.textanimation

import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor
import org.bukkit.entity.Player
import org.koin.core.KoinComponent
import org.koin.core.inject
import xyz.jpenilla.announcerplus.config.ConfigManager
import xyz.jpenilla.announcerplus.textanimation.animation.FlashingText
import xyz.jpenilla.announcerplus.textanimation.animation.PulsingColor
import xyz.jpenilla.announcerplus.textanimation.animation.RandomColor
import xyz.jpenilla.announcerplus.textanimation.animation.ScrollingGradient
import xyz.jpenilla.announcerplus.textanimation.animation.ScrollingText
import xyz.jpenilla.announcerplus.textanimation.animation.Typewriter

class AnimationHolder(private val player: Player?, private val message: String) : KoinComponent {
  companion object {
    private val pattern = "\\{animate:/?([a-z][^}]*)/?}?".toPattern()
  }

  private val configManager: ConfigManager by inject()
  private val matcher = pattern.matcher(message)
  private val animations = HashMap<String, TextAnimation>()

  init {
    while (matcher.find()) {
      val tokens = matcher.group(1).split(":")

      when (tokens[0]) {
        "scroll" -> {
          val speed = try {
            tokens[1].toFloat()
          } catch (e: Exception) {
            0.1f
          }
          animations[matcher.group()] = ScrollingGradient(speed)
        }

        "flash" -> {
          val colors = ArrayList(tokens.subList(1, tokens.size))
          var ticks: Int
          try {
            ticks = colors.last().toInt()
            colors.removeAt(colors.lastIndex)
          } catch (e: Exception) {
            ticks = 10
          }
          animations[matcher.group()] = FlashingText(colors, ticks)
        }

        "pulse" -> {
          val colors = ArrayList(tokens.subList(1, tokens.size))
          var ticks: Int
          try {
            ticks = colors.last().toInt()
            colors.removeAt(colors.lastIndex)
          } catch (e: Exception) {
            ticks = 10
          }
          val textColors = colors.map { color ->
            NamedTextColor.NAMES.value(color)
              ?: TextColor.fromHexString(color)
              ?: NamedTextColor.WHITE
          }
          animations[matcher.group()] = PulsingColor(textColors, ticks)
        }

        "type" -> {
          val text = tokens[1]
          val ticks = try {
            tokens[2].toInt()
          } catch (e: Exception) {
            6
          }
          animations[matcher.group()] = Typewriter(player, text, ticks)
        }

        "randomcolor" -> {
          val type = try {
            RandomColor.Type.of(tokens[1])
          } catch (e: Exception) {
            RandomColor.Type.PULSE
          }
          val ticks = try {
            tokens[2].toInt()
          } catch (e: Exception) {
            10
          }
          animations[matcher.group()] = RandomColor(type, ticks)
        }

        "scrolltext" -> {
          val text = tokens[1]
          val window = try {
            tokens[2].toInt()
          } catch (e: Exception) {
            10
          }
          val ticks = try {
            tokens[3].toInt()
          } catch (e: Exception) {
            4
          }
          animations[matcher.group()] = ScrollingText(player, text, window, ticks)
        }
      }
    }
  }

  fun parseNext(text: String = message): String {
    var msg = text
    animations.forEach { animation ->
      msg = msg.replace(animation.key, animation.value.nextValue())
    }
    return configManager.parse(player, msg)
  }

  fun parseCurrent(text: String = message): String {
    var msg = text
    animations.forEach { animation ->
      msg = msg.replace(animation.key, animation.value.getValue())
    }
    return configManager.parse(player, msg)
  }
}
