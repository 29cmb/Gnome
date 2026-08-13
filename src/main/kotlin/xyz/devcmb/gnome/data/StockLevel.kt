package xyz.devcmb.gnome.data

import net.minecraft.ChatFormatting

enum class StockLevel(val displayText: String, val color: Int, val formattedString: String) {
    LOW("Low", 0xF57600, "low"),
    MEDIUM("Medium", ChatFormatting.YELLOW.color!!, "medium"),
    HIGH("High", 0x55FF56, "high"),
    VERY_HIGH("Very High", 0x65FFFF, "vh"),
    PLENTIFUL("Plentiful", 0xAB6FFF, "plenti")
}