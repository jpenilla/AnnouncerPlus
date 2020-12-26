package xyz.jpenilla.announcerplus.config.message

import com.google.common.collect.ImmutableList
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.scheduler.BukkitTask
import org.koin.core.KoinComponent
import org.koin.core.inject
import org.spongepowered.configurate.CommentedConfigurationNode
import org.spongepowered.configurate.objectmapping.ConfigSerializable
import org.spongepowered.configurate.objectmapping.ObjectMapper
import org.spongepowered.configurate.objectmapping.meta.Comment
import org.spongepowered.configurate.objectmapping.meta.NodeResolver
import org.spongepowered.configurate.objectmapping.meta.Setting
import xyz.jpenilla.announcerplus.AnnouncerPlus
import xyz.jpenilla.announcerplus.config.ConfigManager
import xyz.jpenilla.announcerplus.util.asyncTimer
import xyz.jpenilla.announcerplus.util.dispatchCommandAsConsole
import xyz.jpenilla.announcerplus.util.getOnMain
import xyz.jpenilla.announcerplus.util.runSync
import xyz.jpenilla.jmplib.Chat

@ConfigSerializable
class MessageConfig : KoinComponent {

    @Setting
    @Comment("The list of messages for a config")
    val messages = arrayListOf(
            Message(arrayListOf("<center><rainbow>Test AnnouncerPlus broadcast!")),
            Message().bossBar(BossBarSettings(25, "{animate:flash:YELLOW:PURPLE:40}",
                    "<green>-| <white>{animate:scrolltext:Hello this is an example Boss Bar announcement:20:3}</white> |-")),
            Message(arrayListOf(
                    "{prefix1} 1. <gradient:blue:green:blue>Multi-line test AnnouncerPlus broadcast",
                    "{prefix1} 2. <gradient:red:gold:red>Line number two of three",
                    "{prefix1} 3. <bold><rainbow>this is the last line (line 3)"))
                    .toast(ToastSettings(Material.DIAMOND, ToastSettings.FrameType.CHALLENGE,
                            "<gradient:green:blue><bold><italic>AnnouncerPlus", "<rainbow>This is a Toast message!")),
            Message(arrayListOf("{prefix1} Test <gradient:blue:aqua>AnnouncerPlus</gradient> broadcast with sound<green>!"))
                    .sounds("minecraft:entity.strider.happy,minecraft:entity.villager.ambient,minecraft:block.note_block.cow_bell"),
            Message(arrayListOf("{prefix1} Use <click:run_command:/ap about><hover:show_text:'<rainbow>Click to run!'><rainbow>/ap about</rainbow></hover></click> to check the plugin version"))
                    .actionBar(ActionBarSettings(true, 15, "<{animate:randomcolor:pulse:25}>-| <white>{animate:scrolltext:Hello there this is some very long text being displayed in a scrolling window!! =):20:3}</white> |-")),
            Message(arrayListOf("<bold><italic>Hello, </bold></italic> {nick} {prefix1} {r}!!!!!!!!!{rc}"))
                    .title(TitleSettings(1, 13, 2, "<gradient:green:blue:green:{animate:scroll:0.1}>||||||||||||||||||||||||||||||||||||||||||||", "<{animate:pulse:red:blue:10}>{animate:type:This is a test... typing...:6}")),
            Message(arrayListOf("<center><gradient:red:blue>Centered text Example"))
                    .bossBar(BossBarSettings(25, "PINK",
                            "<bold>This is an example <italic><gradient:blue:light_purple>Boss Bar"))
    )

    @Setting("every-broadcast-commands")
    @Comment("These commands will run as console once each interval\n  Example: \"broadcast This is a test\"")
    val commands = arrayListOf<String>()

    @Setting("every-broadcast-per-player-commands")
    @Comment("These commands will run as console once per player each interval\n  Example: \"minecraft:give %player_name% dirt\"")
    val perPlayerCommands = arrayListOf<String>()

    @Setting("every-broadcast-as-player-commands")
    @Comment("These commands will run once per player each interval, as the player\n  Example: \"ap about\"")
    val asPlayerCommands = arrayListOf<String>()

    @Setting("interval-time-unit")
    @Comment("The unit of time used for the interval\n  Can be SECONDS, MINUTES, or HOURS")
    var timeUnit = TimeUnit.MINUTES

    @Setting("interval-time-amount")
    @Comment("The amount of time used for the interval")
    var interval = 3

    @Setting("random-message-order")
    @Comment("Should the messages be sent in order of the config or in random order")
    var randomOrder = false

    companion object {
        private val MAPPER = ObjectMapper.factoryBuilder().addNodeResolver(NodeResolver.onlyWithSetting()).build()
            .get(MessageConfig::class.java)

        fun loadFrom(node: CommentedConfigurationNode, name: String): MessageConfig =
            MAPPER.load(node).populate(name)
    }

    fun saveTo(node: CommentedConfigurationNode) =
            MAPPER.save(this, node)

    fun populate(name: String): MessageConfig =
        this.apply { this.name = name }

    private var broadcastTask: BukkitTask? = null
    lateinit var name: String
    private val announcerPlus: AnnouncerPlus by inject()
    private val configManager: ConfigManager by inject()
    private val chat: Chat by inject()
    private val broadcastQueue = ArrayDeque<Message>()

    fun broadcast() {
        stop()
        broadcastQueue.clear()
        broadcastQueue.addAll(shuffledMessages())
        broadcastTask = announcerPlus.asyncTimer(0L, timeUnit.getTicks(interval)) {
            if (broadcastQueue.isNotEmpty()) {
                broadcast(broadcastQueue.removeFirst())
            } else {
                broadcast()
            }
        }
    }

    private fun broadcast(message: Message) {
        val onlinePlayers = announcerPlus.getOnMain { ImmutableList.copyOf(Bukkit.getOnlinePlayers()) }
        for (onlinePlayer in onlinePlayers) {
            if (announcerPlus.essentials != null) {
                if (announcerPlus.essentials!!.isAfk(onlinePlayer) && announcerPlus.perms!!.playerHas(onlinePlayer, "${announcerPlus.name}.messages.$name.afk")) {
                    continue
                }
            }
            if (announcerPlus.perms!!.playerHas(onlinePlayer, "${announcerPlus.name}.messages.$name")) {
                with(message) {
                    if (messageText.size != 0) {
                        chat.send(onlinePlayer, configManager.parse(onlinePlayer, messageText))
                    }
                    chat.playSounds(onlinePlayer, soundsRandomized, sounds)
                    messageElements().forEach { it.displayIfEnabled(onlinePlayer) }
                }
                announcerPlus.runSync {
                    message.perPlayerCommands.forEach { dispatchCommandAsConsole(configManager.parse(onlinePlayer, it)) }
                    message.asPlayerCommands.forEach { onlinePlayer.performCommand(configManager.parse(onlinePlayer, it)) }
                    perPlayerCommands.forEach { dispatchCommandAsConsole(configManager.parse(onlinePlayer, it)) }
                    asPlayerCommands.forEach { onlinePlayer.performCommand(configManager.parse(onlinePlayer, it)) }
                }
            }
        }
        announcerPlus.runSync {
            message.commands.forEach { dispatchCommandAsConsole(configManager.parse(null, it)) }
            commands.forEach { dispatchCommandAsConsole(configManager.parse(null, it)) }
        }
    }

    private fun shuffledMessages(): List<Message> {
        if (randomOrder) {
            return messages.shuffled()
        }
        return messages
    }

    fun stop() {
        broadcastTask?.cancel()
    }

    enum class TimeUnit(val ticks: Long) {
        SECONDS(20L),
        MINUTES(1200L),
        HOURS(72000L);

        fun getTicks(units: Int): Long =
                ticks * units
    }
}