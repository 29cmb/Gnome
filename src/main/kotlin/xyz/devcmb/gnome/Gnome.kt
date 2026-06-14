package xyz.devcmb.gnome

import net.fabricmc.api.ModInitializer
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import xyz.devcmb.gnome.feature.DayNightDetection
import xyz.devcmb.gnome.feature.GnomeFeature
import xyz.devcmb.gnome.feature.LimboKickWarning
import xyz.devcmb.gnome.feature.SessionStats

object Gnome : ModInitializer {
	const val MOD_ID: String = "gnome"
	val logger: Logger = LoggerFactory.getLogger(MOD_ID)
	val features: ArrayList<GnomeFeature> = ArrayList()

	override fun onInitialize() {
		registerFeature(DayNightDetection())
		registerFeature(LimboKickWarning())
		registerFeature(SessionStats())
	}

	fun registerFeature(feature: GnomeFeature) {
		feature.init()
		features.add(feature)
		logger.info("Registered feature ${feature::class.simpleName}")
	}
}