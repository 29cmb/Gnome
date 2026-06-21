package xyz.devcmb.gnome.data

import xyz.devcmb.gnome.config.Config

enum class Island(val islandName: String) {
    VERDANT_WOODS("Verdant Woods"),
    FLORAL_FOREST("Floral Forest"),
    DARK_GROVE("Dark Grove"),
    TROPICAL_OVERGROWTH("Tropical Overgrowth"),
    CORAL_SHORES("Coral Shores"),
    TWISTED_SWAMP("Twisted Swamp"),
    ANCIENT_SANDS("Ancient Sands"),
    BLAZING_CANYON("Blazing Canyon"),
    ASHEN_WASTES("Ashen Wastes"),

    SUNKEN_SWAMP("Sunken Swamp"),
    MIRRORED_OASIS("Mirrored Oasis"),
    VOLCANIC_SPRINGS("Volcanic Springs");

    fun getCompletionData(): Config.CompletionData {
        return Config.state.islandProgress[this]!!
    }

    fun updateCompletionData(data: Config.CompletionData) {
        Config.state.islandProgress[this] = data
    }

    fun discoverNewFish(weight: Weight) {
        val currentData = getCompletionData()

        when(weight) {
            Weight.AVERAGE -> currentData.average--
            Weight.LARGE -> currentData.large--
            Weight.MASSIVE -> currentData.massive--
            Weight.GARGANTUAN -> currentData.gargantuan--
        }

        Config.handler.save()
    }

    companion object {
        var currentIsland: Island? = null

        fun updateCurrentIsland(scoreboardTitle: String) {
            currentIsland = entries.find { it.islandName.uppercase() in scoreboardTitle }
        }
    }
}