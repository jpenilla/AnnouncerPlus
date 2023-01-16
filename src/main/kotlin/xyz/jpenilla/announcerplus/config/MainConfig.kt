/*
 * This file is part of AnnouncerPlus, licensed under the MIT License.
 *
 * Copyright (c) 2020-2023 Jason Penilla
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package xyz.jpenilla.announcerplus.config

import org.bukkit.permissions.PermissionDefault
import org.spongepowered.configurate.CommentedConfigurationNode
import org.spongepowered.configurate.kotlin.extensions.get
import org.spongepowered.configurate.objectmapping.ConfigSerializable
import org.spongepowered.configurate.objectmapping.meta.Comment
import xyz.jpenilla.announcerplus.config.visitor.DuplicateCommentRemovingVisitor
import xyz.jpenilla.announcerplus.util.addDefaultPermission

@ConfigSerializable
class MainConfig {

  @Comment("Set whether to check for updates on startup")
  var checkForUpdates = true

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

  @Comment(
    "This setting enables or disables the first-join.conf\n" +
      "If enabled, on a player's first join the first-join.conf will be used instead of any other join configs."
  )
  var firstJoinConfigEnabled = false

  @Comment(
    "Here you can define randomized join configs.\n" +
      "  To assign randomized join configs, give the announcerplus.randomjoin.demo permission, replacing demo with your randomized config\n" +
      "  NOTE: The randomized config named 'demo' will be ignored by the plugin. You must choose a new name to use this feature."
  )
  val randomJoinConfigs = hashMapOf(
    "demo" to arrayListOf(
      JoinQuitPair("config_a", 0.1),
      JoinQuitPair("config_b", 0.2)
    )
  )

  @Comment(
    "Here you can define randomized quit configs.\n" +
      "  To assign randomized quit configs, give the announcerplus.randomquit.demo permission, replacing demo with your randomized config\n" +
      "  NOTE: The randomized config named 'demo' will be ignored by the plugin. You must choose a new name to use this feature."
  )
  val randomQuitConfigs = hashMapOf(
    "demo" to arrayListOf(
      JoinQuitPair("config_a", 0.2),
      JoinQuitPair("config_b", 0.1)
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
    fun loadFrom(node: CommentedConfigurationNode): MainConfig {
      val config = node.get<MainConfig>() ?: error("Failed to deserialize MainConfig")
      config.randomJoinConfigs.keys.forEach {
        addDefaultPermission("announcerplus.randomjoin.$it", PermissionDefault.FALSE)
      }
      config.randomQuitConfigs.keys.forEach {
        addDefaultPermission("announcerplus.randomquit.$it", PermissionDefault.FALSE)
      }
      return config
    }
  }

  fun saveTo(node: CommentedConfigurationNode) {
    node.set(this)

    node.visit(DuplicateCommentRemovingVisitor())
  }
}
