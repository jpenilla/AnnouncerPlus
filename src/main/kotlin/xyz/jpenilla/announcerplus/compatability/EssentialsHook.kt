package xyz.jpenilla.announcerplus.compatability

import net.ess3.api.IEssentials
import net.ess3.api.IUser
import org.bukkit.entity.Player
import xyz.jpenilla.announcerplus.AnnouncerPlus

class EssentialsHook(announcerPlus: AnnouncerPlus) {
    val essentials = announcerPlus.server.pluginManager.getPlugin("Essentials") as IEssentials

    fun isAfk(player: Player): Boolean {
        return user(player).isAfk
    }

    fun isVanished(player: Player): Boolean {
        return user(player).isVanished
    }

    fun user(player: Player): IUser {
        return essentials.getUser(player)
    }
}