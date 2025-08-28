package com.leclowndu93150.sea_of_chests.network;

import com.leclowndu93150.sea_of_chests.capability.LockedChestsProvider;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
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
            // Client side handling
            Minecraft mc = Minecraft.getInstance();
            if (mc.level != null) {
                mc.level.getCapability(LockedChestsProvider.LOCKED_CHESTS_CAPABILITY).ifPresent(lockedChests -> {
                    lockedChests.setLocked(pos, locked);
                });
            }
        });
        return true;
    }
}