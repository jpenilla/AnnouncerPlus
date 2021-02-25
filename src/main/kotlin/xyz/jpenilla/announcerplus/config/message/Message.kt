/*
 * This file is part of AnnouncerPlus, licensed under the MIT License.
 *
 * Copyright (c) 2020-2021 Jason Penilla
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
package xyz.jpenilla.announcerplus.config.message

import com.google.common.collect.ImmutableSet
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

  fun messageElements(): Collection<MessageElement> = ImmutableSet.of(
    actionBar,
    bossBar,
    title,
    toast
  )

  fun sounds(sounds: String): Message = apply { this.sounds = sounds }

  fun actionBar(actionBar: ActionBarSettings): Message = apply { this.actionBar = actionBar }

  fun bossBar(bossBar: BossBarSettings): Message = apply { this.bossBar = bossBar }

  fun title(title: TitleSettings): Message = apply { this.title = title }

  fun toast(toast: ToastSettings): Message = apply { this.toast = toast }

}
