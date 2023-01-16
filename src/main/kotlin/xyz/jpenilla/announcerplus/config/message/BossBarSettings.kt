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
package xyz.jpenilla.announcerplus.config.message

import net.kyori.adventure.bossbar.BossBar
import org.bukkit.entity.Player
import org.koin.core.component.inject
import org.spongepowered.configurate.objectmapping.ConfigSerializable
import org.spongepowered.configurate.objectmapping.meta.Comment
import xyz.jpenilla.announcerplus.task.BossBarUpdateTask
import xyz.jpenilla.announcerplus.util.DisplayTracker

@ConfigSerializable
class BossBarSettings : MessageElement {
  private val displayTracker: DisplayTracker by inject()

  @Comment("Seconds of duration for the Boss Bar to stay on screen")
  var durationSeconds = 12

  @Comment("The text for the Boss Bar. Set to \"\" (empty string) to disable. Accepts animations")
  var text = ""

  @Comment(
    "The color for the Boss Bar. For a list of colors, visit: https://papermc.io/javadocs/paper/1.17/org/bukkit/boss/BarColor.html\n" +
      "  This field technically accepts animations, although only the \"Flashing Text\" animation used with valid Boss Bar colors will actually work."
  )
  var color = "YELLOW"

  @Comment("The overlay for the Boss Bar. Possible values: PROGRESS, NOTCHED_6, NOTCHED_10, NOTCHED_12, NOTCHED_20")
  var overlay = BossBar.Overlay.PROGRESS

  @Comment("The fill mode for the Boss Bar. Possible modes: FILL, DRAIN, FULL, EMPTY")
  var fillMode = BossBarUpdateTask.FillMode.DRAIN

  constructor()
  constructor(durationSeconds: Int, color: String, text: String) {
    this.durationSeconds = durationSeconds
    this.color = color
    this.text = text
  }

  override fun isEnabled(): Boolean {
    return text != ""
  }

  override fun display(player: Player) {
    val task = BossBarUpdateTask(player, durationSeconds, overlay, fillMode, color, text)
    displayTracker.startAndTrack(player.uniqueId, task)
  }
}
