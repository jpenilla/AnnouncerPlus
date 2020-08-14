package xyz.jpenilla.announcerplus

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import xyz.jpenilla.announcerplus.util.RandomCollection

class JoinQuitListener(private val announcerPlus: AnnouncerPlus) : Listener {
    @EventHandler
    fun onJoin(event: PlayerJoinEvent) {
        if (announcerPlus.configManager.mainConfig.joinEvent) {
            event.joinMessage = ""
            for (entry in announcerPlus.configManager.mainConfig.randomJoinConfigs.entries) {
                if (entry.key != "demo" && event.player.hasPermission("announcerplus.randomjoin.${entry.key}")) {
                    val weights = RandomCollection<String>()
                    for (pair in entry.value) {
                        weights.add(pair.weight, pair.configName)
                    }
                    announcerPlus.configManager.joinQuitConfigs[weights.next()]?.onJoin(event.player)
                }
            }
            for (config in announcerPlus.configManager.joinQuitConfigs.values) {
                config.onJoin(event.player)
            }
        }
    }

    @EventHandler
    fun onQuit(event: PlayerQuitEvent) {
        if (announcerPlus.configManager.mainConfig.quitEvent) {
            event.quitMessage = ""
            for (entry in announcerPlus.configManager.mainConfig.randomQuitConfigs.entries) {
                if (entry.key != "demo" && event.player.hasPermission("announcerplus.randomquit.${entry.key}")) {
                    val weights = RandomCollection<String>()
                    for (pair in entry.value) {
                        weights.add(pair.weight, pair.configName)
                    }
                    announcerPlus.configManager.joinQuitConfigs[weights.next()]?.onQuit(event.player)
                }
            }
            for (config in announcerPlus.configManager.joinQuitConfigs.values) {
                config.onQuit(event.player)
            }
        }
    }
}