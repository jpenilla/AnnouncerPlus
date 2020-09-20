package xyz.jpenilla.announcerplus.command

import co.aikar.commands.BaseCommand
import co.aikar.commands.CommandHelp
import co.aikar.commands.annotation.*
import co.aikar.commands.bukkit.contexts.OnlinePlayer
import com.google.common.collect.ImmutableList
import net.kyori.adventure.bossbar.BossBar
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.event.HoverEvent
import net.kyori.adventure.text.feature.pagination.Pagination
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.Style
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Material
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.koin.core.KoinComponent
import org.koin.core.inject
import xyz.jpenilla.announcerplus.AnnouncerPlus
import xyz.jpenilla.announcerplus.config.ConfigManager
import xyz.jpenilla.announcerplus.config.message.MessageConfig
import xyz.jpenilla.announcerplus.config.message.ToastSettings
import xyz.jpenilla.announcerplus.task.ActionBarUpdateTask
import xyz.jpenilla.announcerplus.task.BossBarUpdateTask
import xyz.jpenilla.announcerplus.task.TitleUpdateTask
import xyz.jpenilla.jmplib.Chat
import java.util.*
import kotlin.math.roundToInt

@CommandAlias("announcerplus|announcer|ap")
class CommandAnnouncerPlus : BaseCommand(), KoinComponent {
    private val configManager: ConfigManager by inject()
    private val announcerPlus: AnnouncerPlus by inject()
    private val chat: Chat by inject()

    init {
        randomColor()
    }

    private fun CommandSender.send(message: String) {
        announcerPlus.chat.send(this, configManager.parse(this, message))
    }

    private fun CommandSender.send(messages: List<String>) {
        announcerPlus.chat.send(this, configManager.parse(this, messages))
    }

    @Default
    @HelpCommand
    @Description("Shows help for AnnouncerPlus commands.")
    fun onHelp(sender: CommandSender, help: CommandHelp) {
        randomColor()
        help.showHelp()
    }

    @Subcommand("about")
    @Description("Prints some information about AnnouncerPlus.")
    fun onAbout(sender: CommandSender) {
        randomColor()
        val text = "<hover:show_text:'<rainbow>click me!'><click:open_url:${announcerPlus.description.website}>${announcerPlus.name} <color:$color>${announcerPlus.description.version}"
        val header = "<gradient:white:$color:white><strikethrough>${repeat("-", (announcerPlus.miniMessage.stripTokens(text).length / 1.2).roundToInt() + 4)}"
        val m = listOf(
                header,
                text,
                "By <color:$color>jmp",
                header
        )
        sender.send(chat.getCenteredMessage(m))
    }

    private fun repeat(s: String, i: Int): String {
        val b = StringBuilder()
        repeat(i) { b.append(s) }
        return b.toString()
    }

    @Subcommand("reload|r")
    @Description("Reloads AnnouncerPlus configs.")
    @CommandPermission("announcerplus.reload")
    fun onReload(sender: CommandSender) {
        randomColor()
        sender.send("<gradient:$color:${colors.random()}>Reloading ${announcerPlus.name} config...")
        try {
            announcerPlus.reload()
            sender.send("<green>Done.")
        } catch (e: Exception) {
            sender.send("<gradient:red:gold>I'm sorry, but there was an error reloading the plugin. This is most likely due to misconfiguration. Check console for more info.")
            e.printStackTrace()
        }
    }

    @Subcommand("broadcast|bc")
    @CommandPermission("announcerplus.broadcast")
    @Description("Parses and broadcasts a message to chat in the specified world or all worlds.")
    @CommandCompletion("* *")
    fun onBroadcastChat(sender: CommandSender, world: CommandHelper.WorldPlayers, message: String) {
        for (player in world.players) {
            chat.send(player, configManager.parse(player, message))
        }
    }

    @Subcommand("broadcasttoast|bctoast|bcto")
    @CommandPermission("announcerplus.broadcasttoast")
    @Description("Parses and broadcasts a Toast style message to the specified world or all worlds.")
    @CommandCompletion("* * * * *")
    fun onBroadcastToast(sender: CommandSender, world: CommandHelper.WorldPlayers, icon: Material, frame: ToastSettings.FrameType, header: CommandHelper.QuotedString, body: CommandHelper.QuotedString) {
        val toast = ToastSettings(icon, frame, header.string, body.string)
        for (player in world.players) {
            toast.queueDisplay(player)
        }
    }

    @Subcommand("broadcasttitle|bctitle|bcti")
    @CommandPermission("announcerplus.broadcasttitle")
    @Description("Parses and broadcasts a Title and Subtitle style message to the specified world or all worlds.")
    @CommandCompletion("* @numbers_by_5 * *")
    fun onBroadcastTitle(sender: CommandSender, world: CommandHelper.WorldPlayers, seconds: Int, title: CommandHelper.QuotedString, subTitle: CommandHelper.QuotedString) {
        for (player in world.players) {
            TitleUpdateTask(player, 0, seconds, 1, title.string, subTitle.string).start()
        }
    }

    @Subcommand("broadcastactionbar|bcactionbar|bcab")
    @CommandPermission("announcerplus.broadcastactionbar")
    @Description("Parses and broadcasts an Action Bar style message to the specified world or all worlds.")
    @CommandCompletion("* @numbers_by_5 *")
    fun onBroadcastActionBar(sender: CommandSender, world: CommandHelper.WorldPlayers, seconds: Int, text: String) {
        for (player in world.players) {
            ActionBarUpdateTask(player, seconds * 20L, true, text).start()
        }
    }

    @Subcommand("broadcastbossbar|bcbossbar|bcbb")
    @CommandPermission("announcerplus.broadcastbossbar")
    @Description("Parses and broadcasts a Boss Bar style message to the specified world or all worlds.")
    @CommandCompletion("* @numbers_by_5 * * *")
    fun onBroadcastBossBar(sender: CommandSender, world: CommandHelper.WorldPlayers, seconds: Int, overlay: BossBar.Overlay, fillMode: BossBarUpdateTask.FillMode, color: BossBar.Color, text: String) {
        for (player in world.players) {
            BossBarUpdateTask(player, seconds, overlay, fillMode, color.name, text).start()
        }
    }

    @Subcommand("send")
    @CommandPermission("announcerplus.send")
    @Description("Parses a message and sends it to the specified players.")
    @CommandCompletion("* *")
    fun onSendChat(sender: CommandSender, players: Array<OnlinePlayer>, message: String) {
        for (player in players) {
            chat.send(player.player, configManager.parse(player.player, message))
        }
    }

    @Subcommand("sendtoast")
    @CommandPermission("announcerplus.sendtoast")
    @Description("Parses and sends a Toast style message to the specified players.")
    @CommandCompletion("* * * * *")
    fun onSendToast(sender: CommandSender, players: Array<OnlinePlayer>, icon: Material, frame: ToastSettings.FrameType, header: CommandHelper.QuotedString, body: CommandHelper.QuotedString) {
        val toast = ToastSettings(icon, frame, header.string, body.string)
        for (player in players) {
            toast.queueDisplay(player.player)
        }
    }

    @Subcommand("sendtitle")
    @CommandPermission("announcerplus.sendtitle")
    @Description("Parses and sends a Title and Subtitle style message to the specified players.")
    @CommandCompletion("* @numbers_by_5 * *")
    fun onSendTitle(sender: CommandSender, players: Array<OnlinePlayer>, seconds: Int, title: CommandHelper.QuotedString, subTitle: CommandHelper.QuotedString) {
        for (player in players) {
            TitleUpdateTask(player.player, 0, seconds, 1, title.string, subTitle.string).start()
        }
    }

    @Subcommand("sendactionbar|sendab")
    @CommandPermission("announcerplus.sendactionbar")
    @Description("Parses and sends an Action Bar style message to the specified players.")
    @CommandCompletion("* @numbers_by_5 *")
    fun onSendActionBar(sender: CommandSender, players: Array<OnlinePlayer>, seconds: Int, text: String) {
        for (player in players) {
            ActionBarUpdateTask(player.player, seconds * 20L, true, text).start()
        }
    }

    @Subcommand("sendbossbar|sbossbar|sbb")
    @CommandPermission("announcerplus.sendbossbar")
    @Description("Parses and sends a Boss Bar style message to the specified players.")
    @CommandCompletion("* @numbers_by_5 * * * *")
    fun onSendBossBar(sender: CommandSender, players: Array<OnlinePlayer>, seconds: Int, overlay: BossBar.Overlay, fillMode: BossBarUpdateTask.FillMode, color: BossBar.Color, text: String) {
        for (player in players) {
            BossBarUpdateTask(player.player, seconds, overlay, fillMode, color.name, text).start()
        }
    }

    @Subcommand("parse|p")
    @Description("Parses a message and echoes it backs")
    @CommandPermission("announcerplus.parse")
    fun onParse(sender: Player, message: String) {
        sender.send(message)
    }

    @Subcommand("parseanimation|pa")
    @Description("Parse a message with an animation and display it")
    @CommandPermission("announcerplus.parseanimation")
    @CommandCompletion("@numbers_by_5 *")
    fun onParseAnimation(sender: Player, seconds: Int, message: String) {
        TitleUpdateTask(sender, 0, seconds, 0, message, message).start()
        ActionBarUpdateTask(sender, seconds * 20L, false, message).start()
    }

    @Subcommand("parsetoast|pto")
    @Description("Parse a toast message and display it")
    @CommandPermission("announcerplus.parsetoast")
    @CommandCompletion("* * * *")
    fun onParseToast(sender: Player, icon: Material, frame: ToastSettings.FrameType, header: CommandHelper.QuotedString, body: CommandHelper.QuotedString) {
        ToastSettings(icon, frame, header.string, body.string).queueDisplay(sender)
    }

    @Subcommand("parsetitle|ptitle|pti")
    @CommandPermission("announcerplus.parsetitle")
    @Description("Parses a Title and Subtitle style message and displays it back.")
    @CommandCompletion("@numbers_by_5 * *")
    fun onParseTitle(sender: Player, seconds: Int, title: CommandHelper.QuotedString, subTitle: CommandHelper.QuotedString) {
        TitleUpdateTask(sender, 0, seconds, 1, title.string, subTitle.string).start()
    }

    @Subcommand("parseactionbar|pactionbar|pab")
    @CommandPermission("announcerplus.parseactionbar")
    @Description("Parses an Action Bar style message and displays it back.")
    @CommandCompletion("@numbers_by_5 *")
    fun onParseActionBar(sender: Player, seconds: Int, text: String) {
        ActionBarUpdateTask(sender, seconds * 20L, true, text).start()
    }

    @Subcommand("parsebossbar|pbossbar|pbb")
    @CommandPermission("announcerplus.parsebossbar")
    @Description("Parses a Boss Bar style message and displays it back.")
    @CommandCompletion("@numbers_by_5 * * * *")
    fun onParseBossBar(sender: Player, seconds: Int, overlay: BossBar.Overlay, fillMode: BossBarUpdateTask.FillMode, color: BossBar.Color, text: String) {
        BossBarUpdateTask(sender, seconds, overlay, fillMode, color.name, text).start()
    }

    @Subcommand("list|l")
    @CommandCompletion("* @message_config_pages")
    @Description("Lists the messages of a config")
    @CommandPermission("announcerplus.list")
    fun onList(sender: CommandSender, config: MessageConfig, @Default("1") @Values("@message_config_pages") page: Int) {
        randomColor()
        val pagination = Pagination.builder()
                .resultsPerPage(17)
                .width(53)
                .line { line ->
                    line.character('-')
                    line.style(Style.make { builder ->
                        builder.color(TextColor.fromHexString(color))
                        builder.decorate(TextDecoration.STRIKETHROUGH)
                    })
                }
                .renderer(object : Pagination.Renderer {
                    override fun renderNextPageButton(character: Char, style: Style, clickEvent: ClickEvent): Component {
                        return TextComponent.builder()
                                .append(TextComponent.space())
                                .append(TextComponent.of("[", NamedTextColor.WHITE))
                                .append(TextComponent.of(character, style.clickEvent(clickEvent)))
                                .append(TextComponent.of("]", NamedTextColor.WHITE))
                                .append(TextComponent.space())
                                .build()
                    }

                    override fun renderPreviousPageButton(character: Char, style: Style, clickEvent: ClickEvent): Component {
                        return TextComponent.builder()
                                .append(TextComponent.space())
                                .append(TextComponent.of("[", NamedTextColor.WHITE))
                                .append(TextComponent.of(character, style.clickEvent(clickEvent)))
                                .append(TextComponent.of("]", NamedTextColor.WHITE))
                                .append(TextComponent.space())
                                .build()
                    }
                })
                .nextButton { nextButton ->
                    nextButton.style(Style.make { builder ->
                        builder.decorate(TextDecoration.BOLD)
                        builder.color(TextColor.fromHexString(color))
                        builder.hoverEvent(HoverEvent.showText(TextComponent.of("Next Page", NamedTextColor.GREEN)))
                    })
                }
                .previousButton { prevButton ->
                    prevButton.style(Style.make { builder ->
                        builder.decorate(TextDecoration.BOLD)
                        builder.color(TextColor.fromHexString(color))
                        builder.hoverEvent(HoverEvent.showText(TextComponent.of("Previous Page", NamedTextColor.RED)))
                    })
                }
                .build<String>(TextComponent.of(" Messages "),
                        { value, _ -> Collections.singleton(value?.let { announcerPlus.miniMessage.parse(it) }) },
                        { "/announcerplus list ${config.name} $it" })

        val messages = arrayListOf<String>()
        for (msg in config.messages) {
            for (line in msg.text) {
                val b = StringBuilder()
                if (msg.text.indexOf(line) == 0) {
                    b.append(" <color:$color>-</color:$color> ")
                } else {
                    b.append("   <color:$color>-</color:$color> ")
                }
                b.append("<white>\"</white>$line<reset><white>\"")
                messages.add(configManager.parse(sender, b.toString()))
            }
        }
        val l = arrayListOf<Component>(announcerPlus.miniMessage.parse("Config<gray>:</gray> <color:$color>${config.name}</color:$color> <gray><italic><hover:show_text:'<italic>Click to copy'><click:copy_to_clipboard:announcerplus.messages.${config.name}><white>(</white>announcerplus.messages.${config.name}<white>)</white>"))
        l.addAll(pagination.render(messages, page))
        chat.send(sender, l)
    }

    companion object {
        var color = "#ffffff"
        private val colors = ImmutableList.of(
                "#f44336", "#e91e63", "#9c27b0", "#673ab7", "#3f51b5", "#2196f3", "#03a9f4", "#00bcd4", "#009688", "#4caf50",
                "#8bc34a", "#cddc39", "#ffeb3b", "#ffc107", "#ff9800", "#ff5722", "#795548", "#9e9e9e", "#607d8b", "#333333")

        private fun randomColor() {
            color = colors.random()
        }
    }
}