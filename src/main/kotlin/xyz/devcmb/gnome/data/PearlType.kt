package xyz.devcmb.gnome.data

import xyz.devcmb.gnome.config.Config

enum class PearlType(val match: String, val pristinePart: Double) {
    ROUGH("Rough", 0.01),
    POLISHED("Polished", 0.1),
    PRISTINE("Pristine", 1.0);

    fun calculate(amount: Int): Double {
        return Config.values.sessionStatsPearlTrackingMode.calculate(this, amount)
    }
}