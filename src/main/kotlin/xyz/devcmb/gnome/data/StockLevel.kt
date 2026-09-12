package xyz.devcmb.gnome.data

import net.minecraft.network.chat.TextColor

enum class StockLevel(val displayText: String, val color: Int, val formattedString: String) {
    LOW("Low", 0xF57600, "low"),
    MEDIUM("Medium", TextColor.YELLOW.value, "medium"),
    HIGH("High", 0x55FF56, "high"),
    VERY_HIGH("Very High", 0x65FFFF, "vh"),
    PLENTIFUL("Plentiful", 0xAB6FFF, "plenti")
}