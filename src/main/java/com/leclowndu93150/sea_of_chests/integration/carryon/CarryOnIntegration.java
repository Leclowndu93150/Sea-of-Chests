package com.leclowndu93150.sea_of_chests.integration.carryon;

import com.leclowndu93150.sea_of_chests.SeaOfChests;
import com.leclowndu93150.sea_of_chests.capability.ChunkLockedChestsProvider;
import com.leclowndu93150.sea_of_chests.capability.WorldLockedChestHandlerProvider;
import com.leclowndu93150.sea_of_chests.network.ModNetworking;
import com.leclowndu93150.sea_of_chests.network.UpdateLockStatePacket;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BarrelBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.fml.ModList;
import tschipp.carryon.common.carry.CarryOnData;
import tschipp.carryon.common.carry.CarryOnDataManager;

public class CarryOnIntegration {

    public static final String SEA_OF_CHESTS_LOCKED_KEY = "seaOfChests_locked";
    public static final String SEA_OF_CHESTS_ORIGINAL_POS_KEY = "seaOfChests_originalPos";

    public static boolean isCarryOnLoaded() {
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
            }
        });

        if (wasLocked[0]) {
            CarryOnChestTracker.trackPickup(player, pos, state, true);
            storeLockStateInCarryData(player, true, pos);
        } else {
            CarryOnChestTracker.trackPickup(player, pos, state, false);
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

        boolean wasLocked = CarryOnChestTracker.wasChestLocked(player);
        CarryOnChestTracker.CarriedChestData chestData = CarryOnChestTracker.getCarriedChestData(player);

        SeaOfChests.LOGGER.info("PLACEMENT: External tracker - wasLocked = {}, hasData = {}", wasLocked, chestData != null);

        if (!wasLocked) {
            CarryOnChestTracker.clearTracking(player);
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

        player.level().getCapability(WorldLockedChestHandlerProvider.WORLD_LOCKED_CHEST_HANDLER_CAPABILITY).ifPresent(worldHandler -> {worldHandler.addChest(pos);});

        ModNetworking.sendToClients(new UpdateLockStatePacket(pos, true));

        CarryOnChestTracker.clearTracking(player);
        clearLockStateFromCarryData(player);
        
        return true;
    }

    public static void storeLockStateInCarryData(ServerPlayer player, boolean locked, BlockPos originalPos) {
        if (!isCarryOnLoaded()) {
            return;
        }

        try {
            CarryOnData carryData = CarryOnDataManager.getCarryData(player);
            CompoundTag nbt = carryData.getNbt();
            nbt.putBoolean(SEA_OF_CHESTS_LOCKED_KEY, locked);
            nbt.putLong(SEA_OF_CHESTS_ORIGINAL_POS_KEY, originalPos.asLong());
        } catch (Exception e) {
            SeaOfChests.LOGGER.error("Failed to store lock state in CarryOnData", e);
        }
    }

    public static void clearLockStateFromCarryData(ServerPlayer player) {
        if (!isCarryOnLoaded()) {
            return;
        }

        try {
            CarryOnData carryData = CarryOnDataManager.getCarryData(player);
            CompoundTag nbt = carryData.getNbt();
            nbt.remove(SEA_OF_CHESTS_LOCKED_KEY);
            nbt.remove(SEA_OF_CHESTS_ORIGINAL_POS_KEY);
        } catch (Exception e) {
            SeaOfChests.LOGGER.error("Failed to clear lock state from CarryOnData", e);
        }
    }
}