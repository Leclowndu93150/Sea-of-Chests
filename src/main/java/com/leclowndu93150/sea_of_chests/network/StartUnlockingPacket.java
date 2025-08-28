package com.leclowndu93150.sea_of_chests.network;

import com.leclowndu93150.sea_of_chests.menu.UnlockingStationMenu;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class StartUnlockingPacket {
    
    public StartUnlockingPacket() {
    }
    
    public StartUnlockingPacket(FriendlyByteBuf buf) {
    }
    
    public void toBytes(FriendlyByteBuf buf) {
    }
    
    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player != null && player.containerMenu instanceof UnlockingStationMenu menu) {
                menu.startUnlocking();
            }
        });
        return true;
    }
}