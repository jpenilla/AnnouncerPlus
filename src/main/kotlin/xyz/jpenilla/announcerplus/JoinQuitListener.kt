package xyz.jpenilla.announcerplus

import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerLoginEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.koin.core.KoinComponent
import org.koin.core.inject
import xyz.jpenilla.announcerplus.config.ConfigManager
import xyz.jpenilla.announcerplus.util.RandomCollection
import java.util.*
import kotlin.collections.ArrayList

class JoinQuitListener : Listener, KoinComponent {
    private val configManager: ConfigManager by inject()
    private val newPlayers = ArrayList<UUID>()

    @EventHandler(priority = EventPriority.LOWEST)
    fun onLogin(event: PlayerLoginEvent) {
        if (configManager.mainConfig.joinEvent && configManager.mainConfig.firstJoinEnabled) {
            if (!event.player.hasPlayedBefore()) {
                newPlayers.add(event.player.uniqueId)
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    fun onJoin(event: PlayerJoinEvent) {
        if (configManager.mainConfig.joinEvent) {
            event.joinMessage = ""
            if (newPlayers.remove(event.player.uniqueId)) {
                configManager.firstJoinConfig.onJoin(event.player)
                return
            }
            for (entry in configManager.mainConfig.randomJoinConfigs.entries) {
                if (entry.key != "demo" && event.player.hasPermission("announcerplus.randomjoin.${entry.key}")) {
                    val weights = RandomCollection<String>()
                    for (pair in entry.value) {
                        weights.add(pair.weight, pair.configName)
                    }
                    configManager.joinQuitConfigs[weights.next()]?.onJoin(event.player)
                }
            }
            for (config in configManager.joinQuitConfigs.values) {
                config.onJoin(event.player)
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    fun onQuit(event: PlayerQuitEvent) {
        if (configManager.mainConfig.quitEvent) {
            event.quitMessage = ""
            for (entry in configManager.mainConfig.randomQuitConfigs.entries) {
                if (entry.key != "demo" && event.player.hasPermission("announcerplus.randomquit.${entry.key}")) {
                    val weights = RandomCollection<String>()
                    for (pair in entry.value) {
                        weights.add(pair.weight, pair.configName)
                    }
                    configManager.joinQuitConfigs[weights.next()]?.onQuit(event.player)
                }
            }
            for (config in configManager.joinQuitConfigs.values) {
                config.onQuit(event.player)
            }
        }
    }
}