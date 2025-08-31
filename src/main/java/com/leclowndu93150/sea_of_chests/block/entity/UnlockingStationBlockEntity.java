package com.leclowndu93150.sea_of_chests.block.entity;

import com.leclowndu93150.sea_of_chests.capability.ChunkLockedChestsProvider;
import com.leclowndu93150.sea_of_chests.capability.WorldLockedChestHandlerProvider;
import com.leclowndu93150.sea_of_chests.init.ModBlockEntities;
import com.leclowndu93150.sea_of_chests.init.ModItems;
import com.leclowndu93150.sea_of_chests.menu.UnlockingStationMenu;
import com.leclowndu93150.sea_of_chests.network.ModNetworking;
import com.leclowndu93150.sea_of_chests.network.UpdateLockStatePacket;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.BarrelBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.NonNullList;

public class UnlockingStationBlockEntity extends BlockEntity implements Container, MenuProvider {
    private NonNullList<ItemStack> items = NonNullList.withSize(2, ItemStack.EMPTY);
    private int unlockProgress = 0;
    private int maxUnlockTime = 100;
    private boolean isUnlocking = false;
    private BlockPos targetChestPos = null;
    
    protected final ContainerData dataAccess = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> UnlockingStationBlockEntity.this.unlockProgress;
                case 1 -> UnlockingStationBlockEntity.this.maxUnlockTime;
                case 2 -> UnlockingStationBlockEntity.this.isUnlocking ? 1 : 0;
                default -> 0;
            };
        }
        
        @Override
        public void set(int index, int value) {
            switch (index) {
                case 0 -> UnlockingStationBlockEntity.this.unlockProgress = value;
                case 1 -> UnlockingStationBlockEntity.this.maxUnlockTime = value;
                case 2 -> UnlockingStationBlockEntity.this.isUnlocking = value != 0;
            }
        }
        
        @Override
        public int getCount() {
            return 3;
        }
    };
    
    public UnlockingStationBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.UNLOCKING_STATION.get(), pos, state);
    }
    
    public static void serverTick(Level level, BlockPos pos, BlockState state, UnlockingStationBlockEntity blockEntity) {
        if (blockEntity.isUnlocking) {
            BlockEntity above = level.getBlockEntity(pos.above());
            boolean hasValidChest = (above instanceof ChestBlockEntity || above instanceof BarrelBlockEntity);
            boolean hasLockpick = blockEntity.items.get(0).is(ModItems.LOCKPICK.get());
            
            if (hasValidChest && hasLockpick) {
                blockEntity.unlockProgress++;
                
                if (blockEntity.unlockProgress % 20 == 0) {
                    level.playSound(null, pos, SoundEvents.IRON_DOOR_CLOSE, SoundSource.BLOCKS, 0.3f, 2.0f);
                }
                
                if (blockEntity.unlockProgress >= blockEntity.maxUnlockTime) {
                    blockEntity.completeUnlocking(level, pos);
                }
                
                setChanged(level, pos, state);
            } else {
                blockEntity.resetUnlocking();
            }
        }
    }
    
    public void startUnlocking() {
        if (!isUnlocking && items.get(0).is(ModItems.LOCKPICK.get())) {
            BlockEntity above = level.getBlockEntity(worldPosition.above());
            if (above instanceof ChestBlockEntity || above instanceof BarrelBlockEntity) {
                var chunk = level.getChunkAt(worldPosition.above());
                chunk.getCapability(ChunkLockedChestsProvider.CHUNK_LOCKED_CHESTS_CAPABILITY).ifPresent(chunkLockedChests -> {
                    if (chunkLockedChests.isLocked(worldPosition.above())) {
                        isUnlocking = true;
                        targetChestPos = worldPosition.above();
                        unlockProgress = 0;
                        setChanged();
                    }
                });
            }
        }
    }
    
    private void completeUnlocking(Level level, BlockPos pos) {
        BlockPos chestPos = pos.above();

        var chunk = level.getChunkAt(chestPos);
        chunk.getCapability(ChunkLockedChestsProvider.CHUNK_LOCKED_CHESTS_CAPABILITY).ifPresent(chunkLockedChests -> {
            chunkLockedChests.removeLocked(chestPos);
        });

        level.getCapability(WorldLockedChestHandlerProvider.WORLD_LOCKED_CHEST_HANDLER_CAPABILITY).ifPresent(worldHandler -> {
            worldHandler.removeChest(chestPos);
        });
        
        ModNetworking.sendToClients(new UpdateLockStatePacket(chestPos, false));
        
        items.get(0).shrink(1);
        
        level.playSound(null, pos, SoundEvents.IRON_DOOR_OPEN, SoundSource.BLOCKS, 1.0f, 1.0f);
        level.playSound(null, pos, SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.BLOCKS, 0.5f, 1.0f);
        
        resetUnlocking();
    }
    
    private void resetUnlocking() {
        isUnlocking = false;
        unlockProgress = 0;
        targetChestPos = null;
        setChanged();
    }
    
    public void dropContents() {
        for (ItemStack item : items) {
            if (!item.isEmpty()) {
                Block.popResource(level, worldPosition, item);
            }
        }
        items.clear();
    }
    
    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        ContainerHelper.saveAllItems(tag, items);
        tag.putInt("UnlockProgress", unlockProgress);
        tag.putBoolean("IsUnlocking", isUnlocking);
        if (targetChestPos != null) {
            tag.putLong("TargetChest", targetChestPos.asLong());
        }
    }
    
    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        items = NonNullList.withSize(getContainerSize(), ItemStack.EMPTY);
        ContainerHelper.loadAllItems(tag, items);
        unlockProgress = tag.getInt("UnlockProgress");
        isUnlocking = tag.getBoolean("IsUnlocking");
        if (tag.contains("TargetChest")) {
            targetChestPos = BlockPos.of(tag.getLong("TargetChest"));
        }
    }
    
    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
    
    @Override
    public CompoundTag getUpdateTag() {
        return saveWithoutMetadata();
    }
    
    @Override
    public Component getDisplayName() {
        return Component.translatable("container.sea_of_chests.unlocking_station");
    }
    
    @Override
    public AbstractContainerMenu createMenu(int windowId, Inventory playerInventory, Player player) {
        return new UnlockingStationMenu(windowId, playerInventory, this, dataAccess);
    }
    
    @Override
    public int getContainerSize() {
        return items.size();
    }
    
    @Override
    public boolean isEmpty() {
        return items.stream().allMatch(ItemStack::isEmpty);
    }
    
    @Override
    public ItemStack getItem(int slot) {
        return items.get(slot);
    }
    
    @Override
    public ItemStack removeItem(int slot, int amount) {
        return ContainerHelper.removeItem(items, slot, amount);
    }
    
    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        return ContainerHelper.takeItem(items, slot);
    }
    
    @Override
    public void setItem(int slot, ItemStack stack) {
        items.set(slot, stack);
        if (stack.getCount() > getMaxStackSize()) {
            stack.setCount(getMaxStackSize());
        }
        setChanged();
    }
    
    @Override
    public boolean stillValid(Player player) {
        return Container.stillValidBlockEntity(this, player);
    }
    
    @Override
    public void clearContent() {
        items.clear();
    }
}