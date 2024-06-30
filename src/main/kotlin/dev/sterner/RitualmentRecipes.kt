package dev.sterner

import net.minecraft.recipe.RecipeSerializer
import net.minecraft.recipe.RecipeType
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry


object RitualmentRecipes {

    var EXTENDED_RITUAL_RECIPE_SERIALIZER: RecipeSerializer<ExtendedRitualRecipe> = ExtendedRitualRecipe.Serializer()
    var EXTENDED_RITUAL_RECIPE_TYPE: RecipeType<ExtendedRitualRecipe> = object : RecipeType<ExtendedRitualRecipe> {
        override fun toString(): String {
            return Ritualment.modid + ":extended_ritual"
        }
    }

    fun init() {
        Registry.register(Registries.RECIPE_SERIALIZER, Ritualment.id("extended_ritual"), EXTENDED_RITUAL_RECIPE_SERIALIZER);
        Registry.register(Registries.RECIPE_TYPE, Ritualment.id("extended_ritual"), EXTENDED_RITUAL_RECIPE_TYPE);
    }
}