package xyz.devcmb.gnome.feature

import dev.isxander.yacl3.api.OptionDescription
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.minecraft.ChatFormatting
import net.minecraft.client.Minecraft
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.core.particles.DustParticleOptions
import net.minecraft.network.chat.Component
import net.minecraft.world.phys.Vec3
import xyz.devcmb.gnome.config.Config
import xyz.devcmb.gnome.mixin.accessor.BossEventAccessor
import xyz.devcmb.gnome.util.isOnFishing
import xyz.devcmb.gnome.util.isOnIsland
import kotlin.random.Random
import kotlin.reflect.KMutableProperty0

class PylonBoundaries : GnomeFeature {
    override val id: String = "pylon_boundaries"
    override val name: String = "Pylon Boundaries"
    override val description: OptionDescription = OptionDescription.of(Component.literal("Displays the boundary of a pylon using aqua dust particles."))
    override val enabledProperty: KMutableProperty0<Boolean> = Config.values::pylonBoundariesEnabled

    private val pylonExpression = Regex("(?<name>\\w+)\\W's Pylon")
    private val pylonRadius: Double = 15.0

    override fun init() {
        ClientTickEvents.END_CLIENT_TICK.register { client ->
            if(!Config.values.pylonBoundariesEnabled || !isOnIsland() || !isOnFishing()) return@register
            val bossBars = (client.gui.bossOverlay as BossEventAccessor).`gnome$getEvents`()

            val bossBar = bossBars.toList().find { it.second.name.string.contains("pylon", ignoreCase = true) }
            if(bossBar == null) return@register

            val matches = pylonExpression.find(bossBar.second.name.string) ?: return@register
            val playerName = matches.groups["name"]?.value ?: return@register

            val pylonSource = client.level?.players()?.find { it.name.string == playerName } ?: return@register
            repeat(10) {
                val point = getRandomCirclePoint(pylonSource.position())
                client.particleEngine.createParticle(
                    DustParticleOptions(ChatFormatting.AQUA.color!!, Random.nextDouble(1.4, 2.4).toFloat()),
                    point.x, point.y, point.z,
                    0.0, 0.0, 0.0,
                )
            }
        }
    }

    private fun getRandomCirclePoint(center: Vec3): Vec3 {
        val level = Minecraft.getInstance().level!!

        val side = Random.nextDouble(0.0, pylonRadius * 2)
        val edge = Random.nextInt(4)

        var point = when (edge) {
            0 -> Vec3(center.x - pylonRadius + side, center.y, center.z - pylonRadius)
            1 -> Vec3(center.x + pylonRadius, center.y, center.z - pylonRadius + side)
            2 -> Vec3(center.x + pylonRadius - side, center.y, center.z + pylonRadius)
            else -> Vec3(center.x - pylonRadius, center.y, center.z + pylonRadius - side)
        }

        if(!level.getBlockState(BlockPos.containing(point)).isAir) {
            for(i in 0..100) {
                val newPos = point.with(Direction.Axis.Y, point.y + i * 0.25)
                if(level.getBlockState(BlockPos.containing(newPos)).isAir) {
                    point = newPos
                    break
                }
            }
        }

        return point
    }
}