package xyz.jpenilla.announcerplus.config

import org.spongepowered.configurate.CommentedConfigurationNode
import org.spongepowered.configurate.objectmapping.ConfigSerializable
import org.spongepowered.configurate.objectmapping.ObjectMapper
import org.spongepowered.configurate.objectmapping.meta.Comment

@ConfigSerializable
class MainConfig {

    @Comment("Here you can define custom placeholders for use in plugin messages\n  These placeholders can be used like \"{placeholder}\", i.e. \"{nick}\" or \"{r}rainbow text{rc}\"")
    val customPlaceholders = hashMapOf(
            "nick" to "%essentials_nickname%",
            "user" to "%player_name%",
            "prefix1" to "<bold><blue>[<green>!</green>]</blue></bold>",
            "r" to "<rainbow>",
            "rc" to "</rainbow>"
    )

    @Comment("This setting enables or disables all timed broadcasts")
    var enableBroadcasts = true

    @Comment("This setting enables or disables all Join event features")
    var joinFeatures = true

    @Comment("This setting enables or disables all Quit event features")
    var quitFeatures = true

    @Comment("This setting enables or disables the first-join.conf\n" +
            "If enabled, on a player's first join the first-join.conf will be used instead of any other join configs.")
    var firstJoinConfigEnabled = false

    @Comment("Here you can define randomized join configs.\n" +
            "  To assign randomized join configs, give the announcerplus.randomjoin.demo permission, replacing demo with your randomized config\n" +
            "  WARNING: If you are OP make sure to negate the appropriate permissions so that you do not get duplicate join/quit messages")
    val randomJoinConfigs = hashMapOf(
            "demo" to arrayListOf(
                    JoinQuitPair("default", 0.1),
                    JoinQuitPair("default", 0.2)
            )
    )

    @Comment("Here you can define randomized join configs.\n" +
            "  To assign randomized quit configs, give the announcerplus.randomquit.demo permission, replacing demo with your randomized config\n" +
            "  NOTE: The randomized config named 'demo' will be ignored by the plugin. You must choose a new name to use this feature.\n" +
            "  WARNING: If you are OP make sure to negate the appropriate permissions so that you do not get duplicate join/quit messages")
    val randomQuitConfigs = hashMapOf(
            "demo" to arrayListOf(
                    JoinQuitPair("default", 0.2),
                    JoinQuitPair("default", 0.1)
            )
    )

    @ConfigSerializable
    class JoinQuitPair {
        constructor()
        constructor(configName: String, weight: Double) {
            this.configName = configName
            this.weight = weight
        }

        @Comment("The name of the config (the text before .conf in the file name)")
        var configName = "default"

        @Comment("The weight of this config for random selection, 0.0-1.0")
        var weight = 0.1
    }

    companion object {
        private val MAPPER = ObjectMapper.factory().get(MainConfig::class.java)

        fun loadFrom(node: CommentedConfigurationNode): MainConfig {
            return MAPPER.load(node)
        }
    }

    fun saveTo(node: CommentedConfigurationNode) {
        MAPPER.save(this, node)
    }
}