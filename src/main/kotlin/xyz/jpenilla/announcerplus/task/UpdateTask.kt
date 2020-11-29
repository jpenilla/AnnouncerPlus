package xyz.jpenilla.announcerplus.task

import com.okkero.skedule.CoroutineTask
import com.okkero.skedule.SynchronizationContext
import com.okkero.skedule.schedule
import org.koin.core.KoinComponent
import org.koin.core.get
import xyz.jpenilla.announcerplus.AnnouncerPlus

abstract class UpdateTask : KoinComponent {
    private var updateTask: CoroutineTask? = null
    var ticksLived = 0L

    open fun start(): UpdateTask {
        stop()
        updateTask = get<AnnouncerPlus>().schedule(synchronizationContext()) {
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

    abstract fun synchronizationContext(): SynchronizationContext
}