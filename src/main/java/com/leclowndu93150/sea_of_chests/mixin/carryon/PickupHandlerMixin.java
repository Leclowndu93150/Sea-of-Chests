package com.leclowndu93150.sea_of_chests.mixin.carryon;

import com.leclowndu93150.sea_of_chests.integration.carryon.CarryOnIntegration;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import tschipp.carryon.common.carry.PickupHandler;

import java.util.function.BiFunction;
import javax.annotation.Nullable;

@Mixin(value = PickupHandler.class, remap = false)
public class PickupHandlerMixin {
    
    @Inject(method = "tryPickUpBlock", 
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;removeBlockEntity(Lnet/minecraft/core/BlockPos;)V"))
    private static void seaOfChests_onBlockPickup(ServerPlayer player, BlockPos pos, Level level, @Nullable BiFunction<BlockState, BlockPos, Boolean> pickupCallback, CallbackInfoReturnable<Boolean> cir) {
        BlockState state = level.getBlockState(pos);
        CarryOnIntegration.onChestPickup(player, pos, level, state);
    }
}