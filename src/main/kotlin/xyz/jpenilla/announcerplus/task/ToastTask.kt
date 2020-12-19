package xyz.jpenilla.announcerplus.task

import com.okkero.skedule.SynchronizationContext
import com.okkero.skedule.schedule
import org.bukkit.Bukkit
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.koin.core.KoinComponent
import org.koin.core.inject
import xyz.jpenilla.announcerplus.AnnouncerPlus
import xyz.jpenilla.announcerplus.config.message.ToastSettings
import java.util.concurrent.ConcurrentLinkedDeque
import java.util.logging.Level
import kotlin.random.Random.Default.nextInt

class ToastTask: KoinComponent {
    private val announcerPlus: AnnouncerPlus by inject()
    private val queuedToasts = ConcurrentLinkedDeque<QueuedToast>()

    private val toastTask = announcerPlus.schedule(SynchronizationContext.ASYNC) {
        repeating(1L)
        while (true) {
            if (queuedToasts.isNotEmpty()) {
                val toast = queuedToasts.removeFirst()
                if (toast.player.isOnline) {
                    displayToastImmediately(toast)
                }
            }
            yield()
        }
    }

    @Suppress("deprecation")
    private fun displayToastImmediately(toast: QueuedToast) {
        announcerPlus.schedule {
            val key = NamespacedKey(announcerPlus, "announcerPlus${nextInt(1000000)}")
            try {
                Bukkit.getUnsafe().loadAdvancement(key, toast.toast.getJson(toast.player))
            } catch (e: Exception) {
                announcerPlus.logger.log(Level.WARNING, "Failed to load advancement $toast", e)
            }
            val advancement = Bukkit.getAdvancement(key)
            val progress = toast.player.getAdvancementProgress(advancement!!)
            if (!progress.isDone) {
                progress.remainingCriteria.forEach { progress.awardCriteria(it) }
            }
            waitFor(20L)
            if (progress.isDone) {
                progress.awardedCriteria.forEach { progress.revokeCriteria(it) }
            }
            Bukkit.getUnsafe().removeAdvancement(key)
        }
    }

    fun queueToast(toastSettings: ToastSettings, player: Player) {
        queuedToasts.add(QueuedToast(player, toastSettings))
    }

    fun cancel() {
        toastTask.cancel()
    }

    data class QueuedToast(val player: Player, val toast: ToastSettings)
}