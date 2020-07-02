package xyz.jpenilla.announcerplus

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent


class JoinQuitListener(private val announcerPlus: AnnouncerPlus) : Listener {
    @EventHandler
    fun onJoin(event: PlayerJoinEvent) {
    }
}