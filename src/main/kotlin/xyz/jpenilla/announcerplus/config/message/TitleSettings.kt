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

import org.bukkit.entity.Player
import org.koin.core.component.inject
import org.spongepowered.configurate.objectmapping.ConfigSerializable
import org.spongepowered.configurate.objectmapping.meta.Comment
import xyz.jpenilla.announcerplus.task.TitleUpdateTask
import xyz.jpenilla.announcerplus.util.DisplayTracker

@ConfigSerializable
class TitleSettings : MessageElement {
  private val displayTracker: DisplayTracker by inject()

  constructor()
  constructor(fadeInSeconds: Int, durationSeconds: Int, fadeOutSeconds: Int, title: String, subTitle: String) {
    this.fadeInSeconds = fadeInSeconds
    this.fadeOutSeconds = fadeOutSeconds
    this.title = title
    this.subtitle = subTitle
    this.durationSeconds = durationSeconds
  }

  @Comment("Seconds of duration for the title fade-in animation")
  var fadeInSeconds = 1

  @Comment("Seconds of duration for the title to stay on screen")
  var durationSeconds = 5

  @Comment("Seconds of duration for the title fade-out animation")
  var fadeOutSeconds = 1

  @Comment("Title text. If the title and subtitle are both set to \"\" (empty string), then this title is disabled")
  var title = ""

  @Comment("Subtitle text. If the title and subtitle are both set to \"\" (empty string), then this title is disabled")
  var subtitle = ""

  override fun isEnabled(): Boolean {
    return if (fadeInSeconds == 0 && durationSeconds == 0 && fadeOutSeconds == 0) {
      false
    } else {
      title != "" || subtitle != ""
    }
  }

  override fun display(player: Player) {
    val task = TitleUpdateTask(player, fadeInSeconds, durationSeconds, fadeOutSeconds, title, subtitle)
    displayTracker.startAndTrack(player.uniqueId, task)
  }
}
