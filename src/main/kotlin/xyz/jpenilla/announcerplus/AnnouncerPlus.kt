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
package xyz.jpenilla.announcerplus

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonParser
import kr.entree.spigradle.annotations.PluginMain
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer
import net.milkbowl.vault.permission.Permission
import org.bstats.bukkit.Metrics
import org.bstats.charts.SimplePie
import org.koin.core.KoinComponent
import org.koin.core.context.startKoin
import org.koin.core.inject
import org.koin.dsl.module
import xyz.jpenilla.announcerplus.command.CommandManager
import xyz.jpenilla.announcerplus.compatibility.EssentialsHook
import xyz.jpenilla.announcerplus.config.ConfigManager
import xyz.jpenilla.announcerplus.config.message.MessageConfig
import xyz.jpenilla.announcerplus.task.ToastTask
import xyz.jpenilla.announcerplus.util.UpdateChecker
import xyz.jpenilla.jmplib.BasePlugin
import xyz.jpenilla.jmplib.Environment

@PluginMain
class AnnouncerPlus : BasePlugin(), KoinComponent {
  val gson: Gson = GsonBuilder().create()
  val jsonParser = JsonParser()
  val configManager: ConfigManager by inject()

  var perms: Permission? = null
  var essentials: EssentialsHook? = null
  var toastTask: ToastTask? = null
  private lateinit var commandManager: CommandManager

  override fun onPluginEnable() {
    if (!setupPermissions()) {
      logger.warning("Permissions plugin not found. AnnouncerPlus will not work.")
      isEnabled = false
      return
    }
    if (server.pluginManager.isPluginEnabled("Essentials")) {
      essentials = EssentialsHook()
    }

    startKoin {
      modules(module {
        single { this@AnnouncerPlus }
        single { audiences() }
        single { miniMessage() }
        single { chat() }
        single { ConfigManager(get()) }
        single { gson }
        single { jsonParser }
      })
    }

    if (Environment.majorMinecraftVersion() > 11) {
      toastTask = ToastTask()
    } else {
      logger.info("Sorry, but Toast/Achievement style messages do not work on this version. Update to 1.12 or newer to use this feature.")
    }
    commandManager = CommandManager(this)

    server.pluginManager.registerEvents(JoinQuitListener(), this)
    broadcast()

    UpdateChecker(this, "jmanpenilla/AnnouncerPlus").updateCheck()

    val metrics = Metrics(this, 8067)
    metrics.addCustomChart(SimplePie("join_quit_configs", configManager.joinQuitConfigs.size::toString))
    metrics.addCustomChart(SimplePie("message_configs", configManager.messageConfigs.size::toString))
  }

  private fun broadcast() {
    if (configManager.mainConfig.enableBroadcasts) configManager.messageConfigs.values.forEach(MessageConfig::broadcast)
  }

  fun reload() {
    if (toastTask != null) {
      toastTask?.cancel()
      toastTask = ToastTask()
    }
    configManager.reload()
    broadcast()
  }

  override fun onDisable() {
    toastTask?.cancel()
  }

  private fun setupPermissions(): Boolean {
    val rsp = server.servicesManager.getRegistration(Permission::class.java)
    if (rsp != null) {
      perms = rsp.provider
    }
    return perms != null
  }
}
