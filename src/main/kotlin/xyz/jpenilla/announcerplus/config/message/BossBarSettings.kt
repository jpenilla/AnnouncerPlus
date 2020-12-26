package xyz.jpenilla.announcerplus.config.message

import net.kyori.adventure.bossbar.BossBar
import org.bukkit.entity.Player
import org.spongepowered.configurate.objectmapping.ConfigSerializable
import org.spongepowered.configurate.objectmapping.meta.Comment
import xyz.jpenilla.announcerplus.task.BossBarUpdateTask

@ConfigSerializable
class BossBarSettings : MessageElement {

    @Comment("Seconds of duration for the Boss Bar to stay on screen")
    var durationSeconds = 12

    @Comment("The text for the Boss Bar. Set to \"\" (empty string) to disable. Accepts animations")
    var text = ""

    @Comment("The color for the Boss Bar. For a list of colors, visit: https://papermc.io/javadocs/paper/1.16/org/bukkit/boss/BarColor.html\n" +
            "  This field technically accepts animations, although only the \"Flashing Text\" animation used with valid Boss Bar colors will actually work.")
    var color = "YELLOW"

    @Comment("The overlay for the Boss Bar. Possible values: PROGRESS, NOTCHED_6, NOTCHED_10, NOTCHED_12, NOTCHED_20")
    var overlay = BossBar.Overlay.PROGRESS

    @Comment("The fill mode for the Boss Bar. Possible modes: FILL, DRAIN, FULL, EMPTY")
    var fillMode = BossBarUpdateTask.FillMode.DRAIN

    constructor()
    constructor(durationSeconds: Int, color: String, text: String) {
        this.durationSeconds = durationSeconds
        this.color = color
        this.text = text
    }

    override fun isEnabled(): Boolean {
        return text != ""
    }

    override fun display(player: Player) {
        BossBarUpdateTask(player, durationSeconds, overlay, fillMode, color, text).start()
    }
}
