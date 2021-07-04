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
package xyz.jpenilla.announcerplus.config

import net.kyori.adventure.key.Key.key
import net.kyori.adventure.sound.Sound
import net.kyori.adventure.sound.Sound.sound
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.NodePath.path
import org.spongepowered.configurate.kotlin.extensions.get
import org.spongepowered.configurate.transformation.ConfigurationTransformation
import org.spongepowered.configurate.transformation.TransformAction

object Transformations {
  interface ConfigurationUpgrader {
    val upgrader: ConfigurationTransformation.Versioned

    fun <N : ConfigurationNode> upgrade(node: N): UpgradeResult<N> {
      val original = upgrader.version(node)
      upgrader.apply(node)
      val new = upgrader.version(node)
      return UpgradeResult(original, new, node)
    }
  }

  private val soundsStringToList = TransformAction { _, value ->
    val soundStrings = value.get<String>()?.split(",") ?: emptyList()
    val sounds = soundStrings.map {
      sound(key(it), Sound.Source.MASTER, 1.0f, 1.0f)
    }
    value.setList(Sound::class.java, sounds)
    return@TransformAction null
  }

  object MessageConfig : ConfigurationUpgrader {
    const val LATEST_VERSION = 0

    override val upgrader = ConfigurationTransformation.versionedBuilder()
      .addVersion(LATEST_VERSION, initial())
      .build()

    private fun initial() = ConfigurationTransformation.builder()
      .addAction(path("messages")) { path, value ->
        for (childNode in value.childrenList()) {
          val soundsNode = childNode.node("sounds")
          // the path here is wrong, but we don't use it
          soundsStringToList.visitPath(path, soundsNode)
        }
        return@addAction null
      }
      .build()
  }

  object JoinQuitConfig : ConfigurationUpgrader {
    const val LATEST_VERSION = 0

    override val upgrader = ConfigurationTransformation.versionedBuilder()
      .addVersion(LATEST_VERSION, initial())
      .build()

    private fun initial(): ConfigurationTransformation = ConfigurationTransformation.builder()
      .addAction(path("join-section", "join-sounds"), soundsStringToList)
      .addAction(path("join-section", "join-broadcast-sounds"), soundsStringToList)
      .addAction(path("quit-section", "quit-sounds"), soundsStringToList)
      .build()
  }
}

data class UpgradeResult<N : ConfigurationNode>(
  val originalVersion: Int,
  val newVersion: Int,
  val node: N,
  val didUpgrade: Boolean = originalVersion != newVersion
)
