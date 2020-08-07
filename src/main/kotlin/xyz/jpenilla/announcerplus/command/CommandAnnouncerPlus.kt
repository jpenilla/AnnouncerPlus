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
import org.bukkit.Bukkit
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
    @Description("AnnouncerPlus Help")
    fun onHelp(sender: CommandSender, help: CommandHelp) {
        randomColor()
        help.showHelp()
    }

    @Subcommand("about")
    @Description("About AnnouncerPlus")
    fun onAbout(sender: CommandSender) {
        randomColor()
        val m = listOf(
                "<color:$color>==========================",
                "<hover:show_text:'<rainbow>click me!'><click:open_url:${announcerPlus.description.website}>${announcerPlus.name}  <color:$color>${announcerPlus.description.version}",
                "By <color:$color>jmp",
                "<color:$color>=========================="
        )
        sender.send(chat.getCenteredMessage(m))
    }

    @Subcommand("reload|r")
    @Description("Reloads the config for AnnouncerPlus")
    @CommandPermission("announcerplus.reload")
    fun onReload(sender: CommandSender) {
        randomColor()
        sender.send(chat.getCenteredMessage("<color:$color>Reloading ${announcerPlus.name} config..."))
        announcerPlus.reload()
        sender.send(chat.getCenteredMessage("<green>Done."))
    }

    @Subcommand("broadcast|bc")
    @CommandAlias("broadcast")
    @Description("Parse a message and broadcast it")
    @CommandPermission("announcerplus.broadcast")
    fun onBroadcast(sender: CommandSender, message: String) {
        val players = ImmutableList.copyOf(Bukkit.getOnlinePlayers())
        for (player in players) {
            announcerPlus.chat.send(player, configManager.parse(player, message))
        }
    }

    @Subcommand("parse|p")
    @Description("Parse a message and echo it back")
    @CommandPermission("announcerplus.parse")
    fun onParse(sender: Player, message: String) {
        sender.send(message)
    }

    @Subcommand("parseanimation|pa")
    @Description("Parse a message with an animation and display it")
    @CommandPermission("announcerplus.parseanimation")
    fun onParseAnimation(sender: Player, length: Int, message: String) {
        TitleUpdateTask(announcerPlus, sender, 0, length, 0, message, message).start()
        ActionBarUpdateTask(announcerPlus, sender, length * 20L, false, message).start()
    }

    @Subcommand("parsetoast|pt")
    @Description("Parse a toast message and display it")
    @CommandPermission("announcerplus.parsetoast")
    @CommandCompletion("* * * *")
    fun onParseToast(sender: Player, icon: Material, frame: ToastSettings.FrameType, header: String, @Optional footer: String?) {
        ToastSettings(icon, frame, header, footer ?: "").display(announcerPlus, sender)
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