package xyz.jpenilla.announcerplus.config.message

import org.bukkit.entity.Player
import xyz.jpenilla.announcerplus.AnnouncerPlus

abstract class MessageElement {
    abstract fun isEnabled(): Boolean
    abstract fun display(announcerPlus: AnnouncerPlus, player: Player)

    fun displayIfEnabled(announcerPlus: AnnouncerPlus, player: Player) {
        if (isEnabled()) {
            display(announcerPlus, player)
        }
    }
}