/*
 * This file is part of AnnouncerPlus, licensed under the MIT License.
 *
 * Copyright (c) 2020-2023 Jason Penilla
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package xyz.jpenilla.announcerplus.config.message

import com.google.gson.JsonObject
import io.papermc.lib.PaperLib.getMinecraftVersion
import net.kyori.adventure.nbt.TagStringIO
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer
import org.bukkit.Material
import org.bukkit.entity.Player
import org.koin.core.component.inject
import org.spongepowered.configurate.objectmapping.ConfigSerializable
import org.spongepowered.configurate.objectmapping.meta.Comment
import xyz.jpenilla.announcerplus.AnnouncerPlus
import xyz.jpenilla.announcerplus.util.compoundBinaryTag
import xyz.jpenilla.announcerplus.util.listBinaryTag
import xyz.jpenilla.announcerplus.util.miniMessage
import xyz.jpenilla.announcerplus.util.ofChildren

@ConfigSerializable
class ToastSettings : MessageElement {

  @Comment("The icon for the Toast/Advancement notification")
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
  constructor(icon: Material, frameType: FrameType, header: String, footer: String, iconEnchanted: Boolean = false, iconCustomModelData: Int = -1) {
    this.icon = icon
    this.frame = frameType
    this.header = header
    this.footer = footer
    this.iconEnchanted = iconEnchanted
    this.iconCustomModelData = iconCustomModelData
  }

  private val announcerPlus: AnnouncerPlus by inject()

  override fun isEnabled(): Boolean {
    return header != "" || footer != ""
  }

  override fun display(player: Player) {
    announcerPlus.toastTask?.queueToast(this, player)
  }

  fun advancementJson(player: Player): JsonObject {
    val json = JsonObject()
    val display = JsonObject()
    val icon = JsonObject()
    val iconString = if (getMinecraftVersion() <= 12) this.icon.name else this.icon.key.toString()
    icon.addProperty("item", iconString)
    val nbt = compoundBinaryTag {
      putInt("CustomModelData", iconCustomModelData)
      if (iconEnchanted) {
        put(
          "Enchantments",
          listBinaryTag(
            compoundBinaryTag {
              putString("id", "aqua_affinity")
              putInt("lvl", 1)
            }
          )
        )
      }
    }
    icon.addProperty("nbt", TagStringIO.get().asString(nbt))
    display.add("icon", icon)
    val titleComponent = ofChildren(
      miniMessage(announcerPlus.configManager.parse(player, header)),
      Component.newline(),
      miniMessage(announcerPlus.configManager.parse(player, footer))
    )
    val gsonComponentSerializer = if (getMinecraftVersion() >= 16) {
      GsonComponentSerializer.gson()
    } else {
      GsonComponentSerializer.colorDownsamplingGson()
    }
    val title = gsonComponentSerializer.serializeToTree(titleComponent)
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
    return json
  }

  enum class FrameType(val value: String) {
    CHALLENGE("challenge"),
    GOAL("goal"),
    TASK("task"),
  }
}
