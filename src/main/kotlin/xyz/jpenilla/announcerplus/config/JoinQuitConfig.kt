package xyz.jpenilla.announcerplus.config

import com.google.common.collect.ImmutableList
import com.okkero.skedule.SynchronizationContext
import com.okkero.skedule.schedule
import org.bukkit.Bukkit
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Player
import xyz.jpenilla.announcerplus.AnnouncerPlus

class JoinQuitConfig(private val announcerPlus: AnnouncerPlus, val name: String, private val data: YamlConfiguration) {
    private val chat = announcerPlus.chat

    var titleFadeInSeconds = 1
    var titleDurationSeconds = 4
    var titleFadeOutSeconds = 1
    var actionBarDurationSeconds = 6
    var randomJoinSound = true
    var randomJoinBroadcastSound = true
    var randomQuitSound = true
    lateinit var permission: String
    lateinit var titleTitle: String
    lateinit var titleSubtitle: String
    lateinit var actionBarText: String
    lateinit var joinSounds: String
    lateinit var joinBroadcastSounds: String
    lateinit var quitSounds: String
    val joinMessages = ArrayList<String>()
    val joinBroadcasts = ArrayList<String>()
    val quitBroadcasts = ArrayList<String>()
    val joinCommands = ArrayList<String>()
    val quitCommands = ArrayList<String>()
    val runAsPlayerJoinCommands = ArrayList<String>()

    init {
        load()
    }

    private fun load() {
        titleFadeInSeconds = data.getInt("title.fadeInSeconds", 1)
        titleDurationSeconds = data.getInt("title.durationSeconds", 4)
        titleFadeOutSeconds = data.getInt("title.fadeOutSeconds", 1)
        actionBarDurationSeconds = data.getInt("actionBar.durationSeconds", 6)

        randomJoinSound = data.getBoolean("randomJoinSound", true)
        randomJoinBroadcastSound = data.getBoolean("randomJoinBroadcastSound", true)
        randomQuitSound = data.getBoolean("randomQuitSound", true)

        permission = data.getString("seePermission", "")!!
        titleTitle = data.getString("title.title", "")!!
        titleSubtitle = data.getString("title.subTitle", "")!!
        actionBarText = data.getString("actionBar.text", "")!!
        joinSounds = data.getString("joinSounds", "")!!
        joinBroadcastSounds = data.getString("joinBroadcastSounds", "")!!
        quitSounds = data.getString("quitSounds", "")!!

        joinMessages.clear()
        joinMessages.addAll(data.getStringList("joinMessages"))
        joinBroadcasts.clear()
        joinBroadcasts.addAll(data.getStringList("joinBroadcasts"))
        quitBroadcasts.clear()
        quitBroadcasts.addAll(data.getStringList("quitBroadcasts"))
        joinCommands.clear()
        joinCommands.addAll(data.getStringList("joinCommands"))
        quitCommands.clear()
        quitCommands.addAll(data.getStringList("quitCommands"))
        runAsPlayerJoinCommands.clear()
        runAsPlayerJoinCommands.addAll(data.getStringList("runAsPlayerJoinCommands"))
    }

    fun onJoin(player: Player) {
        if (player.hasPermission("announcerplus.join.$name")) {
            chat.send(player, announcerPlus.cfg.parse(player, joinMessages))
            announcerPlus.schedule {
                waitFor(3L)
                if (!isVanished(player)) {
                    val players = ImmutableList.copyOf(Bukkit.getOnlinePlayers())
                    switchContext(SynchronizationContext.ASYNC)
                    val m = announcerPlus.cfg.parse(player, joinBroadcasts)
                    for (p in players) {
                        if (p.name != player.name) {
                            if (announcerPlus.perms!!.playerHas(p, permission) || permission == "") {
                                chat.send(p, m)
                                if (joinBroadcastSounds != "") {
                                    chat.playSounds(p, randomJoinBroadcastSound, joinBroadcastSounds)
                                }
                            }
                        }
                    }
                    switchContext(SynchronizationContext.SYNC)
                    for (command in joinCommands) {
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), chat.papiParse(player, command))
                    }
                    for (command in runAsPlayerJoinCommands) {
                        Bukkit.dispatchCommand(player, chat.papiParse(player, command))
                    }
                }
            }
            announcerPlus.schedule(SynchronizationContext.ASYNC) {
                if (titleTitle != "" && titleSubtitle != "") {
                    val title = chat.getTitleSeconds(announcerPlus.cfg.parse(player, titleTitle), announcerPlus.cfg.parse(player, titleSubtitle), titleFadeInSeconds, titleDurationSeconds, titleFadeOutSeconds)
                    chat.showTitle(player, title)
                }
                if (actionBarText != "") {
                    chat.sendActionBar(player, actionBarDurationSeconds, announcerPlus.cfg.parse(player, actionBarText))
                }
                if (joinSounds != "") {
                    chat.playSounds(player, randomJoinSound, joinSounds)
                }
            }
        }
    }

    fun onQuit(player: Player) {
        if (player.hasPermission("announcerplus.quit.$name") && !isVanished(player)) {
            val players = ImmutableList.copyOf(Bukkit.getOnlinePlayers())
            val m = announcerPlus.cfg.parse(player, quitBroadcasts)
            for (p in players) {
                if (p.name != player.name) {
                    if (announcerPlus.perms!!.playerHas(p, permission) || permission == "") {
                        chat.send(p, m)
                        if (quitSounds != "") {
                            chat.playSounds(p, randomQuitSound, quitSounds)
                        }
                    }
                }
            }
            for (command in quitCommands) {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), chat.papiParse(player, command))
            }
        }
    }

    private fun isVanished(player: Player): Boolean {
        for (meta in player.getMetadata("vanished")) {
            if (meta.asBoolean()) return true
        }
        if (announcerPlus.essentials != null) {
            return announcerPlus.essentials!!.isVanished(player)
        }
        return false
    }
}