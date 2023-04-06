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

import net.kyori.adventure.key.Key.key
import net.kyori.adventure.sound.Sound
import net.kyori.adventure.sound.Sound.sound
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.permissions.PermissionDefault
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.spongepowered.configurate.CommentedConfigurationNode
import org.spongepowered.configurate.NodePath.path
import org.spongepowered.configurate.kotlin.extensions.get
import org.spongepowered.configurate.objectmapping.ConfigSerializable
import org.spongepowered.configurate.objectmapping.meta.Comment
import org.spongepowered.configurate.objectmapping.meta.Setting
import org.spongepowered.configurate.transformation.ConfigurationTransformation
import xyz.jpenilla.announcerplus.AnnouncerPlus
import xyz.jpenilla.announcerplus.config.message.ActionBarSettings
import xyz.jpenilla.announcerplus.config.message.BossBarSettings
import xyz.jpenilla.announcerplus.config.message.MessageElement
import xyz.jpenilla.announcerplus.config.message.TitleSettings
import xyz.jpenilla.announcerplus.config.message.ToastSettings
import xyz.jpenilla.announcerplus.config.visitor.DuplicateCommentRemovingVisitor
import xyz.jpenilla.announcerplus.util.Constants
import xyz.jpenilla.announcerplus.util.addDefaultPermission
import xyz.jpenilla.announcerplus.util.dispatchCommandAsConsole
import xyz.jpenilla.announcerplus.util.playSounds
import xyz.jpenilla.announcerplus.util.schedule
import xyz.jpenilla.announcerplus.util.scheduleAsync
import xyz.jpenilla.announcerplus.util.scheduleGlobal
import xyz.jpenilla.pluginbase.legacy.Chat
import xyz.jpenilla.pluginbase.legacy.Environment

@ConfigSerializable
class JoinQuitConfig : SelfSavable<CommentedConfigurationNode>, KoinComponent {

  @Setting("visible-permission")
  @Comment("If set to something other than \"\", this setting's value will be the permission required to see these join/quit messages when they are broadcasted for a player")
  var permission = ""

  @Setting("join-section")
  @Comment("Player Join related settings")
  var join = JoinSection()

  @Setting("quit-section")
  @Comment("Player Quit related settings")
  var quit = QuitSection()

  @Comment("Should duplicate comments be removed from this config?")
  var removeDuplicateComments = true

  @ConfigSerializable
  class JoinSection {
    @Setting("randomize-join-broadcast-sounds")
    @Comment(Constants.CONFIG_COMMENT_SOUNDS_RANDOM)
    var randomBroadcastSound = true

    @Setting("join-broadcast-sounds")
    @Comment("These sound(s) will be played to online players on player join.")
    val broadcastSounds = arrayListOf(sound(key("minecraft:entity.enderman.teleport"), Sound.Source.MASTER, 1.0f, 1.0f))

    @Setting("randomize-join-sounds")
    @Comment(Constants.CONFIG_COMMENT_SOUNDS_RANDOM)
    var randomSound = true

    @Setting("join-sounds")
    @Comment("These sound(s) will be played to the joining player.")
    val sounds = arrayListOf(
      sound(key("minecraft:entity.strider.happy"), Sound.Source.MASTER, 1.0f, 1.0f),
      sound(key("minecraft:entity.villager.ambient"), Sound.Source.MASTER, 1.0f, 1.0f),
      sound(key("minecraft:block.note_block.cow_bell"), Sound.Source.MASTER, 1.0f, 1.0f)
    )

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
    val broadcasts =
      arrayListOf("<hover:show_text:'<yellow>Username</yellow><gray>:</gray> {user}'>{nick}</hover> <yellow>joined the game")

    @Setting("join-commands")
    @Comment("These commands will be run by the console on Player join.\n  Example: \"minecraft:give %player_name% dirt\"")
    val commands = arrayListOf<String>()

    @Setting("as-player-join-commands")
    @Comment("These commands will be run as the Player on Player join.\n  Example: \"ap about\"")
    val asPlayerCommands = arrayListOf<String>()

    @Setting("title-settings")
    @Comment("Settings relating to showing a title to the joining Player")
    var title = TitleSettings(
      1, 7, 1,
      "<bold><italic><gradient:green:blue:green:{animate:scroll:0.1}>Welcome</gradient><yellow>{animate:flash:!:!!:!!!:10}",
      "<{animate:pulse:red:blue:yellow:green:10}>{user}"
    )

    @Setting("action-bar-settings")
    @Comment("Settings relating to showing an Action Bar to the joining Player")
    var actionBar = ActionBarSettings(
      false, 8,
      "<gradient:green:blue:green:{animate:scroll:0.1}>|||||||||||||||||||||||||||||||||||||||</gradient>"
    )

    @Setting("boss-bar-settings")
    @Comment("Settings relating to showing a Boss Bar to the joining Player")
    var bossBar = BossBarSettings()

    @Setting("toast-settings")
    @Comment("Configure the Toast that will be showed to the joining player")
    var toast = ToastSettings(
      Material.DIAMOND, ToastSettings.FrameType.CHALLENGE,
      "<gradient:green:blue><bold><italic>AnnouncerPlus", "<rainbow>Welcome to the server!"
    )

    fun messageElements(): Collection<MessageElement> = setOf(
      actionBar,
      bossBar,
      title,
      toast
    )
  }

  @ConfigSerializable
  class QuitSection {
    @Setting("randomize-quit-sounds")
    @Comment(Constants.CONFIG_COMMENT_SOUNDS_RANDOM)
    var randomSound = true

    @Setting("quit-sounds")
    @Comment("These sound(s) will be played to online players on player quit")
    val sounds = arrayListOf(sound(key("minecraft:entity.enderman.teleport"), Sound.Source.MASTER, 1.0f, 1.0f))

    @Setting("quit-broadcasts")
    @Comment("These messages will be sent to online players on player quit. Also known as quit messages")
    val broadcasts =
      arrayListOf("<hover:show_text:'<yellow>Username</yellow><gray>:</gray> {user}'>{nick}</hover> <yellow>left the game")

    @Setting("quit-commands")
    @Comment("These commands will be run by the console on Player quit.\n  Example: \"broadcast %player_name% left\"")
    val commands = arrayListOf<String>()
  }

  fun populate(name: String?): JoinQuitConfig =
    this.apply { this.name = name }

  private val announcerPlus: AnnouncerPlus by inject()
  private val chat: Chat by inject()

  @Transient
  private var name: String? = null

  fun onJoin(player: Player) {
    // name is null for first-join config
    if (name != null && !player.hasPermission("announcerplus.join.$name")) return
    chat.send(player, announcerPlus.configManager.parse(player, join.messages))
    announcerPlus.schedule(player, 3L) {
      if (!isVanished(player)) {
        announcerPlus.scheduleAsync {
          Bukkit.getOnlinePlayers().toList().forEach { onlinePlayer ->
            if (onlinePlayer.name != player.name) {
              if (announcerPlus.perms!!.playerHas(onlinePlayer, permission) || permission.isEmpty()) {
                chat.send(onlinePlayer, announcerPlus.configManager.parse(player, join.broadcasts))
                if (join.broadcastSounds.isNotEmpty()) {
                  announcerPlus.audiences().player(onlinePlayer).playSounds(join.broadcastSounds, join.randomBroadcastSound)
                }
              }
            }
          }
        }
        announcerPlus.scheduleGlobal {
          join.commands.forEach { dispatchCommandAsConsole(announcerPlus.configManager.parse(player, it)) }
        }
        join.asPlayerCommands.forEach { player.performCommand(announcerPlus.configManager.parse(player, it)) }
      }
    }
    announcerPlus.scheduleAsync(if (Environment.majorMinecraftVersion() <= 12) 5L else 0L) {
      join.messageElements().forEach { it.displayIfEnabled(player) }
      if (join.sounds.isNotEmpty()) {
        announcerPlus.audiences().player(player).playSounds(join.sounds, join.randomSound)
      }
    }
  }

  fun onQuit(player: Player) {
    if (name == null || !player.hasPermission("announcerplus.quit.$name") || isVanished(player)) return
    for (onlinePlayer in Bukkit.getOnlinePlayers()) {
      if (onlinePlayer.name != player.name) {
        if (announcerPlus.perms!!.playerHas(onlinePlayer, permission) || permission.isEmpty()) {
          chat.send(onlinePlayer, announcerPlus.configManager.parse(player, quit.broadcasts))
          if (quit.sounds.isNotEmpty()) {
            announcerPlus.audiences().player(onlinePlayer).playSounds(quit.sounds, quit.randomSound)
          }
        }
      }
    }
    announcerPlus.scheduleGlobal {
      quit.commands.forEach { dispatchCommandAsConsole(announcerPlus.configManager.parse(player, it)) }
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

  override fun saveTo(node: CommentedConfigurationNode) {
    node.set(this)
    node.node("version").apply {
      set(LATEST_VERSION)
      comment("The version of this configuration. For internal use only, do not modify.")
    }

    if (removeDuplicateComments) {
      node.visit(DuplicateCommentRemovingVisitor())
    }
  }

  companion object : ConfigurationUpgrader, NamedConfigurationFactory<JoinQuitConfig, CommentedConfigurationNode> {
    const val LATEST_VERSION = 0

    override val upgrader = ConfigurationTransformation.versionedBuilder()
      .addVersion(LATEST_VERSION, initial())
      .build()

    private fun initial(): ConfigurationTransformation = ConfigurationTransformation.builder()
      .addAction(path("join-section", "join-sounds"), Transformations.upgradeSoundsString)
      .addAction(path("join-section", "join-broadcast-sounds"), Transformations.upgradeSoundsString)
      .addAction(path("quit-section", "quit-sounds"), Transformations.upgradeSoundsString)
      .build()

    override fun loadFrom(node: CommentedConfigurationNode, configName: String?): JoinQuitConfig {
      val config = node.get<JoinQuitConfig>()?.populate(configName) ?: error("Failed to deserialize JoinQuitConfig")
      if (configName != null) {
        addDefaultPermission("announcerplus.join.${config.name}", PermissionDefault.FALSE)
        addDefaultPermission("announcerplus.quit.${config.name}", PermissionDefault.FALSE)
      }
      return config
    }
  }
}
