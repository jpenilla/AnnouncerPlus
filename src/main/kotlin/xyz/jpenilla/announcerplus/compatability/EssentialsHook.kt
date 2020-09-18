package xyz.jpenilla.announcerplus.compatability

import net.ess3.api.IEssentials
import net.ess3.api.IUser
import org.bukkit.Bukkit
import org.bukkit.entity.Player

class EssentialsHook {
    val essentials = Bukkit.getServer().pluginManager.getPlugin("Essentials") as IEssentials

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