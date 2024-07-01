package dev.sterner.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import dev.sterner.ExtendedRitualFunction;
import dev.sterner.ExtendedRitualRecipe;
import moriyashiine.bewitchment.api.BewitchmentAPI;
import moriyashiine.bewitchment.api.registry.RitualFunction;
import moriyashiine.bewitchment.client.packet.SpawnSmokeParticlesPacket;
import moriyashiine.bewitchment.common.Bewitchment;
import moriyashiine.bewitchment.common.block.entity.GlyphBlockEntity;
import moriyashiine.bewitchment.common.block.entity.WitchAltarBlockEntity;
import moriyashiine.bewitchment.common.recipe.RitualRecipe;
import moriyashiine.bewitchment.common.registry.BWRecipeTypes;
import moriyashiine.bewitchment.common.registry.BWSoundEvents;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.minecraft.block.BlockState;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.RecipeType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@Mixin(value = GlyphBlockEntity.class)
public abstract class GlyphBlockEntityMixin {

    @Shadow public RitualFunction ritualFunction;

    @Shadow protected abstract boolean hasValidChalk(RitualRecipe recipe);

    @Shadow public abstract void setStack(int slot, ItemStack stack);

    @Shadow private BlockPos effectivePos;

    @Shadow private int timer;

    @Shadow private int endTime;

    @Shadow private boolean catFamiliar;

    @Shadow public abstract void syncGlyph();

    @Shadow private BlockPos altarPos;

    @Shadow public abstract int size();

    @WrapOperation(method = "tick", at = @At(value = "INVOKE", target = "Lmoriyashiine/bewitchment/api/registry/RitualFunction;start(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/inventory/Inventory;Z)V"))
    private static void ritualment$tick0(RitualFunction instance, ServerWorld world, BlockPos glyphPos, BlockPos effectivePos, Inventory inventory, boolean catFamiliar, Operation<Void> original){
        if (instance instanceof ExtendedRitualFunction extendedRitualFunction) {
            extendedRitualFunction.start(instance, world, glyphPos, effectivePos, inventory, catFamiliar);
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

    @Inject(method = "tick", at = @At(value = "FIELD", target = "Lmoriyashiine/bewitchment/common/block/entity/GlyphBlockEntity;effectivePos:Lnet/minecraft/util/math/BlockPos;"))
    private static void ritualment$tick2(World world, BlockPos glyphPos, BlockState state, GlyphBlockEntity blockEntity, CallbackInfo ci){
        if (blockEntity.ritualFunction instanceof ExtendedRitualFunction extendedRitualFunction) {
            extendedRitualFunction.end(blockEntity.ritualFunction, world, glyphPos);
        }
    }

    @WrapOperation(method = "onUse", at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/world/World;getRecipeManager()Lnet/minecraft/recipe/RecipeManager;"))
    private RecipeType<RitualRecipe> d(Operation<RecipeType<RitualRecipe>> original, @Local World world, @Local BlockPos pos){
        var v = original.call();
        SimpleInventory test = new SimpleInventory(size());
        List<ItemEntity> items = world.getEntitiesByType(EntityType.ITEM, new Box(pos).expand(2, 0, 2), entity -> true);
        for (ItemEntity entity : items) {
            test.addStack(entity.getStack().copy().split(1));
        }
        List<RitualRecipe> recipe = world.getRecipeManager().listAllOfType(BWRecipeTypes.RITUAL_RECIPE_TYPE).stream().toList();
        for (RitualRecipe ritualRecipe : recipe) {
            System.out.println(ritualRecipe.getId());
        }
        return original.call();
    }

    @WrapOperation(method = "onUse", at = @At(value = "INVOKE", target = "Lmoriyashiine/bewitchment/common/block/entity/GlyphBlockEntity;hasValidChalk(Lmoriyashiine/bewitchment/common/recipe/RitualRecipe;)Z"))
    private boolean ritualment$grabRecipes(GlyphBlockEntity instance, RitualRecipe ritualRecipe, Operation<Boolean> original, @Local(ordinal = 0) LivingEntity user, @Local World world, @Local BlockPos pos, @Local SimpleInventory test, @Local List<ItemEntity> items){

        if (ritualRecipe instanceof ExtendedRitualRecipe extendedRitualRecipe) {
            if (extendedRitualRecipe.input.size() == items.size() && hasValidChalk(extendedRitualRecipe)) {
                if (extendedRitualRecipe.ritualFunction.isValid((ServerWorld) world, pos, test)) {
                    boolean cat = user instanceof PlayerEntity player && BewitchmentAPI.getFamiliar(player) == EntityType.CAT;
                    if (altarPos != null && ((WitchAltarBlockEntity) world.getBlockEntity(altarPos)).drain((int) (extendedRitualRecipe.cost * (cat ? 0.75f : 1)), false)) {
                        world.playSound(null, pos, BWSoundEvents.BLOCK_GLYPH_FIRE, SoundCategory.BLOCKS, 1, 1);
                        if (user instanceof PlayerEntity player) {
                            player.sendMessage(Text.translatable("ritual." + extendedRitualRecipe.getId().toString().replace(":", ".").replace("/", ".")), true);
                        }
                        for (int i = 0; i < items.size(); i++) {
                            ItemEntity item = items.get(i);
                            PlayerLookup.tracking(item).forEach(foundPlayer -> SpawnSmokeParticlesPacket.send(foundPlayer, item));
                            setStack(i, item.getStack().split(1));
                        }
                        effectivePos = pos;
                        ritualFunction = extendedRitualRecipe.ritualFunction;
                        timer = -100;
                        endTime = extendedRitualRecipe.runningTime;
                        catFamiliar = cat;
                        syncGlyph();
                        return false;
                    }
                    world.playSound(null, pos, BWSoundEvents.BLOCK_GLYPH_FAIL, SoundCategory.BLOCKS, 1, 1);
                    if (user instanceof PlayerEntity player) {
                        player.sendMessage(Text.translatable(Bewitchment.MOD_ID + ".message.insufficent_altar_power"), true);
                    }
                    return false;
                }
                world.playSound(null, pos, BWSoundEvents.BLOCK_GLYPH_FAIL, SoundCategory.BLOCKS, 1, 1);
                if (user instanceof PlayerEntity player) {
                    player.sendMessage(Text.translatable(extendedRitualRecipe.ritualFunction.getInvalidMessage()), true);
                }
                return false;
            }
        }
        return false;//!(ritualRecipe instanceof ExtendedRitualRecipe);
    }

    @Inject(method = "onUse", at = @At(value = "FIELD", ordinal = 2, target = "Lmoriyashiine/bewitchment/common/block/entity/GlyphBlockEntity;ritualFunction:Lmoriyashiine/bewitchment/api/registry/RitualFunction;"))
    private void ritualment$grabRecipe(World world, BlockPos pos, LivingEntity user, Hand hand, LivingEntity sacrifice, CallbackInfo ci, @Local RitualRecipe ritualRecipe){
        if (ritualRecipe instanceof ExtendedRitualRecipe extendedRitualRecipe) {

        }
    }
}
