package xyz.devcmb.gnome

import net.minecraft.client.Minecraft
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.FontDescription
import net.minecraft.network.chat.MutableComponent
import net.minecraft.network.chat.Style
import net.minecraft.resources.Identifier
import xyz.devcmb.gnome.mixin.accessor.TabListAccessor
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.builder.RequiredArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import net.fabricmc.fabric.api.client.command.v2.ClientCommands
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.TooltipFlag
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.concurrent.CompletableFuture

// stealing from pe3ep part 1
// https://github.com/pe3ep/Trident/blob/master/src/main/kotlin/cc/pe3epwithyou/trident/state/MCCIState.kt
fun isOnIsland(): Boolean {
    val server = Minecraft.getInstance().currentServer ?: return false
    return server.ip.contains("mccisland.net", true)
}

fun MutableComponent.withFont(identifier: Identifier)
    = this.withStyle(Style.EMPTY.withFont(FontDescription.Resource(identifier)))

fun MutableComponent.withBold(toggle: Boolean)
    = this.withStyle(Style.EMPTY.withBold(toggle))

fun Minecraft.sendMessage(message: Component) = this.gui.chat.addClientSystemMessage(message)

// todo: maybe replace with noxesium instance information
fun isOnFishing(): Boolean {
    val tabList = (Minecraft.getInstance().gui.tabList as TabListAccessor)
    val footer = tabList.`gnome$getFooter`() ?: return false

    return footer.string.contains("fishtance", ignoreCase = true)
}

// stealing from pe3ep part 2
// https://github.com/pe3ep/Trident/blob/master/src/main/kotlin/cc/pe3epwithyou/trident/utils/Command.kt
// true lifesaver for my tired ass

/**
 * Simple DSL to create commands
 *
 * Example usage:
 * ```kt
 * // Simple command without arguments
 * val simpleCommand = Command("simple") {
 *   executes {
 *     // ...
 *   }
 * }
 *
 * // Command with arguments
 * val commandWithArgs = Command("foo") {
 *   argument("bar") {
 *     executes {
 *       val arg = it.getArgument("bar", String::class.java)
 *       // ...
 *     }
 *   }
 * }
 * ```
 */
@Suppress("unused")
class Command(
    name: String, block: Builder.() -> Unit
) {
    private val root: LiteralArgumentBuilder<FabricClientCommandSource> =
        ClientCommands.literal(name)

    init {
        Builder(root).block()
    }

    fun build(): LiteralArgumentBuilder<FabricClientCommandSource> = root

    fun register(dispatcher: CommandDispatcher<FabricClientCommandSource>) {
        dispatcher.register(root)
    }

    class Builder(
        private val node: LiteralArgumentBuilder<FabricClientCommandSource>
    ) {

        /** Context variant: executes { ctx -> ... } */
        fun executes(block: (CommandContext<FabricClientCommandSource>) -> Unit) {
            node.executes { ctx ->
                block(ctx)
                0
            }
        }

        fun literal(
            name: String, block: Builder.() -> Unit
        ) {
            val literal = ClientCommands.literal(name)
            Builder(literal).block()
            node.then(literal)
        }

        fun <T : Any> argument(
            name: String, type: ArgumentType<T>, block: ArgumentBuilder<T>.() -> Unit
        ) {
            val argNode = ClientCommands.argument<T>(name, type)
            ArgumentBuilder(argNode).block()
            node.then(argNode)
        }

        /** Convenience: string argument */
        fun argument(
            name: String, block: ArgumentBuilder<String>.() -> Unit
        ) {
            argument(name, StringArgumentType.string(), block)
        }
    }

    class ArgumentBuilder<T>(
        private val node: RequiredArgumentBuilder<FabricClientCommandSource, T>
    ) {
        fun executes(block: (CommandContext<FabricClientCommandSource>) -> Unit) {
            node.executes { ctx ->
                block(ctx)
                0
            }
        }

        fun suggests(
            provider: (CommandContext<FabricClientCommandSource>, SuggestionsBuilder) -> CompletableFuture<Suggestions>
        ) {
            node.suggests(provider)
        }

        fun literal(
            name: String, block: Builder.() -> Unit
        ) {
            val literal = ClientCommands.literal(name)
            Builder(literal).block()
            node.then(literal)
        }

        fun <U : Any> argument(
            name: String, type: ArgumentType<U>, block: ArgumentBuilder<U>.() -> Unit
        ) {
            val argNode = ClientCommands.argument<U>(name, type)
            ArgumentBuilder(argNode).block()
            node.then(argNode)
        }

        fun argument(
            name: String, block: ArgumentBuilder<String>.() -> Unit
        ) {
            argument(name, StringArgumentType.string(), block)
        }
    }
}

fun Double.round2Places(): String {
    return BigDecimal(this.toString())
        .setScale(2, RoundingMode.HALF_UP)
        .stripTrailingZeros()
        .toPlainString()
}

// stealing from pe3ep part 3
// https://github.com/pe3ep/Trident/blob/master/src/main/kotlin/cc/pe3epwithyou/trident/utils/extensions/ItemStackExtensions.kt

fun ItemStack.getLore(): List<Component> {
    val player = Minecraft.getInstance().player ?: return emptyList()

    return this.getTooltipLines(
        Item.TooltipContext.EMPTY,
        player,
        TooltipFlag.Default.NORMAL
    )
}