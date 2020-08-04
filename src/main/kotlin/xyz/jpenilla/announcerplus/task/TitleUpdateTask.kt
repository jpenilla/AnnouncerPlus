package xyz.jpenilla.announcerplus.task

import com.okkero.skedule.SynchronizationContext
import org.bukkit.entity.Player
import xyz.jpenilla.announcerplus.AnnouncerPlus
import xyz.jpenilla.announcerplus.textanimation.AnimationHolder
import java.time.temporal.ChronoUnit

class TitleUpdateTask(private val announcerPlus: AnnouncerPlus, private val player: Player, private val fadeIn: Int, private val duration: Int, private val fadeOut: Int, private val title: String, private val subTitle: String) : UpdateTask(announcerPlus) {
    private val titleAnimationHolder = AnimationHolder(title)
    private val subTitleAnimationHolder = AnimationHolder(subTitle)

    override fun stop() {
        super.stop()
        if (fadeOut == 0) {
            announcerPlus.chat.showTitle(player, announcerPlus.chat.getTitleSeconds("", "", 0, 0, 0))
        } else {
            announcerPlus.chat.showTitle(player, announcerPlus.chat.getTitleSeconds(announcerPlus.configManager.parse(player, titleAnimationHolder.parseNext(title)), announcerPlus.configManager.parse(player, subTitleAnimationHolder.parseNext(subTitle)), 0, 0, fadeOut))
        }
    }

    override fun update() {
        if (ticksLived == 0L && fadeIn != 0) {
            announcerPlus.chat.showTitle(player, announcerPlus.chat.getTitleSeconds(announcerPlus.configManager.parse(player, titleAnimationHolder.parseNext(title)), announcerPlus.configManager.parse(player, subTitleAnimationHolder.parseNext(subTitle)), fadeIn, 1, 0))
        } else if (ticksLived >= 20L * fadeIn) {
            announcerPlus.chat.showTitle(player, announcerPlus.chat.getTitle(announcerPlus.configManager.parse(player, titleAnimationHolder.parseNext(title)), announcerPlus.configManager.parse(player, subTitleAnimationHolder.parseNext(subTitle)), ChronoUnit.SECONDS, 0, ChronoUnit.MILLIS, 200, ChronoUnit.SECONDS, 0))
        }
    }

    override fun shouldContinue(): Boolean {
        return ticksLived < 20L * (fadeIn + duration) && player.isOnline
    }

    override fun getSynchronizationContext(): SynchronizationContext {
        return SynchronizationContext.ASYNC
    }
}