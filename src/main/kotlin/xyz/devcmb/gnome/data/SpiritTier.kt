package xyz.devcmb.gnome.data

import net.minecraft.resources.Identifier

enum class SpiritTier(val match: String, val resource: Identifier) {
    // Normal doesn't have a prefix, so it should only be used when the other 2 fail to match
    NORMAL("According to all known laws of aviation", Identifier.fromNamespaceAndPath("mcc", "island_items/infinibag/material/spirit_strong")),
    REFINED("Refined", Identifier.fromNamespaceAndPath("mcc", "island_items/infinibag/material/spirit_refined_strong")),
    PURE("Pure", Identifier.fromNamespaceAndPath("mcc", "island_items/infinibag/material/spirit_pure_strong")),
}