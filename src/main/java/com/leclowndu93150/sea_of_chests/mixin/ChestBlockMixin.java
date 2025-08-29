package com.leclowndu93150.sea_of_chests.mixin;

import com.leclowndu93150.sea_of_chests.capability.ChunkLockedChestsProvider;
import com.leclowndu93150.sea_of_chests.capability.WorldLockedChestHandlerProvider;
import com.leclowndu93150.sea_of_chests.network.ModNetworking;
import com.leclowndu93150.sea_of_chests.network.UpdateLockStatePacket;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.network.chat.Component;
import net.minecraftforge.fml.ModList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import tschipp.carryon.common.carry.CarryOnData;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Mixin(ChestBlock.class)
public class ChestBlockMixin {
    
    private static final float ITEM_DELETE_CHANCE = 0.3f;
    private static final Random RANDOM = new Random();
    
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
    
    @Inject(
        method = "onRemove",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/Containers;dropContents(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/Container;)V"),
        cancellable = true
    )
    private void handleLockedChestDrops(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving, CallbackInfo ci) {
        if (!level.isClientSide() && !state.is(newState.getBlock())) {
            LevelChunk chunk = level.getChunkAt(pos);
            boolean[] wasLocked = new boolean[1];
            
            chunk.getCapability(ChunkLockedChestsProvider.CHUNK_LOCKED_CHESTS_CAPABILITY).ifPresent(chunkLockedChests -> {
                if (chunkLockedChests.isLocked(pos)) {
                    wasLocked[0] = true;
                    chunkLockedChests.setLocked(pos, false);
                }
            });

            level.getCapability(WorldLockedChestHandlerProvider.WORLD_LOCKED_CHEST_HANDLER_CAPABILITY).ifPresent(worldHandler -> {
                worldHandler.removeChest(pos);
            });

            ModNetworking.sendToClients(new UpdateLockStatePacket(pos, false));
            
            if (wasLocked[0]) {
                BlockEntity blockEntity = level.getBlockEntity(pos);
                if (blockEntity instanceof Container container) {
                    Player breakingPlayer = null;
                    for (Player player : level.players()) {
                        if (player.distanceToSqr(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) < 36) {
                            breakingPlayer = player;
                            break;
                        }
                    }

                    List<ItemStack> itemsToKeep = new ArrayList<>();
                    int deletedCount = 0;
                    
                    for (int i = 0; i < container.getContainerSize(); i++) {
                        ItemStack stack = container.getItem(i);
                        if (!stack.isEmpty()) {
                            if (RANDOM.nextFloat() < ITEM_DELETE_CHANCE) {
                                deletedCount++;
                                if (level instanceof ServerLevel serverLevel) {
                                    serverLevel.sendParticles(
                                        ParticleTypes.SMOKE,
                                        pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                                        10, 0.2, 0.2, 0.2, 0.05
                                    );
                                }
                            } else {
                                itemsToKeep.add(stack.copy());
                            }
                        }
                    }

                    container.clearContent();

                    for (ItemStack stack : itemsToKeep) {
                        double x = pos.getX() + 0.5 + (RANDOM.nextDouble() - 0.5) * 0.5;
                        double y = pos.getY() + 0.5;
                        double z = pos.getZ() + 0.5 + (RANDOM.nextDouble() - 0.5) * 0.5;
                        ItemEntity itemEntity = new ItemEntity(level, x, y, z, stack);
                        itemEntity.setDefaultPickUpDelay();
                        level.addFreshEntity(itemEntity);
                    }
                    
                    if (deletedCount > 0) {
                        level.playSound(null, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.5f, 1.0f + (RANDOM.nextFloat() - 0.5f) * 0.2f);
                        
                        if (breakingPlayer != null) {
                            breakingPlayer.displayClientMessage(
                                Component.literal("Some items were destroyed by forcefully breaking the locked chest!").withStyle(style -> style.withColor(0xFF5555)),
                                true
                            );
                        }
                    }
                    
                    ci.cancel();
                }
            } else {
                Containers.dropContents(level, pos, level.getBlockEntity(pos) instanceof Container c ? c : null);
                ci.cancel();
            }
        }
    }
}