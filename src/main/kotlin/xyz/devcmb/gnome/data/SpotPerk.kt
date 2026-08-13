package xyz.devcmb.gnome.data

enum class SpotPerk(
    val displayText: String,
    val icon: String,
    val type: PerkType,
    val numerical: Boolean,
    val copyFormat: (value: Int) -> String
) {
    // Hook
    STRONG_HOOK("Strong Hook", "_fonts/icon/fishing_perk/strong_hook.png", PerkType.STRONG, false, { "${it}str" }),
    WISE_HOOK("Wise Hook", "_fonts/icon/fishing_perk/wise_hook.png", PerkType.WISE, false, { "${it}wis" }),
    GLIMMERING_HOOK("Glimmering Hook", "_fonts/icon/fishing_perk/glimmering_hook.png", PerkType.GLIMMERING, false, { "${it}glim" }),
    GREEDY_HOOK("Greedy Hook", "_fonts/icon/fishing_perk/greedy_hook.png", PerkType.GREEDY, false, { "${it}greedy" }),
    LUCKY_HOOK("Lucky Hook", "_fonts/icon/fishing_perk/lucky_hook.png", PerkType.LUCKY, false, { "${it}lucky" }),

    // Magnet
    XP_MAGNET("XP Magnet", "_fonts/icon/fishing_perk/xp_magnet.png", PerkType.STRONG, false, { "${it}xp" }),
    FISH_MAGNET("Fish Magnet", "_fonts/icon/fishing_perk/fish_magnet.png", PerkType.WISE, false, { "${it}fm" }),
    PEARL_MAGNET("Pearl Magnet", "_fonts/icon/fishing_perk/pearl_magnet.png", PerkType.GLIMMERING, false, { "${it}pm" }),
    TREASURE_MAGNET("Treasure Magnet", "_fonts/icon/fishing_perk/treasure_magnet.png", PerkType.GREEDY, false, { "${it}tm" }),
    SPIRIT_MAGNET("Spirit Magnet", "_fonts/icon/fishing_perk/spirit_magnet.png", PerkType.LUCKY, false, { "${it}lm" }),

    // ANGLR
    ELUSIVE_CHANCE("Elusive Fish Chance", "_fonts/icon/fishing_perk/anglr_lure_strong.png", PerkType.STRONG, false, { "elu" }),
    WAYFINDER_DATA("Wayfinder Data", "_fonts/icon/fishing_perk/anglr_lure_wise.png", PerkType.WISE, true, { "wfd" }),
    PEARL_CHANCE("Pearl Chance", "_fonts/icon/fishing_perk/anglr_lure_glimmering.png", PerkType.GLIMMERING, false, { "pearl chance" }),
    TREASURE_CHANCE("Treasure Chance", "_fonts/icon/fishing_perk/anglr_lure_greedy.png", PerkType.GREEDY, false, { "treasure chance" }),
    SPIRIT_CHANCE("Spirit Chance", "_fonts/icon/fishing_perk/anglr_lure_lucky.png", PerkType.LUCKY, false, { "spirit chance" }),
}