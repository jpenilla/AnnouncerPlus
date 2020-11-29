package xyz.jpenilla.announcerplus.command

import cloud.commandframework.arguments.standard.EnumArgument
import cloud.commandframework.arguments.standard.StringArgument
import cloud.commandframework.bukkit.parsers.MaterialArgument
import cloud.commandframework.context.CommandContext
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

class CommandBroadcast : BaseCommand {
    private val commandManager: CommandManager by inject()
    private val configManager: ConfigManager by inject()
    private val argumentFactory: ArgumentFactory by inject()
    private val announcerPlus: AnnouncerPlus by inject()
    private val chat: Chat by inject()

    override fun register() {
        with(commandManager) {
            command(
                    commandBuilder("ap", metaWithDescription("Parses and broadcasts a message to chat in the specified world or all worlds."))
                            .literal("broadcast")
                            .argument(argumentFactory.worldPlayers("world"))
                            .argument(StringArgument.greedy("message"))
                            .permission("announcerplus.broadcast")
                            .handler(::executeBroadcast)
            )

            if (announcerPlus.toastTask != null) {
                command(
                        commandBuilder("ap", metaWithDescription("Parses and broadcasts a Toast style message to the specified world or all worlds."))
                                .literal("broadcasttoast")
                                .argument(argumentFactory.worldPlayers("world"))
                                .argument(MaterialArgument.of("icon"))
                                .argument(EnumArgument.of(ToastSettings.FrameType::class.java, "frame"))
                                .argument(StringArgument.quoted("header"), description("Quoted String"))
                                .argument(StringArgument.quoted("body"), description("Quoted String"))
                                .permission("announcerplus.broadcasttoast")
                                .handler(::executeBroadcastToast)
                )
            }

            command(
                    commandBuilder("ap", metaWithDescription("Parses and broadcasts a Title and Subtitle style message to the specified world or all worlds."))
                            .literal("broadcasttitle")
                            .argument(argumentFactory.worldPlayers("world"))
                            .argument(argumentFactory.positiveInteger("seconds"))
                            .argument(StringArgument.quoted("title"), description("Quoted String"))
                            .argument(StringArgument.quoted("subtitle"), description("Quoted String"))
                            .permission("announcerplus.broadcasttitle")
                            .handler(::executeBroadcastTitle)
            )

            command(
                    commandBuilder("ap", metaWithDescription("Parses and broadcasts an Action Bar style message to the specified world or all worlds."))
                            .literal("broadcastactionbar")
                            .argument(argumentFactory.worldPlayers("world"))
                            .argument(argumentFactory.positiveInteger("seconds"))
                            .argument(StringArgument.greedy("text"))
                            .permission("announcerplus.broadcastactionbar")
                            .handler(::executeBroadcastActionBar)
            )

            command(
                    commandBuilder("ap", metaWithDescription("Parses and broadcasts a Boss Bar style message to the specified world or all worlds."))
                            .literal("broadcastbossbar")
                            .argument(argumentFactory.worldPlayers("world"))
                            .argument(argumentFactory.positiveInteger("seconds"))
                            .argument(EnumArgument.of(BossBar.Overlay::class.java, "overlay"))
                            .argument(EnumArgument.of(BossBarUpdateTask.FillMode::class.java, "fillmode"))
                            .argument(EnumArgument.of(BossBar.Color::class.java, "color"))
                            .argument(StringArgument.greedy("text"))
                            .permission("announcerplus.broadcastbossbar")
                            .handler(::executeBroadcastBossBar)
            )
        }
    }

    private fun executeBroadcast(ctx: CommandContext<CommandSender>) {
        for (player in ctx.get<ArgumentFactory.WorldPlayers>("world").players) {
            chat.send(player, configManager.parse(player, ctx.get<String>("message")))
        }
    }

    private fun executeBroadcastToast(ctx: CommandContext<CommandSender>) {
        val toast = ToastSettings(ctx.get("icon"), ctx.get("frame"), ctx.get("header"), ctx.get("body"))
        for (player in ctx.get<ArgumentFactory.WorldPlayers>("world").players) {
            toast.displayIfEnabled(player)
        }
    }

    private fun executeBroadcastTitle(ctx: CommandContext<CommandSender>) {
        for (player in ctx.get<ArgumentFactory.WorldPlayers>("world").players) {
            TitleUpdateTask(player, 0, ctx.get("seconds"), 1, ctx.get("title"), ctx.get("subtitle")).start()
        }
    }

    private fun executeBroadcastActionBar(ctx: CommandContext<CommandSender>) {
        for (player in ctx.get<ArgumentFactory.WorldPlayers>("world").players) {
            ActionBarUpdateTask(player, ctx.get<Int>("seconds") * 20L, true, ctx.get("text")).start()
        }
    }

    private fun executeBroadcastBossBar(ctx: CommandContext<CommandSender>) {
        for (player in ctx.get<ArgumentFactory.WorldPlayers>("world").players) {
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