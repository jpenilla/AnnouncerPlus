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
package xyz.jpenilla.announcerplus.util

import cloud.commandframework.ArgumentDescription
import cloud.commandframework.arguments.parser.ArgumentParseResult
import cloud.commandframework.kotlin.extension.argumentDescription
import net.kyori.adventure.nbt.BinaryTag
import net.kyori.adventure.nbt.CompoundBinaryTag
import net.kyori.adventure.nbt.ListBinaryTag
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.empty
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.ComponentLike
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextColor.color
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.util.HSVLike
import org.bukkit.Bukkit
import org.bukkit.permissions.Permission
import org.bukkit.permissions.PermissionDefault
import kotlin.random.Random.Default.nextDouble
import kotlin.random.Random.Default.nextFloat

fun dispatchCommandAsConsole(command: String): Boolean =
  Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command)

fun addDefaultPermission(permission: String, default: PermissionDefault) {
  Bukkit.getPluginManager().getPermission(permission)?.apply {
    Bukkit.getPluginManager().removePermission(this)
  }
  Bukkit.getPluginManager().addPermission(Permission(permission, default))
}

fun description(description: String = ""): ArgumentDescription = argumentDescription(description)

fun <R> failure(message: ComponentLike): ArgumentParseResult<R> = ArgumentParseResult.failure(ComponentException(message))

val miniMessage: MiniMessage = MiniMessage.miniMessage()

fun miniMessage(message: String): Component = miniMessage.deserialize(message)

fun randomColor(): TextColor = color(
  HSVLike.hsvLike(
    nextFloat(),
    nextDouble(0.5, 1.0).toFloat(),
    nextDouble(0.5, 1.0).toFloat()
  )
)

fun compoundBinaryTag(builder: CompoundBinaryTag.Builder.() -> Unit): CompoundBinaryTag =
  CompoundBinaryTag.builder().apply(builder).build()

fun listBinaryTag(builder: ListBinaryTag.Builder<BinaryTag>.() -> Unit): ListBinaryTag =
  ListBinaryTag.builder().apply(builder).build()

fun listBinaryTag(vararg tags: BinaryTag): ListBinaryTag =
  listBinaryTag { tags.forEach(this::add) }

fun ofChildren(vararg children: ComponentLike): TextComponent {
  if (children.isEmpty()) return empty()
  return text().append(*children).build()
}
