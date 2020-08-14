package xyz.jpenilla.announcerplus.config

import ninja.leaping.configurate.commented.CommentedConfigurationNode
import ninja.leaping.configurate.objectmapping.ObjectMapper
import ninja.leaping.configurate.objectmapping.Setting
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable

@ConfigSerializable
class MainConfig {
    companion object {
        private val MAPPER = ObjectMapper.forClass(MainConfig::class.java)

        fun loadFrom(node: CommentedConfigurationNode): MainConfig {
            return MAPPER.bindToNew().populate(node)
        }
    }

    fun saveTo(node: CommentedConfigurationNode) {
        MAPPER.bind(this).serialize(node)
    }

    @Setting(value = "custom-placeholders", comment = "Here you can define custom placeholders for use in plugin messages\n  These placeholders can be used like \"{placeholder}\", i.e. \"{nick}\" or \"{r}rainbow text{rc}\"")
    val placeholders = hashMapOf(
            Pair("nick", "%essentials_nickname%"),
            Pair("user", "%player_name%"),
            Pair("prefix1", "<bold><blue>[<green>!</green>]</blue></bold>"),
            Pair("r", "<rainbow>"),
            Pair("rc", "</rainbow>"))

    @Setting(value = "join-features", comment = "This setting enables or disables all Join event features")
    var joinEvent = true

    @Setting(value = "quit-features", comment = "This setting enables or disables all Quit event features")
    var quitEvent = true

    @Setting(value = "random-join-configs", comment = "Here you can define randomized join configs.\n" +
            "  To assign randomized join configs, give the announcerplus.randomjoin.demo permission, replacing demo with your randomized config\n" +
            "  WARNING: If you are OP make sure to negate the appropriate permissions so that you do not get duplicate join/quit messages")
    val randomJoinConfigs = hashMapOf(
            Pair("demo", arrayListOf(
                    JoinQuitPair("default", 0.1),
                    JoinQuitPair("default", 0.2)))
    )

    @Setting(value = "random-quit-configs", comment = "Here you can define randomized join configs.\n" +
            "  To assign randomized quit configs, give the announcerplus.randomquit.demo permission, replacing demo with your randomized config\n" +
            "  NOTE: The randomized config named 'demo' will be ignored by the plugin. You must choose a new name to use this feature.\n" +
            "  WARNING: If you are OP make sure to negate the appropriate permissions so that you do not get duplicate join/quit messages")
    val randomQuitConfigs = hashMapOf(
            Pair("demo", arrayListOf(
                    JoinQuitPair("default", 0.2),
                    JoinQuitPair("default", 0.1)))
    )

    @ConfigSerializable
    class JoinQuitPair {
        constructor()
        constructor(configName: String, weight: Double) {
            this.configName = configName
            this.weight = weight
        }

        @Setting(value = "config-name", comment = "The name of the config (the text before .conf in the file name)")
        var configName = "default"

        @Setting(value = "config-weight", comment = "The weight of this config for random selection, 0.0-1.0")
        var weight = 0.1
    }
}