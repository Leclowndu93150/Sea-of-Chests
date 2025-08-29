package com.leclowndu93150.sea_of_chests.integration.carryon;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class CarryOnChestTracker {
    
    private static final Map<UUID, CarriedChestData> carriedChests = new ConcurrentHashMap<>();
    
    public static class CarriedChestData {
        public final boolean wasLocked;
        public final BlockPos originalPos;
        public final BlockState originalState;
        public final long pickupTime;
        
        public CarriedChestData(boolean wasLocked, BlockPos originalPos, BlockState originalState) {
            this.wasLocked = wasLocked;
            this.originalPos = originalPos;
            this.originalState = originalState;
            this.pickupTime = System.currentTimeMillis();
        }
    }
    
    public static void trackPickup(ServerPlayer player, BlockPos pos, BlockState state, boolean wasLocked) {
        carriedChests.put(player.getUUID(), new CarriedChestData(wasLocked, pos, state));
    }
    
    public static boolean wasChestLocked(ServerPlayer player) {
        CarriedChestData data = carriedChests.get(player.getUUID());
        return data != null && data.wasLocked;
    }
    
    public static CarriedChestData getCarriedChestData(ServerPlayer player) {
        return carriedChests.get(player.getUUID());
    }
    
    public static void clearTracking(ServerPlayer player) {
        carriedChests.remove(player.getUUID());
    }
}