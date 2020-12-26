package xyz.jpenilla.announcerplus.command

import cloud.commandframework.arguments.standard.EnumArgument
import cloud.commandframework.arguments.standard.StringArgument
import cloud.commandframework.bukkit.arguments.selector.MultiplePlayerSelector
import cloud.commandframework.bukkit.parsers.MaterialArgument
import cloud.commandframework.bukkit.parsers.selector.MultiplePlayerSelectorArgument
import cloud.commandframework.context.CommandContext
import cloud.commandframework.kotlin.extension.commandBuilder
import cloud.commandframework.kotlin.extension.description
import net.kyori.adventure.bossbar.BossBar
import org.bukkit.command.CommandSender
import org.koin.core.inject
import xyz.jpenilla.announcerplus.AnnouncerPlus
import xyz.jpenilla.announcerplus.config.ConfigManager
import xyz.jpenilla.announcerplus.config.message.ToastSettings
import xyz.jpenilla.announcerplus.task.ActionBarUpdateTask
import xyz.jpenilla.announcerplus.task.BossBarUpdateTask
import xyz.jpenilla.announcerplus.task.TitleUpdateTask
import xyz.jpenilla.jmplib.Chat

class CommandSend : BaseCommand {
    private val commandManager: CommandManager by inject()
    private val configManager: ConfigManager by inject()
    private val argumentFactory: ArgumentFactory by inject()
    private val announcerPlus: AnnouncerPlus by inject()
    private val chat: Chat by inject()

    override fun register() {
        commandManager.commandBuilder("ap") {
            registerCopy("send") {
                permission = "announcerplus.send"
                commandDescription("Parses a message and sends it to the specified players.")
                argument(MultiplePlayerSelectorArgument.of("players"))
                argument(StringArgument.greedy("message"))
                handler(::executeSend)
            }
            if (announcerPlus.toastTask != null) {
                registerCopy("sendtoast") {
                    permission = "announcerplus.sendtoast"
                    commandDescription("Parses and sends a Toast style message to the specified players.")
                    argument(MultiplePlayerSelectorArgument.of("players"))
                    argument(MaterialArgument.of("icon"))
                    argument(EnumArgument.of(ToastSettings.FrameType::class.java, "frame"))
                    argument(StringArgument.quoted("header"), description("Quoted String"))
                    argument(StringArgument.quoted("body"), description("Quoted String"))
                    handler(::executeSendToast)
                }
            }
            registerCopy("sendtitle") {
                permission = "announcerplus.sendtitle"
                commandDescription("Parses and sends a Title and Subtitle style message to the specified players.")
                argument(MultiplePlayerSelectorArgument.of("players"))
                argument(argumentFactory.positiveInteger("seconds"))
                argument(StringArgument.quoted("title"), description("Quoted String"))
                argument(StringArgument.quoted("subtitle"), description("Quoted String"))
                handler(::executeSendTitle)
            }
            registerCopy("sendactionbar") {
                permission = "announcerplus.sendactionbar"
                commandDescription("Parses and sends an Action Bar style message to the specified players.")
                argument(MultiplePlayerSelectorArgument.of("players"))
                argument(argumentFactory.positiveInteger("seconds"))
                argument(StringArgument.greedy("text"))
                handler(::executeSendActionBar)
            }
            registerCopy("sendbossbar") {
                permission = "announcerplus.sendbossbar"
                commandDescription("Parses and sends a Boss Bar style message to the specified players.")
                argument(MultiplePlayerSelectorArgument.of("players"))
                argument(argumentFactory.positiveInteger("seconds"))
                argument(EnumArgument.of(BossBar.Overlay::class.java, "overlay"))
                argument(EnumArgument.of(BossBarUpdateTask.FillMode::class.java, "fillmode"))
                argument(EnumArgument.of(BossBar.Color::class.java, "color"))
                argument(StringArgument.greedy("text"))
                handler(::executeSendBossBar)
            }
        }
    }

    private fun executeSend(ctx: CommandContext<CommandSender>) {
        for (player in ctx.get<MultiplePlayerSelector>("players").players) {
            chat.send(player, configManager.parse(player, ctx.get<String>("message")))
        }
    }

    private fun executeSendToast(ctx: CommandContext<CommandSender>) {
        val toast = ToastSettings(ctx.get("icon"), ctx.get("frame"), ctx.get("header"), ctx.get("body"))
        for (player in ctx.get<MultiplePlayerSelector>("players").players) {
            toast.displayIfEnabled(player)
        }
    }

    private fun executeSendTitle(ctx: CommandContext<CommandSender>) {
        for (player in ctx.get<MultiplePlayerSelector>("players").players) {
            TitleUpdateTask(player, 0, ctx.get("seconds"), 1, ctx.get("title"), ctx.get("subtitle")).start()
        }
    }

    private fun executeSendActionBar(ctx: CommandContext<CommandSender>) {
        for (player in ctx.get<MultiplePlayerSelector>("players").players) {
            ActionBarUpdateTask(player, ctx.get<Int>("seconds") * 20L, true, ctx.get("text")).start()
        }
    }

    private fun executeSendBossBar(ctx: CommandContext<CommandSender>) {
        for (player in ctx.get<MultiplePlayerSelector>("players").players) {
            BossBarUpdateTask(
                    player,
                    ctx.get("seconds"),
                    ctx.get("overlay"),
                    ctx.get("fillmode"),
                    ctx.get<BossBar.Color>("color").toString(),
                    ctx.get("text")
            ).start()
        }
    }
}
