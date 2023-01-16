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
import net.kyori.adventure.text.Component.empty
import org.bukkit.entity.Player
import org.koin.core.component.get
import xyz.jpenilla.announcerplus.textanimation.AnimationHolder
import xyz.jpenilla.announcerplus.util.miniMessage

class ActionBarUpdateTask(
  private val player: Player,
  private val lifeTime: Long,
  private val shouldFade: Boolean,
  private val text: String
) : UpdateTask() {
  private val audience = get<BukkitAudiences>().player(player)
  private val animationHolder = AnimationHolder.create(player, text)

  override fun stop() {
    super.stop()
    if (!shouldFade) {
      audience.sendActionBar(empty())
    }
  }

  override fun update() {
    audience.sendActionBar(miniMessage(animationHolder.parseNext(text)))
  }

  override fun shouldContinue(): Boolean {
    return ticksLived < lifeTime && player.isOnline
  }

  override fun synchronizationContext() = SynchronizationContext.ASYNC
}
