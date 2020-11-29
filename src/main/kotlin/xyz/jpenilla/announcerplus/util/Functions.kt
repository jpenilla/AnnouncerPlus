package xyz.jpenilla.announcerplus.util

import org.bukkit.Bukkit

fun dispatchCommandAsConsole(command: String): Boolean =
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command)