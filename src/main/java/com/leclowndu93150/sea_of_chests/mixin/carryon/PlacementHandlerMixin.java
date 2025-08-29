package com.leclowndu93150.sea_of_chests.mixin.carryon;

import com.leclowndu93150.sea_of_chests.capability.ChunkLockedChestsProvider;
import com.leclowndu93150.sea_of_chests.integration.carryon.CarryOnChestTracker;
import com.leclowndu93150.sea_of_chests.integration.carryon.CarryOnIntegration;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
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
    
    @Redirect(
        method = "getPlacementState",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/level/block/Block;getStateForPlacement(Lnet/minecraft/world/item/context/BlockPlaceContext;)Lnet/minecraft/world/level/block/state/BlockState;"
        )
    )
    private static BlockState seaOfChests_preventLockedChestMerging(
        net.minecraft.world.level.block.Block block,
        BlockPlaceContext context
    ) {
        BlockState originalState = block.getStateForPlacement(context);
        
        if (originalState != null && block instanceof ChestBlock) {
            Level level = context.getLevel();
            BlockPos clickedPos = context.getClickedPos();
            
            if (originalState.hasProperty(ChestBlock.TYPE)) {
                ChestType chestType = originalState.getValue(ChestBlock.TYPE);
                
                if (chestType != ChestType.SINGLE) {
                    Direction facing = originalState.getValue(ChestBlock.FACING);
                    BlockPos adjacentPos = null;
                    
                    if (chestType == ChestType.LEFT) {
                        adjacentPos = clickedPos.relative(facing.getClockWise());
                    } else if (chestType == ChestType.RIGHT) {
                        adjacentPos = clickedPos.relative(facing.getCounterClockWise());
                    }
                    
                    if (adjacentPos != null && !level.isClientSide()) {
                        final BlockPos finalAdjacentPos = adjacentPos;
                        LevelChunk chunk = level.getChunkAt(finalAdjacentPos);
                        boolean[] isLocked = new boolean[1];
                        
                        chunk.getCapability(ChunkLockedChestsProvider.CHUNK_LOCKED_CHESTS_CAPABILITY).ifPresent(chunkLockedChests -> {
                            if (chunkLockedChests.isLocked(finalAdjacentPos)) {
                                isLocked[0] = true;
                            }
                        });
                        
                        if (isLocked[0]) {
                            return originalState.setValue(ChestBlock.TYPE, ChestType.SINGLE);
                        }
                        
                        if (context.getPlayer() instanceof ServerPlayer serverPlayer) {
                            boolean wasCarriedChestLocked = CarryOnChestTracker.wasChestLocked(serverPlayer);
                            if (wasCarriedChestLocked) {
                                return originalState.setValue(ChestBlock.TYPE, ChestType.SINGLE);
                            }
                        }
                    }
                }
            }
        }
        
        return originalState;
    }
}