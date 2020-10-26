package xyz.jpenilla.announcerplus.config.message

import org.bukkit.entity.Player
import org.spongepowered.configurate.objectmapping.ConfigSerializable
import org.spongepowered.configurate.objectmapping.meta.Comment
import xyz.jpenilla.announcerplus.task.ActionBarUpdateTask

@ConfigSerializable
class ActionBarSettings : MessageElement {

    @Comment("Seconds of duration for the Action Bar to stay on screen")
    var durationSeconds = 6

    @Comment("Should the fade out animation of the Action Bar be enabled?")
    var enableFadeOut = false

    @Comment("The text for the Action Bar. Set to \"\" (empty string) to disable. Accepts animations")
    var text = ""

    constructor()
    constructor(fadeEnabled: Boolean, durationSeconds: Int, text: String) {
        this.enableFadeOut = fadeEnabled
        this.durationSeconds = durationSeconds
        this.text = text
    }

    override fun isEnabled(): Boolean {
        return text != ""
    }

    override fun display(player: Player) {
        ActionBarUpdateTask(player, durationSeconds * 20L, enableFadeOut, text).start()
    }
}