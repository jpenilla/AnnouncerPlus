package xyz.jpenilla.announcerplus.textanimation.animation

import net.kyori.adventure.text.format.TextColor
import xyz.jpenilla.announcerplus.textanimation.TextAnimation
import kotlin.random.Random

class RandomColor(type: Type, ticks: Int) : TextAnimation {
    private var animation = when (type) {
        Type.FLASH -> {
            FlashingText(getRandomColors(64).map { color -> color.asHexString() }, ticks)
        }
        Type.PULSE -> {
            PulsingColor(getRandomColors(64), ticks)
        }
    }

    override fun getValue(): String {
        return animation.getValue()
    }

    override fun nextValue(): String {
        return animation.nextValue()
    }

    private fun getRandomColor(): TextColor {
        return TextColor.of(Random.nextInt(256), Random.nextInt(256), Random.nextInt(256))
    }

    private fun getRandomColors(amount: Int): List<TextColor> {
        val randomColors = arrayListOf<TextColor>()
        for (i in 0 until amount) {
            randomColors.add(getRandomColor())
        }
        return randomColors
    }

    enum class Type {
        FLASH, PULSE;

        companion object {
            fun of(text: String): Type {
                return valueOf(text.toUpperCase())
            }
        }
    }
}

