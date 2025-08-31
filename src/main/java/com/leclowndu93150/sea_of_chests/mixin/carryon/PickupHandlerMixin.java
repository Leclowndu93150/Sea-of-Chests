package com.leclowndu93150.sea_of_chests.mixin.carryon;

import com.leclowndu93150.sea_of_chests.integration.carryon.CarryOnIntegration;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import tschipp.carryon.Constants;
import tschipp.carryon.common.carry.CarryOnData;
import tschipp.carryon.common.carry.CarryOnDataManager;
import tschipp.carryon.common.carry.PickupHandler;
import tschipp.carryon.common.config.ListHandler;
import tschipp.carryon.common.pickupcondition.PickupCondition;
import tschipp.carryon.common.pickupcondition.PickupConditionHandler;
import tschipp.carryon.common.scripting.CarryOnScript;
import tschipp.carryon.common.scripting.ScriptManager;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.function.BiFunction;

import static tschipp.carryon.common.carry.PickupHandler.canCarryGeneral;

@Mixin(value = PickupHandler.class, remap = false)
public class PickupHandlerMixin {

    
    /**
     * @author SeaOfChests
     * @reason Integration with Sea of Chests locked chest system
     */
    @Overwrite
    public static boolean tryPickUpBlock(ServerPlayer player, BlockPos pos, Level level, @Nullable BiFunction<BlockState, BlockPos, Boolean> pickupCallback) {
        if (!canCarryGeneral(player, Vec3.atCenterOf(pos))) {
            return false;
        } else {
            CarryOnData carry = CarryOnDataManager.getCarryData(player);
            BlockEntity blockEntity = level.getBlockEntity(pos);
            BlockState state = level.getBlockState(pos);
            CompoundTag nbt = null;
            if (blockEntity != null) {
                nbt = blockEntity.saveWithId();
            }

            if (!ListHandler.isPermitted(state.getBlock())) {
                return false;
            } else if (state.getDestroySpeed(level, pos) == -1.0F && !player.isCreative() && !Constants.COMMON_CONFIG.settings.pickupUnbreakableBlocks) {
                return false;
            } else if (blockEntity == null && !Constants.COMMON_CONFIG.settings.pickupAllBlocks) {
                return false;
            } else if (blockEntity != null && nbt.contains("Lock") && !nbt.getString("Lock").equals("")) {
                return false;
            } else {
                Optional<PickupCondition> cond = PickupConditionHandler.getPickupCondition(state);
                if (cond.isPresent() && !((PickupCondition)cond.get()).isFulfilled(player)) {
                    return false;
                } else {
                    boolean doPickup = pickupCallback == null ? true : (Boolean)pickupCallback.apply(state, pos);
                    if (!doPickup) {
                        return false;
                    } else {
                        Optional<CarryOnScript> result = ScriptManager.inspectBlock(state, level, pos, nbt);
                        if (result.isPresent()) {
                            CarryOnScript script = (CarryOnScript)result.get();
                            if (!script.fulfillsConditions(player)) {
                                return false;
                            }

                            carry.setActiveScript(script);
                            String cmd = script.scriptEffects().commandInit();
                            if (!cmd.isEmpty()) {
                                Commands var10000 = player.getServer().getCommands();
                                CommandSourceStack var10001 = player.getServer().createCommandSourceStack();
                                String var10002 = player.getGameProfile().getName();
                                var10000.performPrefixedCommand(var10001, "/execute as " + var10002 + " run " + cmd);
                            }
                        }

                        carry.setBlock(state, blockEntity);

                        CarryOnIntegration.onChestPickup(player, pos, level, state);
                        
                        level.removeBlockEntity(pos);
                        level.removeBlock(pos, false);
                        CarryOnDataManager.setCarryData(player, carry);
                        level.playSound((Player)null, pos, state.getSoundType().getHitSound(), SoundSource.BLOCKS, 1.0F, 0.5F);
                        player.swing(InteractionHand.MAIN_HAND, true);
                        return true;
                    }
                }
            }
        }
    }
}