package xyz.jpenilla.announcerplus.config

import com.google.common.collect.ImmutableList
import com.okkero.skedule.SynchronizationContext
import com.okkero.skedule.schedule
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.koin.core.KoinComponent
import org.koin.core.inject
import org.spongepowered.configurate.CommentedConfigurationNode
import org.spongepowered.configurate.objectmapping.ConfigSerializable
import org.spongepowered.configurate.objectmapping.ObjectMapper
import org.spongepowered.configurate.objectmapping.meta.Comment
import org.spongepowered.configurate.objectmapping.meta.NodeResolver
import org.spongepowered.configurate.objectmapping.meta.Setting
import xyz.jpenilla.announcerplus.AnnouncerPlus
import xyz.jpenilla.announcerplus.config.message.ActionBarSettings
import xyz.jpenilla.announcerplus.config.message.BossBarSettings
import xyz.jpenilla.announcerplus.config.message.TitleSettings
import xyz.jpenilla.announcerplus.config.message.ToastSettings
import xyz.jpenilla.announcerplus.util.Constants
import xyz.jpenilla.jmplib.Chat

@ConfigSerializable
class JoinQuitConfig : KoinComponent {

    @Setting("visible-permission")
    @Comment("If set to something other than \"\", this setting's value will be the permission required to see these join/quit messages when they are broadcasted for a player")
    var permission = ""

    @Setting("join-section")
    @Comment("Player Join related settings")
    var join = JoinSection()

    @Setting("quit-section")
    @Comment("Player Quit related settings")
    var quit = QuitSection()

    @ConfigSerializable
    class JoinSection {
        @Setting("randomize-join-sounds")
        @Comment(Constants.CONFIG_COMMENT_SOUNDS_RANDOM)
        var randomSound = true

        @Setting("randomize-join-broadcast-sounds")
        @Comment(Constants.CONFIG_COMMENT_SOUNDS_RANDOM)
        var randomBroadcastSound = true

        @Setting("join-sounds")
        @Comment("These sound(s) will be played to the joining player.\n  ${Constants.CONFIG_COMMENT_SOUNDS_LINE2}")
        var sounds = "minecraft:entity.strider.happy,minecraft:entity.villager.ambient,minecraft:block.note_block.cow_bell"

        @Setting("join-broadcast-sounds")
        @Comment("These sound(s) will be played to the joining player.\n  ${Constants.CONFIG_COMMENT_SOUNDS_LINE2}")
        var broadcastSounds = "minecraft:entity.enderman.teleport"

        @Setting("join-messages")
        @Comment("These messages will be sent to the joining Player. These messages are sometimes called a \"Message of the Day\" or a \"MotD\"")
        val messages = arrayListOf(
                "<hover:show_text:'<yellow>Username</yellow><gray>:</gray> {user}'>{nick}</hover> <yellow>joined the game",
                "<center><rainbow><italic>Welcome,</rainbow> {user}<yellow>!",
                "<center><gradient:black:white:black>------------------------------------</gradient>",
                "This server is using <blue>Announcer<italic>Plus<reset>!",
                "<gradient:green:white>Configure these messages by editing the config files!"
        )

        @Setting("join-broadcasts")
        @Comment("These messages will be sent to every Player online except the joining Player. Also known as join messages.")
        val broadcasts = arrayListOf("<hover:show_text:'<yellow>Username</yellow><gray>:</gray> {user}'>{nick}</hover> <yellow>joined the game")

        @Setting("join-commands")
        @Comment("These commands will be run by the console on Player join.\n  Example: \"minecraft:give %player_name% dirt\"")
        val commands = arrayListOf<String>()

        @Setting("as-player-join-commands")
        @Comment("These commands will be run as the Player on Player join.\n  Example: \"ap about\"")
        val asPlayerCommands = arrayListOf<String>()

        @Setting("title-settings")
        @Comment("Settings relating to showing a title to the joining Player")
        var title = TitleSettings(1, 7, 1,
                "<bold><italic><gradient:green:blue:green:{animate:scroll:0.1}>Welcome</gradient><yellow>{animate:flash:!:!!:!!!:10}",
                "<{animate:pulse:red:blue:yellow:green:10}>{user}")

        @Setting("action-bar-settings")
        @Comment("Settings relating to showing an Action Bar to the joining Player")
        var actionBar = ActionBarSettings(false, 8,
                "<gradient:green:blue:green:{animate:scroll:0.1}>|||||||||||||||||||||||||||||||||||||||</gradient>")

        @Setting("boss-bar-settings")
        @Comment("Settings relating to showing a Boss Bar to the joining Player")
        var bossBar = BossBarSettings()

        @Setting("toast-settings")
        @Comment("Configure the Toast that will be showed to the joining player")
        var toast = ToastSettings(Material.DIAMOND, ToastSettings.FrameType.CHALLENGE,
                "<gradient:green:blue><bold><italic>AnnouncerPlus", "<rainbow>Welcome to the server!")
    }

    @ConfigSerializable
    class QuitSection {
        @Setting("randomize-quit-sounds")
        @Comment(Constants.CONFIG_COMMENT_SOUNDS_RANDOM)
        var randomSound = true

        @Setting("quit-sounds")
        @Comment("These sound(s) will be played to online players on player quit\n  ${Constants.CONFIG_COMMENT_SOUNDS_LINE2}")
        var sounds = "minecraft:entity.enderman.teleport"

        @Setting("quit-broadcasts")
        @Comment("These messages will be sent to online players on player quit. Also known as quit messages")
        val broadcasts = arrayListOf("<hover:show_text:'<yellow>Username</yellow><gray>:</gray> {user}'>{nick}</hover> <yellow>left the game")

        @Setting("quit-commands")
        @Comment("These commands will be run by the console on Player quit.\n  Example: \"broadcast %player_name% left\"")
        val commands = arrayListOf<String>()
    }

    companion object {
        private val MAPPER = ObjectMapper.factoryBuilder().addNodeResolver(NodeResolver.onlyWithSetting()).build().get(JoinQuitConfig::class.java)

        fun loadFrom(node: CommentedConfigurationNode, name: String?): JoinQuitConfig {
            return MAPPER.load(node).populate(name)
        }
    }

    fun saveTo(node: CommentedConfigurationNode) {
        MAPPER.save(this, node)
    }

    fun populate(name: String?): JoinQuitConfig {
        this.name = name
        return this
    }

    private val announcerPlus: AnnouncerPlus by inject()
    private val chat: Chat by inject()
    private var name: String? = null

    fun onJoin(player: Player) {
        /* name is null for first-join config */
        if (name == null || player.hasPermission("announcerplus.join.$name")) {
            chat.send(player, announcerPlus.configManager.parse(player, join.messages))
            announcerPlus.schedule {
                waitFor(3L)
                if (!isVanished(player)) {
                    val players = ImmutableList.copyOf(Bukkit.getOnlinePlayers())
                    switchContext(SynchronizationContext.ASYNC)
                    val m = announcerPlus.configManager.parse(player, join.broadcasts)
                    for (p in players) {
                        if (p.name != player.name) {
                            if (announcerPlus.perms!!.playerHas(p, permission) || permission == "") {
                                chat.send(p, m)
                                if (join.broadcastSounds != "") {
                                    chat.playSounds(p, join.randomBroadcastSound, join.broadcastSounds)
                                }
                            }
                        }
                    }
                    switchContext(SynchronizationContext.SYNC)
                    for (command in join.commands) {
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), announcerPlus.configManager.parse(player, command))
                    }
                    for (command in join.asPlayerCommands) {
                        Bukkit.dispatchCommand(player, announcerPlus.configManager.parse(player, command))
                    }
                }
            }
            announcerPlus.schedule(SynchronizationContext.ASYNC) {
                join.title.displayIfEnabled(player)
                join.actionBar.displayIfEnabled(player)
                join.bossBar.displayIfEnabled(player)
                join.toast.queueDisplay(player)
                if (join.sounds != "") {
                    chat.playSounds(player, join.randomSound, join.sounds)
                }
            }
        }
    }

    fun onQuit(player: Player) {
        if (name == null) {
            return
        }
        if (player.hasPermission("announcerplus.quit.$name") && !isVanished(player)) {
            val players = ImmutableList.copyOf(Bukkit.getOnlinePlayers())
            val m = announcerPlus.configManager.parse(player, quit.broadcasts)
            for (p in players) {
                if (p.name != player.name) {
                    if (announcerPlus.perms!!.playerHas(p, permission) || permission == "") {
                        chat.send(p, m)
                        if (quit.sounds != "") {
                            chat.playSounds(p, quit.randomSound, quit.sounds)
                        }
                    }
                }
            }
            for (command in quit.commands) {
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