package xyz.devcmb.gnome.data

import net.minecraft.ChatFormatting

enum class PerkType(val color: Int) {
    STRONG(ChatFormatting.RED.color!!),
    WISE(0x219BF3),
    GLIMMERING(0x8833FF),
    GREEDY(0xFF7E40),
    LUCKY(0x23C725)
}