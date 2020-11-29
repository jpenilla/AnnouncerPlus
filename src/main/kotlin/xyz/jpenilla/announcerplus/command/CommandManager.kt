package xyz.jpenilla.announcerplus.command

import cloud.commandframework.Description
import cloud.commandframework.bukkit.BukkitCommandMeta
import cloud.commandframework.bukkit.BukkitCommandMetaBuilder
import cloud.commandframework.execution.AsynchronousCommandExecutionCoordinator
import cloud.commandframework.minecraft.extras.MinecraftExceptionHandler
import cloud.commandframework.minecraft.extras.MinecraftHelp
import cloud.commandframework.paper.PaperCommandManager
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor
import org.bukkit.command.CommandSender
import org.koin.core.context.loadKoinModules
import org.koin.dsl.module
import xyz.jpenilla.announcerplus.AnnouncerPlus
import xyz.jpenilla.announcerplus.util.Constants
import java.util.function.Function

class CommandManager(announcerPlus: AnnouncerPlus) : PaperCommandManager<CommandSender>(
        announcerPlus,
        AsynchronousCommandExecutionCoordinator.newBuilder<CommandSender>().build(),
        Function.identity(),
        Function.identity()
) {

    private val minecraftHelp = MinecraftHelp(
            "/announcerplus help",
            announcerPlus.audience::sender,
            this
    ).apply {
        helpColors = MinecraftHelp.HelpColors.of(
                TextColor.color(0x00a3ff),
                NamedTextColor.WHITE,
                TextColor.color(0x284fff),
                NamedTextColor.GRAY,
                NamedTextColor.DARK_GRAY
        )
        setMessage(MinecraftHelp.MESSAGE_HELP_TITLE, "AnnouncerPlus Help")
    }

    init {
        MinecraftExceptionHandler<CommandSender>()
                .withDefaultHandlers()
                .withDecorator {
                    Component.text()
                            .append(Constants.CHAT_PREFIX)
                            .append(it)
                            .build()
                }
                .apply(this, announcerPlus.audience::sender)

        try {
            this.registerBrigadier()
            this.brigadierManager()?.setNativeNumberSuggestions(false)
            announcerPlus.logger.info("Successfully registered Mojang Brigadier support for commands.")
        } catch (ignored: Exception) {
        }

        try {
            this.registerAsynchronousCompletions()
            announcerPlus.logger.info("Successfully registered asynchronous command completion listener.")
        } catch (ignored: Exception) {
        }

        loadKoinModules(module {
            single { this@CommandManager }
            single { minecraftHelp }
            single { ArgumentFactory() }
        })

        listOf(
                CommandAnnouncerPlus(),
                CommandBroadcast(),
                CommandSend(),
                CommandParse()
        ).forEach(BaseCommand::register)
    }

}

fun metaWithDescription(description: String): BukkitCommandMeta =
        BukkitCommandMetaBuilder.builder().withDescription(description).build()

fun description(description: String): Description =
        Description.of(description)