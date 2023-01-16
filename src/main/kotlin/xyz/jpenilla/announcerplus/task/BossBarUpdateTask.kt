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

import net.kyori.adventure.bossbar.BossBar
import net.kyori.adventure.platform.bukkit.BukkitAudiences
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player
import org.koin.core.component.get
import org.koin.core.component.inject
import xyz.jpenilla.announcerplus.config.ConfigManager
import xyz.jpenilla.announcerplus.textanimation.AnimationHolder
import xyz.jpenilla.announcerplus.util.miniMessage
import java.util.logging.Logger

class BossBarUpdateTask(
  private val player: Player,
  private val lifeTime: Int,
  overlay: BossBar.Overlay,
  private val fillMode: FillMode,
  private val color: String,
  private val text: String
) : UpdateTask() {
  private val logger: Logger by inject()
  private val configManager: ConfigManager by inject()
  private val audience = get<BukkitAudiences>().player(player)
  private val textAnimation = AnimationHolder.create(player, text)
  private val colorAnimation = AnimationHolder.create(player, color)
  private val bar = BossBar.bossBar(Component.empty(), 0.5f, BossBar.Color.BLUE, overlay)

  override fun stop() {
    super.stop()
    audience.hideBossBar(bar)
  }

  override fun update() {
    val colorName = colorAnimation.parseNext(color)
    val color = BossBar.Color.NAMES.value(colorName.lowercase()) ?: run {
      logger.warning("Failed to parse boss bar color from '$colorName' (full string: '$color'). Falling back to BLUE. Possible colors: [${BossBar.Color.NAMES.keys().joinToString(", ")}]")
      BossBar.Color.BLUE
    }
    bar.color(color)
    bar.name(miniMessage(configManager.parse(player, textAnimation.parseNext(text))))
    when (fillMode) {
      FillMode.FILL -> bar.progress(ticksLived / (lifeTime * 20f))
      FillMode.DRAIN -> bar.progress(1f - (ticksLived / (lifeTime * 20f)))
      FillMode.FULL -> if (ticksLived == 0L) bar.progress(1f)
      FillMode.EMPTY -> if (ticksLived == 0L) bar.progress(0f)
    }
    if (ticksLived == 0L) audience.showBossBar(bar)
  }

  override fun shouldContinue(): Boolean {
    return ticksLived < lifeTime * 20L && player.isOnline
  }

  override fun synchronizationContext() = SynchronizationContext.ASYNC

  enum class FillMode {
    FILL,
    DRAIN,
    FULL,
    EMPTY
  }
}
