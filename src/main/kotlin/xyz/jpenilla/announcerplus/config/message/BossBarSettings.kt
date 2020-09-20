package xyz.jpenilla.announcerplus.config.message

import net.kyori.adventure.bossbar.BossBar
import ninja.leaping.configurate.objectmapping.Setting
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable
import org.bukkit.entity.Player
import xyz.jpenilla.announcerplus.task.BossBarUpdateTask

@ConfigSerializable
class BossBarSettings : MessageElement {

    @Setting(value = "duration-seconds", comment = "Seconds of duration for the Boss Bar to stay on screen")
    var durationSeconds = 12

    @Setting(value = "text", comment = "The text for the Boss Bar. Set to \"\" (empty string) to disable. Accepts animations")
    var text = ""

    @Setting(value = "color", comment = "The color for the Boss Bar. For a list of colors, visit: https://papermc.io/javadocs/paper/1.16/org/bukkit/boss/BarColor.html\n" +
            "  This field technically accepts animations, although only the \"Flashing Text\" animation used with valid Boss Bar colors will actually work.")
    var color = "YELLOW"

    @Setting(value = "overlay", comment = "The overlay for the Boss Bar. Possible values: PROGRESS, NOTCHED_6, NOTCHED_10, NOTCHED_12, NOTCHED_20")
    var overlay = BossBar.Overlay.PROGRESS

    @Setting(value = "fill-mode", comment = "The fill mode for the Boss Bar. Possible modes: FILL, DRAIN, FULL, EMPTY")
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