package xyz.devcmb.gnome.util

import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent
import net.minecraft.resources.Identifier

object Font {
    val glyphs: HashMap<String, String> = HashMap()

    fun registerGlyph(path: String, char: String) {
        glyphs[path] = char
    }

    fun getGlyph(path: String): MutableComponent {
        return Component.literal(glyphs[path] ?: "???")
            .withFont(Identifier.fromNamespaceAndPath("mcc", "icon"))
    }

    fun getGlyphString(path: String): String {
        return glyphs[path] ?: "???"
    }
}