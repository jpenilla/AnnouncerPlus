package xyz.jpenilla.announcerplus.command

import co.aikar.commands.BaseCommand
import co.aikar.commands.CommandHelp
import co.aikar.commands.annotation.*
import net.kyori.adventure.platform.bukkit.BukkitAudiences
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import xyz.jpenilla.announcerplus.AnnouncerPlus
import xyz.jpenilla.announcerplus.config.Config
import xyz.jpenilla.jmplib.Chat
import kotlin.math.ceil


@CommandAlias("announcerplus|announcer|ap")
class CommandAnnouncerPlus : BaseCommand() {
    @Dependency
    private lateinit var cfg: Config

    @Dependency
    private lateinit var announcerPlus: AnnouncerPlus

    private lateinit var color: String
    private fun randomColor() {
        color = listOf("#007AFF", "#54FFA1", "#FFB200", "#FF006C", "#D600FF", "#FFF200", "#FF00C5", "#7400FF",
                "#E2FF00", "#FF5600", "#001CFF", "#FF0003", "#00FF07", "#65FF00", "#E400FF", "#00B9FF").random()
    }

    @Default
    @HelpCommand
    @Description("AnnouncerPlus Help")
    fun onHelp(sender: CommandSender, help: CommandHelp) {
        randomColor()
        val m = "----[ <color:$color>" + announcerPlus.name + "</color:$color> Help ]----"
        send(sender, m)
        help.showHelp()
    }

    @Subcommand("about")
    @Description("About AnnouncerPlus")
    fun onAbout(sender: CommandSender) {
        randomColor()
        val m = listOf(
                "<color:$color>==========================",
                announcerPlus.name + " <color:$color>" + announcerPlus.description.version,
                "By <color:$color>jmp",
                "<color:$color>=========================="
        )
        send(sender, m)
    }

    @Subcommand("reload|r")
    @Description("Reloads the config for AnnouncerPlus")
    @CommandPermission("announcerplus.admin")
    fun onReload(sender: CommandSender) {
        randomColor()
        send(sender, "<color:$color>Reloading ${announcerPlus.name} config...")
        announcerPlus.reload()
        send(sender, "<green>Done.")
    }

    @Subcommand("list|l")
    @CommandCompletion("@configs")
    @Description("Lists the messages of a config")
    @CommandPermission("announcerplus.admin")
    fun onList(sender: CommandSender, @Values("@configs") config: String, @Optional page: Int?) {
        randomColor()
        val msgConfig = cfg.messageConfigs[config]!!
        var p = 1
        val pageSize = 4
        val pages = ceil(msgConfig.messages.size / pageSize.toDouble()).toInt()
        if (page != null) {
            p = page
            if (p < 1 || p > pages) {
                Chat.sendMsg(sender, "&4Page does not exist")
                return
            }
        }

        val m = ArrayList<String>()
        m.add("Messages<gray>:</gray> <color:$color>$config</color:$color> <gray><italic>(announcerplus.messages.$config)")
        m.add("<color:$color>Page <white>$p</white> / <white>$pages</white> ==============================")
        val n = pageSize * (p - 1)
        for (i in n until (n + pageSize)) {
            try {
                m.add(" <color:$color>-</color:$color> <white>\"</white>${msgConfig.messages[i]}<white>\"")
            } catch (e: Exception) {
            }
        }
        m.add("<color:$color>Page <white>$p</white> / <white>$pages</white> ==============================")

        send(sender, m.toList())
    }

    private val miniMessage = MiniMessage.instance()

    private fun send(sender: CommandSender, messages: List<String>) {
        for (message in messages) {
            send(sender, message)
        }
    }

    private fun send(sender: CommandSender, message: String) {
        val finalMessage = if (sender is Player) {
            announcerPlus.cfg.replacePlaceholders(sender, message)
        } else {
            announcerPlus.cfg.replacePlaceholders(null, message)
        }
        val audience = BukkitAudiences.create(announcerPlus)
        when (sender) {
            is Player -> {
                val c = miniMessage.parse(finalMessage)
                audience.player(sender).sendMessage(c)
            }
            else -> {
                val c = miniMessage.parse(miniMessage.stripTokens(finalMessage))
                audience.console().sendMessage(c)
            }
        }
    }
}