package xyz.jpenilla.announcerplus.command

import cloud.commandframework.context.CommandContext
import cloud.commandframework.kotlin.extension.commandBuilder
import cloud.commandframework.kotlin.extension.description
import cloud.commandframework.minecraft.extras.MinecraftHelp
import com.google.common.collect.ImmutableSet
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.LinearComponents
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.event.HoverEvent
import net.kyori.adventure.text.feature.pagination.Pagination
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.Style
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.command.CommandSender
import org.koin.core.inject
import xyz.jpenilla.announcerplus.AnnouncerPlus
import xyz.jpenilla.announcerplus.config.ConfigManager
import xyz.jpenilla.announcerplus.config.message.MessageConfig
import xyz.jpenilla.jmplib.Chat
import java.util.Collections
import kotlin.math.roundToInt

class CommandAnnouncerPlus : BaseCommand {
    private val announcerPlus: AnnouncerPlus by inject()
    private val commandManager: CommandManager by inject()
    private val configManager: ConfigManager by inject()
    private val minecraftHelp: MinecraftHelp<CommandSender> by inject()
    private val argumentFactory: ArgumentFactory by inject()
    private val chat: Chat by inject()

    override fun register() {
        commandManager.commandBuilder("ap", aliases = arrayOf("announcerplus", "announcer")) {
            registerCopy {
                commandDescription("Shows help for AnnouncerPlus commands.")
                literal("help")
                argument(description("Help Query")) {
                    argumentFactory.helpQuery("query")
                }
                handler(::executeHelp)
            }
            registerCopy {
                commandDescription("Prints some information about AnnouncerPlus.")
                literal("about")
                handler(::executeAbout)
            }
            registerCopy {
                permission = "announcerplus.reload"
                commandDescription("Reloads AnnouncerPlus configs.")
                literal("reload")
                handler(::executeReload)
            }
            registerCopy {
                permission = "announcerplus.list"
                commandDescription("Displays the chat messages of a message config to the command sender.")
                literal("list")
                argument(argumentFactory.messageConfig("config"))
                argument(argumentFactory.positiveInteger("page").asOptional())
                handler(::executeList)
            }
        }
    }

    private fun executeHelp(ctx: CommandContext<CommandSender>) {
        minecraftHelp.queryCommands(ctx.getOrDefault("query", "")!!, ctx.sender)
    }

    private fun executeAbout(ctx: CommandContext<CommandSender>) {
        val color = randomColor()
        val text = "<hover:show_text:'<rainbow>click me!'><click:open_url:${announcerPlus.description.website}>${announcerPlus.name} <$color>${announcerPlus.description.version}"
        val header = "<gradient:white:$color:white><strikethrough>${"-".repeat((announcerPlus.miniMessage.stripTokens(text).length / 1.2).roundToInt() + 4)}"
        listOf(
                header,
                text,
                "By <$color>jmp",
                header
        ).forEach { chat.send(ctx.sender, chat.getCenteredMessage(it)) }
    }

    private fun executeReload(ctx: CommandContext<CommandSender>) {
        chat.send(ctx.sender, chat.getCenteredMessage("<italic><gradient:${randomColor()}:${randomColor()}>Reloading ${announcerPlus.name} config..."))
        try {
            announcerPlus.reload()
            chat.send(ctx.sender, chat.getCenteredMessage("<green>Done."))
        } catch (e: Exception) {
            chat.send(ctx.sender, "<gradient:red:gold>I'm sorry, but there was an error reloading the plugin. This is most likely due to misconfiguration. Check console for more info.")
            e.printStackTrace()
        }
    }

    private fun executeList(ctx: CommandContext<CommandSender>) {
        val color = randomColor()
        val config = ctx.get<MessageConfig>("config")
        val page = ctx.getOrDefault("page", 1) ?: 1
        val pagination = Pagination.builder().apply {
            resultsPerPage(17)
            width(53)
            line { line ->
                line.character('-')
                line.style(Style.style { builder ->
                    builder.color(TextColor.fromHexString(color))
                    builder.decorate(TextDecoration.STRIKETHROUGH)
                })
            }
            renderer(object : Pagination.Renderer {
                override fun renderNextPageButton(character: Char, style: Style, clickEvent: ClickEvent): Component {
                    LinearComponents.linear(

                    )
                    return Component.text()
                        .append(Component.space())
                        .append(Component.text("[", NamedTextColor.WHITE))
                        .append(Component.text(character, style.clickEvent(clickEvent)))
                        .append(Component.text("]", NamedTextColor.WHITE))
                        .append(Component.space())
                        .build()
                }

                override fun renderPreviousPageButton(
                    character: Char,
                    style: Style,
                    clickEvent: ClickEvent
                ): Component {
                    return Component.text()
                        .append(Component.space())
                        .append(Component.text("[", NamedTextColor.WHITE))
                        .append(Component.text(character, style.clickEvent(clickEvent)))
                        .append(Component.text("]", NamedTextColor.WHITE))
                        .append(Component.space())
                        .build()
                }
            })
            nextButton { nextButton ->
                nextButton.style(Style.style { builder ->
                    builder.decorate(TextDecoration.BOLD)
                    builder.color(TextColor.fromHexString(color))
                    builder.hoverEvent(HoverEvent.showText(Component.text("Next Page", NamedTextColor.GREEN)))
                })
            }
            previousButton { prevButton ->
                prevButton.style(Style.style { builder ->
                    builder.decorate(TextDecoration.BOLD)
                    builder.color(TextColor.fromHexString(color))
                    builder.hoverEvent(HoverEvent.showText(Component.text("Previous Page", NamedTextColor.RED)))
                })
            }
        }.build<String>(
            Component.text("Messages"),
            { value, _ -> Collections.singleton(value?.let { announcerPlus.miniMessage.parse(it) }) },
            { "/announcerplus list ${config.name} $it" }
        )

        val messages = arrayListOf<String>()
        for (msg in config.messages) {
            for (line in msg.messageText) {
                val b = StringBuilder()
                if (msg.messageText.indexOf(line) == 0) {
                    b.append(" <color:$color>-</color:$color> ")
                } else {
                    b.append("   <color:$color>-</color:$color> ")
                }
                b.append("<white>\"</white>$line<reset><white>\"")
                messages.add(configManager.parse(ctx.sender, b.toString()))
            }
        }
        val l = arrayListOf<Component>(announcerPlus.miniMessage.parse("Config<gray>:</gray> <color:$color>${config.name}</color:$color> <gray><italic><hover:show_text:'<italic>Click to copy'><click:copy_to_clipboard:announcerplus.messages.${config.name}><white>(</white>announcerplus.messages.${config.name}<white>)</white>"))
        l.addAll(pagination.render(messages, page))
        chat.send(ctx.sender, l)
    }

    companion object {
        private val colors = ImmutableSet.of(
                "#f44336", "#e91e63", "#9c27b0", "#673ab7", "#3f51b5", "#2196f3", "#03a9f4", "#00bcd4", "#009688", "#4caf50",
                "#8bc34a", "#cddc39", "#ffeb3b", "#ffc107", "#ff9800", "#ff5722", "#795548", "#9e9e9e", "#607d8b", "#333333")
        fun randomColor(): String = colors.random()
    }
}
