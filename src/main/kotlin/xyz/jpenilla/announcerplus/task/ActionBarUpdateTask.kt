package xyz.jpenilla.announcerplus.task

import com.okkero.skedule.SynchronizationContext
import org.bukkit.entity.Player
import xyz.jpenilla.announcerplus.AnnouncerPlus
import xyz.jpenilla.announcerplus.textanimation.AnimationHolder

class ActionBarUpdateTask(private val announcerPlus: AnnouncerPlus, private val player: Player, private val lifeTime: Long, private val shouldFade: Boolean, private val text: String) : UpdateTask(announcerPlus) {
    private val animationHolder = AnimationHolder(announcerPlus, player, text)

    override fun stop() {
        super.stop()
        if (!shouldFade) {
            announcerPlus.chat.sendActionBar(player, "")
        }
    }

    override fun update() {
        announcerPlus.chat.sendActionBar(player, animationHolder.parseNext(text))
    }

    override fun shouldContinue(): Boolean {
        return ticksLived < lifeTime && player.isOnline
    }

    override fun getSynchronizationContext(): SynchronizationContext {
        return SynchronizationContext.ASYNC
    }
}