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
package xyz.jpenilla.announcerplus.listener

import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import xyz.jpenilla.announcerplus.config.ConfigManager
import xyz.jpenilla.announcerplus.config.MainConfig.JoinQuitPair
import xyz.jpenilla.pluginbase.legacy.WeightedRandom

class JoinQuitListener : Listener, KoinComponent {
  private val configManager: ConfigManager by inject()

  @EventHandler(priority = EventPriority.HIGHEST)
  fun onJoin(event: PlayerJoinEvent) {
    if (configManager.mainConfig.joinFeatures) {
      event.joinMessage = ""
      if (configManager.mainConfig.firstJoinConfigEnabled && !event.player.hasPlayedBefore()) {
        configManager.firstJoinConfig.onJoin(event.player)
        return
      }
      for (entry in configManager.mainConfig.randomJoinConfigs.entries) {
        if (entry.key != "demo" && event.player.hasPermission("announcerplus.randomjoin.${entry.key}")) {
          val randomConfigName = entry.value.selectRandomWeighted()
          configManager.joinQuitConfigs[randomConfigName]?.onJoin(event.player)
        }
      }
      for (config in configManager.joinQuitConfigs.values) {
        config.onJoin(event.player)
      }
    }
  }

  @EventHandler(priority = EventPriority.HIGHEST)
  fun onQuit(event: PlayerQuitEvent) {
    if (configManager.mainConfig.quitFeatures) {
      event.quitMessage = ""
      for (entry in configManager.mainConfig.randomQuitConfigs.entries) {
        if (entry.key != "demo" && event.player.hasPermission("announcerplus.randomquit.${entry.key}")) {
          val randomConfigName = entry.value.selectRandomWeighted()
          configManager.joinQuitConfigs[randomConfigName]?.onQuit(event.player)
        }
      }
      for (config in configManager.joinQuitConfigs.values) {
        config.onQuit(event.player)
      }
    }
  }

  private fun Collection<JoinQuitPair>.selectRandomWeighted(): String {
    if (isEmpty()) error("Cannot randomly select from an empty collection")
    return WeightedRandom<String>().apply {
      for (pair in this@selectRandomWeighted) {
        add(pair.weight, pair.configName)
      }
    }.next()
  }
}
