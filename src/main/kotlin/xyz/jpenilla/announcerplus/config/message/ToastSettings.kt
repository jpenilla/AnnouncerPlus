package xyz.jpenilla.announcerplus.config.message

import com.google.gson.JsonObject
import com.okkero.skedule.schedule
import ninja.leaping.configurate.objectmapping.Setting
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import xyz.jpenilla.announcerplus.AnnouncerPlus
import java.util.concurrent.ThreadLocalRandom


@ConfigSerializable
class ToastSettings : MessageElement {
    constructor()
    constructor(icon: Material, frameType: FrameType, header: String, footer: String) {
        this.icon = icon
        this.frame = frameType
        this.header = header
        this.footer = footer
    }

    @Setting(value = "icon", comment = "The icon for the Toast/Advancement notification")
    var icon = Material.NETHERITE_INGOT

    @Setting(value = "header", comment = "The text for the header of the Toast. If this and the footer are set to \"\" (empty string), the toast is disabled")
    var header = ""

    @Setting(value = "footer", comment = "The text for the footer of the Toast. If this and the header are set to \"\" (empty string), the toast is disabled")
    var footer = ""

    @Setting(value = "frame", comment = "The frame for the Toast. Can be CHALLENGE, GOAL, or TASK")
    var frame = FrameType.GOAL

    enum class FrameType(val value: String) {
        CHALLENGE("challenge"),
        GOAL("goal"),
        TASK("task"),
    }

    override fun isEnabled(): Boolean {
        return header != "" || footer != ""
    }

    override fun display(announcerPlus: AnnouncerPlus, player: Player) {
        announcerPlus.schedule {
            val key = NamespacedKey(announcerPlus, "announcerPlus${ThreadLocalRandom.current().nextInt(1000000)}")
            try {
                Bukkit.getUnsafe().loadAdvancement(key, getJson(announcerPlus, player))
            } catch (e: Exception) {
            }
            val advancement = Bukkit.getAdvancement(key)!!
            val progress = player.getAdvancementProgress(advancement)
            if (!progress.isDone) {
                for (criteria in progress.remainingCriteria) {
                    progress.awardCriteria(criteria)
                }
            }
            waitFor(20L)
            if (progress.isDone) {
                for (criteria in progress.awardedCriteria) {
                    progress.revokeCriteria(criteria)
                }
            }
            Bukkit.getUnsafe().removeAdvancement(key)
        }
    }

    private fun getJson(announcerPlus: AnnouncerPlus, player: Player): String {
        val json = JsonObject()
        val display = JsonObject()
        val icon = JsonObject()
        icon.addProperty("item", this.icon.key.toString())
        display.add("icon", icon)
        val title = announcerPlus.jsonParser.parse(announcerPlus.gsonComponentSerializer.serialize(announcerPlus.miniMessage.parse(announcerPlus.configManager.parse(player, "$header<reset>\n$footer"))))
        display.add("title", title)
        display.addProperty("description", "AnnouncerPlus Toast Description")
        display.addProperty("frame", frame.value)
        display.addProperty("announce_to_chat", false)
        display.addProperty("show_toast", true)
        display.addProperty("hidden", true)
        val trigger = JsonObject()
        trigger.addProperty("trigger", "minecraft:impossible")
        val criteria = JsonObject()
        criteria.add("impossible", trigger)
        json.add("criteria", criteria)
        json.add("display", display)
        return announcerPlus.gson.toJson(json)
    }
}