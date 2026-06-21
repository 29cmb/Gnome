package xyz.devcmb.gnome.data

import net.minecraft.resources.Identifier

enum class TreasureTier(val match: String, val resource: Identifier) {
    COMMON(
        "Common",
        Identifier.fromNamespaceAndPath("mcc", "island_items/infinibag/openable/anglr_treasure_common")
    ),
    UNCOMMON(
        "Uncommon",
        Identifier.fromNamespaceAndPath("mcc", "island_items/infinibag/openable/anglr_treasure_uncommon")
    ),
    RARE(
        "Rare",
        Identifier.fromNamespaceAndPath("mcc", "island_items/infinibag/openable/anglr_treasure_rare")
    ),
    EPIC(
        "Epic",
        Identifier.fromNamespaceAndPath("mcc", "island_items/infinibag/openable/anglr_treasure_epic")
    ),
    LEGENDARY(
        "Legendary",
        Identifier.fromNamespaceAndPath("mcc", "island_items/infinibag/openable/anglr_treasure_legendary")
    ),
    MYTHIC(
        "Mythic",
        Identifier.fromNamespaceAndPath("mcc", "island_items/infinibag/openable/anglr_treasure_mythic")
    )
}