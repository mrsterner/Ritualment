package dev.sterner

import moriyashiine.bewitchment.common.recipe.RitualRecipe
import net.fabricmc.api.ModInitializer
import net.minecraft.util.Identifier
import org.slf4j.LoggerFactory

object Ritualment : ModInitializer {
	val modid = "ritualment"
    private val logger = LoggerFactory.getLogger(modid)



	override fun onInitialize() {
		RitualmentRecipes.init()
	}

	fun id(s: String): Identifier {
		return Identifier(modid, s)
	}
}