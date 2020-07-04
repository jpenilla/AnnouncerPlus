package xyz.jpenilla.announcerplus.command

import co.aikar.commands.BaseCommand
import co.aikar.commands.CommandHelp
import co.aikar.commands.annotation.*
import com.google.common.collect.ImmutableList
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
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

    @Dependency
    private lateinit var chat: Chat

    private lateinit var color: String
    private fun randomColor() {
        color = if (announcerPlus.prisma != null) {
            announcerPlus.prisma!!.randomColor()
        } else {
            listOf("#007AFF", "#54FFA1", "#FFB200", "#FF006C", "#D600FF", "#FFF200", "#FF00C5", "#7400FF",
                    "#E2FF00", "#FF5600", "#001CFF", "#FF0003", "#00FF07", "#65FF00", "#E400FF", "#00B9FF").random()
        }
    }

    @Default
    @HelpCommand
    @Description("AnnouncerPlus Help")
    fun onHelp(sender: CommandSender, help: CommandHelp) {
        randomColor()
        val m = "----[ <color:$color>" + announcerPlus.name + "</color:$color> Help ]----"
        chat.sendPlaceholders(sender, m)
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
        chat.sendPlaceholders(sender, m)
    }

    @Subcommand("reload|r")
    @Description("Reloads the config for AnnouncerPlus")
    @CommandPermission("announcerplus.reload")
    fun onReload(sender: CommandSender) {
        randomColor()
        chat.sendPlaceholders(sender, "<color:$color>Reloading ${announcerPlus.name} config...")
        announcerPlus.reload()
        chat.sendPlaceholders(sender, "<green>Done.")
    }

    @Subcommand("broadcast|bc")
    @CommandAlias("broadcast")
    @Description("Parse a message and broadcast it")
    @CommandPermission("announcerplus.broadcast")
    fun onBroadcast(sender: CommandSender, message: String) {
        val players = ImmutableList.copyOf(Bukkit.getOnlinePlayers())
        for (player in players) {
            chat.sendPlaceholders(player, message, cfg.placeholders)
        }
    }

    @Subcommand("parse|p")
    @Description("Parse a message and echo it back")
    @CommandPermission("announcerplus.parse")
    fun onParse(sender: CommandSender, message: String) {
        chat.sendPlaceholders(sender, message, cfg.placeholders)
    }

    @Subcommand("list|l")
    @CommandCompletion("@configs")
    @Description("Lists the messages of a config")
    @CommandPermission("announcerplus.list")
    fun onList(sender: CommandSender, @Values("@configs") config: String, @Optional page: Int?) {
        randomColor()
        val msgConfig = cfg.messageConfigs[config]!!
        var p = 1
        val pageSize = 4
        val pages = ceil(msgConfig.messages.size / pageSize.toDouble()).toInt()
        if (page != null) {
            p = page
            if (p < 1 || p > pages) {
                chat.sendPlaceholders(sender, "<red>Page does not exist</red>")
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

        chat.sendPlaceholders(sender, m.toList(), cfg.placeholders)
    }
}