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
package xyz.jpenilla.announcerplus.util

import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import java.util.logging.Logger

class LegacyChecker(private val logger: Logger) {
  private companion object {
    private const val DISABLE_LEGACY_CHECK_PROPERTY_NAME = "AnnouncerPlus.disableLegacyCheck"
    private const val SECTION_SYMBOL_STRING = LegacyComponentSerializer.SECTION_CHAR.toString()
    private const val EMPTY_STRING = ""
  }

  private val legacyCheck = run {
    val disableCheck = java.lang.Boolean.getBoolean(DISABLE_LEGACY_CHECK_PROPERTY_NAME)
    if (disableCheck) {
      logger.warning("System property '$DISABLE_LEGACY_CHECK_PROPERTY_NAME' has been set to 'true'. This disables safeguards against unsupported behavior, and as such, you forfeit any support by having this set.")
    }
    !disableCheck
  }

  fun check(message: String): String {
    if (legacyCheck && message.contains(LegacyComponentSerializer.SECTION_CHAR)) {
      logger.warning("Legacy color codes have been detected in a message. This is not supported behavior. Message: '${message.replace(LegacyComponentSerializer.SECTION_CHAR, LegacyComponentSerializer.AMPERSAND_CHAR)}'") // todo: fix paper logging so we don't need to replace here
      return message.replace(SECTION_SYMBOL_STRING, EMPTY_STRING)
    }
    return message
  }
}
