package xyz.jpenilla.announcerplus

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent

class JoinQuitListener(private val announcerPlus: AnnouncerPlus) : Listener {
    @EventHandler
    fun onJoin(event: PlayerJoinEvent) {
        if (announcerPlus.configManager.mainConfig.joinEvent) {
            event.joinMessage = ""
            for (config in announcerPlus.configManager.joinQuitConfigs.values) {
                config.onJoin(event.player)
            }
        }
    }

    @EventHandler
    fun onQuit(event: PlayerQuitEvent) {
        if (announcerPlus.configManager.mainConfig.quitEvent) {
            event.quitMessage = ""
            for (config in announcerPlus.configManager.joinQuitConfigs.values) {
                config.onQuit(event.player)
            }
        }
    }
}