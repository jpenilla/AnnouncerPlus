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
package xyz.jpenilla.announcerplus.config.serializer

import net.kyori.adventure.key.Key
import net.kyori.adventure.sound.Sound
import net.kyori.adventure.sound.Sound.sound
import org.spongepowered.configurate.CommentedConfigurationNode
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.kotlin.extensions.get
import org.spongepowered.configurate.kotlin.extensions.typedSet
import org.spongepowered.configurate.serialize.SerializationException
import org.spongepowered.configurate.serialize.TypeSerializer
import java.lang.reflect.Type

object SoundSerializer : TypeSerializer<Sound> {
  private const val NAME = "name"
  private const val SOURCE = "source"
  private const val PITCH = "pitch"
  private const val VOLUME = "volume"
  private const val SEED = "seed"

  @Throws(SerializationException::class)
  override fun deserialize(type: Type, value: ConfigurationNode): Sound? {
    if (value.empty()) {
      return null
    }
    val name = value.node(NAME).get<Key>()
    val source = value.node(SOURCE).get<Sound.Source>()
    val pitch = value.node(PITCH).getFloat(1.0f)
    val volume = value.node(VOLUME).getFloat(1.0f)
    val seed = value.node(SEED).takeUnless { it.virtual() }?.long
    if (name == null || source == null) {
      throw SerializationException("A name and source are required to deserialize a Sound")
    }
    return sound { sound ->
      sound.type(name)
      sound.source(source)
      sound.volume(volume)
      sound.pitch(pitch)
      seed?.let { sound.seed(it) }
    }
  }

  @Throws(SerializationException::class)
  override fun serialize(type: Type, obj: Sound?, value: ConfigurationNode) {
    if (obj == null) {
      value.set(null)
      return
    }

    val nameNode = value.node(NAME)
    nameNode.typedSet(obj.name())
    addComment(nameNode, "The resource location of this sound (e.g 'minecraft:ambient.cave' or 'my_plugin:custom_sound').")

    val sourceNode = value.node(SOURCE)
    sourceNode.typedSet(obj.source())
    addComment(sourceNode, "A Sound Source telling the game where the sound is coming from. Possible values: ${Sound.Source.NAMES.keys().joinToString(", ", "[", "]")}")

    val pitchNode = value.node(PITCH)
    pitchNode.set(obj.pitch())
    addComment(pitchNode, "A floating-point number in the range [0.0f,2.0f] representing which pitch the sound should be played at.")

    val volumeNode = value.node(VOLUME)
    volumeNode.set(obj.volume())
    addComment(volumeNode, "A floating-point number in the range [0.0f,∞) representing how loud the sound should be played. Increasing volume does not actually play the sound louder, but increases the radius of where it can be heard.")

    val seedNode = value.node(SEED)
    if (obj.seed().isPresent) {
      seedNode.set(obj.seed().asLong)
      addComment(seedNode, "The seed used for playback of weighted sound effects. When the seed is not provided, a random one will be used instead.")
    } else {
      seedNode.set(null)
    }
  }

  private fun <N : ConfigurationNode> addComment(node: N, comment: String) {
    if (node is CommentedConfigurationNode) {
      node.comment(comment)
    }
  }
}
