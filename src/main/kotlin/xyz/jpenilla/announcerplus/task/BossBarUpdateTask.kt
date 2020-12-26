package xyz.jpenilla.announcerplus.task

import net.kyori.adventure.bossbar.BossBar
import net.kyori.adventure.platform.bukkit.BukkitAudiences
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.entity.Player
import org.koin.core.get
import org.koin.core.inject
import xyz.jpenilla.announcerplus.config.ConfigManager
import xyz.jpenilla.announcerplus.textanimation.AnimationHolder

class BossBarUpdateTask(
    private val player: Player,
    private val lifeTime: Int,
    overlay: BossBar.Overlay,
    private val fillMode: FillMode,
    private val color: String,
    private val text: String
) : UpdateTask() {
    private val configManager: ConfigManager by inject()
    private val miniMessage: MiniMessage by inject()
    private val audience = get<BukkitAudiences>().player(player)
    private val textAnimation = AnimationHolder(player, text)
    private val colorAnimation = AnimationHolder(player, color)
    private val bar = BossBar.bossBar(Component.empty(), 0.5f, BossBar.Color.BLUE, overlay)

    override fun stop() {
        super.stop()
        audience.hideBossBar(bar)
    }

    override fun update() {
        bar.color(BossBar.Color.NAMES.value(colorAnimation.parseNext(color).toLowerCase()) ?: BossBar.Color.BLUE)
        bar.name(miniMessage.parse(configManager.parse(player, textAnimation.parseNext(text))))
        when (fillMode) {
            FillMode.FILL -> bar.progress(ticksLived / (lifeTime * 20f))
            FillMode.DRAIN -> bar.progress(1f - (ticksLived / (lifeTime * 20f)))
            FillMode.FULL -> if (ticksLived == 0L) bar.progress(1f)
            FillMode.EMPTY -> if (ticksLived == 0L) bar.progress(0f)
        }
        if (ticksLived == 0L) audience.showBossBar(bar)
    }

    override fun shouldContinue(): Boolean {
        return ticksLived < lifeTime * 20L && player.isOnline
    }

    override fun synchronizationContext() = SynchronizationContext.ASYNC

    enum class FillMode {
        FILL, DRAIN, FULL, EMPTY
    }
}