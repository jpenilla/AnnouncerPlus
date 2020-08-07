package xyz.jpenilla.announcerplus.textanimation.animation

import xyz.jpenilla.announcerplus.textanimation.TextAnimation

class ScrollingGradient(private val increment: Float) : TextAnimation {
    private var phase = -1f

    override fun getValue(): String {
        return phase.toString()
    }

    override fun nextValue(): String {
        if (phase >= 1f) {
            phase = -1f
        }
        phase += increment
        return getValue()
    }
}