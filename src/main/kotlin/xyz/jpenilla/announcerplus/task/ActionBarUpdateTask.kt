package xyz.jpenilla.announcerplus.task

import com.okkero.skedule.SynchronizationContext
import org.bukkit.entity.Player
import org.koin.core.inject
import xyz.jpenilla.announcerplus.textanimation.AnimationHolder
import xyz.jpenilla.jmplib.Chat

class ActionBarUpdateTask(private val player: Player, private val lifeTime: Long, private val shouldFade: Boolean, private val text: String) : UpdateTask() {
    private val chat: Chat by inject()
    private val animationHolder = AnimationHolder(player, text)

    override fun stop() {
        super.stop()
        if (!shouldFade) {
            chat.sendActionBar(player, "")
        }
    }

    override fun update() {
        chat.sendActionBar(player, animationHolder.parseNext(text))
    }

    override fun shouldContinue(): Boolean {
        return ticksLived < lifeTime && player.isOnline
    }

    override fun synchronizationContext() = SynchronizationContext.ASYNC
}