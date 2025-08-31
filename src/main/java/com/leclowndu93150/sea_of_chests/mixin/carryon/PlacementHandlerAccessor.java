package com.leclowndu93150.sea_of_chests.mixin.carryon;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;
import tschipp.carryon.common.carry.PlacementHandler;

@Mixin(value = PlacementHandler.class, remap = false)
public interface PlacementHandlerAccessor {
    
    @Invoker("getPlacementState")
    static BlockState invokeGetPlacementState(BlockState state, ServerPlayer player, BlockPlaceContext context, BlockPos pos) {
        throw new AssertionError();
    }
    
    @Invoker("updateProperty")
    static <T extends Comparable<T>> BlockState invokeUpdateProperty(BlockState state, BlockState otherState, net.minecraft.world.level.block.state.properties.Property<T> prop) {
        throw new AssertionError();
    }
}