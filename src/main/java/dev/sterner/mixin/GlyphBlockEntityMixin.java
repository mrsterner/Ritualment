package dev.sterner.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import dev.sterner.ExtendedRitualFunction;
import moriyashiine.bewitchment.api.registry.RitualFunction;
import moriyashiine.bewitchment.common.block.entity.GlyphBlockEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(GlyphBlockEntity.class)
public class GlyphBlockEntityMixin {

    @WrapOperation(method = "tick", at = @At(value = "INVOKE", target = "Lmoriyashiine/bewitchment/api/registry/RitualFunction;start(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/inventory/Inventory;Z)V"))
    private static void ritualment$tick0(RitualFunction instance, ServerWorld world, BlockPos glyphPos, BlockPos effectivePos, Inventory inventory, boolean catFamiliar, Operation<Void> original){
        if (instance instanceof ExtendedRitualFunction extendedRitualFunction) {
            extendedRitualFunction.start(instance, world, glyphPos, effectivePos, catFamiliar);
        } else {
            original.call(instance, world, glyphPos, effectivePos, inventory, catFamiliar);
        }
    }

    @WrapOperation(method = "tick", at = @At(value = "INVOKE", target = "Lmoriyashiine/bewitchment/api/registry/RitualFunction;tick(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/BlockPos;Z)V"))
    private static void ritualment$tick1(RitualFunction instance, World world, BlockPos glyphPos, BlockPos effectivePos, boolean catFamiliar, Operation<Void> original){
        if (instance instanceof ExtendedRitualFunction extendedRitualFunction) {
            extendedRitualFunction.tick(instance, world, glyphPos, effectivePos, catFamiliar);
        } else {
            original.call(instance, world, glyphPos, effectivePos, catFamiliar);
        }
    }

    @WrapOperation(method = "tick", at = @At(value = "FIELD", target = "Lmoriyashiine/bewitchment/common/block/entity/GlyphBlockEntity;effectivePos:Lnet/minecraft/util/math/BlockPos;"))
    private static void ritualment$tick2(RitualFunction instance, World world, BlockPos glyphPos, BlockPos effectivePos, boolean catFamiliar, Operation<Void> original){
        if (instance instanceof ExtendedRitualFunction extendedRitualFunction) {
            extendedRitualFunction.end(instance, world, glyphPos, effectivePos, catFamiliar);
        }
        original.call(instance, world, glyphPos, effectivePos, catFamiliar);
    }
}
