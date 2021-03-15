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
package xyz.jpenilla.announcerplus.task

import com.google.gson.Gson
import com.google.gson.JsonObject
import io.papermc.lib.PaperLib.getMinecraftVersion
import org.bukkit.entity.Player
import org.koin.core.KoinComponent
import org.koin.core.inject
import xyz.jpenilla.announcerplus.AnnouncerPlus
import xyz.jpenilla.announcerplus.config.message.ToastSettings
import xyz.jpenilla.announcerplus.util.asyncTimer
import xyz.jpenilla.announcerplus.util.runSync
import xyz.jpenilla.jmplib.Crafty
import java.lang.reflect.Modifier
import java.util.concurrent.ConcurrentLinkedDeque
import kotlin.random.Random.Default.nextInt

class ToastTask : KoinComponent {
  private val announcerPlus: AnnouncerPlus by inject()
  private val queuedToasts = ConcurrentLinkedDeque<QueuedToast>()

  private val toastTask = announcerPlus.asyncTimer(0L, 1L) {
    if (queuedToasts.isNotEmpty()) {
      val toast = queuedToasts.removeFirst()
      if (toast.player.isOnline) {
        displayToastImmediately(toast)
      }
    }
  }

  fun queueToast(toastSettings: ToastSettings, player: Player) {
    queuedToasts.add(QueuedToast(player, toastSettings))
  }

  fun cancel() {
    toastTask.cancel()
  }

  data class QueuedToast(val player: Player, val toast: ToastSettings)

  private fun displayToastImmediately(queuedToast: QueuedToast) {
    val (player, toast) = queuedToast
    announcerPlus.runSync {
      val minecraftKey =
        MinecraftKey_ctr.newInstance(announcerPlus.name.toLowerCase(), nextInt(1000000).toString())
      val serializedAdvancement = if (getMinecraftVersion() >= 16) {
        SerializedAdvancement_deserialize(
          null,
          toast.getJson(player),
          LootDeserializationContext_ctr.newInstance(
            minecraftKey,
            MinecraftServer_getLootPredicateManager(MinecraftServer_getServer())
          )
        )
      } else {
        ChatDeserializer_deserialize(
          null,
          AdvancementDataWorld_DESERIALIZER,
          announcerPlus.gson.toJson(toast.getJson(player)),
          SerializedAdvancement_class
        )
      }
      val advancement = SerializedAdvancement_getAdvancement(serializedAdvancement, minecraftKey)
      val nmsPlayer = player.handle
      val advancementData = EntityPlayer_getAdvancementData(nmsPlayer)
      val advancementProgress = AdvancementDataPlayer_getProgress(advancementData, advancement)
      (AdvancementProgress_remainingCriteria(advancementProgress) as Iterable<*>).forEach {
        AdvancementDataPlayer_grantCriteria(advancementData, advancement, it)
      }
      announcerPlus.runSync(20L) {
        (AdvancementProgress_awardedCriteria(advancementProgress) as Iterable<*>).forEach {
          AdvancementDataPlayer_revokeCriteria(advancementData, advancement, it)
        }
      }
    }
  }

  private companion object Reflect {
    val MinecraftServer_class = Crafty.needNmsClass("MinecraftServer")
    val MinecraftServer_getServer =
      Crafty.findStaticMethod(MinecraftServer_class, "getServer", MinecraftServer_class)
        ?: error("Cannot find getServer")

    val MinecraftKey_class = Crafty.needNmsClass("MinecraftKey")
    val MinecraftKey_ctr = MinecraftKey_class.getDeclaredConstructor(String::class.java, String::class.java)
      ?: error("Cannot find MinecraftKey constructor")

    val Advancement_class = Crafty.needNmsClass("Advancement")
    val SerializedAdvancement_class = Crafty.needNmsClass("Advancement\$SerializedAdvancement")

    val SerializedAdvancement_getAdvancement = SerializedAdvancement_class.declaredMethods.find { method ->
      method.returnType == Advancement_class
        && method.parameterCount == 1
        && method.parameterTypes[0] == MinecraftKey_class
        && !Modifier.isStatic(method.modifiers)
    } ?: error("Cannot find SerializedAdvancement#getAdvancement")

    val EntityPlayer_class = Crafty.needNmsClass("EntityPlayer")
    val CraftPlayer_class = Crafty.needCraftClass("entity.CraftPlayer")
    val CraftPlayer_getHandle = CraftPlayer_class.getMethod("getHandle")
      ?: error("Cannot find CraftPlayer#getHandle")
    val Player.handle: Any
      get() = CraftPlayer_getHandle(this)

    val AdvancementProgress_class = Crafty.needNmsClass("AdvancementProgress")
    val AdvancementDataPlayer_class = Crafty.needNmsClass("AdvancementDataPlayer")
    val EntityPlayer_getAdvancementData = EntityPlayer_class.methods.find { method ->
      method.returnType == AdvancementDataPlayer_class
        && method.parameterCount == 0
    } ?: error("Cannot find EntityPlayer#getAdvancementData")

    val AdvancementDataPlayer_getProgress = AdvancementDataPlayer_class.methods.find { method ->
      method.returnType == AdvancementProgress_class
        && method.parameterCount == 1
        && method.parameterTypes[0] == Advancement_class
    } ?: error("Cannot find AdvancementDataPlayer#getProgress")

    val AdvancementDataPlayer_grantCriteria =
      AdvancementDataPlayer_class.getDeclaredMethod("grantCriteria", Advancement_class, String::class.java)
        ?: error("Cannot find AdvancementDataPlayer#grantCriteria")
    val AdvancementDataPlayer_revokeCriteria = try {
      AdvancementDataPlayer_class.getDeclaredMethod("revokeCritera", Advancement_class, String::class.java) // lol
    } catch (e: NoSuchMethodException) {
      AdvancementDataPlayer_class.getDeclaredMethod("revokeCriteria", Advancement_class, String::class.java)
    } ?: error("Cannot find AdvancementDataPlayer#revokeCriteria")
    val AdvancementProgress_remainingCriteria = AdvancementProgress_class.getDeclaredMethod("getRemainingCriteria")
      ?: error("Cannot find AdvancementProgress#getRemainingCriteria")
    val AdvancementProgress_awardedCriteria = AdvancementProgress_class.getDeclaredMethod("getAwardedCriteria")
      ?: error("Cannot find AdvancementProgress#getAwardedCriteria")

    // start 1.12 -> 1.15
    val ChatDeserializer_class by lazy { Crafty.needNmsClass("ChatDeserializer") }
    val AdvancementDataWorld_class by lazy { Crafty.needNmsClass("AdvancementDataWorld") }

    val ChatDeserializer_deserialize by lazy {
      ChatDeserializer_class.methods.find { method ->
        method.parameterCount == 3
          && method.parameterTypes.contains(Gson::class.java)
          && method.parameterTypes.contains(String::class.java)
          && method.parameterTypes.contains(Class::class.java)
      } ?: error("Cannot find ChatDeserializer.deserialize")
    }

    val AdvancementDataWorld_DESERIALIZER by lazy {
      AdvancementDataWorld_class.getField("DESERIALIZER").get(null)
        ?: error("Cannot find AdvancementDataWorld.DESERIALIZER")
    }
    // end 1.12 -> 1.15

    // start 1.16+
    val LootPredicateManager_class by lazy { Crafty.needNmsClass("LootPredicateManager") }
    val MinecraftServer_getLootPredicateManager by lazy {
      MinecraftServer_class.declaredMethods.find { method ->
        method.returnType == LootPredicateManager_class
          && method.parameterCount == 0
      } ?: error("Cannot find MinecraftServer#getLootPredicateManager")
    }

    val LootDeserializationContext_class by lazy { Crafty.needNmsClass("LootDeserializationContext") }
    val LootDeserializationContext_ctr by lazy {
      LootDeserializationContext_class.getDeclaredConstructor(MinecraftKey_class, LootPredicateManager_class)
        ?: error("Cannot find LootDeserializationContext constructor")
    }

    val SerializedAdvancement_deserialize by lazy {
      SerializedAdvancement_class.declaredMethods.find { method ->
        method.returnType == SerializedAdvancement_class
          && method.parameterCount == 2
          && method.parameterTypes.contains(JsonObject::class.java)
          && method.parameterTypes.contains(LootDeserializationContext_class)
      } ?: error("Cannot find SerializedAdvancement.deserialize")
    }
    // end 1.16+
  }
}
