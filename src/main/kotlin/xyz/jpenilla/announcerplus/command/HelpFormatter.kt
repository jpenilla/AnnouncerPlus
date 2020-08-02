package xyz.jpenilla.announcerplus.command

import co.aikar.commands.*
import com.okkero.skedule.schedule
import org.bukkit.command.ConsoleCommandSender
import xyz.jpenilla.announcerplus.AnnouncerPlus
import xyz.jpenilla.announcerplus.command.CommandAnnouncerPlus.Companion.color
import xyz.jpenilla.jmplib.TextUtil


class HelpFormatter(private val announcerPlus: AnnouncerPlus, manager: PaperCommandManager) : CommandHelpFormatter(manager) {
    var loaded = false

    override fun printDetailedHelpHeader(help: CommandHelp, issuer: CommandIssuer, entry: HelpEntry) {
        issuer.send(TextUtil.replacePlaceholders("<color:$color>─────<white>[</white> {commandprefix}{command} <white>Detailed Help ]</white>─────", arrayToMap(getHeaderFooterFormatReplacements(help)), false))
    }

    override fun printSearchHeader(help: CommandHelp, issuer: CommandIssuer) {
        issuer.send(TextUtil.replacePlaceholders("<color:$color>─────<white>[</white> {commandprefix}{command} <italic>{search}</italic> <white>Search Results ]</white>─────", arrayToMap(getHeaderFooterFormatReplacements(help)), false))
    }

    override fun printHelpHeader(help: CommandHelp, issuer: CommandIssuer) {
        issuer.send(TextUtil.replacePlaceholders("<color:$color>─────<white>[</white> {commandprefix}{command} <white>Help ]</white>─────", arrayToMap(getHeaderFooterFormatReplacements(help)), false))
    }

    private fun getFooter(help: CommandHelp): String {
        val builder = StringBuilder()
        if (help.page > 1) {
            builder.append("<color:$color><bold><click:run_command:/announcerplus help ${listToSpaceSeparatedString(help.search)} ${help.page - 1}><hover:show_text:'<italic>Click for previous page'><<</bold></click></hover> </color:$color>")
        }
        builder.append("Page <color:$color>{page}</color:$color> of <color:$color>{totalpages}</color:$color> (<color:$color>{results} results<white>)</white> ──────────")
        if (help.page < help.totalPages && !help.isOnlyPage) {
            builder.append("<white><bold><click:run_command:/announcerplus help ${listToSpaceSeparatedString(help.search)} ${help.page + 1}><hover:show_text:'<italic>Click for next page'> >></bold></click></hover></white>")
        }
        return builder.toString()
    }

    override fun printSearchFooter(help: CommandHelp, issuer: CommandIssuer) {
        val msg = listOf(TextUtil.replacePlaceholders(getFooter(help), arrayToMap(getHeaderFooterFormatReplacements(help)), false), "")
        issuer.send(msg)
    }

    override fun printHelpFooter(help: CommandHelp, issuer: CommandIssuer) {
        printSearchFooter(help, issuer)
    }

    override fun printDetailedHelpFooter(help: CommandHelp?, issuer: CommandIssuer, entry: HelpEntry) {
    }

    override fun printDetailedHelpCommand(help: CommandHelp, issuer: CommandIssuer, entry: HelpEntry) {
        issuer.send(TextUtil.replacePlaceholders(" <white>- <click:suggest_command:/{command} ><hover:show_text:'<italic>Click to suggest'>/</white><color:$color>{command}</color:$color> <gray>{parameters}</gray></hover></click> <color:$color>{separator}</color:$color> {description}", arrayToMap(getEntryFormatReplacements(help, entry)), false))
    }

    override fun printHelpCommand(help: CommandHelp, issuer: CommandIssuer, entry: HelpEntry) {
        printDetailedHelpCommand(help, issuer, entry)
    }

    override fun printSearchEntry(help: CommandHelp, issuer: CommandIssuer, entry: HelpEntry) {
        printDetailedHelpCommand(help, issuer, entry)
    }

    private fun CommandIssuer.send(message: String) {
        if (this is BukkitCommandIssuer && loaded) {
            if (this.isPlayer || this.issuer is ConsoleCommandSender) {
                announcerPlus.chat.sendParsed(this.issuer, message)
            }
        }
    }

    private fun CommandIssuer.send(messages: List<String>) {
        for (m in messages) {
            send(m)
        }
    }

    private fun listToSpaceSeparatedString(strings: List<String>?): String {
        val b = StringBuilder()
        if (strings != null) {
            for ((index, s) in strings.withIndex()) {
                b.append(s)
                if (index != strings.size - 1) {
                    b.append(" ")
                }
            }
        }
        return b.toString()
    }

    private fun arrayToMap(list: Array<String>): Map<String, String> {
        val map = HashMap<String, String>()
        var entry = ""
        var first = true
        for (r in list) {
            if (first) {
                entry = r
                first = false
            } else {
                map[entry] = r
                first = true
            }
        }
        return map
    }

    init {
        //TODO: Find a better fix for stopping the console getting spammed with help messages on load
        announcerPlus.schedule {
            waitFor(1L)
            loaded = true
        }
    }
}