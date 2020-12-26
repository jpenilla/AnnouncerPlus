package xyz.jpenilla.announcerplus.util

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

fun Plugin.asyncTimer(delay: Long, interval: Long, runnable: Runnable): BukkitTask =
    Bukkit.getScheduler().runTaskTimerAsynchronously(this, runnable, delay, interval)

fun Plugin.syncTimer(delay: Long, interval: Long, runnable: Runnable): BukkitTask =
    Bukkit.getScheduler().runTaskTimer(this, runnable, delay, interval)

fun <T> Plugin.getOnMain(supplier: () -> T): T {
    val future = CompletableFuture<T>()
    runSync { future.complete(supplier()) }
    return future.join()
}