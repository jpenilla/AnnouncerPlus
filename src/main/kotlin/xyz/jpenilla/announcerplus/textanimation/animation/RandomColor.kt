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
package xyz.jpenilla.announcerplus.textanimation.animation

import net.kyori.adventure.text.format.TextColor
import xyz.jpenilla.announcerplus.textanimation.TextAnimation
import xyz.jpenilla.announcerplus.util.randomColor

class RandomColor(type: Type, ticks: Int) : TextAnimation {
  companion object : TextAnimation.Factory {
    override fun create(stringProcessor: (String) -> String, tokens: MutableList<String>): TextAnimation {
      val type = try {
        Type.of(tokens[0])
      } catch (e: Exception) {
        Type.PULSE
      }
      val ticks = try {
        tokens[1].toInt()
      } catch (e: Exception) {
        10
      }
      return RandomColor(type, ticks)
    }
  }

  private var animation = when (type) {
    Type.FLASH -> FlashingText(randomColors(128).map(TextColor::asHexString), ticks)
    Type.PULSE -> PulsingColor(randomColors(128), ticks)
  }

  override fun getValue(): String = animation.getValue()

  override fun nextValue(): String = animation.nextValue()

  private fun randomColors(amount: Int): List<TextColor> {
    val randomColors = arrayListOf<TextColor>()
    for (i in 0 until amount) {
      randomColors.add(randomColor())
    }
    return randomColors
  }

  enum class Type {
    FLASH,
    PULSE;

    companion object {
      fun of(text: String): Type {
        return valueOf(text.uppercase())
      }
    }
  }
}
