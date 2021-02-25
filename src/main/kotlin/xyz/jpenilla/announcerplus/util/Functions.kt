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

import cloud.commandframework.ArgumentDescription
import org.bukkit.Bukkit
import org.bukkit.plugin.Plugin
import org.bukkit.scheduler.BukkitTask
import java.util.concurrent.CompletableFuture

fun dispatchCommandAsConsole(command: String): Boolean =
    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command)

fun Plugin.runSync(
    delay: Long = 0L,
    runnable: Runnable
): BukkitTask =
    Bukkit.getScheduler().runTaskLater(this, runnable, delay)

fun Plugin.runAsync(
    delay: Long = 0L,
    runnable: Runnable
): BukkitTask =
    Bukkit.getScheduler().runTaskLaterAsynchronously(this, runnable, delay)

fun Plugin.asyncTimer(
    delay: Long,
    interval: Long,
    runnable: Runnable
): BukkitTask =
    Bukkit.getScheduler().runTaskTimerAsynchronously(this, runnable, delay, interval)

fun Plugin.syncTimer(
    delay: Long,
    interval: Long,
    runnable: Runnable
): BukkitTask =
    Bukkit.getScheduler().runTaskTimer(this, runnable, delay, interval)

fun <T> Plugin.getOnMain(supplier: () -> T): T {
    val future = CompletableFuture<T>()
    runSync { future.complete(supplier()) }
    return future.join()
}

/**
 * Get a [ArgumentDescription], defaulting to [ArgumentDescription.empty]
 *
 * @param description description string
 * @return the description
 * @since 1.4.0
 */
fun description(
  description: String = ""
): ArgumentDescription =
  if (description.isEmpty()) ArgumentDescription.empty() else ArgumentDescription.of(description)
