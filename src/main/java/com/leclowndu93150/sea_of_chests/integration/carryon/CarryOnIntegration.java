package com.leclowndu93150.sea_of_chests.integration.carryon;

import com.leclowndu93150.sea_of_chests.SeaOfChests;
import com.leclowndu93150.sea_of_chests.capability.ChunkLockedChestsProvider;
import com.leclowndu93150.sea_of_chests.capability.WorldLockedChestHandlerProvider;
import com.leclowndu93150.sea_of_chests.client.ClientCacheHandler;
import com.leclowndu93150.sea_of_chests.network.ModNetworking;
import com.leclowndu93150.sea_of_chests.network.UpdateLockStatePacket;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BarrelBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.ModList;
import tschipp.carryon.common.carry.CarryOnData;
import tschipp.carryon.common.carry.CarryOnDataManager;

public class CarryOnIntegration {
    
    public static final String SEA_OF_CHESTS_LOCKED_KEY = "seaOfChests_locked";
    
    public static boolean isCarryOnLoaded() {
        System.out.println("CarryOn loaded: " + ModList.get().isLoaded("carryon"));
        return ModList.get().isLoaded("carryon");
    }
    
    public static boolean onChestPickup(ServerPlayer player, BlockPos pos, Level level, BlockState state) {
        if (!isCarryOnLoaded()) {
            return true;
        }
        
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (!(blockEntity instanceof ChestBlockEntity || blockEntity instanceof BarrelBlockEntity)) {
            return true;
        }
        
        LevelChunk chunk = level.getChunkAt(pos);
        boolean[] wasLocked = new boolean[1];
        
        chunk.getCapability(ChunkLockedChestsProvider.CHUNK_LOCKED_CHESTS_CAPABILITY).ifPresent(chunkLockedChests -> {
            if (chunkLockedChests.isLocked(pos)) {
                wasLocked[0] = true;
                chunkLockedChests.setLocked(pos, false);
                
                level.getCapability(WorldLockedChestHandlerProvider.WORLD_LOCKED_CHEST_HANDLER_CAPABILITY).ifPresent(worldHandler -> {
                    worldHandler.removeChest(pos);
                });
                
                ModNetworking.sendToClients(new UpdateLockStatePacket(pos, false));
                SeaOfChests.LOGGER.info("Removed lock state from chest at {} due to CarryOn pickup", pos);
            }
        });
        
        if (wasLocked[0]) {
            storeLockStateInCarryData(player, true);
        }
        
        return true;
    }
    
    public static boolean onChestPlacement(ServerPlayer player, BlockPos pos, BlockState state) {
        if (!isCarryOnLoaded()) {
            return true;
        }
        
        if (!(state.getBlock().asItem().getDefaultInstance().getItem() instanceof BlockItem)) {
            return true;
        }
        
        boolean wasLocked = getLockStateFromCarryData(player);
        if (!wasLocked) {
            return true;
        }
        
        BlockEntity blockEntity = player.level().getBlockEntity(pos);
        if (!(blockEntity instanceof ChestBlockEntity || blockEntity instanceof BarrelBlockEntity)) {
            return true;
        }
        
        LevelChunk chunk = player.level().getChunkAt(pos);
        
        chunk.getCapability(ChunkLockedChestsProvider.CHUNK_LOCKED_CHESTS_CAPABILITY).ifPresent(chunkLockedChests -> {
            chunkLockedChests.setLocked(pos, true);
        });
        
        player.level().getCapability(WorldLockedChestHandlerProvider.WORLD_LOCKED_CHEST_HANDLER_CAPABILITY).ifPresent(worldHandler -> {
            worldHandler.addChest(pos);
        });
        
        ModNetworking.sendToClients(new UpdateLockStatePacket(pos, true));
        
        clearLockStateFromCarryData(player);
        
        if (player.level().isClientSide()) {
            onClientChestPlaced(pos);
        }
        
        SeaOfChests.LOGGER.info("Restored lock state to chest at {} after CarryOn placement", pos);
        
        return true;
    }
    
    @OnlyIn(Dist.CLIENT)
    public static void onClientChestPlaced(BlockPos pos) {
        ClientCacheHandler.onLockStateChanged(pos, true);
    }
    
    public static void storeLockStateInCarryData(ServerPlayer player, boolean locked) {
        if (!isCarryOnLoaded()) {
            return;
        }
        
        try {
            CarryOnData carryData = CarryOnDataManager.getCarryData(player);
            CompoundTag nbt = carryData.getNbt();
            nbt.putBoolean(SEA_OF_CHESTS_LOCKED_KEY, locked);
        } catch (Exception e) {
            SeaOfChests.LOGGER.error("Failed to store lock state in CarryOnData", e);
        }
    }
    
    public static boolean getLockStateFromCarryData(ServerPlayer player) {
        if (!isCarryOnLoaded()) {
            return false;
        }
        
        try {
            CarryOnData carryData = tschipp.carryon.common.carry.CarryOnDataManager.getCarryData(player);
            CompoundTag nbt = carryData.getNbt();
            return nbt.getBoolean(SEA_OF_CHESTS_LOCKED_KEY);
        } catch (Exception e) {
            SeaOfChests.LOGGER.error("Failed to get lock state from CarryOnData", e);
            return false;
        }
    }
    
    public static void clearLockStateFromCarryData(ServerPlayer player) {
        if (!isCarryOnLoaded()) {
            return;
        }
        
        try {
            CarryOnData carryData = tschipp.carryon.common.carry.CarryOnDataManager.getCarryData(player);
            CompoundTag nbt = carryData.getNbt();
            nbt.remove(SEA_OF_CHESTS_LOCKED_KEY);
        } catch (Exception e) {
            SeaOfChests.LOGGER.error("Failed to clear lock state from CarryOnData", e);
        }
    }
}