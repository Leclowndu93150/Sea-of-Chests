package com.leclowndu93150.sea_of_chests.network;

import com.leclowndu93150.sea_of_chests.capability.ChunkLockedChestsProvider;
import com.leclowndu93150.sea_of_chests.client.ClientCacheHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class UpdateLockStatePacket {
    private final BlockPos pos;
    private final boolean locked;
    
    public UpdateLockStatePacket(BlockPos pos, boolean locked) {
        this.pos = pos;
        this.locked = locked;
    }
    
    public UpdateLockStatePacket(FriendlyByteBuf buf) {
        this.pos = buf.readBlockPos();
        this.locked = buf.readBoolean();
    }
    
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeBlockPos(pos);
        buf.writeBoolean(locked);
    }
    
    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc.level != null) {
                LevelChunk chunk = mc.level.getChunkAt(pos);
                chunk.getCapability(ChunkLockedChestsProvider.CHUNK_LOCKED_CHESTS_CAPABILITY).ifPresent(chunkLockedChests -> {
                    chunkLockedChests.setLocked(pos, locked);
                });
                ClientCacheHandler.onLockStateChanged(pos, locked);
            }
        });
        return true;
    }
}