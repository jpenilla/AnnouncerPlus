package xyz.jpenilla.announcerplus.config

import com.google.common.collect.ImmutableList
import com.okkero.skedule.schedule
import org.bukkit.Bukkit
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Player
import xyz.jpenilla.announcerplus.AnnouncerPlus

class JoinQuitConfig(private val announcerPlus: AnnouncerPlus, val name: String, private val data: YamlConfiguration) {
    val joinMessages = ArrayList<String>()
    val joinBroadcasts = ArrayList<String>()
    val quitBroadcasts = ArrayList<String>()
    private lateinit var permission: String
    private val chat = announcerPlus.chat

    init {
        load()
    }

    private fun load() {
        permission = data.getString("seePermission", "")!!
        joinMessages.clear()
        joinMessages.addAll(data.getStringList("joinMessages"))
        joinBroadcasts.clear()
        joinBroadcasts.addAll(data.getStringList("joinBroadcasts"))
        quitBroadcasts.clear()
        quitBroadcasts.addAll(data.getStringList("quitBroadcasts"))
    }

    fun onJoin(player: Player) {
        if (player.hasPermission("announcerplus.join.$name")) {
            chat.sendPlaceholders(player, joinMessages, announcerPlus.cfg.placeholders)
            announcerPlus.schedule {
                waitFor(3L)
                if (!isVanished(player)) {
                    val players = ImmutableList.copyOf(Bukkit.getOnlinePlayers())
                    val m = chat.replacePlaceholders(player, joinBroadcasts, announcerPlus.cfg.placeholders)
                    for (p in players) {
                        if (p.name != player.name) {
                            if (announcerPlus.perms!!.playerHas(p, permission) || permission == "") {
                                chat.send(p, m)
                            }
                        }
                    }
                }
            }
        }
    }

    fun onQuit(player: Player) {
        if (player.hasPermission("announcerplus.quit.$name") && !isVanished(player)) {
            val players = ImmutableList.copyOf(Bukkit.getOnlinePlayers())

            val m = chat.replacePlaceholders(player, quitBroadcasts, announcerPlus.cfg.placeholders)
            for (p in players) {
                if (p.name != player.name) {
                    if (announcerPlus.perms!!.playerHas(p, permission) || permission == "") {
                        chat.send(p, m)
                    }
                }
            }
        }
    }

    private fun isVanished(player: Player): Boolean {
        for (meta in player.getMetadata("vanished")) {
            if (meta.asBoolean()) return true
        }
        return false
    }
}