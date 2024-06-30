package dev.sterner

import moriyashiine.bewitchment.api.registry.RitualFunction
import moriyashiine.bewitchment.common.Bewitchment
import moriyashiine.bewitchment.common.registry.BWRegistries
import moriyashiine.bewitchment.common.ritualfunction.TurnToDayRitualFunction
import net.fabricmc.api.ModInitializer
import net.minecraft.particle.ParticleTypes
import net.minecraft.registry.Registry
import net.minecraft.util.Identifier
import org.slf4j.LoggerFactory


object Ritualment : ModInitializer {
	val modid = "ritualment"
    private val logger = LoggerFactory.getLogger(modid)


	val EXTENDED_RITUAL: RitualFunction = ExtendedRitualFunction(null, null)

	override fun onInitialize() {
		Bewitchment()
		Registry.register(BWRegistries.RITUAL_FUNCTION, "extended_ritual", EXTENDED_RITUAL)
		RitualmentRecipes.init()
	}

	fun id(s: String): Identifier {
		return Identifier(modid, s)
	}
}