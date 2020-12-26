package xyz.jpenilla.announcerplus.util

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.newline
import net.kyori.adventure.text.Component.space
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor.color
import net.kyori.adventure.text.format.TextDecoration

object Constants {
    const val CONFIG_COMMENT_SOUNDS_LINE2: String =
        "Separate multiple sounds by commas, i.e. \"minecraft:entity.strider.happy,minecraft:entity.villager.ambient,minecraft:block.note_block.cow_bell\""

    const val CONFIG_COMMENT_SOUNDS_RANDOM: String =
        "Should a random join sound be chosen(true) or should all of them play(false)"

    val CHAT_PREFIX: Component = text()
        .append(text("[", NamedTextColor.WHITE))
        .append(text("A", color(0x47EB46)))
        .append(text("P", color(0x2CF58B)))
        .append(text("]", NamedTextColor.WHITE))
        .append(space())
        .hoverEvent(
            text()
                .append(text("Announcer", color(0x47EB46)))
                .append(text("Plus", color(0x2CF58B), TextDecoration.ITALIC))
                .append(newline())
                .append(
                    text(
                        "  Click for help",
                        NamedTextColor.GRAY,
                        TextDecoration.ITALIC
                    )
                )
                .build()
        )
        .clickEvent(ClickEvent.runCommand("/announcerplus help"))
        .build()
}
