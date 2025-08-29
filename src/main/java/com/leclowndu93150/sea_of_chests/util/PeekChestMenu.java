package com.leclowndu93150.sea_of_chests.util;

import com.leclowndu93150.sea_of_chests.init.ModMenuTypes;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class PeekChestMenu extends ChestMenu {
    
    public PeekChestMenu(int windowId, Inventory playerInventory) {
        this(windowId, playerInventory, new SimpleContainer(27));
    }
    
    public PeekChestMenu(int windowId, Inventory playerInventory, Container container) {
        super(ModMenuTypes.PEEK_CHEST_MENU.get(), windowId, playerInventory, container, container.getContainerSize() / 9);
    }
    
    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }
    
    @Override
    public boolean stillValid(Player player) {
        return true;
    }
    
    @Override
    public void clicked(int slotId, int dragType, ClickType clickType, Player player) {
    }
    
    @Override
    public boolean canTakeItemForPickAll(ItemStack stack, Slot slot) {
        return false;
    }

}