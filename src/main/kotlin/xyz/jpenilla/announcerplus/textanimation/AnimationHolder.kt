package xyz.jpenilla.announcerplus.textanimation

import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor
import java.util.regex.Pattern

class AnimationHolder(private val message: String) {
    companion object {
        private val pattern = Pattern.compile("\\{animate:/?([a-z][^}\\s]*)/?}?")
    }

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
                    animations[matcher.group()] = AnimatedGradient(speed)
                }

                "flash" -> {
                    val colors = ArrayList(tokens.subList(1, tokens.size))
                    var ticks: Int
                    try {
                        ticks = colors.last().toInt()
                        colors.removeAt(colors.lastIndex)
                    } catch (e: NumberFormatException) {
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
                    } catch (e: NumberFormatException) {
                        ticks = 10
                    }
                    val textColors = colors.map { color ->
                        NamedTextColor.NAMES.value(color) ?: (TextColor.fromHexString(color) ?: NamedTextColor.WHITE)
                    }
                    animations[matcher.group()] = PulsingText(textColors, ticks)
                }
            }
        }
    }

    fun parseNext(text: String?): String {
        var msg = text ?: message
        for (animation in animations) {
            msg = msg.replace(animation.key, animation.value.nextValue())
        }
        return msg
    }

    fun parseCurrent(text: String?): String {
        var msg = text ?: message
        for (animation in animations) {
            msg = msg.replace(animation.key, animation.value.getValue())
        }
        return msg
    }
}