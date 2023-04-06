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

import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import xyz.jpenilla.announcerplus.AnnouncerPlus
import xyz.jpenilla.announcerplus.util.TaskHandle
import xyz.jpenilla.announcerplus.util.asyncTimer

abstract class UpdateTask : KoinComponent {
  var stopCallback: (UpdateTask) -> Unit = {}
  private val announcerPlus: AnnouncerPlus by inject()
  private var updateTask: TaskHandle<*>? = null
  protected var ticksLived = 0L
    private set

  open fun start(): UpdateTask {
    if (updateTask != null) error("UpdateTask can only be started once!")
    val runnable = Runnable {
      if (!shouldContinue()) {
        stop()
        return@Runnable
      }
      update()
      ticksLived++
    }
    updateTask = when (synchronizationContext()) {
      SynchronizationContext.SYNC -> throw UnsupportedOperationException()
      SynchronizationContext.ASYNC -> announcerPlus.asyncTimer(0L, 1L, runnable)
    }
    return this
  }

  open fun stop() {
    updateTask?.cancel()
    updateTask = null
    stopCallback(this)
  }

  abstract fun update()

  abstract fun shouldContinue(): Boolean

  abstract fun synchronizationContext(): SynchronizationContext

  enum class SynchronizationContext {
    SYNC,
    ASYNC
  }
}
