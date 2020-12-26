package xyz.jpenilla.announcerplus.task

import org.bukkit.entity.Player
import org.koin.core.inject
import xyz.jpenilla.announcerplus.config.ConfigManager
import xyz.jpenilla.announcerplus.textanimation.AnimationHolder
import xyz.jpenilla.jmplib.Chat
import java.time.temporal.ChronoUnit

class TitleUpdateTask(
    private val player: Player,
    private val fadeIn: Int,
    private val duration: Int,
    private val fadeOut: Int,
    private val title: String,
    private val subTitle: String
) : UpdateTask() {
    private val chat: Chat by inject()
    private val configManager: ConfigManager by inject()
    private val titleAnimation = AnimationHolder(player, title)
    private val subTitleAnimation = AnimationHolder(player, subTitle)

    override fun stop() {
        super.stop()
        if (fadeOut == 0) {
            chat.showTitle(player, chat.getTitleSeconds("", "", 0, 0, 0))
        } else {
            chat.showTitle(player, chat.getTitleSeconds(configManager.parse(player, titleAnimation.parseNext(title)), subTitleAnimation.parseNext(subTitle), 0, 0, fadeOut))
        }
    }

    override fun update() {
        when (fadeIn) {
            0 -> chat.showTitle(player, chat.getTitle(configManager.parse(player, titleAnimation.parseNext(title)), subTitleAnimation.parseNext(subTitle), ChronoUnit.SECONDS, 0, ChronoUnit.MILLIS, 200, ChronoUnit.SECONDS, 0))
            else -> when (ticksLived) {
                0L -> chat.showTitle(player, chat.getTitle(configManager.parse(player, titleAnimation.parseNext(title)), subTitleAnimation.parseNext(subTitle), ChronoUnit.SECONDS, fadeIn, ChronoUnit.MILLIS, 200, ChronoUnit.SECONDS, 0))
                else -> if (ticksLived > fadeIn * 20L) {
                    chat.showTitle(player, chat.getTitle(configManager.parse(player, titleAnimation.parseNext(title)), subTitleAnimation.parseNext(subTitle), ChronoUnit.SECONDS, 0, ChronoUnit.MILLIS, 200, ChronoUnit.SECONDS, 0))
                }
            }
        }
    }

    override fun shouldContinue(): Boolean {
        return ticksLived < 20L * (fadeIn + duration) && player.isOnline
    }

    override fun synchronizationContext() = SynchronizationContext.ASYNC
}
