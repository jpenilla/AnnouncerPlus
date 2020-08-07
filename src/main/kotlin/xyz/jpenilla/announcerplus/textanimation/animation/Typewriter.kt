package xyz.jpenilla.announcerplus.textanimation.animation

import xyz.jpenilla.announcerplus.textanimation.TextAnimation

class Typewriter(val text: String, private val ticks: Int) : TextAnimation {
    private var index = 0
    private var ticksLived = 0
    private var showUnderscore = true

    override fun getValue(): String {
        return "${text.substring(0, index)}${if (showUnderscore) {
            "_"
        } else {
            " "
        }}"
    }

    override fun nextValue(): String {
        ticksLived++
        if (ticksLived % 5 == 0) {
            showUnderscore = !showUnderscore
        }
        if (ticksLived % ticks == 0) {
            nextFrame()
        }
        return getValue()
    }

    private fun nextFrame() {
        index++
        if (index > text.length) {
            index = 0
        }
    }
}