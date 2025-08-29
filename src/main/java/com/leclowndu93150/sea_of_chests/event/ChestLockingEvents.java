package com.leclowndu93150.sea_of_chests.event;

import com.leclowndu93150.sea_of_chests.SeaOfChests;
import com.leclowndu93150.sea_of_chests.capability.ChunkLockedChestsProvider;
import com.leclowndu93150.sea_of_chests.capability.WorldLockedChestHandlerProvider;
import com.leclowndu93150.sea_of_chests.init.ModItems;
import com.leclowndu93150.sea_of_chests.integration.carryon.CarryOnChestTracker;
import com.leclowndu93150.sea_of_chests.network.ModNetworking;
import com.leclowndu93150.sea_of_chests.network.UpdateLockStatePacket;
import com.leclowndu93150.sea_of_chests.util.ChestUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BarrelBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Mod.EventBusSubscriber(modid = SeaOfChests.MODID)
public class ChestLockingEvents {
    
    private static final float LOCKED_CHEST_BREAK_SPEED_MODIFIER = 0.1f;
    private static final float ITEM_DELETE_CHANCE = 0.3f;
    private static final Random RANDOM = new Random();
    
    @SubscribeEvent
    public static void onPlayerInteract(PlayerInteractEvent.RightClickBlock event) {
        Level level = event.getLevel();
        BlockPos pos = event.getPos();
        Player player = event.getEntity();
        
        if (level.isClientSide()) return;
        
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof RandomizableContainerBlockEntity container) {
            if (container instanceof ChestBlockEntity || container instanceof BarrelBlockEntity) {
                LevelChunk chunk = level.getChunkAt(pos);

                chunk.getCapability(ChunkLockedChestsProvider.CHUNK_LOCKED_CHESTS_CAPABILITY).ifPresent(chunkLockedChests -> {
                    if (chunkLockedChests.isLocked(pos)) {
                        ItemStack heldItem = player.getItemInHand(event.getHand());
                        if (heldItem.getItem() == ModItems.MAGNIFYING_GLASS.get() && player.isShiftKeyDown()) {
                            return;
                        }
                        
                        event.setCanceled(true);
                        
                        player.displayClientMessage(
                            Component.literal("This chest is locked! Bring it to an Unlocking Station.").withStyle(style -> style.withColor(0xFF5555)), 
                            true
                        );
                    }
                });
            }
        }
    }
    
    @SubscribeEvent
    public static void onBreakSpeed(PlayerEvent.BreakSpeed event) {
        BlockPos pos = event.getPosition().orElse(null);
        if (pos == null) return;
        
        Level level = event.getEntity().level();
        BlockEntity blockEntity = level.getBlockEntity(pos);
        
        if (blockEntity instanceof RandomizableContainerBlockEntity container) {
            if (container instanceof ChestBlockEntity || container instanceof BarrelBlockEntity) {
                LevelChunk chunk = level.getChunkAt(pos);
                
                chunk.getCapability(ChunkLockedChestsProvider.CHUNK_LOCKED_CHESTS_CAPABILITY).ifPresent(chunkLockedChests -> {
                    if (chunkLockedChests.isLocked(pos)) {
                        event.setNewSpeed(event.getOriginalSpeed() * LOCKED_CHEST_BREAK_SPEED_MODIFIER);
                    }
                });
            }
        }
    }
    
    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (event.getLevel().isClientSide()) return;
        
        Player player = event.getPlayer();
        if (player instanceof ServerPlayer serverPlayer) {
            CarryOnChestTracker.CarriedChestData carriedData = CarryOnChestTracker.getCarriedChestData(serverPlayer);
            if (carriedData != null && carriedData.originalPos.equals(event.getPos())) {
                return;
            }
        }
        
        BlockPos pos = event.getPos();
        Level level = (Level) event.getLevel();
        BlockEntity blockEntity = level.getBlockEntity(pos);
        
        if (blockEntity instanceof RandomizableContainerBlockEntity container) {
            if (container instanceof ChestBlockEntity || container instanceof BarrelBlockEntity) {
                LevelChunk chunk = level.getChunkAt(pos);
                
                chunk.getCapability(ChunkLockedChestsProvider.CHUNK_LOCKED_CHESTS_CAPABILITY).ifPresent(chunkLockedChests -> {
                    if (chunkLockedChests.isLocked(pos)) {
                        handleLockedChestBreak(level, pos, container, event.getPlayer());
                    } else {
                        chunkLockedChests.setLocked(pos, false);
                        level.getCapability(WorldLockedChestHandlerProvider.WORLD_LOCKED_CHEST_HANDLER_CAPABILITY).ifPresent(worldHandler -> {
                            worldHandler.removeChest(pos);
                        });
                        ModNetworking.sendToClients(new UpdateLockStatePacket(pos, false));
                    }
                });
            }
        }
    }
    
    private static void handleLockedChestBreak(Level level, BlockPos pos, RandomizableContainerBlockEntity container, Player player) {
        if (!(level instanceof ServerLevel serverLevel)) return;

        LevelChunk chunk = level.getChunkAt(pos);
        chunk.getCapability(ChunkLockedChestsProvider.CHUNK_LOCKED_CHESTS_CAPABILITY).ifPresent(chunkLockedChests -> {
            chunkLockedChests.setLocked(pos, false);
        });
        
        level.getCapability(WorldLockedChestHandlerProvider.WORLD_LOCKED_CHEST_HANDLER_CAPABILITY).ifPresent(worldHandler -> {
            worldHandler.removeChest(pos);
        });

        ModNetworking.sendToClients(new UpdateLockStatePacket(pos, false));
        
        List<ItemStack> itemsToKeep = new ArrayList<>();
        int deletedCount = 0;
        
        for (int i = 0; i < container.getContainerSize(); i++) {
            ItemStack stack = container.getItem(i);
            if (!stack.isEmpty()) {
                if (RANDOM.nextFloat() < ITEM_DELETE_CHANCE) {
                    deletedCount++;
                    
                    double x = pos.getX() + 0.5;
                    double y = pos.getY() + 0.5;
                    double z = pos.getZ() + 0.5;
                    serverLevel.sendParticles(
                        ParticleTypes.SMOKE,
                        x, y, z,
                        10,
                        0.2, 0.2, 0.2,
                        0.05
                    );
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
            level.playSound(
                null,
                pos,
                SoundEvents.FIRE_EXTINGUISH,
                SoundSource.BLOCKS,
                0.5f,
                1.0f + (RANDOM.nextFloat() - 0.5f) * 0.2f
            );
            
            if (player != null) {
                player.displayClientMessage(
                    Component.literal("Some items were destroyed by forcefully breaking the locked chest!").withStyle(style -> style.withColor(0xFF5555)),
                    true
                );
            }
        }
    }
}