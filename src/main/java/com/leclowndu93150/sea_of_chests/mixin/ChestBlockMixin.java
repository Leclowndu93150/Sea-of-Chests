package com.leclowndu93150.sea_of_chests.mixin;

import com.leclowndu93150.sea_of_chests.capability.ChunkLockedChestsProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(ChestBlock.class)
public class ChestBlockMixin {
    
    @Inject(
        method = "candidatePartnerFacing",
        at = @At("HEAD"),
        cancellable = true
    )
    private void preventLockedChestConnection(BlockPlaceContext context, Direction direction, CallbackInfoReturnable<Direction> cir) {
        Level level = context.getLevel();
        BlockPos targetPos = context.getClickedPos().relative(direction);
        
        if (!level.isClientSide()) {
            LevelChunk chunk = level.getChunkAt(targetPos);
            chunk.getCapability(ChunkLockedChestsProvider.CHUNK_LOCKED_CHESTS_CAPABILITY).ifPresent(chunkLockedChests -> {
                if (chunkLockedChests.isLocked(targetPos)) {
                    cir.setReturnValue(null);
                }
            });
        }
    }
    
    @Inject(
        method = "getStateForPlacement",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/context/BlockPlaceContext;getClickedPos()Lnet/minecraft/core/BlockPos;", ordinal = 0),
        locals = LocalCapture.CAPTURE_FAILHARD
    )
    private void checkAdjacentLockedChests(BlockPlaceContext context, CallbackInfoReturnable<BlockState> cir, ChestType chestType, Direction direction) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        
        if (!level.isClientSide()) {
            BlockPos leftPos = pos.relative(direction.getClockWise());
            BlockPos rightPos = pos.relative(direction.getCounterClockWise());
            
            LevelChunk leftChunk = level.getChunkAt(leftPos);
            leftChunk.getCapability(ChunkLockedChestsProvider.CHUNK_LOCKED_CHESTS_CAPABILITY).ifPresent(chunkLockedChests -> {
                if (chunkLockedChests.isLocked(leftPos)) {
                    BlockState state = level.getBlockState(leftPos);
                    if (state.getBlock() instanceof ChestBlock && state.getValue(ChestBlock.TYPE) == ChestType.SINGLE) {
                        level.setBlock(leftPos, state.setValue(ChestBlock.TYPE, ChestType.SINGLE), 3);
                    }
                }
            });
            
            LevelChunk rightChunk = level.getChunkAt(rightPos);
            rightChunk.getCapability(ChunkLockedChestsProvider.CHUNK_LOCKED_CHESTS_CAPABILITY).ifPresent(chunkLockedChests -> {
                if (chunkLockedChests.isLocked(rightPos)) {
                    BlockState state = level.getBlockState(rightPos);
                    if (state.getBlock() instanceof ChestBlock && state.getValue(ChestBlock.TYPE) == ChestType.SINGLE) {
                        level.setBlock(rightPos, state.setValue(ChestBlock.TYPE, ChestType.SINGLE), 3);
                    }
                }
            });
        }
    }
}