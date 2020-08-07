package xyz.jpenilla.announcerplus.config.message

import ninja.leaping.configurate.objectmapping.Setting
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable
import xyz.jpenilla.announcerplus.util.Constants

@ConfigSerializable
class Message {
    constructor()
    constructor(text: List<String>) {
        this.text.addAll(text)
    }

    @Setting(value = "message-text", comment = "The lines of text for this message. Can be empty for no chat messages.")
    val text = arrayListOf<String>()

    @Setting(value = "action-bar", comment = "Configure the Action Bar for this message")
    var actionBar = ActionBarSettings()

    @Setting(value = "title", comment = "Configure the Title for this message")
    var title = TitleSettings()

    @Setting(value = "toast", comment = "Configure the Toast/Achievement/Advancement for this message")
    var toast = ToastSettings()

    @Setting(value = "sounds", comment = "The sounds to play when this message is sent\n  ${Constants.CONFIG_COMMENT_SOUNDS_LINE2}")
    var sounds = ""

    @Setting(value = "sounds-randomized", comment = Constants.CONFIG_COMMENT_SOUNDS_RANDOM)
    var randomSound = true

    @Setting(value = "commands", comment = "These commands will run as console on broadcast. Example: \"broadcast This is a test\"")
    val commands = arrayListOf<String>()

    @Setting(value = "per-player-commands", comment = "These commands will run as console once per player on broadcast. Example: \"minecraft:give %player_name% dirt\"")
    val perPlayerCommands = arrayListOf<String>()

    @Setting(value = "as-player-commands", comment = "These commands will run once per player, as the player on broadcast. Example: \"ap about\"")
    val asPlayerCommands = arrayListOf<String>()

    fun sounds(sounds: String): Message {
        this.sounds = sounds
        return this
    }

    fun actionBar(actionBar: ActionBarSettings): Message {
        this.actionBar = actionBar
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