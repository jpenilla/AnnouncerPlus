package xyz.jpenilla.announcerplus.textanimation.animation

import xyz.jpenilla.announcerplus.textanimation.TextAnimation

class ScrollingText(text: String, private val windowSize: Int, private val ticks: Int) : TextAnimation {
    private val spaces = getSpaces(windowSize)
    private val text = "$spaces$text$spaces"
    private var index = 0
    private var ticksLived = 0

    private fun getSpaces(amount: Int): String {
        val sb = StringBuilder()
        for (i in 1..amount) {
            sb.append(" ")
        }
        return sb.toString()
    }

    private fun next() {
        index++
        if (index > text.length - windowSize) {
            index = 0
        }
    }

    override fun getValue(): String {
        return text.substring(index, index + windowSize)
    }

    override fun nextValue(): String {
        ticksLived++
        if (ticksLived % ticks == 0) {
            next()
        }
        return getValue()
    }
}