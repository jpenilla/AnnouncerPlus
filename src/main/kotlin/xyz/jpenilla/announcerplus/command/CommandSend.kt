package xyz.jpenilla.announcerplus.command

import cloud.commandframework.arguments.standard.EnumArgument
import cloud.commandframework.arguments.standard.StringArgument
import cloud.commandframework.bukkit.arguments.selector.MultiplePlayerSelector
import cloud.commandframework.bukkit.parsers.MaterialArgument
import cloud.commandframework.bukkit.parsers.selector.MultiplePlayerSelectorArgument
import cloud.commandframework.context.CommandContext
import net.kyori.adventure.bossbar.BossBar
import org.bukkit.command.CommandSender
import org.koin.core.inject
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
    private val chat: Chat by inject()

    override fun register() {
        with(commandManager) {
            command(
                    commandBuilder("ap", metaWithDescription("Parses a message and sends it to the specified players."))
                            .literal("send")
                            .argument(MultiplePlayerSelectorArgument.of("players"))
                            .argument(StringArgument.greedy("message"))
                            .permission("announcerplus.send")
                            .handler(::executeSend)
            )

            command(
                    commandBuilder("ap", metaWithDescription("Parses and sends a Toast style message to the specified players."))
                            .literal("sendtoast")
                            .argument(MultiplePlayerSelectorArgument.of("players"))
                            .argument(MaterialArgument.of("icon"))
                            .argument(EnumArgument.of(ToastSettings.FrameType::class.java, "frame"))
                            .argument(StringArgument.quoted("header"))
                            .argument(StringArgument.quoted("body"))
                            .permission("announcerplus.sendtoast")
                            .handler(::executeSendToast)
            )

            command(
                    commandBuilder("ap", metaWithDescription("Parses and sends a Title and Subtitle style message to the specified players."))
                            .literal("sendtitle")
                            .argument(MultiplePlayerSelectorArgument.of("players"))
                            .argument(argumentFactory.positiveInteger("seconds"))
                            .argument(StringArgument.quoted("title"))
                            .argument(StringArgument.quoted("subtitle"))
                            .permission("announcerplus.sendtitle")
                            .handler(::executeSendTitle)
            )

            command(
                    commandBuilder("ap", metaWithDescription("Parses and sends an Action Bar style message to the specified players."))
                            .literal("sendactionbar")
                            .argument(MultiplePlayerSelectorArgument.of("players"))
                            .argument(argumentFactory.positiveInteger("seconds"))
                            .argument(StringArgument.greedy("text"))
                            .permission("announcerplus.sendactionbar")
                            .handler(::executeSendActionBar)
            )

            command(
                    commandBuilder("ap", metaWithDescription("Parses and sends a Boss Bar style message to the specified players."))
                            .literal("sendbossbar")
                            .argument(MultiplePlayerSelectorArgument.of("players"))
                            .argument(argumentFactory.positiveInteger("seconds"))
                            .argument(EnumArgument.of(BossBar.Overlay::class.java, "overlay"))
                            .argument(EnumArgument.of(BossBarUpdateTask.FillMode::class.java, "fillmode"))
                            .argument(EnumArgument.of(BossBar.Color::class.java, "color"))
                            .argument(StringArgument.greedy("text"))
                            .permission("announcerplus.sendbossbar")
                            .handler(::executeSendBossBar)
            )
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
            toast.queueDisplay(player)
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