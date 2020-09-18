package xyz.jpenilla.announcerplus.task

import com.okkero.skedule.schedule
import org.bukkit.entity.Player
import org.koin.core.KoinComponent
import org.koin.core.get
import xyz.jpenilla.announcerplus.AnnouncerPlus
import xyz.jpenilla.announcerplus.config.message.ToastSettings

class ToastTask: KoinComponent {
    private val queuedToasts = ArrayDeque<Pair<Player, ToastSettings>>()

    private val toastTask = get<AnnouncerPlus>().schedule {
        repeating(1L)
        while (true) {
            if (queuedToasts.isNotEmpty()) {
                val toast = queuedToasts.removeFirst()
                if (toast.first.isOnline) {
                    toast.second.displayIfEnabled(toast.first)
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