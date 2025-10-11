/*
 * This file is part of AnnouncerPlus, licensed under the MIT License.
 *
 * Copyright (c) 2020-2024 Jason Penilla
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
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.mojang.serialization.Codec
import com.mojang.serialization.DataResult
import com.mojang.serialization.DynamicOps
import com.mojang.serialization.JsonOps
import org.bukkit.entity.Player
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import xyz.jpenilla.announcerplus.AnnouncerPlus
import xyz.jpenilla.announcerplus.config.message.ToastSettings
import xyz.jpenilla.announcerplus.task.ToastTask.DFU.getError
import xyz.jpenilla.announcerplus.task.ToastTask.DFU.getResult
import xyz.jpenilla.announcerplus.util.TaskHandle
import xyz.jpenilla.announcerplus.util.asyncTimer
import xyz.jpenilla.announcerplus.util.schedule
import xyz.jpenilla.pluginbase.legacy.Crafty
import xyz.jpenilla.pluginbase.legacy.environment.Environment.currentMinecraft
import xyz.jpenilla.pluginbase.legacy.environment.MinecraftReleases.v1_16
import xyz.jpenilla.pluginbase.legacy.environment.MinecraftReleases.v1_17
import xyz.jpenilla.pluginbase.legacy.environment.MinecraftReleases.v1_20
import xyz.jpenilla.pluginbase.legacy.environment.MinecraftReleases.v1_20_2
import xyz.jpenilla.pluginbase.legacy.environment.MinecraftReleases.v1_20_4
import xyz.jpenilla.pluginbase.legacy.environment.MinecraftReleases.v1_20_5
import xyz.jpenilla.reflectionremapper.ReflectionRemapper
import java.lang.reflect.Constructor
import java.lang.reflect.Field
import java.lang.reflect.Method
import java.lang.reflect.Modifier
import java.util.Optional
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.Semaphore
import java.util.logging.Level
import kotlin.random.Random.Default.nextInt

class ToastTask : KoinComponent {
  private val announcerPlus: AnnouncerPlus by inject()
  private val semaphore: Semaphore = Semaphore(1)
  private val toastQueue: MutableMap<UUID, MutableList<ToastSettings>> = ConcurrentHashMap()

  init {
    ensureInitialized() // Load mappings on init (not lazily on first advancement)
  }

  private val toastTask: TaskHandle<*> = announcerPlus.asyncTimer(0L, 1L, ::poll)

  private fun poll() {
    if (!semaphore.tryAcquire()) {
      return
    }
    try {
      for (id in toastQueue.keys.toList()) {
        val toasts = toastQueue.remove(id) ?: continue
        val player = announcerPlus.server.getPlayer(id) ?: continue
        displayToasts(player, toasts)
      }
    } finally {
      semaphore.release()
    }
  }

  fun queueToast(toastSettings: ToastSettings, player: Player) {
    player.toastQueue().add(toastSettings)
  }

  fun cancel() {
    toastTask.cancel()
  }

  private fun Player.toastQueue(): MutableList<ToastSettings> = toastQueue.computeIfAbsent(uniqueId) { CopyOnWriteArrayList() }

  private fun displayToasts(player: Player, toasts: List<ToastSettings>) {
    if (toasts.size > 999) {
      announcerPlus.logger.log(Level.WARNING, "Over 999 toasts queued at once for $player (${toasts.size})! Some may not display.")
    }
    val advancements = toasts.map { it.buildAdvancement(player) }
    announcerPlus.schedule(player) {
      for ((advancement, _) in advancements) {
        if (USE_ADVANCEMENT_HOLDER) {
          AdvancementTree_addAll!!(ServerAdvancementManager_tree(MinecraftServer_getAdvancements(MinecraftServer_getServer(null))), mutableSetOf(advancement))
        }
        grant(player, advancement)
      }
      announcerPlus.schedule(player, 2L) {
        for ((advancement, id) in advancements) {
          revoke(player, advancement)
          if (USE_ADVANCEMENT_HOLDER) {
            AdvancementTree_remove!!(ServerAdvancementManager_tree(MinecraftServer_getAdvancements(MinecraftServer_getServer(null))), mutableSetOf(id))
          }
        }
      }
    }
  }

  private fun ToastSettings.buildAdvancement(player: Player): Pair<Any, Any> {
    val resourceLocation = ResourceLocation_ctr.newInstance(
      announcerPlus.name.lowercase(),
      nextInt(1000000).toString()
    )
    val json = advancementJson(player)
    if (USE_ADVANCEMENT_HOLDER) {
      val advancement = if (Advancement_CODEC == null) {
        Advancement_fromJson(
          null,
          json,
          DeserializationContext_ctr.newInstance(
            resourceLocation,
            MinecraftServer_getPredicateManager(MinecraftServer_getServer(null))
          )
        )
      } else {
        val result = (Advancement_CODEC.get(null) as Codec<*>).parse(dynamicOps(), json)
        result.getError().ifPresent { announcerPlus.logger.log(Level.WARNING, "Failed to deserialize advancement: $it") }
        result.getResult().orElseThrow { IllegalStateException() }
      }
      return AdvancementHolder_ctr!!.newInstance(
        resourceLocation,
        advancement
      ) to resourceLocation
    }
    val advancementBuilder = if (currentMinecraft().isAtLeast(v1_16)) {
      AdvancementBuilder_fromJson(
        null,
        json,
        DeserializationContext_ctr.newInstance(
          resourceLocation,
          MinecraftServer_getPredicateManager(MinecraftServer_getServer(null))
        )
      )
    } else {
      ChatDeserializer_deserialize(
        null,
        AdvancementDataWorld_DESERIALIZER,
        announcerPlus.gson.toJson(json),
        AdvancementBuilder_class
      )
    }
    return AdvancementBuilder_build(advancementBuilder, resourceLocation) to resourceLocation
  }

  @Suppress("UNCHECKED_CAST")
  private fun <T : JsonElement> dynamicOps(): DynamicOps<T> {
    if (currentMinecraft().isAtLeast(v1_20) && currentMinecraft().isOlderThan(v1_20_5)) {
      return JsonOps.INSTANCE as DynamicOps<T>
    }
    return DFU.registryOps()
  }

  private object DFU {
    private val getError = DataResult::class.java.getMethod("error")
    private val getResult = DataResult::class.java.getMethod("result")

    @Suppress("UNCHECKED_CAST")
    fun <T> DataResult<T>.getError(): Optional<Any> {
      return getError(this) as Optional<Any>
    }

    @Suppress("UNCHECKED_CAST")
    fun <T> DataResult<T>.getResult(): Optional<T> {
      return getResult(this) as Optional<T>
    }

    private val registryOps: DynamicOps<*> by lazy {
      val server = MinecraftServer_getServer(null)
      val regAccessMth = server::class.java.getMethod("registryAccess")
      val regAccess = regAccessMth.invoke(server)
      val jsonOps = JsonOps.INSTANCE
      val createContextMth = regAccess::class.java.getMethod("createSerializationContext", DynamicOps::class.java)
      createContextMth.invoke(regAccess, jsonOps) as DynamicOps<*>
    }

    @Suppress("UNCHECKED_CAST")
    fun <T> registryOps(): DynamicOps<T> = registryOps as DynamicOps<T>
  }

  private fun grant(player: Player, advancement: Any) {
    val playerAdvancements = ServerPlayer_getAdvancements(player.handle)
    val advancementProgress = PlayerAdvancements_getProgress(playerAdvancements, advancement)
    (AdvancementProgress_remainingCriteria(advancementProgress) as Iterable<*>).forEach {
      PlayerAdvancements_award(playerAdvancements, advancement, it)
    }
  }

  private fun revoke(player: Player, advancement: Any) {
    val playerAdvancements = ServerPlayer_getAdvancements(player.handle)
    val advancementProgress = PlayerAdvancements_getProgress(playerAdvancements, advancement)
    (AdvancementProgress_completedCriteria(advancementProgress) as Iterable<*>).forEach {
      PlayerAdvancements_revoke(playerAdvancements, advancement, it)
    }
  }

  private companion object Reflect {
    fun ensureInitialized() {
      // no-op
    }

    val MinecraftServer_class = Crafty.needNMSClassOrElse("MinecraftServer", "net.minecraft.server.MinecraftServer")
    val MinecraftServer_getServer = MinecraftServer_class.getDeclaredMethod("getServer")
      ?: error("Cannot find getServer")

    val ResourceLocation_class = Crafty.needNMSClassOrElse(
      "MinecraftKey",
      "net.minecraft.resources.MinecraftKey",
      "net.minecraft.resources.ResourceLocation"
    )
    val ResourceLocation_ctr = ResourceLocation_class.getDeclaredConstructor(String::class.java, String::class.java)
      ?.also { it.isAccessible = true }
      ?: error("Cannot find ResourceLocation constructor")

    val Advancement_class = Crafty.needNMSClassOrElse("Advancement", "net.minecraft.advancements.Advancement")
    val AdvancementHolder_class = Crafty.findClass("net.minecraft.advancements.AdvancementHolder")
    val AdvancementHolder_ctr = AdvancementHolder_class?.let {
      runCatching { it.getDeclaredConstructor(ResourceLocation_class, Advancement_class) }.getOrNull()
    }

    fun advancementOrHolderCls() = AdvancementHolder_class ?: Advancement_class
    val AdvancementBuilder_class = Crafty.needNMSClassOrElse(
      "Advancement\$SerializedAdvancement",
      "net.minecraft.advancements.Advancement\$SerializedAdvancement",
      "net.minecraft.advancements.Advancement\$Builder"
    )

    // lazy: not used >= 1.20.2
    val AdvancementBuilder_build by lazy {
      AdvancementBuilder_class.declaredMethods.find { method ->
        method.returnType == Advancement_class &&
          method.parameterCount == 1 &&
          method.parameterTypes[0] == ResourceLocation_class &&
          !Modifier.isStatic(method.modifiers)
      } ?: error("Cannot find Advancement\$Builder#build")
    }

    val ServerPlayer_class = Crafty.needNMSClassOrElse(
      "EntityPlayer",
      "net.minecraft.server.level.EntityPlayer",
      "net.minecraft.server.level.ServerPlayer"
    )
    val CraftPlayer_class = Crafty.needCraftClass("entity.CraftPlayer")
    val CraftPlayer_getHandle = CraftPlayer_class.getMethod("getHandle")
      ?: error("Cannot find CraftPlayer#getHandle")
    val Player.handle: Any
      get() = CraftPlayer_getHandle(this)

    val AdvancementProgress_class = Crafty.needNMSClassOrElse("AdvancementProgress", "net.minecraft.advancements.AdvancementProgress")
    val PlayerAdvancements_class = Crafty.needNMSClassOrElse(
      "AdvancementDataPlayer",
      "net.minecraft.server.AdvancementDataPlayer",
      "net.minecraft.server.PlayerAdvancements"
    )
    val ServerPlayer_getAdvancements = ServerPlayer_class.methods.find { method ->
      method.returnType == PlayerAdvancements_class &&
        method.parameterCount == 0
    } ?: error("Cannot find ServerPlayer#getAdvancements")

    val PlayerAdvancements_getProgress = PlayerAdvancements_class.methods.find { method ->
      method.returnType == AdvancementProgress_class &&
        method.parameterCount == 1 &&
        method.parameterTypes[0] == advancementOrHolderCls()
    } ?: error("Cannot find PlayerAdvancements#getProgress")

    // start 1.20.2+
    val USE_ADVANCEMENT_HOLDER: Boolean = currentMinecraft().isAtLeast(v1_20_2)

    // 1.20.2 only
    val Advancement_fromJson by lazy {
      Advancement_class.declaredMethods.find { method ->
        method.returnType == Advancement_class &&
          method.parameterCount == 2 &&
          method.parameterTypes.contains(JsonObject::class.java) &&
          method.parameterTypes.contains(DeserializationContext_class)
      } ?: error("Cannot find Advancement#fromJson")
    }

    val ServerAdvancementManager_class by lazy {
      Crafty.findClass("net.minecraft.server.ServerAdvancementManager")
        ?: Crafty.findClass("net.minecraft.server.AdvancementDataWorld")
        ?: error("Could not find ServerAdvancementManager class")
    }

    val MinecraftServer_getAdvancements by lazy {
      MinecraftServer_class.declaredMethods.find {
        it.parameterCount == 0 && it.returnType == ServerAdvancementManager_class
      } ?: error("Could not find MinecraftServer#getAdvancements()")
    }

    val AdvancementTree_class by lazy {
      Crafty.findClass("net.minecraft.advancements.AdvancementTree")
        ?: error("Could not find AdvancementTree class")
    }

    val ServerAdvancementManager_tree by lazy {
      ServerAdvancementManager_class.declaredMethods.find {
        it.parameterCount == 0 && it.returnType == AdvancementTree_class
      } ?: error("Could not find ServerAdvancementManager#tree()")
    }

    val AdvancementTree_addAll: Method?
    val AdvancementTree_remove: Method?
    // end 1.20.2+

    // start 1.20.4+
    val Advancement_CODEC: Field?
    // end 1.20.4+

    val PlayerAdvancements_award: Method
    val PlayerAdvancements_revoke: Method
    val AdvancementProgress_remainingCriteria: Method
    val AdvancementProgress_completedCriteria: Method

    init {
      if (currentMinecraft().isOlderThan(v1_17)) {
        PlayerAdvancements_award = PlayerAdvancements_class.getDeclaredMethod("grantCriteria", Advancement_class, String::class.java)
        PlayerAdvancements_revoke = try {
          PlayerAdvancements_class.getDeclaredMethod("revokeCritera", Advancement_class, String::class.java) // lol
        } catch (e: NoSuchMethodException) {
          PlayerAdvancements_class.getDeclaredMethod("revokeCriteria", Advancement_class, String::class.java)
        }
        AdvancementProgress_remainingCriteria = AdvancementProgress_class.getDeclaredMethod("getRemainingCriteria")
        AdvancementProgress_completedCriteria = AdvancementProgress_class.getDeclaredMethod("getAwardedCriteria")
        AdvancementTree_addAll = null
        AdvancementTree_remove = null
        Advancement_CODEC = null
      } else {
        val reflectionRemapper = ReflectionRemapper.forReobfMappingsInPaperJar()
        if (currentMinecraft().isAtLeast(v1_20_4)) {
          Advancement_CODEC = Advancement_class.getDeclaredField(
            reflectionRemapper.remapFieldName(Advancement_class, "CODEC")
          )
        } else {
          Advancement_CODEC = null
        }

        PlayerAdvancements_award = PlayerAdvancements_class.getDeclaredMethod(
          reflectionRemapper.remapMethodName(PlayerAdvancements_class, "award", advancementOrHolderCls(), String::class.java),
          advancementOrHolderCls(),
          String::class.java
        )
        PlayerAdvancements_revoke = PlayerAdvancements_class.getDeclaredMethod(
          reflectionRemapper.remapMethodName(PlayerAdvancements_class, "revoke", advancementOrHolderCls(), String::class.java),
          advancementOrHolderCls(),
          String::class.java
        )
        AdvancementProgress_remainingCriteria = AdvancementProgress_class.getDeclaredMethod(
          reflectionRemapper.remapMethodName(AdvancementProgress_class, "getRemainingCriteria")
        )
        AdvancementProgress_completedCriteria = AdvancementProgress_class.getDeclaredMethod(
          reflectionRemapper.remapMethodName(AdvancementProgress_class, "getCompletedCriteria")
        )

        if (USE_ADVANCEMENT_HOLDER) {
          AdvancementTree_addAll = AdvancementTree_class.getDeclaredMethod(
            reflectionRemapper.remapMethodName(AdvancementTree_class, "addAll", Collection::class.java),
            Collection::class.java
          )
          AdvancementTree_remove = AdvancementTree_class.getDeclaredMethod(
            reflectionRemapper.remapMethodName(AdvancementTree_class, "remove", Set::class.java),
            Set::class.java
          )
        } else {
          AdvancementTree_addAll = null
          AdvancementTree_remove = null
        }
      }
    }

    // start 1.12 -> 1.15
    val ChatDeserializer_class by lazy { Crafty.needNmsClass("ChatDeserializer") }
    val AdvancementDataWorld_class by lazy { Crafty.needNmsClass("AdvancementDataWorld") }

    val ChatDeserializer_deserialize by lazy {
      ChatDeserializer_class.methods.find { method ->
        method.parameterCount == 3 &&
          method.parameterTypes.contains(Gson::class.java) &&
          method.parameterTypes.contains(String::class.java) &&
          method.parameterTypes.contains(Class::class.java)
      } ?: error("Cannot find ChatDeserializer.deserialize")
    }

    val AdvancementDataWorld_DESERIALIZER by lazy {
      AdvancementDataWorld_class.getField("DESERIALIZER").get(null)
        ?: error("Cannot find AdvancementDataWorld.DESERIALIZER")
    }
    // end 1.12 -> 1.15

    // start 1.16+
    val PredicateManager_class by lazy {
      Crafty.needNMSClassOrElse(
        "LootPredicateManager",
        "net.minecraft.world.level.storage.loot.LootPredicateManager",
        "net.minecraft.world.level.storage.loot.PredicateManager",
        "net.minecraft.world.level.storage.loot.LootDataManager"
      )
    }
    val MinecraftServer_getPredicateManager by lazy {
      MinecraftServer_class.declaredMethods.find { method ->
        method.returnType == PredicateManager_class &&
          method.parameterCount == 0
      } ?: error("Cannot find MinecraftServer#getPredicateManager")
    }

    val DeserializationContext_class by lazy {
      Crafty.needNMSClassOrElse(
        "LootDeserializationContext",
        "net.minecraft.advancements.critereon.LootDeserializationContext",
        "net.minecraft.advancements.critereon.DeserializationContext"
      )
    }
    val DeserializationContext_ctr: Constructor<*> by lazy {
      DeserializationContext_class.getDeclaredConstructor(ResourceLocation_class, PredicateManager_class)
    }

    val AdvancementBuilder_fromJson by lazy {
      AdvancementBuilder_class.declaredMethods.find { method ->
        method.returnType == AdvancementBuilder_class &&
          method.parameterCount == 2 &&
          method.parameterTypes.contains(JsonObject::class.java) &&
          method.parameterTypes.contains(DeserializationContext_class)
      } ?: error("Cannot find Advancement\$Builder#fromJson")
    }
    // end 1.16+
  }
}
