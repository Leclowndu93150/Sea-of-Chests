package com.leclowndu93150.sea_of_chests.mixin;

import com.leclowndu93150.sea_of_chests.init.ModBlocks;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.level.block.Block;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(ItemBlockRenderTypes.class)
public class ItemBlockRenderTypesMixin {

    @Shadow
    private static Map<Block, RenderType> TYPE_BY_BLOCK;

    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void addCustomTranslucentBlock(CallbackInfo ci) {
        TYPE_BY_BLOCK.put(ModBlocks.UNLOCKING_STATION.get(), RenderType.cutoutMipped());
    }
}