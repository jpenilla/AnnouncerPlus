package xyz.jpenilla.announcerplus.command

import org.koin.core.KoinComponent

interface BaseCommand : KoinComponent {
    fun register()
}
