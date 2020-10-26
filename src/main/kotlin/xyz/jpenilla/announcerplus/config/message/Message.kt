package xyz.jpenilla.announcerplus.config.message

import org.spongepowered.configurate.objectmapping.ConfigSerializable
import org.spongepowered.configurate.objectmapping.meta.Comment
import xyz.jpenilla.announcerplus.util.Constants

@ConfigSerializable
class Message {

    @Comment("The lines of text for this message. Can be empty for no chat messages.")
    val messageText = arrayListOf<String>()

    @Comment("Configure the Action Bar for this message")
    var actionBar = ActionBarSettings()

    @Comment("Configure the Boss Bar for this message")
    var bossBar = BossBarSettings()

    @Comment("Configure the Title for this message")
    var title = TitleSettings()

    @Comment("Configure the Toast/Achievement/Advancement for this message")
    var toast = ToastSettings()

    @Comment("The sounds to play when this message is sent\n  ${Constants.CONFIG_COMMENT_SOUNDS_LINE2}")
    var sounds = ""

    @Comment(Constants.CONFIG_COMMENT_SOUNDS_RANDOM)
    var soundsRandomized = true

    @Comment("These commands will run as console on broadcast. Example: \"broadcast This is a test\"")
    val commands = arrayListOf<String>()

    @Comment("These commands will run as console once per player on broadcast. Example: \"minecraft:give %player_name% dirt\"")
    val perPlayerCommands = arrayListOf<String>()

    @Comment("These commands will run once per player, as the player on broadcast. Example: \"ap about\"")
    val asPlayerCommands = arrayListOf<String>()

    constructor()
    constructor(text: List<String>) {
        this.messageText.addAll(text)
    }

    fun sounds(sounds: String): Message {
        this.sounds = sounds
        return this
    }

    fun actionBar(actionBar: ActionBarSettings): Message {
        this.actionBar = actionBar
        return this
    }

    fun bossBar(bossBar: BossBarSettings): Message {
        this.bossBar = bossBar
        return this
    }

    fun title(title: TitleSettings): Message {
        this.title = title
        return this
    }

    fun toast(toast: ToastSettings): Message {
        this.toast = toast
        return this
    }
}