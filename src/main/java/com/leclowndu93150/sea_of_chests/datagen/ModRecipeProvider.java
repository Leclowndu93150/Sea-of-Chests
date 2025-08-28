package com.leclowndu93150.sea_of_chests.datagen;

import com.leclowndu93150.sea_of_chests.init.ModBlocks;
import com.leclowndu93150.sea_of_chests.init.ModItems;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.*;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.crafting.conditions.IConditionBuilder;

import java.util.function.Consumer;

public class ModRecipeProvider extends RecipeProvider implements IConditionBuilder {
    
    public ModRecipeProvider(PackOutput output) {
        super(output);
    }
    
    @Override
    protected void buildRecipes(Consumer<FinishedRecipe> consumer) {
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.LOCKPICK.get(), 4)
                .pattern(" I ")
                .pattern(" I ")
                .pattern(" N ")
                .define('I', Items.IRON_INGOT)
                .define('N', Items.IRON_NUGGET)
                .unlockedBy(getHasName(Items.IRON_INGOT), has(Items.IRON_INGOT))
                .save(consumer);
        
        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, ModBlocks.UNLOCKING_STATION.get())
                .pattern(" B ")
                .pattern("DOD")
                .pattern("OOO")
                .define('B', Items.BOOK)
                .define('D', Items.DIAMOND)
                .define('O', Blocks.OBSIDIAN)
                .unlockedBy(getHasName(Items.DIAMOND), has(Items.DIAMOND))
                .save(consumer);
    }
}