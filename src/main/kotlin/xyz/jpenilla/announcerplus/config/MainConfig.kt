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
}