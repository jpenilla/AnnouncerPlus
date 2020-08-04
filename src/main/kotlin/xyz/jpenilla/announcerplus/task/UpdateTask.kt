package xyz.jpenilla.announcerplus.task

import com.okkero.skedule.CoroutineTask
import com.okkero.skedule.SynchronizationContext
import com.okkero.skedule.schedule
import xyz.jpenilla.announcerplus.AnnouncerPlus

abstract class UpdateTask(private val announcerPlus: AnnouncerPlus) {
    private var updateTask: CoroutineTask? = null
    var ticksLived = 0L

    open fun start(): UpdateTask {
        stop()
        updateTask = announcerPlus.schedule(getSynchronizationContext()) {
            repeating(1L)
            while (shouldContinue()) {
                update()
                ticksLived++
                yield()
            }
            stop()
        }
        return this
    }

    open fun stop() {
        updateTask?.cancel()
        updateTask = null
    }

    abstract fun update()

    abstract fun shouldContinue(): Boolean

    abstract fun getSynchronizationContext(): SynchronizationContext
}