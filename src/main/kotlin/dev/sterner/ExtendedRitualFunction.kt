package dev.sterner

import moriyashiine.bewitchment.api.registry.RitualFunction
import net.minecraft.entity.LivingEntity
import net.minecraft.inventory.Inventory
import net.minecraft.particle.ParticleType
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import java.util.function.Predicate

class ExtendedRitualFunction(startParticle: ParticleType<*>?,
                             sacrifice: Predicate<LivingEntity>?
) : RitualFunction(startParticle, sacrifice) {

    fun end(instance: RitualFunction, world: World, glyphPos: BlockPos) {

    }

    fun tick(instance: RitualFunction, world: World, glyphPos: BlockPos, effectivePos: BlockPos, catFamiliar: Boolean) {

    }

    fun start(instance: RitualFunction, world: ServerWorld, glyphPos: BlockPos, effectivePos: BlockPos, inventory: Inventory, catFamiliar: Boolean) {

    }

    override fun getInvalidMessage(): String {
        return super.getInvalidMessage()
    }

    override fun isValid(world: ServerWorld?, pos: BlockPos?, inventory: Inventory?): Boolean {
        return super.isValid(world, pos, inventory)
    }


    override fun start(
        world: ServerWorld?,
        glyphPos: BlockPos?,
        effectivePos: BlockPos?,
        inventory: Inventory?,
        catFamiliar: Boolean
    ) {
        //NO-OP
    }

    override fun tick(world: World?, glyphPos: BlockPos?, effectivePos: BlockPos?, catFamiliar: Boolean) {
        //NO-OP
    }
}