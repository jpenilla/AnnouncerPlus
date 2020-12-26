package xyz.jpenilla.announcerplus.textanimation.animation

import org.bukkit.entity.Player
import org.koin.core.inject
import xyz.jpenilla.announcerplus.config.ConfigManager
import xyz.jpenilla.announcerplus.textanimation.TextAnimation

class Typewriter(private val player: Player?, private val text: String, private val ticks: Int) : TextAnimation {
    private val configManager: ConfigManager by inject()
    private var index = 0
    private var ticksLived = 0
    private var showUnderscore = true

    override fun getValue(): String {
        val s = try {
            configManager.parse(player, text).substring(0, index)
        } catch (e: Exception) {
            //if the placeholders changed in a way that causes us to out of bounds
            index = 0
            configManager.parse(player, text).substring(0, index)
        }
        return "$s${if (showUnderscore) "_" else " "}"
    }

    override fun nextValue(): String {
        ticksLived++
        if (ticksLived % 5 == 0) {
            showUnderscore = !showUnderscore
        }
        if (ticksLived % ticks == 0) {
            index++
            if (index > configManager.parse(player, text).length) {
                index = 0
            }
        }
        return getValue()
    }
}
