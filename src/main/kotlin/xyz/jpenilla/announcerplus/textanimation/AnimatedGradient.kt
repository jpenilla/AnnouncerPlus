package xyz.jpenilla.announcerplus.textanimation

class AnimatedGradient(private val increment: Float) : TextAnimation {
    private var phase = -1f

    override fun getValue(): String {
        return phase.toString()
    }

    override fun nextValue(): String {
        if (phase >= 1f) {
            phase = -1f
        }
        phase += increment
        return phase.toString()
    }
}