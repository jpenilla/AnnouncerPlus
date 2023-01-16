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

import xyz.jpenilla.announcerplus.textanimation.animation.FlashingText
import xyz.jpenilla.announcerplus.textanimation.animation.PulsingColor
import xyz.jpenilla.announcerplus.textanimation.animation.RandomColor
import xyz.jpenilla.announcerplus.textanimation.animation.ScrollingGradient
import xyz.jpenilla.announcerplus.textanimation.animation.ScrollingText
import xyz.jpenilla.announcerplus.textanimation.animation.Typewriter

interface TextAnimation {
  fun getValue(): String
  fun nextValue(): String

  fun interface Factory {
    fun create(
      stringProcessor: (String) -> String,
      tokens: MutableList<String>
    ): TextAnimation
  }

  companion object {
    val types: Map<String, Factory> = buildMap {
      put("scroll", ScrollingGradient)
      put("flash", FlashingText)
      put("pulse", PulsingColor)
      put("type", Typewriter)
      put("randomcolor", RandomColor)
      put("scrolltext", ScrollingText)
    }
  }
}
