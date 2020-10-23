package xyz.jpenilla.announcerplus.textanimation

import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor
import org.bukkit.entity.Player
import org.koin.core.KoinComponent
import org.koin.core.inject
import xyz.jpenilla.announcerplus.config.ConfigManager
import xyz.jpenilla.announcerplus.textanimation.animation.FlashingText
import xyz.jpenilla.announcerplus.textanimation.animation.PulsingColor
import xyz.jpenilla.announcerplus.textanimation.animation.RandomColor
import xyz.jpenilla.announcerplus.textanimation.animation.ScrollingGradient
import xyz.jpenilla.announcerplus.textanimation.animation.ScrollingText
import xyz.jpenilla.announcerplus.textanimation.animation.Typewriter

class AnimationHolder(private val player: Player?, private val message: String) : KoinComponent {
    companion object {
        private val pattern = "\\{animate:/?([a-z][^}]*)/?}?".toPattern()
    }

    private val configManager: ConfigManager by inject()
    private val matcher = pattern.matcher(message)
    private val animations = HashMap<String, TextAnimation>()

    init {
        while (matcher.find()) {
            val tokens = matcher.group(1).split(":")

            when (tokens[0]) {
                "scroll" -> {
                    val speed = try {
                        tokens[1].toFloat()
                    } catch (e: Exception) {
                        0.1f
                    }
                    animations[matcher.group()] = ScrollingGradient(speed)
                }

                "flash" -> {
                    val colors = ArrayList(tokens.subList(1, tokens.size))
                    var ticks: Int
                    try {
                        ticks = colors.last().toInt()
                        colors.removeAt(colors.lastIndex)
                    } catch (e: Exception) {
                        ticks = 10
                    }
                    animations[matcher.group()] = FlashingText(colors, ticks)
                }

                "pulse" -> {
                    val colors = ArrayList(tokens.subList(1, tokens.size))
                    var ticks: Int
                    try {
                        ticks = colors.last().toInt()
                        colors.removeAt(colors.lastIndex)
                    } catch (e: Exception) {
                        ticks = 10
                    }
                    val textColors = colors.map { color ->
                        NamedTextColor.NAMES.value(color) ?: (TextColor.fromHexString(color) ?: NamedTextColor.WHITE)
                    }
                    animations[matcher.group()] = PulsingColor(textColors, ticks)
                }

                "type" -> {
                    val text = tokens[1]
                    val ticks = try {
                        tokens[2].toInt()
                    } catch (e: Exception) {
                        6
                    }
                    animations[matcher.group()] = Typewriter(player, text, ticks)
                }

                "randomcolor" -> {
                    val type = try {
                        RandomColor.Type.of(tokens[1])
                    } catch (e: Exception) {
                        RandomColor.Type.PULSE
                    }
                    val ticks = try {
                        tokens[2].toInt()
                    } catch (e: Exception) {
                        10
                    }
                    animations[matcher.group()] = RandomColor(type, ticks)
                }

                "scrolltext" -> {
                    val text = tokens[1]
                    val window = try {
                        tokens[2].toInt()
                    } catch (e: Exception) {
                        10
                    }
                    val ticks = try {
                        tokens[3].toInt()
                    } catch (e: Exception) {
                        4
                    }
                    animations[matcher.group()] = ScrollingText(player, text, window, ticks)
                }
            }
        }
    }

    fun parseNext(text: String?): String {
        var msg = text ?: message
        for (animation in animations) {
            msg = msg.replace(animation.key, animation.value.nextValue())
        }
        return configManager.parse(player, msg)
    }

    fun parseCurrent(text: String?): String {
        var msg = text ?: message
        for (animation in animations) {
            msg = msg.replace(animation.key, animation.value.getValue())
        }
        return configManager.parse(player, msg)
    }
}