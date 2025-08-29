package com.leclowndu93150.sea_of_chests.mixin.carryon;

import com.leclowndu93150.sea_of_chests.integration.carryon.CarryOnIntegration;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import tschipp.carryon.common.carry.CarryOnDataManager;
import tschipp.carryon.common.carry.PlacementHandler;

import java.util.function.BiFunction;

@Mixin(value = PlacementHandler.class, remap = false)
public class PlacementHandlerMixin {
    
    @Inject(method = "tryPlaceBlock",
            at = @At(value = "INVOKE", target = "Ltschipp/carryon/common/carry/CarryOnData;clear()V", shift = At.Shift.BEFORE))
    private static void seaOfChests_onBlockPlacement(ServerPlayer player, BlockPos pos, Direction facing, BiFunction<BlockPos, BlockState, Boolean> placementCallback, CallbackInfoReturnable<Boolean> cir) {
        BlockState state = CarryOnDataManager.getCarryData(player).getBlock();
        CarryOnIntegration.onChestPlacement(player, pos, state);
    }
}