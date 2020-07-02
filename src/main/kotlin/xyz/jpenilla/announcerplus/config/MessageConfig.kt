package xyz.jpenilla.announcerplus.config

import com.google.common.collect.ImmutableList
import com.okkero.skedule.CoroutineTask
import com.okkero.skedule.SynchronizationContext
import com.okkero.skedule.schedule
import net.kyori.adventure.platform.bukkit.BukkitAudiences
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Bukkit
import org.bukkit.configuration.file.YamlConfiguration
import xyz.jpenilla.announcerplus.AnnouncerPlus

class MessageConfig(private val announcerPlus: AnnouncerPlus, val name: String, private val data: YamlConfiguration) {
    private var broadcastAllTask: CoroutineTask? = null

    val messages = ArrayList<String>()
    var timeUnit: TimeUnit = TimeUnit.SECONDS
    var interval = 10
    var randomOrder = false
    var delay = 20L

    init {
        load()
    }

    fun load() {
        timeUnit = TimeUnit.valueOf(data.getString("intervalUnit")!!)
        delay = when (timeUnit) {
            TimeUnit.SECONDS -> 20L
            TimeUnit.MINUTES -> 20L * 60L
            TimeUnit.HOURS -> 20L * 60L * 60L
        }
        interval = data.getInt("interval")
        randomOrder = data.getBoolean("randomOrder")
        messages.clear()
        messages.addAll(data.getStringList("messages"))
    }

    fun broadcast() {
        stop()
        broadcastAllTask = announcerPlus.schedule(SynchronizationContext.ASYNC) {
            val tempMessages = messages
            if (randomOrder) {
                tempMessages.shuffle()
            }
            repeating(delay * interval)
            for (message in tempMessages) {
                switchContext(SynchronizationContext.SYNC)
                val players = ImmutableList.copyOf(Bukkit.getOnlinePlayers())
                switchContext(SynchronizationContext.ASYNC)

                for (player in players) {
                    if (announcerPlus.perms!!.playerHas(player, "${announcerPlus.name}.messages.$name")) {
                        val m = MiniMessage.instance().parse(announcerPlus.cfg.replacePlaceholders(player, message))
                        BukkitAudiences.create(announcerPlus).player(player).sendMessage(m)
                    }
                }
                yield()
            }
            broadcast()
        }
    }

    fun stop() {
        broadcastAllTask?.cancel()
    }

    enum class TimeUnit {
        SECONDS,
        MINUTES,
        HOURS
    }
}