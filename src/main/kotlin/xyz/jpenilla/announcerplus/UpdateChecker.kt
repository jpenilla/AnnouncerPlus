package xyz.jpenilla.announcerplus

import com.okkero.skedule.SynchronizationContext
import com.okkero.skedule.schedule
import java.io.IOException
import java.net.URL
import java.util.*

class UpdateChecker(private val announcerPlus: AnnouncerPlus, private val resourceId: Int) {
    fun updateCheck() {
        announcerPlus.schedule(SynchronizationContext.ASYNC) {
            var initialRun = true
            repeating(20L * 60L * 60L * 2L)
            while (true) {
                try {
                    var l = ""
                    URL("https://api.spigotmc.org/legacy/update.php?resource=$resourceId").openStream().use { inputStream ->
                        Scanner(inputStream).use { scanner ->
                            if (scanner.hasNext()) {
                                l = scanner.next()
                            }
                        }
                    }
                    when {
                        l == announcerPlus.description.version -> {
                            if (initialRun) {
                                announcerPlus.logger.info("You are running the latest version of ${announcerPlus.name}! :)")
                            }
                        }
                        l.contains("SNAPSHOT") -> announcerPlus.logger.info("[!] You are running a development build of ${announcerPlus.name} (${announcerPlus.description.version}) [!]")
                        else -> {
                            announcerPlus.logger.info("[!] ${announcerPlus.name} is outdated! (${announcerPlus.description.version})")
                            announcerPlus.logger.info("[!] $l is available at ${announcerPlus.description.website}")
                        }
                    }
                    if (initialRun) {
                        initialRun = false
                    }
                } catch (exception: IOException) {
                    announcerPlus.logger.info("Cannot look for updates: " + exception.message)
                }
                yield()
            }
        }
    }
}
