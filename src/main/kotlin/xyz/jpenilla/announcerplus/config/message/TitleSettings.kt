package xyz.jpenilla.announcerplus.config.message

import ninja.leaping.configurate.objectmapping.Setting
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable
import org.bukkit.entity.Player
import xyz.jpenilla.announcerplus.task.TitleUpdateTask

@ConfigSerializable
class TitleSettings : MessageElement {
    constructor()
    constructor(fadeInSeconds: Int, durationSeconds: Int, fadeOutSeconds: Int, title: String, subTitle: String) {
        this.fadeInSeconds = fadeInSeconds
        this.fadeOutSeconds = fadeOutSeconds
        this.title = title
        this.subTitle = subTitle
        this.durationSeconds = durationSeconds
    }

    @Setting(value = "fade-in-seconds", comment = "Seconds of duration for the title fade-in animation")
    var fadeInSeconds = 1

    @Setting(value = "duration-seconds", comment = "Seconds of duration for the title to stay on screen")
    var durationSeconds = 5

    @Setting(value = "fade-out-seconds", comment = "Seconds of duration for the title fade-out animation")
    var fadeOutSeconds = 1

    @Setting(value = "title", comment = "Title text. If the title and subtitle are both set to \"\" (empty string), then this title is disabled")
    var title = ""

    @Setting(value = "subtitle", comment = "Subtitle text. If the title and subtitle are both set to \"\" (empty string), then this title is disabled")
    var subTitle = ""

    override fun isEnabled(): Boolean {
        return if (fadeInSeconds == 0 && durationSeconds == 0 && fadeOutSeconds == 0) {
            false
        } else {
            title != "" || subTitle != ""
        }
    }

    override fun display(player: Player) {
        TitleUpdateTask(player, fadeInSeconds, durationSeconds, fadeOutSeconds, title, subTitle).start()
    }
}