package xyz.devcmb.gnome.data

import net.minecraft.resources.Identifier

enum class SpiritTier(val match: String, val resource: Identifier) {
    NORMAL("", Identifier.fromNamespaceAndPath("mcc", "island_items/infinibag/material/spirit_strong")),
    REFINED("Refined", Identifier.fromNamespaceAndPath("mcc", "island_items/infinibag/material/spirit_refined_strong")),
    PURE("Pure", Identifier.fromNamespaceAndPath("mcc", "island_items/infinibag/material/spirit_pure_strong")),
}