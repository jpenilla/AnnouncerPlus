package xyz.jpenilla.announcerplus.config.message

import org.bukkit.entity.Player
import org.spongepowered.configurate.objectmapping.ConfigSerializable
import org.spongepowered.configurate.objectmapping.meta.Comment
import xyz.jpenilla.announcerplus.task.TitleUpdateTask

@ConfigSerializable
class TitleSettings : MessageElement {
    constructor()
    constructor(fadeInSeconds: Int, durationSeconds: Int, fadeOutSeconds: Int, title: String, subTitle: String) {
        this.fadeInSeconds = fadeInSeconds
        this.fadeOutSeconds = fadeOutSeconds
        this.title = title
        this.subtitle = subTitle
        this.durationSeconds = durationSeconds
    }

    @Comment("Seconds of duration for the title fade-in animation")
    var fadeInSeconds = 1

    @Comment("Seconds of duration for the title to stay on screen")
    var durationSeconds = 5

    @Comment("Seconds of duration for the title fade-out animation")
    var fadeOutSeconds = 1

    @Comment("Title text. If the title and subtitle are both set to \"\" (empty string), then this title is disabled")
    var title = ""

    @Comment("Subtitle text. If the title and subtitle are both set to \"\" (empty string), then this title is disabled")
    var subtitle = ""

    override fun isEnabled(): Boolean {
        return if (fadeInSeconds == 0 && durationSeconds == 0 && fadeOutSeconds == 0) {
            false
        } else {
            title != "" || subtitle != ""
        }
    }

    override fun display(player: Player) {
        TitleUpdateTask(player, fadeInSeconds, durationSeconds, fadeOutSeconds, title, subtitle).start()
    }
}
