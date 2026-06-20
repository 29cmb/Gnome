package xyz.devcmb.gnome.lib

import com.terraformersmc.modmenu.api.ConfigScreenFactory
import com.terraformersmc.modmenu.api.ModMenuApi
import net.minecraft.client.gui.screens.Screen
import xyz.devcmb.gnome.config.Config

class GnomeModMenuImpl : ModMenuApi {
    override fun getModConfigScreenFactory(): ConfigScreenFactory<Screen> {
        return ConfigScreenFactory(Config.Companion::getScreen)
    }
}