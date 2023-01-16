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

import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor
import xyz.jpenilla.announcerplus.textanimation.TextAnimation
import kotlin.math.roundToInt

class PulsingColor(colors: List<TextColor>, ticks: Int) : TextAnimation {
  companion object : TextAnimation.Factory {
    override fun create(stringProcessor: (String) -> String, tokens: MutableList<String>): TextAnimation {
      var ticks: Int
      try {
        ticks = tokens.last().toInt()
        tokens.removeAt(tokens.lastIndex)
      } catch (e: Exception) {
        ticks = 10
      }
      return PulsingColor(tokens.map(::decodeColor), ticks)
    }

    private fun decodeColor(string: String): TextColor =
      NamedTextColor.NAMES.value(string)
        ?: TextColor.fromHexString(string)
        ?: NamedTextColor.WHITE
  }

  private var index = 0
  private var colorIndex = 0
  private val colors = ArrayList(colors)

  init {
    this.colors.add(this.colors[0])
  }

  private var color = this.colors[0].asHexString()
  private val factorStep = 1.0f / ticks

  override fun getValue(): String {
    return color
  }

  override fun nextValue(): String {
    color = nextColor().asHexString()
    return getValue()
  }

  private fun interpolate(color1: TextColor, color2: TextColor, factor: Float): TextColor {
    return TextColor.color(
      (color1.red() + factor * (color2.red() - color1.red())).roundToInt(),
      (color1.green() + factor * (color2.green() - color1.green())).roundToInt(),
      (color1.blue() + factor * (color2.blue() - color1.blue())).roundToInt()
    )
  }

  private fun nextColor(): TextColor {
    if (factorStep * index > 1) {
      colorIndex++
      index = 0
    }

    val factor = factorStep * index++

    if (colorIndex + 1 > colors.lastIndex) {
      colorIndex = 0
    }

    return interpolate(colors[colorIndex], colors[colorIndex + 1], factor)
  }
}
