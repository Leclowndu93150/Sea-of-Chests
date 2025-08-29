package com.leclowndu93150.sea_of_chests.menu;

import com.leclowndu93150.sea_of_chests.block.entity.UnlockingStationBlockEntity;
import com.leclowndu93150.sea_of_chests.init.ModBlocks;
import com.leclowndu93150.sea_of_chests.init.ModItems;
import com.leclowndu93150.sea_of_chests.init.ModMenuTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

public class UnlockingStationMenu extends AbstractContainerMenu {
    private final Container container;
    private final ContainerData data;
    private final Level level;
    
    public UnlockingStationMenu(int windowId, Inventory playerInventory, FriendlyByteBuf extraData) {
        this(windowId, playerInventory, playerInventory.player.level().getBlockEntity(extraData.readBlockPos()), new SimpleContainerData(3));
    }
    
    public UnlockingStationMenu(int windowId, Inventory playerInventory, BlockEntity entity, ContainerData data) {
        super(ModMenuTypes.UNLOCKING_STATION_MENU.get(), windowId);
        checkContainerSize((Container) entity, 2);
        this.container = (Container) entity;
        this.level = playerInventory.player.level();
        this.data = data;
        
        this.addSlot(new Slot(container, 0, 80, 35) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.is(ModItems.LOCKPICK.get());
            }
        });
        
        this.addSlot(new Slot(container, 1, 134, 35) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }
        });
        
        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
            }
        }
        
        for (int col = 0; col < 9; ++col) {
            this.addSlot(new Slot(playerInventory, col, 8 + col * 18, 142));
        }
        
        addDataSlots(data);
    }
    
    public boolean isCrafting() {
        return data.get(2) != 0;
    }
    
    public int getScaledProgress() {
        int progress = this.data.get(0);
        int maxProgress = this.data.get(1);
        int progressArrowSize = 24;
        
        return maxProgress != 0 && progress != 0 ? progress * progressArrowSize / maxProgress : 0;
    }
    
    public void startUnlocking() {
        if (container instanceof UnlockingStationBlockEntity station) {
            station.startUnlocking();
        }
    }
    
    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        
        if (slot != null && slot.hasItem()) {
            ItemStack slotStack = slot.getItem();
            itemstack = slotStack.copy();

            if (index < 2) {
                if (!this.moveItemStackTo(slotStack, 2, 38, true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                if (slotStack.is(ModItems.LOCKPICK.get())) {
                    if (!this.moveItemStackTo(slotStack, 0, 1, false)) {
                        return ItemStack.EMPTY;
                    }
                } else {
                    return ItemStack.EMPTY;
                }
            }
            
            if (slotStack.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }
        
        return itemstack;
    }
    
    @Override
    public boolean stillValid(Player player) {
        return container.stillValid(player);
    }
}