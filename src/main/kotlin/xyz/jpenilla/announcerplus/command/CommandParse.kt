package xyz.jpenilla.announcerplus.command

import cloud.commandframework.arguments.standard.EnumArgument
import cloud.commandframework.arguments.standard.StringArgument
import cloud.commandframework.bukkit.parsers.MaterialArgument
import cloud.commandframework.context.CommandContext
import net.kyori.adventure.bossbar.BossBar
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.koin.core.inject
import xyz.jpenilla.announcerplus.config.ConfigManager
import xyz.jpenilla.announcerplus.config.message.ToastSettings
import xyz.jpenilla.announcerplus.task.ActionBarUpdateTask
import xyz.jpenilla.announcerplus.task.BossBarUpdateTask
import xyz.jpenilla.announcerplus.task.TitleUpdateTask
import xyz.jpenilla.jmplib.Chat

class CommandParse : BaseCommand {
    private val commandManager: CommandManager by inject()
    private val configManager: ConfigManager by inject()
    private val argumentFactory: ArgumentFactory by inject()
    private val chat: Chat by inject()

    override fun register() {
        with(commandManager) {
            command(
                    commandBuilder("ap", metaWithDescription("Parses a message and echoes it back."))
                            .literal("parse")
                            .argument(StringArgument.greedy("message"))
                            .permission("announcerplus.parse")
                            .handler(::executeParse)
            )

            command(
                    commandBuilder("ap", metaWithDescription("Parses a Toast style message and displays it to the command sender."))
                            .literal("parsetoast")
                            .argument(MaterialArgument.of("icon"))
                            .argument(EnumArgument.of(ToastSettings.FrameType::class.java, "frame"))
                            .argument(StringArgument.quoted("header"))
                            .argument(StringArgument.quoted("body"))
                            .permission("announcerplus.parsetoast")
                            .senderType(Player::class.java)
                            .handler(::executeParseToast)
            )

            command(
                    commandBuilder("ap", metaWithDescription("Parses a Title and Subtitle style message and displays it to the command sender."))
                            .literal("parsetitle")
                            .argument(argumentFactory.positiveInteger("seconds"))
                            .argument(StringArgument.quoted("title"))
                            .argument(StringArgument.quoted("subtitle"))
                            .permission("announcerplus.parsetitle")
                            .senderType(Player::class.java)
                            .handler(::executeParseTitle)
            )

            command(
                    commandBuilder("ap", metaWithDescription("Parses an Action Bar style message and displays it to the command sender."))
                            .literal("parseactionbar")
                            .argument(argumentFactory.positiveInteger("seconds"))
                            .argument(StringArgument.greedy("text"))
                            .permission("announcerplus.parseactionbar")
                            .senderType(Player::class.java)
                            .handler(::executeParseActionBar)
            )

            command(
                    commandBuilder("ap", metaWithDescription("Parses a Boss Bar style message and displays it to the command sender."))
                            .literal("parsebossbar")
                            .argument(argumentFactory.positiveInteger("seconds"))
                            .argument(EnumArgument.of(BossBar.Overlay::class.java, "overlay"))
                            .argument(EnumArgument.of(BossBarUpdateTask.FillMode::class.java, "fillmode"))
                            .argument(EnumArgument.of(BossBar.Color::class.java, "color"))
                            .argument(StringArgument.greedy("text"))
                            .permission("announcerplus.parsebossbar")
                            .senderType(Player::class.java)
                            .handler(::executeParseBossBar)
            )

            command(
                    commandBuilder("ap", metaWithDescription("Parses a message with an animation and displays it to the command sender."))
                            .literal("parseanimation")
                            .argument(argumentFactory.positiveInteger("seconds"))
                            .argument(StringArgument.greedy("message"))
                            .permission("announcerplus.parseanimation")
                            .senderType(Player::class.java)
                            .handler(::executeParseAnimation)
            )
        }
    }

    private fun executeParse(ctx: CommandContext<CommandSender>) {
        chat.send(ctx.sender, configManager.parse(ctx.sender, ctx.get<String>("message")))
    }

    private fun executeParseToast(ctx: CommandContext<CommandSender>) {
        val toast = ToastSettings(ctx.get("icon"), ctx.get("frame"), ctx.get("header"), ctx.get("body"))
        toast.queueDisplay(ctx.sender as Player)
    }

    private fun executeParseTitle(ctx: CommandContext<CommandSender>) {
        TitleUpdateTask(ctx.sender as Player, 0, ctx.get("seconds"), 1, ctx.get("title"), ctx.get("subtitle")).start()
    }

    private fun executeParseActionBar(ctx: CommandContext<CommandSender>) {
        ActionBarUpdateTask(ctx.sender as Player, ctx.get<Int>("seconds") * 20L, true, ctx.get("text")).start()
    }

    private fun executeParseBossBar(ctx: CommandContext<CommandSender>) {
        BossBarUpdateTask(
                ctx.sender as Player,
                ctx.get("seconds"),
                ctx.get("overlay"),
                ctx.get("fillmode"),
                ctx.get<BossBar.Color>("color").toString(),
                ctx.get("text")
        ).start()
    }

    private fun executeParseAnimation(ctx: CommandContext<CommandSender>) {
        val player = ctx.sender as Player
        val seconds = ctx.get<Int>("seconds")
        val message = ctx.get<String>("message")
        TitleUpdateTask(player, 0, seconds, 0, message, message).start()
        ActionBarUpdateTask(player, seconds * 20L, false, message).start()
    }
}