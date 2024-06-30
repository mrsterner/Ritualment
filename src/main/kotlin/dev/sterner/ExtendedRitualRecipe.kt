package dev.sterner

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParseException
import dev.sterner.RecipeUtils.deserializeEntityTypes
import moriyashiine.bewitchment.api.registry.RitualFunction
import moriyashiine.bewitchment.common.recipe.RitualRecipe
import moriyashiine.bewitchment.common.registry.BWRegistries
import net.minecraft.entity.EntityType
import net.minecraft.inventory.Inventory
import net.minecraft.item.ItemStack
import net.minecraft.network.PacketByteBuf
import net.minecraft.recipe.Ingredient
import net.minecraft.recipe.RecipeSerializer
import net.minecraft.recipe.RecipeType
import net.minecraft.registry.Registries
import net.minecraft.util.Identifier
import net.minecraft.util.JsonHelper
import net.minecraft.util.collection.DefaultedList
import net.minecraft.world.World
import java.util.stream.Collectors
import java.util.stream.IntStream


class ExtendedRitualRecipe(
    identifier: Identifier,
    inner: String,
    outer: String,
    private val inputs: DefaultedList<Ingredient>,
    private val outputs: List<ItemStack>,
    private val sacrifices: List<EntityType<*>>,
    private val summons: List<EntityType<*>>,
    ritualFunction: RitualFunction,
    cost: Int,
    runningTime: Int,
    private val command: Set<CommandType>

) : RitualRecipe(identifier,
    inputs, inner,
    outer, ritualFunction, cost, runningTime
) {

    override fun getId(): Identifier {
        return id
    }

    override fun getSerializer(): RecipeSerializer<*> {
        return RitualmentRecipes.EXTENDED_RITUAL_RECIPE_SERIALIZER
    }

    override fun getType(): RecipeType<*> {
        return RitualmentRecipes.EXTENDED_RITUAL_RECIPE_TYPE
    }

    override fun matches(inventory: Inventory, world: World?): Boolean {
        return matches(inventory, inputs)
    }

    fun matches(inv: Inventory, input: DefaultedList<Ingredient>): Boolean {
        val checklist: MutableList<ItemStack> = ArrayList()
        for (i in 0 until inv.size()) {
            val stack = inv.getStack(i)
            if (!stack.isEmpty) {
                checklist.add(stack)
            }
        }
        if (input.size != checklist.size) {
            return false
        }
        for (ingredient in input) {
            var found = false
            for (stack in checklist) {
                if (ingredient.test(stack)) {
                    found = true
                    checklist.remove(stack)
                    break
                }
            }
            if (!found) {
                return false
            }
        }
        return true
    }

    override fun fits(width: Int, height: Int): Boolean {
        return true
    }

    class Serializer : RecipeSerializer<ExtendedRitualRecipe> {
        override fun read(id: Identifier?, json: JsonObject?): ExtendedRitualRecipe {
            val ritual = BWRegistries.RITUAL_FUNCTION.get(Identifier(JsonHelper.getString(json, "ritual_function")))

            //Inputs
            var inputs = DefaultedList.of<Ingredient?>()
            if (JsonHelper.hasArray(json, "inputs")) {
                inputs = RecipeUtils.deserializeIngredients(JsonHelper.getArray(json, "inputs"))
            }


            //Outputs
            var outputs = DefaultedList.of<ItemStack?>()
            if (JsonHelper.hasArray(json, "outputs")) {
                outputs = RecipeUtils.deserializeStacks(JsonHelper.getArray(json, "outputs"))
            }


            //Sacrifices
            var sacrifices: List<EntityType<*>> = listOf<EntityType<*>>()
            if (JsonHelper.hasArray(json, "summons")) {
                val sacrificeArray = JsonHelper.getArray(json, "sacrifices")
                sacrifices = deserializeEntityTypes(sacrificeArray)
            }


            //Summons
            var summons: List<EntityType<*>> = listOf<EntityType<*>>()
            if (JsonHelper.hasArray(json, "summons")) {
                val summonArray = JsonHelper.getArray(json, "summons")
                summons = deserializeEntityTypes(summonArray)
            }

            //Duration
            val duration = JsonHelper.getInt(json, "duration", 20 * 8)

            //Command
            var commands: Set<CommandType> = setOf()
            if (JsonHelper.hasArray(json, "commands")) {
                val commandArray: JsonArray = JsonHelper.getArray(json, "commands")
                commands = RecipeUtils.deserializeCommands(commandArray)
            }

            val inner = JsonHelper.getString(json, "inner")
            if (inner.isEmpty()) {
                throw JsonParseException("Inner circle is empty")
            }
            val outer = JsonHelper.getString(json, "outer", "")

            return ExtendedRitualRecipe(id!!, inner, outer!!, inputs, outputs, sacrifices, summons, ritual!!, JsonHelper.getInt(json, "running_time", 0), JsonHelper.getInt(json, "cost"), commands)
        }

        override fun read(id: Identifier, buf: PacketByteBuf): ExtendedRitualRecipe {

            //Inner
            val inner = buf.readString()

            //Outer
            val outer = buf.readString()

            //Ritual
            val rite = BWRegistries.RITUAL_FUNCTION.get(buf.readIdentifier())

            //Inputs
            val inputs = DefaultedList.ofSize(buf.readVarInt(), Ingredient.EMPTY)
            inputs.replaceAll { ignored: Ingredient? -> Ingredient.fromPacket(buf) }

            //Outputs
            val outputs = DefaultedList.ofSize(buf.readVarInt(), ItemStack.EMPTY)
            outputs.replaceAll { ignored: ItemStack? -> buf.readItemStack() }


            //Sacrifices
            val sacrificeSize = buf.readInt()
            val sacrificeList = IntStream.range(0, sacrificeSize).mapToObj { i: Int ->
                Registries.ENTITY_TYPE[Identifier(
                    buf.readString()
                )]
            }.collect(Collectors.toList())


            //Summons
            val summonsSize = buf.readInt()
            val summons = IntStream.range(0, summonsSize).mapToObj { i: Int ->
                Registries.ENTITY_TYPE[Identifier(buf.readString())]
            }.collect(Collectors.toList())


            //RunningTime
            val runningTime = buf.readVarInt()

            //Cost
            val cost = buf.readVarInt()

            //Commands
            val commandTypeSet: MutableSet<CommandType> = HashSet()
            for (i in 0 until buf.readInt()) {
                commandTypeSet.add(CommandType(buf.readString(), buf.readString()))
            }

            return ExtendedRitualRecipe(id, inner, outer, inputs, outputs, sacrificeList, summons, rite!!, runningTime, cost, commandTypeSet)
        }

        override fun write(buf: PacketByteBuf?, recipe: ExtendedRitualRecipe?) {
            buf!!.writeString(recipe!!.inner)
            buf.writeString(recipe.outer)

            //Inputs
            buf.writeVarInt(recipe.inputs.size)
            recipe.inputs.forEach { ingredient -> ingredient.write(buf) }

            //Outputs
            buf.writeVarInt(recipe.outputs.size)
            recipe.outputs.forEach(buf::writeItemStack)


            //Sacrifices
            buf.writeInt(recipe.sacrifices.size)
            recipe.sacrifices.stream().map { entityType ->
                Registries.ENTITY_TYPE.getId(
                    entityType
                ).toString()
            }.forEach(buf::writeString)


            //Summons
            buf.writeInt(recipe.summons.size)
            recipe.summons.stream().map { entityType ->
                Registries.ENTITY_TYPE.getId(entityType).toString()
            }.forEach(buf::writeString)


            buf.writeString(BWRegistries.RITUAL_FUNCTION.getId(recipe.ritualFunction).toString())

            buf.writeInt(recipe.runningTime)
            buf.writeInt(recipe.cost)

            //Commands
            buf.writeVarInt(recipe.command.size)
            recipe.command.forEach { commandType ->
                buf.writeString(commandType.command)
                buf.writeString(commandType.type)
            }
        }

    }
}