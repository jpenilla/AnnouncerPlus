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

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import org.bukkit.plugin.java.JavaPlugin
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.URL
import java.util.LinkedList

class UpdateChecker(private val plugin: JavaPlugin, private val githubRepo: String) {
  private val gson: Gson = GsonBuilder().create()

  fun run() {
    plugin.scheduleAsync {
      updateCheck()
    }
  }

  private fun updateCheck() {
    val result = try {
      BufferedReader(InputStreamReader(URL("https://api.github.com/repos/$githubRepo/releases").openStream(), Charsets.UTF_8)).use {
        gson.fromJson(it, JsonArray::class.java)
      }
    } catch (exception: IOException) {
      plugin.logger.info("Cannot look for updates: " + exception.message)
      return
    }
    val versionMap = LinkedHashMap<String, String>()
    result.forEach { versionMap[it.asJsonObject["tag_name"].asString] = it.asJsonObject["html_url"].asString }
    val versionList = LinkedList(versionMap.keys)
    val currentVersion = "v" + plugin.description.version
    if (versionList[0] == currentVersion) {
      return // Up to date, do nothing
    }
    if (currentVersion.contains("SNAPSHOT")) {
      plugin.logger.info("This server is running a development build of ${plugin.name}! ($currentVersion)")
      plugin.logger.info("The latest official release is " + versionList[0])
      return
    }
    val versionsBehind = versionList.indexOf(currentVersion)
    plugin.logger.info("There is an update available for ${plugin.name}!")
    plugin.logger.info("This server is running version $currentVersion, which is ${if (versionsBehind == -1) "UNKNOWN" else versionsBehind} versions outdated.")
    plugin.logger.info("Download the latest version, ${versionList[0]} from GitHub at the link below:")
    plugin.logger.info(versionMap[versionList[0]])
  }
}
