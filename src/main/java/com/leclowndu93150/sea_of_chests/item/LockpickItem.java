package com.leclowndu93150.sea_of_chests.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class LockpickItem extends Item {
    
    public LockpickItem(Properties properties) {
        super(properties.durability(5).setNoRepair());
    }
    
    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal("Used to unlock locked chests").withStyle(style -> style.withColor(0xAAAAAA)));
        tooltip.add(Component.literal("Place in Unlocking Station to use").withStyle(style -> style.withColor(0x888888)));
        super.appendHoverText(stack, level, tooltip, flag);
    }
}