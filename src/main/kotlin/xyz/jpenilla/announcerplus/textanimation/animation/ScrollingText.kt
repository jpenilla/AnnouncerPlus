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

import xyz.jpenilla.announcerplus.textanimation.TextAnimation

class ScrollingText(
  private val stringProcessor: (String) -> String,
  text: String,
  private val windowSize: Int,
  private val ticks: Int
) : TextAnimation {
  companion object : TextAnimation.Factory {
    override fun create(stringProcessor: (String) -> String, tokens: MutableList<String>): TextAnimation {
      val text = tokens[0]
      val window = try {
        tokens[1].toInt()
      } catch (e: Exception) {
        10
      }
      val ticks = try {
        tokens[2].toInt()
      } catch (e: Exception) {
        4
      }
      return ScrollingText(stringProcessor, text, window, ticks)
    }
  }

  private val spaces = getSpaces(windowSize)
  private val text = "$spaces$text$spaces"
  private var index = 0
  private var ticksLived = 0

  private fun getSpaces(amount: Int): String {
    val sb = StringBuilder()
    for (i in 1..amount) {
      sb.append(" ")
    }
    return sb.toString()
  }

  override fun getValue(): String {
    return try {
      stringProcessor(text).substring(index, index + windowSize)
    } catch (e: Exception) {
      // if the placeholders changed in a way that causes us to out of bounds
      index = 0
      stringProcessor(text).substring(index, index + windowSize)
    }
  }

  override fun nextValue(): String {
    ticksLived++
    if (ticksLived % ticks == 0) {
      index++
      if (index > stringProcessor(text).length - windowSize) {
        index = 0
      }
    }
    return getValue()
  }
}
