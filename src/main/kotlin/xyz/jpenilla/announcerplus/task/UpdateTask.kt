package xyz.jpenilla.announcerplus.task

import org.bukkit.scheduler.BukkitTask
import org.koin.core.KoinComponent
import org.koin.core.inject
import xyz.jpenilla.announcerplus.AnnouncerPlus
import xyz.jpenilla.announcerplus.util.asyncTimer
import xyz.jpenilla.announcerplus.util.syncTimer

abstract class UpdateTask : KoinComponent {
    private val announcerPlus: AnnouncerPlus by inject()
    private var updateTask: BukkitTask? = null
    var ticksLived = 0L

    open fun start(): UpdateTask = apply {
        stop()
        val runnable = Runnable {
            if (!shouldContinue()) {
                stop()
            }
            update()
            ticksLived++
        }
        updateTask = when (synchronizationContext()) {
            SynchronizationContext.SYNC -> announcerPlus.syncTimer(0L, 1L, runnable)
            SynchronizationContext.ASYNC -> announcerPlus.asyncTimer(0L, 1L, runnable)
        }
    }

    open fun stop() {
        updateTask?.cancel()
        updateTask = null
    }

    abstract fun update()

    abstract fun shouldContinue(): Boolean

    abstract fun synchronizationContext(): SynchronizationContext

    enum class SynchronizationContext {
        SYNC,
        ASYNC
    }
}