package com.leclowndu93150.sea_of_chests.network;

import com.leclowndu93150.sea_of_chests.capability.LockedChestsProvider;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

public class SyncLockedChestsPacket {
    private final Set<BlockPos> lockedPositions;
    
    public SyncLockedChestsPacket(Set<BlockPos> lockedPositions) {
        this.lockedPositions = lockedPositions;
    }
    
    public SyncLockedChestsPacket(FriendlyByteBuf buf) {
        int size = buf.readInt();
        this.lockedPositions = new HashSet<>();
        for (int i = 0; i < size; i++) {
            this.lockedPositions.add(buf.readBlockPos());
        }
    }
    
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(lockedPositions.size());
        for (BlockPos pos : lockedPositions) {
            buf.writeBlockPos(pos);
        }
    }
    
    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            // Client side handling
            Minecraft mc = Minecraft.getInstance();
            if (mc.level != null) {
                mc.level.getCapability(LockedChestsProvider.LOCKED_CHESTS_CAPABILITY).ifPresent(lockedChests -> {
                    // Clear existing and set new locked positions
                    lockedChests.clearAll();
                    for (BlockPos pos : lockedPositions) {
                        lockedChests.setLocked(pos, true);
                    }
                });
            }
        });
        return true;
    }
}