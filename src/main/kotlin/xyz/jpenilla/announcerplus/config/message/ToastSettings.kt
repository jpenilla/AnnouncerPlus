package xyz.jpenilla.announcerplus.config.message

import com.google.gson.JsonObject
import org.bukkit.Material
import org.bukkit.entity.Player
import org.koin.core.inject
import org.spongepowered.configurate.objectmapping.ConfigSerializable
import org.spongepowered.configurate.objectmapping.meta.Comment
import xyz.jpenilla.announcerplus.AnnouncerPlus

@ConfigSerializable
class ToastSettings : MessageElement {

    @Comment("The icon for the Toast/Advancement notification")
    @Transient
    var icon = Material.DIAMOND

    @Comment("Should the icon item be enchanted?")
    var iconEnchanted = false

    @Comment("Enter custom model data for the icon item. -1 to disable")
    var iconCustomModelData = -1

    @Comment("The text for the header of the Toast. If this and the footer are set to \"\" (empty string), the toast is disabled")
    var header = ""

    @Comment("The text for the footer of the Toast. If this and the header are set to \"\" (empty string), the toast is disabled")
    var footer = ""

    @Comment("The frame for the Toast. Can be CHALLENGE, GOAL, or TASK")
    var frame = FrameType.GOAL

    constructor()
    constructor(icon: Material, frameType: FrameType, header: String, footer: String) {
        this.icon = icon
        this.frame = frameType
        this.header = header
        this.footer = footer
    }

    private val announcerPlus: AnnouncerPlus by inject()

    override fun isEnabled(): Boolean {
        return header != "" || footer != ""
    }

    override fun display(player: Player) {
        announcerPlus.toastTask?.queueToast(this, player)
    }

    fun getJson(player: Player): String {
        val json = JsonObject()
        val display = JsonObject()
        val icon = JsonObject()
        icon.addProperty("item", this.icon.key.toString())
        val nbtBuilder = StringBuilder("{CustomModelData:$iconCustomModelData")
        if (iconEnchanted) {
            nbtBuilder.append(",Enchantments:[{id:\"aqua_affinity\",lvl:1}]}")
        } else {
            nbtBuilder.append("}")
        }
        icon.addProperty("nbt", nbtBuilder.toString())
        display.add("icon", icon)
        val titleComponent = announcerPlus.miniMessage.parse(announcerPlus.configManager.parse(player, "$header<reset>\n$footer"))
        val title = announcerPlus.jsonParser.parse(if (announcerPlus.majorMinecraftVersion < 16) {
            announcerPlus.downsamplingGsonComponentSerializer.serialize(titleComponent)
        } else {
            announcerPlus.gsonComponentSerializer.serialize(titleComponent)
        })
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

    enum class FrameType(val value: String) {
        CHALLENGE("challenge"),
        GOAL("goal"),
        TASK("task"),
    }
}