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
package xyz.jpenilla.announcerplus.util

import net.kyori.adventure.audience.Audience
import net.kyori.adventure.sound.Sound
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.flattener.ComponentFlattener
import net.kyori.adventure.text.flattener.FlattenerListener
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.util.HSVLike
import org.bukkit.plugin.Plugin
import org.bukkit.scheduler.BukkitTask
import xyz.jpenilla.jmplib.ChatCentering
import java.nio.file.Path
import java.util.concurrent.CompletableFuture
import kotlin.math.max
import kotlin.math.min

val Plugin.dataPath: Path
  get() = dataFolder.toPath()

fun Plugin.runSync(
  delay: Long = 0L,
  runnable: Runnable
): BukkitTask =
  server.scheduler.runTaskLater(this, runnable, delay)

fun Plugin.runAsync(
  delay: Long = 0L,
  runnable: Runnable
): BukkitTask =
  server.scheduler.runTaskLaterAsynchronously(this, runnable, delay)

fun Plugin.asyncTimer(
  delay: Long,
  interval: Long,
  runnable: Runnable
): BukkitTask =
  server.scheduler.runTaskTimerAsynchronously(this, runnable, delay, interval)

fun Plugin.syncTimer(
  delay: Long,
  interval: Long,
  runnable: Runnable
): BukkitTask =
  server.scheduler.runTaskTimer(this, runnable, delay, interval)

fun <T> Plugin.getOnMain(supplier: () -> T): T {
  val future = CompletableFuture<T>()
  runSync { future.complete(supplier()) }
  return future.join()
}

fun Audience.playSounds(sounds: List<Sound>, randomize: Boolean) {
  if (sounds.isEmpty()) {
    return
  }

  if (randomize) {
    playSound(sounds.random())
  } else {
    for (sound in sounds) {
      playSound(sound)
    }
  }
}

/**
 * Measure the approximate length of this [Component] by flattening it and summing
 * the lengths of each string provided to the [FlattenerListener].
 *
 * @param flattener [ComponentFlattener] to use
 * @return approximate length of this [Component]
 */
fun Component.measurePlain(flattener: ComponentFlattener = ComponentFlattener.basic()): Int {
  val listener = object : FlattenerListener {
    var length: Int = 0

    override fun component(text: String) {
      length += text.length
    }
  }
  flattener.flatten(this, listener)
  return listener.length
}

fun Component.center(): Component = ofChildren(Component.text(ChatCentering.spacePrefix(this)), this)

fun TextColor.modifyHSV(
  hRatio: Float = 1f,
  sRatio: Float = 1f,
  vRatio: Float = 1f
): TextColor {
  val (h, s, v) = asHSV()
  return TextColor.color(
    HSVLike.of(
      (h * hRatio).clamp(0f, 1f),
      (s * sRatio).clamp(0f, 1f),
      (v * vRatio).clamp(0f, 1f)
    )
  )
}

fun Float.clamp(min: Float, max: Float): Float = min(max, max(this, min))

operator fun HSVLike.component1(): Float = h()
operator fun HSVLike.component2(): Float = s()
operator fun HSVLike.component3(): Float = v()
