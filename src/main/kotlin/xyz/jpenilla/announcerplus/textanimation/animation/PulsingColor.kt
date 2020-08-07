package xyz.jpenilla.announcerplus.textanimation.animation

import net.kyori.adventure.text.format.TextColor
import xyz.jpenilla.announcerplus.textanimation.TextAnimation
import kotlin.math.roundToInt

class PulsingColor(colors: List<TextColor>, ticks: Int) : TextAnimation {
    private var index = 0
    private var colorIndex = 0
    private val colors = ArrayList(colors)
    init {
        this.colors.add(this.colors[0])
    }
    private var color = this.colors[0].asHexString()
    private val factorStep: Float = 1.0f / ticks

    override fun getValue(): String {
        return color
    }

    override fun nextValue(): String {
        color = nextColor().asHexString()
        return getValue()
    }

    private fun interpolate(color1: TextColor, color2: TextColor, factor: Float): TextColor {
        return TextColor.of(
                (color1.red() + factor * (color2.red() - color1.red())).roundToInt(),
                (color1.green() + factor * (color2.green() - color1.green())).roundToInt(),
                (color1.blue() + factor * (color2.blue() - color1.blue())).roundToInt()
        )
    }

    private fun nextColor(): TextColor {
        if (factorStep * index > 1) {
            colorIndex++
            index = 0
        }

        val factor = factorStep * index++

        if (colorIndex + 1 > colors.lastIndex) {
            colorIndex = 0
        }

        return interpolate(colors[colorIndex], colors[colorIndex + 1], factor)
    }
}