package xyz.jpenilla.announcerplus.util

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration

object Constants {
    const val CONFIG_COMMENT_SOUNDS_LINE2: String =
            "Separate multiple sounds by commas, i.e. \"minecraft:entity.strider.happy,minecraft:entity.villager.ambient,minecraft:block.note_block.cow_bell\""

    const val CONFIG_COMMENT_SOUNDS_RANDOM: String =
            "Should a random join sound be chosen(true) or should all of them play(false)"

    val CHAT_PREFIX: Component =
            Component.text()
                    .append(Component.text("[", NamedTextColor.WHITE))
                    .append(Component.text("A", TextColor.color(0x47EB46)))
                    .append(Component.text("P", TextColor.color(0x2CF58B)))
                    .append(Component.text("]", NamedTextColor.WHITE))
                    .append(Component.space())
                    .hoverEvent(Component.text()
                            .append(Component.text("Announcer", TextColor.color(0x47EB46)))
                            .append(Component.text("Plus", TextColor.color(0x2CF58B), TextDecoration.ITALIC))
                            .append(Component.newline())
                            .append(Component.text("  Click for help", NamedTextColor.GRAY, TextDecoration.ITALIC))
                            .build())
                    .clickEvent(ClickEvent.runCommand("/announcerplus help"))
                    .build()
}