package xyz.jpenilla.announcerplus.task

import com.okkero.skedule.schedule
import org.bukkit.entity.Player
import xyz.jpenilla.announcerplus.AnnouncerPlus
import xyz.jpenilla.announcerplus.config.message.ToastSettings

class ToastTask(announcerPlus: AnnouncerPlus) {
    private val queuedToasts = ArrayDeque<Pair<Player, ToastSettings>>()

    private val toastTask = announcerPlus.schedule {
        repeating(1L)
        while (true) {
            if (queuedToasts.isNotEmpty()) {
                val toast = queuedToasts.removeFirst()
                if (toast.first.isOnline) {
                    toast.second.displayIfEnabled(announcerPlus, toast.first)
                    yield()
                }
            } else {
                yield()
            }
        }
    }

    fun queueToast(toastSettings: ToastSettings, player: Player) {
        queuedToasts.add(Pair(player, toastSettings))
    }

    fun cancel() {
        toastTask.cancel()
    }
}