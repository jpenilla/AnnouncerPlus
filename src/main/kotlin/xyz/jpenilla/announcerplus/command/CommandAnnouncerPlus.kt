package xyz.jpenilla.announcerplus.command

import co.aikar.commands.BaseCommand
import co.aikar.commands.CommandHelp
import co.aikar.commands.annotation.*
import co.aikar.commands.annotation.Optional
import com.google.common.collect.ImmutableList
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.event.HoverEvent
import net.kyori.adventure.text.event.HoverEventSource
import net.kyori.adventure.text.feature.pagination.Pagination
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.Style
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Material
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import xyz.jpenilla.announcerplus.AnnouncerPlus
import xyz.jpenilla.announcerplus.config.ConfigManager
import xyz.jpenilla.announcerplus.config.message.MessageConfig
import xyz.jpenilla.announcerplus.config.message.ToastSettings
import xyz.jpenilla.announcerplus.task.ActionBarUpdateTask
import xyz.jpenilla.announcerplus.task.TitleUpdateTask
import xyz.jpenilla.jmplib.Chat
import java.util.*


@CommandAlias("announcerplus|announcer|ap")
class CommandAnnouncerPlus : BaseCommand() {

    @Dependency
    private lateinit var configManager: ConfigManager

    @Dependency
    private lateinit var announcerPlus: AnnouncerPlus

    @Dependency
    private lateinit var chat: Chat

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
        val m = listOf(
                "<gradient:white:$color:white><strikethrough>---------------------------",
                "<hover:show_text:'<rainbow>click me!'><click:open_url:${announcerPlus.description.website}>${announcerPlus.name} <color:$color>${announcerPlus.description.version}",
                "By <color:$color>jmp",
                "<gradient:white:$color:white><strikethrough>---------------------------"
        )
        sender.send(chat.getCenteredMessage(m))
    }

    @Subcommand("reload|r")
    @Description("Reloads AnnouncerPlus configs.")
    @CommandPermission("announcerplus.reload")
    fun onReload(sender: CommandSender) {
        randomColor()
        sender.send(chat.getCenteredMessage("<color:$color>Reloading ${announcerPlus.name} config..."))
        try {
            announcerPlus.reload()
            sender.send(chat.getCenteredMessage("<green>Done."))
        } catch (e: Exception) {
            sender.send(chat.getCenteredMessage("<gradient:red:gold>I'm sorry, but there was an error reloading the plugin. This is most likely due to misconfiguration. Check console for more info."))
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
    @CommandCompletion("* * * *")
    fun onBroadcastToast(sender: CommandSender, world: CommandHelper.WorldPlayers, icon: Material, frame: ToastSettings.FrameType, text: CommandHelper.StringPair) {
        for (player in world.players) {
            ToastSettings(icon, frame, text.first, text.second).display(announcerPlus, player)
        }
    }

    @Subcommand("broadcasttitle|bctitle|bcti")
    @CommandPermission("announcerplus.broadcasttitle")
    @Description("Parses and broadcasts a Title and Subtitle style message to the specified world or all worlds.")
    @CommandCompletion("* @numbers_by_5 *")
    fun onBroadcastTitle(sender: CommandSender, world: CommandHelper.WorldPlayers, seconds: Int, text: CommandHelper.StringPair) {
        for (player in world.players) {
            TitleUpdateTask(announcerPlus, player, 0, seconds, 1, text.first, text.second).start()
        }
    }

    @Subcommand("broadcastactionbar|bcactionbar|bcab")
    @CommandPermission("announcerplus.broadcastactionbar")
    @Description("Parses and broadcasts an Action Bar style message to the specified world or all worlds.")
    @CommandCompletion("* @numbers_by_5 *")
    fun onBroadcastActionBar(sender: CommandSender, world: CommandHelper.WorldPlayers, seconds: Int, text: String) {
        for (player in world.players) {
            ActionBarUpdateTask(announcerPlus, player, seconds * 20L, true, text).start()
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
        TitleUpdateTask(announcerPlus, sender, 0, seconds, 0, message, message).start()
        ActionBarUpdateTask(announcerPlus, sender, seconds * 20L, false, message).start()
    }

    @Subcommand("parsetoast|pto")
    @Description("Parse a toast message and display it")
    @CommandPermission("announcerplus.parsetoast")
    @CommandCompletion("* * *")
    fun onParseToast(sender: Player, icon: Material, frame: ToastSettings.FrameType, text: CommandHelper.StringPair) {
        ToastSettings(icon, frame, text.first, text.second).display(announcerPlus, sender)
    }

    @Subcommand("parsetitle|ptitle|pti")
    @CommandPermission("announcerplus.parsetitle")
    @Description("Parses a Title and Subtitle style message and displays it back.")
    @CommandCompletion("@numbers_by_5 *")
    fun onParseTitle(sender: Player, seconds: Int, text: CommandHelper.StringPair) {
        TitleUpdateTask(announcerPlus, sender, 0, seconds, 1, text.first, text.second).start()
    }

    @Subcommand("parseactionbar|pactionbar|pab")
    @CommandPermission("announcerplus.parseactionbar")
    @Description("Parses an Action Bar style message and displays it back.")
    @CommandCompletion("@numbers_by_5 *")
    fun onParseActionBar(sender: Player, seconds: Int, text: String) {
        ActionBarUpdateTask(announcerPlus, sender, seconds * 20L, true, text).start()
    }

    @Subcommand("list|l")
    @CommandCompletion("* @message_config_pages")
    @Description("Lists the messages of a config")
    @CommandPermission("announcerplus.list")
    fun onList(sender: CommandSender, config: MessageConfig, @Optional @Values("@message_config_pages") page: Int?) {
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
                        val hover: HoverEventSource<Component> = HoverEvent.showText(TextComponent.of("Next Page", NamedTextColor.GREEN))
                        builder.hoverEvent(hover)
                    })
                }
                .previousButton { prevButton ->
                    prevButton.style(Style.make { builder ->
                        builder.decorate(TextDecoration.BOLD)
                        builder.color(TextColor.fromHexString(color))
                        val hover: HoverEventSource<Component> = HoverEvent.showText(TextComponent.of("Previous Page", NamedTextColor.RED))
                        builder.hoverEvent(hover)
                    })
                }
                .build<String>(TextComponent.of("Messages"),
                        { value: String?, index: Int ->
                            Collections.singleton(
                                    value?.let { announcerPlus.miniMessage.parse(it) }
                            )
                        }) { p: Int -> "/announcerplus list ${config.name} $p" }

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
        l.addAll(pagination.render(messages, page ?: 1))
        sendComponents(sender, l)
    }

    private fun sendComponents(sender: CommandSender, components: List<Component>) {
        for (component in components) {
            if (sender is Player) {
                announcerPlus.audience.player(sender).sendMessage(component)
            } else {
                announcerPlus.audience.console().sendMessage(announcerPlus.miniMessage.parse(announcerPlus.miniMessage.stripTokens(announcerPlus.miniMessage.serialize(component))))
            }
        }
    }

    companion object {
        var color = "#ffffff"
        private val colors: ImmutableList<String> = ImmutableList.of(
                "#f44336", "#e91e63", "#9c27b0", "#673ab7", "#3f51b5", "#2196f3", "#03a9f4", "#00bcd4", "#009688", "#4caf50",
                "#8bc34a", "#cddc39", "#ffeb3b", "#ffc107", "#ff9800", "#ff5722", "#795548", "#9e9e9e", "#607d8b", "#333333")

        private fun randomColor() {
            color = colors.random()
        }
    }
}