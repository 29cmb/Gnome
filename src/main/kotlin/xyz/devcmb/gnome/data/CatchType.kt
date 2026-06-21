package xyz.devcmb.gnome.data

enum class CatchType(val regex: () -> Regex) {
    JUNK(Regex("\\[(?<type>Rusted Can|Tangled Kelp|Lost Shoe|Royal Residue|Forgotten Crown)](?: x(?<amount>[0-9]*))?")),
    FISH({
        Regex("\\[(?<fish>.*?) (?<weight>[${Weight.entries.joinToString("") { it.glyph() }}])+](?: x(?<amount>[0-9]+))?")
    }),
    PEARL(Regex("\\[(?<type>.*) Pearl](?: x(?<amount>[0-9]+))?")),
    TREASURE(Regex("\\[(?<tier>.*) Treasure](?: x(?<amount>[0-9]+))?")),
    SPIRIT(Regex("\\[(?<tier>Refined|Pure)? *(?<type>.*) Spirit](?: x(?<amount>[0-9]+))?"));

    constructor(regex: Regex) : this({ regex })

    fun regex(): Regex {
        return regex.invoke()
    }
}