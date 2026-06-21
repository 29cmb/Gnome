package xyz.devcmb.gnome.data

import net.minecraft.resources.Identifier
import xyz.devcmb.gnome.config.Config

enum class PearlType(val match: String, val resource: Identifier, val pristinePart: Double) {
    ROUGH(
        "Rough",
        Identifier.fromNamespaceAndPath("mcc", "island_items/infinibag/material/pearl_rough"),
        0.01
    ),

    POLISHED(
        "Polished",
        Identifier.fromNamespaceAndPath("mcc", "island_items/infinibag/material/pearl_polished"),
        0.1
    ),

    PRISTINE("Pristine",
        Identifier.fromNamespaceAndPath("mcc", "island_items/infinibag/material/pearl_pristine"),
        1.0
    );

    fun calculate(amount: Int): Double {
        return Config.values.sessionStatsPearlTrackingMode.calculate(this, amount)
    }
}