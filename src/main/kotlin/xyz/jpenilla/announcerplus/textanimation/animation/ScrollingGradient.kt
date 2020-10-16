package xyz.jpenilla.announcerplus.textanimation.animation

import xyz.jpenilla.announcerplus.textanimation.TextAnimation

class ScrollingGradient(private val increment: Float) : TextAnimation {
    companion object {
        private val GRADIENT_PHASE_RANGE = -1.0f..1.0f
    }

    private var phase = -1f

    override fun getValue(): String {
        return phase.coerceIn(GRADIENT_PHASE_RANGE).toString()
    }

    override fun nextValue(): String {
        if (phase >= 1f) {
            phase = -1f
        }
        phase += increment
        return getValue()
    }
}