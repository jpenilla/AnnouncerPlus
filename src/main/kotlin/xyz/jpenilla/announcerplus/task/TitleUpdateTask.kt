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
package xyz.jpenilla.announcerplus.task

import net.kyori.adventure.platform.bukkit.BukkitAudiences
import net.kyori.adventure.text.Component
import net.kyori.adventure.title.Title
import net.kyori.adventure.title.Title.title
import org.bukkit.entity.Player
import org.koin.core.component.get
import org.koin.core.component.inject
import xyz.jpenilla.announcerplus.config.ConfigManager
import xyz.jpenilla.announcerplus.textanimation.AnimationHolder
import xyz.jpenilla.announcerplus.util.miniMessage
import java.time.Duration
import java.time.Duration.ZERO

class TitleUpdateTask(
  private val player: Player,
  private val fadeIn: Int,
  private val duration: Int,
  private val fadeOut: Int,
  private val title: String,
  private val subtitle: String
) : UpdateTask() {
  private val audience = get<BukkitAudiences>().player(player)
  private val configManager: ConfigManager by inject()
  private val titleAnimation = AnimationHolder.create(player, title)
  private val subTitleAnimation = AnimationHolder.create(player, subtitle)

  private fun times(
    fadeIn: Duration = ZERO,
    stay: Duration = ZERO,
    fadeOut: Duration = ZERO
  ): Title.Times =
    Title.Times.times(fadeIn, stay, fadeOut)

  private fun title(): Component =
    miniMessage(configManager.parse(player, titleAnimation.parseNext(title)))

  private fun subtitle(): Component =
    miniMessage(configManager.parse(player, subTitleAnimation.parseNext(subtitle)))

  override fun stop() {
    super.stop()
    if (fadeOut == 0) {
      return
    }
    audience.showTitle(
      title(
        title(),
        subtitle(),
        times(
          stay = Duration.ofMillis(200L),
          fadeOut = Duration.ofSeconds(fadeOut.toLong())
        )
      )
    )
  }

  override fun update() {
    if (fadeIn == 0) {
      audience.showTitle(
        title(
          title(),
          subtitle(),
          times(stay = Duration.ofMillis(200L))
        )
      )
      return
    }
    if (ticksLived == 0L) {
      audience.showTitle(
        title(
          title(),
          subtitle(),
          times(
            fadeIn = Duration.ofSeconds(fadeIn.toLong()),
            stay = Duration.ofMillis(200L)
          )
        )
      )
    } else if (ticksLived > fadeIn * 20L) {
      audience.showTitle(
        title(
          title(),
          subtitle(),
          times(stay = Duration.ofMillis(200L))
        )
      )
    }
  }

  override fun shouldContinue(): Boolean {
    return ticksLived < 20L * (fadeIn + duration) && player.isOnline
  }

  override fun synchronizationContext() = SynchronizationContext.ASYNC
}
