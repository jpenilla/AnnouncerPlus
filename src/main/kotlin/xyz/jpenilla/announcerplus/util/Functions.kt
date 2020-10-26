package xyz.jpenilla.announcerplus.util

import org.bukkit.Bukkit

fun dispatchCommandAsConsole(command: String) {
    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command)
}