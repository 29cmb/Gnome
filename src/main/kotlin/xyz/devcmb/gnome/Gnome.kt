package xyz.devcmb.gnome

import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import xyz.devcmb.gnome.config.Config
import xyz.devcmb.gnome.feature.DayNightDetection
import xyz.devcmb.gnome.feature.GnomeFeature
import xyz.devcmb.gnome.feature.LimboKickWarning
import xyz.devcmb.gnome.feature.SessionStats

object Gnome : ModInitializer {
	const val MOD_ID: String = "gnome"
	val logger: Logger = LoggerFactory.getLogger(MOD_ID)
	val features: ArrayList<GnomeFeature> = ArrayList()

	override fun onInitialize() {
		Config.handler.load()

		registerFeature(DayNightDetection())
		registerFeature(LimboKickWarning())
		registerFeature(SessionStats())

		ClientCommandRegistrationCallback.EVENT.register { dispatcher, _ ->
			GnomeCommand.register(dispatcher)
		}
	}

	@Suppress("UNCHECKED_CAST")
	fun <T: GnomeFeature> getFeature(clazz: Class<T>): T {
		return features.find { clazz.isInstance(it) } as? T
			?: throw IllegalArgumentException("Feature ${clazz.simpleName} is not currently registered!")
	}

	inline fun <reified T : GnomeFeature> getFeature(): T = getFeature(T::class.java)

	fun registerFeature(feature: GnomeFeature) {
		feature.init()
		features.add(feature)
		logger.info("Registered feature ${feature::class.simpleName}")
	}
}