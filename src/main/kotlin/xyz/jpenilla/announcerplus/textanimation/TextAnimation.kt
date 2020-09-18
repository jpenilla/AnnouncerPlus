package xyz.jpenilla.announcerplus.textanimation

import org.koin.core.KoinComponent

interface TextAnimation: KoinComponent {
    fun getValue(): String
    fun nextValue(): String
}