package com.leclowndu93150.sea_of_chests.event;

import com.leclowndu93150.sea_of_chests.SeaOfChests;
import com.leclowndu93150.sea_of_chests.capability.ILockedChests;
import com.leclowndu93150.sea_of_chests.capability.LockedChestsProvider;
import com.leclowndu93150.sea_of_chests.network.ModNetworking;
import com.leclowndu93150.sea_of_chests.network.SyncLockedChestsPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = SeaOfChests.MODID)
public class ModEvents {
    
    @SubscribeEvent
    public static void onAttachCapabilitiesLevel(AttachCapabilitiesEvent<Level> event) {
        event.addCapability(new ResourceLocation(SeaOfChests.MODID, "locked_chests"), new LockedChestsProvider());
    }
    
    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            Level level = serverPlayer.level();
            level.getCapability(LockedChestsProvider.LOCKED_CHESTS_CAPABILITY).ifPresent(lockedChests -> {
                ModNetworking.sendToPlayer(new SyncLockedChestsPacket(lockedChests.getLockedChests()), serverPlayer);
            });
        }
    }
    
    @SubscribeEvent 
    public static void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            Level level = serverPlayer.level();
            level.getCapability(LockedChestsProvider.LOCKED_CHESTS_CAPABILITY).ifPresent(lockedChests -> {
                ModNetworking.sendToPlayer(new SyncLockedChestsPacket(lockedChests.getLockedChests()), serverPlayer);
            });
        }
    }
    
    @Mod.EventBusSubscriber(modid = SeaOfChests.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class ModEventBus {
        
        @SubscribeEvent
        public static void onRegisterCapabilities(RegisterCapabilitiesEvent event) {
            event.register(ILockedChests.class);
        }
    }
}