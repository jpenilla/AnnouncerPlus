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
package xyz.jpenilla.announcerplus

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import io.papermc.lib.PaperLib.suggestPaper
import net.milkbowl.vault.permission.Permission
import org.bstats.bukkit.Metrics
import org.bstats.charts.SimplePie
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import xyz.jpenilla.announcerplus.command.Commands
import xyz.jpenilla.announcerplus.compatibility.EssentialsHook
import xyz.jpenilla.announcerplus.config.ConfigManager
import xyz.jpenilla.announcerplus.config.message.MessageConfig
import xyz.jpenilla.announcerplus.listener.JoinQuitListener
import xyz.jpenilla.announcerplus.task.ToastTask
import xyz.jpenilla.announcerplus.util.AsyncTasksThreadFactory
import xyz.jpenilla.announcerplus.util.Constants
import xyz.jpenilla.announcerplus.util.DisplayTracker
import xyz.jpenilla.announcerplus.util.UpdateChecker
import xyz.jpenilla.announcerplus.util.dataPath
import xyz.jpenilla.pluginbase.legacy.PluginBase
import xyz.jpenilla.pluginbase.legacy.environment.Environment
import xyz.jpenilla.pluginbase.legacy.environment.Environment.currentMinecraft
import xyz.jpenilla.pluginbase.legacy.environment.MinecraftReleases.v1_12
import xyz.jpenilla.pluginbase.legacy.environment.MinecraftReleases.v1_17
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
import java.util.logging.Level

class AnnouncerPlus : PluginBase(), KoinComponent {
  val gson: Gson = GsonBuilder().create()
  val configManager: ConfigManager by inject()

  var perms: Permission? = null
  var essentials: EssentialsHook? = null
  var toastTask: ToastTask? = null
  private var asyncExecutor: ScheduledExecutorService? = null
  private lateinit var commands: Commands

  override fun enable() {
    if (!setupPermissions()) {
      logger.warning("Permissions plugin not found. AnnouncerPlus will not work.")
      isEnabled = false
      return
    }
    suggestPaper(this, Level.WARNING)
    if (server.pluginManager.isPluginEnabled("Essentials")) {
      essentials = EssentialsHook()
    }

    asyncExecutor = Executors.newScheduledThreadPool(4, AsyncTasksThreadFactory())

    val module = module {
      single { this@AnnouncerPlus }
      single { audiences() }
      single { miniMessage() }
      single { DisplayTracker() }
      single { ConfigManager(get(), get(), get(Constants.DATA_PATH)) }
      single { gson }
      single { this@AnnouncerPlus.logger }
      single(Constants.DATA_PATH) { dataPath }
    }

    startKoin {
      modules(module)
    }

    initToastTask()
    commands = Commands(this)

    server.pluginManager.registerEvents(JoinQuitListener(), this)
    broadcast()

    if (configManager.mainConfig.checkForUpdates) {
      UpdateChecker(this, "jpenilla/AnnouncerPlus").run()
    }

    setupMetrics()
  }

  private fun setupMetrics() {
    val metrics = Metrics(this, 8067)
    metrics.addCustomChart(SimplePie("join_quit_configs") { configManager.joinQuitConfigs.size.toString() })
    metrics.addCustomChart(SimplePie("message_configs") { configManager.messageConfigs.size.toString() })
  }

  private fun initToastTask() {
    if (currentMinecraft().isOlderThan(v1_12)) {
      logger.info("Sorry, but Toast/Advancement style messages do not work on this version. Update to 1.12 or newer to use this feature.")
      return
    }

    if (currentMinecraft().isAtLeast(v1_17)) {
      if (Environment.paper()) {
        try {
          toastTask = ToastTask()
        } catch (e: ExceptionInInitializerError) {
          slF4JLogger.info("Failed to initialize toast/advancement message support", e)
        }
      } else {
        logger.info("Toast/Advancement style messages require Paper in order to function on this version, consider using Paper if you want to take advantage of this feature!")
      }
      return
    }

    toastTask = ToastTask()
  }

  private fun broadcast() {
    if (configManager.mainConfig.enableBroadcasts) {
      configManager.messageConfigs.values.forEach(MessageConfig::broadcast)
    }
  }

  @Synchronized
  fun reload() {
    if (toastTask != null) {
      toastTask?.cancel()
      toastTask = ToastTask()
    }
    configManager.reload()
    broadcast()
  }

  override fun disable() {
    toastTask?.cancel()
    stopKoin()

    asyncExecutor?.let {
      it.shutdown()
      if (!it.awaitTermination(1L, TimeUnit.SECONDS)) {
        it.shutdownNow()
      }
    }
    asyncExecutor = null
  }

  fun asyncExecutor(): ScheduledExecutorService {
    return requireNotNull(asyncExecutor) { "Async Executor not initialized" }
  }

  private fun setupPermissions(): Boolean {
    val rsp = server.servicesManager.getRegistration(Permission::class.java)
    if (rsp != null) {
      perms = rsp.provider
    }
    return perms != null
  }
}
