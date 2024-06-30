package dev.sterner

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.mojang.serialization.Dynamic
import com.mojang.serialization.JsonOps
import net.minecraft.entity.EntityType
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtOps
import net.minecraft.recipe.Ingredient
import net.minecraft.registry.Registries
import net.minecraft.util.Identifier
import net.minecraft.util.JsonHelper
import net.minecraft.util.collection.DefaultedList
import java.util.*
import java.util.function.BiConsumer
import java.util.function.BinaryOperator
import java.util.function.Supplier
import java.util.stream.Collector
import java.util.stream.Collectors
import java.util.stream.IntStream
import java.util.stream.Stream
import kotlin.collections.ArrayList


object RecipeUtils {
    fun arrayStream(array: JsonArray): Stream<JsonElement> {
        return IntStream.range(0, array.size()).mapToObj(array::get)
    }

    /**
     * Deserialize a List of entityTypes from a json array
     */
    fun deserializeEntityTypes(array: JsonArray): List<EntityType<*>> {
        return if (array.isJsonArray()) {
            arrayStream(array.getAsJsonArray()).map { entry -> deserializeEntityType(entry.getAsJsonObject()) }.collect(
                DefaultedListCollector.toList()
            )
        } else {
            DefaultedList.copyOf(deserializeEntityType(array.getAsJsonObject()))
        }
    }

    /**
     * Deserialize an EntityType from a json object
     */
    fun deserializeEntityType(`object`: JsonObject?): EntityType<*> {
        val id: Identifier = Identifier(JsonHelper.getString(`object`, "entity"))
        return Registries.ENTITY_TYPE.get(id)
    }

    /**
     * Deserialize a DefaultedList of ItemStacks from a json array
     */
    fun deserializeStacks(array: JsonArray): DefaultedList<ItemStack> {
        return if (array.isJsonArray()) {
            arrayStream(array.getAsJsonArray()).map { entry -> deserializeStack(entry.getAsJsonObject()) }.collect(
                DefaultedListCollector.toList()
            )
        } else {
            DefaultedList.copyOf(deserializeStack(array.getAsJsonObject()))
        }
    }

    /**
     * Deserialize an ItemStack from a json object
     */
    fun deserializeStack(`object`: JsonObject): ItemStack {
        val id: Identifier = Identifier(JsonHelper.getString(`object`, "item"))
        val item: Item = Registries.ITEM.get(id)
        check(Items.AIR !== item) { "Invalid item: $item" }
        var count = 1
        if (`object`.has("count")) {
            count = JsonHelper.getInt(`object`, "count")
        }
        val stack = ItemStack(item, count)
        if (`object`.has("nbt")) {
            val tag = Dynamic.convert(JsonOps.INSTANCE, NbtOps.INSTANCE, `object`.get("nbt")) as NbtCompound
            stack.nbt = tag
        }
        return stack
    }

    /**
     * Deserialize a DefaultedList of Pairs of ItemStacks and Floats from a json array
     */
    fun deserializeStackPairs(array: JsonArray): DefaultedList<com.mojang.datafixers.util.Pair<ItemStack, Float>> {
        return if (array.isJsonArray()) {
            arrayStream(array.getAsJsonArray()).map { entry -> deserializeStackPair(entry.getAsJsonObject()) }.collect(
                DefaultedListCollector.toList()
            )
        } else {
            DefaultedList.copyOf(deserializeStackPair(array.getAsJsonObject()))
        }
    }

    /**
     * Deserialize a Pair of an ItemStack and a Float from a json object
     */
    fun deserializeStackPair(`object`: JsonObject): com.mojang.datafixers.util.Pair<ItemStack, Float> {
        val id: Identifier = Identifier(JsonHelper.getString(`object`, "item"))
        val item: Item = Registries.ITEM.get(id)
        check(Items.AIR !== item) { "Invalid item: $item" }
        var count = 1
        var chance = 1.2f
        if (`object`.has("count")) {
            count = JsonHelper.getInt(`object`, "count")
        }
        if (`object`.has("chance")) {
            chance = JsonHelper.getFloat(`object`, "chance")
        }
        val stack = ItemStack(item, count)
        if (`object`.has("nbt")) {
            val tag = Dynamic.convert(JsonOps.INSTANCE, NbtOps.INSTANCE, `object`.get("nbt")) as NbtCompound
            stack.nbt = tag
        }
        return com.mojang.datafixers.util.Pair.of(stack, chance)
    }

    /**
     * Deserialize a Set of CommandTypes from a json array
     */
    fun deserializeCommands(array: JsonArray): Set<CommandType> {
        if (!array.isEmpty()) {
            return arrayStream(array.getAsJsonArray()).map { entry -> deserializeCommand(entry.getAsJsonObject()) }
                .collect(Collectors.toSet())
        }
        return setOf()
    }

    /**
     * Deserialize a CommandType from a json object
     */
    fun deserializeCommand(`object`: JsonObject?): CommandType {
        return CommandType(JsonHelper.getString(`object`, "command"), JsonHelper.getString(`object`, "type"))
    }

    /**
     * Deserialize a DefaultedList of Ingredients from a json array
     */
    fun deserializeIngredients(json: JsonArray): DefaultedList<Ingredient> {
        val ingredients = DefaultedList.of<Ingredient>()
        for (i in 0 until json.size()) {
            val ingredient = Ingredient.fromJson(json.get(i))
            if (!ingredient.isEmpty) {
                ingredients.add(ingredient)
            }
        }
        return ingredients
    }

    fun containsAllIngredients(ingredients: List<Ingredient>, items: List<ItemStack?>): Boolean {
        val checkedIndexes: MutableList<Int> = ArrayList()
        for (ingredient in ingredients) {
            for (i in items.indices) {
                if (!checkedIndexes.contains(i)) {
                    if (ingredient.test(items[i])) {
                        checkedIndexes.add(i)
                        break
                    }
                }
            }
        }
        return checkedIndexes.size == ingredients.size
    }

    class DefaultedListCollector<T> : Collector<T, DefaultedList<T>, DefaultedList<T>> {
        override fun supplier(): Supplier<DefaultedList<T>> {
            return Supplier { DefaultedList.of<T>() }
        }

        override fun accumulator(): BiConsumer<DefaultedList<T>, T> {
            return BiConsumer { list, item -> list.add(item) }
        }

        override fun combiner(): BinaryOperator<DefaultedList<T>> {
            return BinaryOperator { left: DefaultedList<T>, right: DefaultedList<T>? ->
                left.addAll(right!!)
                left
            }
        }

        override fun finisher(): java.util.function.Function<DefaultedList<T>, DefaultedList<T>> {
            return java.util.function.Function<DefaultedList<T>, DefaultedList<T>> { i -> i }
        }

        override fun characteristics(): Set<Collector.Characteristics> {
            return CH_ID
        }

        companion object {
            private val CH_ID: Set<Collector.Characteristics> =
                Collections.unmodifiableSet(EnumSet.of(Collector.Characteristics.IDENTITY_FINISH))

            fun <T> toList(): DefaultedListCollector<T> {
                return DefaultedListCollector()
            }
        }
    }
}