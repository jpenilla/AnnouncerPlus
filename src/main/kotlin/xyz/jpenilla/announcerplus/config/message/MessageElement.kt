package xyz.jpenilla.announcerplus.config.message

import org.bukkit.entity.Player
import org.koin.core.KoinComponent

interface MessageElement : KoinComponent {
    fun isEnabled(): Boolean
    fun display(player: Player)

    fun displayIfEnabled(player: Player) {
        if (isEnabled()) {
            display(player)
        }
    }
}
