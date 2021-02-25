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
package xyz.jpenilla.announcerplus.task

import org.bukkit.entity.Player
import org.koin.core.inject
import xyz.jpenilla.announcerplus.config.ConfigManager
import xyz.jpenilla.announcerplus.textanimation.AnimationHolder
import xyz.jpenilla.jmplib.Chat
import java.time.temporal.ChronoUnit

class TitleUpdateTask(
  private val player: Player,
  private val fadeIn: Int,
  private val duration: Int,
  private val fadeOut: Int,
  private val title: String,
  private val subTitle: String
) : UpdateTask() {
  private val chat: Chat by inject()
  private val configManager: ConfigManager by inject()
  private val titleAnimation = AnimationHolder(player, title)
  private val subTitleAnimation = AnimationHolder(player, subTitle)

  override fun stop() {
    super.stop()
    if (fadeOut != 0) {
      chat.showTitle(
        player,
        chat.getTitleSeconds(configManager.parse(player, titleAnimation.parseNext(title)), subTitleAnimation.parseNext(subTitle), 0, 0, fadeOut)
      )
    }
  }

  override fun update() {
    if (fadeIn == 0) {
      chat.showTitle(
        player,
        chat.getTitle(configManager.parse(player, titleAnimation.parseNext(title)), subTitleAnimation.parseNext(subTitle), ChronoUnit.SECONDS, 0, ChronoUnit.MILLIS, 200, ChronoUnit.SECONDS, 0)
      )
    } else {
      if (ticksLived == 0L) {
        chat.showTitle(
          player,
          chat.getTitle(configManager.parse(player, titleAnimation.parseNext(title)), subTitleAnimation.parseNext(subTitle), ChronoUnit.SECONDS, fadeIn, ChronoUnit.MILLIS, 200, ChronoUnit.SECONDS, 0)
        )
      } else if (ticksLived > fadeIn * 20L) {
        chat.showTitle(
          player,
          chat.getTitle(configManager.parse(player, titleAnimation.parseNext(title)), subTitleAnimation.parseNext(subTitle), ChronoUnit.SECONDS, 0, ChronoUnit.MILLIS, 200, ChronoUnit.SECONDS, 0)
        )
      }
    }
  }

  override fun shouldContinue(): Boolean {
    return ticksLived < 20L * (fadeIn + duration) && player.isOnline
  }

  override fun synchronizationContext() = SynchronizationContext.ASYNC
}
