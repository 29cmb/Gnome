package xyz.devcmb.gnome.data

import net.minecraft.network.chat.Component
import xyz.devcmb.gnome.util.Font

enum class Weight(val glyph: String) {
    AVERAGE("_fonts/icon/fishing/average_bubble.png"),
    LARGE("_fonts/icon/fishing/large_bubble.png"),
    MASSIVE("_fonts/icon/fishing/massive_bubble.png"),
    GARGANTUAN("_fonts/icon/fishing/gargantuan_bubble.png");

    fun glyph(): String {
        return Font.getGlyphString(glyph)
    }

    fun icon(): Component {
        return Font.getGlyph(glyph)
    }

    fun getCompletion(island: Island): Int {
        val completion = island.getCompletionData()
        return when(this) {
            AVERAGE -> completion.average
            LARGE -> completion.large
            MASSIVE -> completion.massive
            GARGANTUAN -> completion.gargantuan
        }
    }
}