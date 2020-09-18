package xyz.jpenilla.announcerplus.config.message

import ninja.leaping.configurate.objectmapping.Setting
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable
import org.bukkit.entity.Player
import xyz.jpenilla.announcerplus.task.ActionBarUpdateTask

@ConfigSerializable
class ActionBarSettings : MessageElement {

    @Setting(value = "duration-seconds", comment = "Seconds of duration for the Action Bar to stay on screen")
    var durationSeconds = 6

    @Setting(value = "enable-fade-out", comment = "Should the fade out animation of the Action Bar be enabled?")
    var fadeEnabled = false

    @Setting(value = "text", comment = "The text for the Action Bar. Set to \"\" (empty string) to disable. Accepts animations")
    var text = ""

    constructor()
    constructor(fadeEnabled: Boolean, durationSeconds: Int, text: String) {
        this.fadeEnabled = fadeEnabled
        this.durationSeconds = durationSeconds
        this.text = text
    }

    override fun isEnabled(): Boolean {
        return text != ""
    }

    override fun display(player: Player) {
        ActionBarUpdateTask(player, durationSeconds * 20L, fadeEnabled, text).start()
    }
}