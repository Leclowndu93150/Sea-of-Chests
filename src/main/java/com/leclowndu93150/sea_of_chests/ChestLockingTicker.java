package com.leclowndu93150.sea_of_chests;

import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BarrelBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.server.ServerLifecycleHooks;
import com.leclowndu93150.sea_of_chests.capability.ChunkLockedChestsProvider;
import com.leclowndu93150.sea_of_chests.capability.WorldLockedChestHandlerProvider;
import com.leclowndu93150.sea_of_chests.network.ModNetworking;
import com.leclowndu93150.sea_of_chests.network.UpdateLockStatePacket;

import java.util.Set;

public class ChestLockingTicker {
    private final static Object listLock = new Object();
    private final static Object worldLock = new Object();
    private final static Set<Entry> chestEntries = new ObjectOpenHashSet<>();
    private final static Set<Entry> pendingEntries = new ObjectOpenHashSet<>();
    private static boolean tickingList = false;

    public static void addEntry(RandomizableContainerBlockEntity incoming, Level level, BlockPos position) {
        if (!(incoming instanceof ChestBlockEntity || incoming instanceof BarrelBlockEntity)) {
            return;
        }

        if (incoming.lootTable == null) {
            return;
        }

        ResourceKey<Level> dimension = level.dimension();
        ChunkPos chunkPos = new ChunkPos(position);

        Set<ChunkPos> chunks = new ObjectLinkedOpenHashSet<>();
        chunks.add(chunkPos);

        int oX = chunkPos.x;
        int oZ = chunkPos.z;

        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                ChunkPos newPos = new ChunkPos(oX + x, oZ + z);
                chunks.add(newPos);
            }
        }

        Entry newEntry = new Entry(dimension, position, chunks, level.getGameTime());
        synchronized (listLock) {
            if (tickingList) {
                pendingEntries.add(newEntry);
            } else {
                chestEntries.add(newEntry);
            }
        }
    }

    public static void onServerTick() {
        Set<Entry> toRemove = new ObjectOpenHashSet<>();
        Set<Entry> copy;
        synchronized (listLock) {
            tickingList = true;
            copy = new ObjectOpenHashSet<>(chestEntries);
            tickingList = false;
        }
        synchronized (worldLock) {
            MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
            if (server == null) {
                return;
            }
            for (Entry entry : copy) {
                ServerLevel level = server.getLevel(entry.getDimension());
                if (level == null || (level.getGameTime() - entry.getAddedAt() > 100)) {
                    toRemove.add(entry);
                    continue;
                }

                if (!level.getChunkSource().hasChunk(entry.getPosition().getX() >> 4, entry.getPosition().getZ() >> 4)) {
                    continue;
                }

                boolean skip = false;
                for (ChunkPos chunkPos : entry.getChunkPositions()) {
                    if (!level.getChunkSource().hasChunk(chunkPos.x, chunkPos.z)) {
                        skip = true;
                        break;
                    }
                }
                if (skip) {
                    continue;
                }

                BlockEntity blockEntity = level.getBlockEntity(entry.getPosition());
                if (!(blockEntity instanceof RandomizableContainerBlockEntity container)) {
                    toRemove.add(entry);
                    continue;
                }
                if (!(container instanceof ChestBlockEntity || container instanceof BarrelBlockEntity)) {
                    toRemove.add(entry);
                    continue;
                }
                if (container.lootTable == null) {
                    toRemove.add(entry);
                    continue;
                }

                LevelChunk chunk = level.getChunkAt(entry.getPosition());
                chunk.getCapability(ChunkLockedChestsProvider.CHUNK_LOCKED_CHESTS_CAPABILITY).ifPresent(chunkLockedChests -> {
                    if (!chunkLockedChests.isLocked(entry.getPosition())) {
                        chunkLockedChests.setLocked(entry.getPosition(), true);
                        
                        level.getCapability(WorldLockedChestHandlerProvider.WORLD_LOCKED_CHEST_HANDLER_CAPABILITY).ifPresent(worldHandler -> {
                            worldHandler.addChest(entry.getPosition());
                        });

                        ModNetworking.sendToClients(new UpdateLockStatePacket(entry.getPosition(), true));
                    }
                });

                toRemove.add(entry);
            }
        }
        synchronized (listLock) {
            tickingList = true;
            chestEntries.removeAll(toRemove);
            chestEntries.addAll(pendingEntries);
            tickingList = false;
            pendingEntries.clear();
        }
    }

    public static class Entry {
        private final ResourceKey<Level> dimension;
        private final BlockPos position;
        private final Set<ChunkPos> chunks;
        private final long addedAt;

        public Entry(ResourceKey<Level> dimension, BlockPos position, Set<ChunkPos> chunks, long addedAt) {
            this.dimension = dimension;
            this.position = position;
            this.chunks = chunks;
            this.addedAt = addedAt;
        }

        public ResourceKey<Level> getDimension() {
            return dimension;
        }

        public BlockPos getPosition() {
            return position;
        }

        public Set<ChunkPos> getChunkPositions() {
            return chunks;
        }

        public long getAddedAt() {
            return addedAt;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Entry entry = (Entry) o;

            if (!dimension.equals(entry.dimension)) return false;
            return position.equals(entry.position);
        }

        @Override
        public int hashCode() {
            int result = dimension.hashCode();
            result = 31 * result + position.hashCode();
            return result;
        }
    }
}