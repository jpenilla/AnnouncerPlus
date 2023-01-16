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

import xyz.jpenilla.announcerplus.task.ActionBarUpdateTask
import xyz.jpenilla.announcerplus.task.TitleUpdateTask
import xyz.jpenilla.announcerplus.task.UpdateTask
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

class DisplayTracker {
  private val activeTitles: MutableMap<UUID, TitleUpdateTask> = ConcurrentHashMap()
  private val activeActionBars: MutableMap<UUID, ActionBarUpdateTask> = ConcurrentHashMap()

  @Suppress("unchecked_cast")
  fun startAndTrack(player: UUID, task: UpdateTask) {
    val map = map(task)
    if (map == null) {
      task.start()
      return
    }
    map[player]?.stop()
    (map as MutableMap<UUID, Any>)[player] = task
    task.stopCallback = { map(it)!!.remove(player) }
    task.start()
  }

  private fun map(task: UpdateTask): MutableMap<UUID, out UpdateTask>? = when (task) {
    is ActionBarUpdateTask -> activeActionBars
    is TitleUpdateTask -> activeTitles
    else -> null
  }
}
