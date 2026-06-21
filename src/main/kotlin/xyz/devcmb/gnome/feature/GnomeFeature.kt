package xyz.devcmb.gnome.feature

import dev.isxander.yacl3.api.OptionDescription
import kotlin.reflect.KMutableProperty0

interface GnomeFeature {
    val id: String
    val name: String
    val description: OptionDescription
    val enabledProperty: KMutableProperty0<Boolean>

    fun init()
}