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
package xyz.jpenilla.announcerplus.command.commands

import cloud.commandframework.context.CommandContext
import net.kyori.adventure.extra.kotlin.text
import net.kyori.adventure.text.Component.space
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.event.ClickEvent.openUrl
import xyz.jpenilla.announcerplus.command.BaseCommand
import xyz.jpenilla.announcerplus.command.Commander
import xyz.jpenilla.announcerplus.util.center
import xyz.jpenilla.announcerplus.util.measurePlain
import xyz.jpenilla.announcerplus.util.miniMessage
import xyz.jpenilla.announcerplus.util.modifyHSV
import xyz.jpenilla.announcerplus.util.randomColor
import kotlin.math.roundToInt

class AboutCommand : BaseCommand() {
  override fun register() {
    commands.registerSubcommand("about") {
      permission = "announcerplus.command.about"
      commandDescription("Prints some information about AnnouncerPlus.")
      handler(::execute)
    }
  }

  private fun execute(ctx: CommandContext<Commander>) {
    val audience = ctx.sender
    val color = randomColor()
    val nameAndVersion = text {
      hoverEvent(miniMessage("<rainbow>click me!"))
      clickEvent(openUrl(announcerPlus.description.website!!))
      append(text(announcerPlus.description.name))
      append(space())
      val lightenedColor = color.modifyHSV(sRatio = 0.3f, vRatio = 2.0f)
      append(miniMessage("<gradient:$color:$lightenedColor>${announcerPlus.description.version}"))
    }
    val spaces = " ".repeat((nameAndVersion.measurePlain() * 1.5).roundToInt())
    val header = miniMessage("<gradient:$color:white:$color><strikethrough>$spaces").center()
    sequenceOf(
      header,
      nameAndVersion.center(),
      text {
        content("By ")
        append(text("jmp", color))
      }.center(),
      header
    ).forEach(audience::sendMessage)
  }
}
