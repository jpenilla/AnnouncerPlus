package xyz.jpenilla.announcerplus.textanimation.animation

import xyz.jpenilla.announcerplus.textanimation.TextAnimation

class FlashingText(private val colors: List<String>, private val ticks: Int) : TextAnimation {
    private var index = 0
    private var ticksLived = 0
    private var color = colors[0]

    override fun getValue(): String {
        return color
    }

    override fun nextValue(): String {
        ticksLived++
        if (ticksLived % ticks == 0) {
            index++
            if (index > colors.lastIndex) {
                index = 0
            }
            color = colors[index]
        }
        return getValue()
    }
}