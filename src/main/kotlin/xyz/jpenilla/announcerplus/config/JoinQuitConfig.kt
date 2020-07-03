package xyz.jpenilla.announcerplus.config

import com.google.common.collect.ImmutableList
import com.okkero.skedule.SynchronizationContext
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
                val players = ImmutableList.copyOf(Bukkit.getOnlinePlayers())
                switchContext(SynchronizationContext.ASYNC)

                for (p in players) {
                    if (announcerPlus.perms!!.playerHas(p, permission) || permission == "") {
                        if (p.uniqueId != player.uniqueId) {
                            chat.sendPlaceholders(p, joinBroadcasts, announcerPlus.cfg.placeholders)
                        }
                    }
                }
            }
        }
    }

    fun onQuit(player: Player) {
        if (player.hasPermission("announcerplus.quit.$name")) {
            announcerPlus.schedule {
                val players = ImmutableList.copyOf(Bukkit.getOnlinePlayers())
                switchContext(SynchronizationContext.ASYNC)

                for (p in players) {
                    if (announcerPlus.perms!!.playerHas(p, permission) || permission == "") {
                        if (p.uniqueId != player.uniqueId) {
                            chat.sendPlaceholders(p, quitBroadcasts, announcerPlus.cfg.placeholders)
                        }
                    }
                }
            }
        }
    }
}