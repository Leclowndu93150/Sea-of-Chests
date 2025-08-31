package com.leclowndu93150.sea_of_chests.mixin.carryon;

import com.leclowndu93150.sea_of_chests.capability.ChunkLockedChestsProvider;
import com.leclowndu93150.sea_of_chests.integration.carryon.CarryOnChestTracker;
import com.leclowndu93150.sea_of_chests.integration.carryon.CarryOnIntegration;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import tschipp.carryon.common.carry.CarryOnData;
import tschipp.carryon.common.carry.CarryOnDataManager;
import tschipp.carryon.common.carry.PlacementHandler;
import tschipp.carryon.common.config.ListHandler;
import tschipp.carryon.common.scripting.CarryOnScript;

import javax.annotation.Nullable;
import java.util.function.BiFunction;

@Mixin(value = PlacementHandler.class, remap = false)
public abstract class PlacementHandlerMixin {

    @Shadow
    protected static <T extends Comparable<T>> BlockState updateProperty(BlockState state, BlockState otherState, Property<T> prop) {
        return null;
    }

    /**
     * @author SeaOfChests
     * @reason Integration with Sea of Chests locked chest system - prevent double chest merging with locked chests
     */
    @Overwrite
     public static BlockState getPlacementState(BlockState state, ServerPlayer player, BlockPlaceContext context, BlockPos pos) {
        BlockState placementState = state.getBlock().getStateForPlacement(context);

        if (placementState != null && state.getBlock() instanceof ChestBlock) {
            Level level = context.getLevel();
            BlockPos clickedPos = context.getClickedPos();
            
            if (placementState.hasProperty(ChestBlock.TYPE)) {
                ChestType chestType = placementState.getValue(ChestBlock.TYPE);
                
                if (chestType != ChestType.SINGLE) {
                    Direction facing = placementState.getValue(ChestBlock.FACING);
                    BlockPos adjacentPos = null;
                    
                    if (chestType == ChestType.LEFT) {
                        adjacentPos = clickedPos.relative(facing.getClockWise());
                    } else if (chestType == ChestType.RIGHT) {
                        adjacentPos = clickedPos.relative(facing.getCounterClockWise());
                    }
                    
                    if (adjacentPos != null && !level.isClientSide()) {
                        final BlockPos finalAdjacentPos = adjacentPos;
                        LevelChunk chunk = level.getChunkAt(finalAdjacentPos);
                        boolean[] isLocked = new boolean[1];
                        
                        chunk.getCapability(ChunkLockedChestsProvider.CHUNK_LOCKED_CHESTS_CAPABILITY).ifPresent(chunkLockedChests -> {
                            if (chunkLockedChests.isLocked(finalAdjacentPos)) {
                                isLocked[0] = true;
                            }
                        });
                        
                        if (isLocked[0]) {
                            placementState = placementState.setValue(ChestBlock.TYPE, ChestType.SINGLE);
                        }
                        
                        if (context.getPlayer() instanceof ServerPlayer serverPlayer) {
                            boolean wasCarriedChestLocked = CarryOnChestTracker.wasChestLocked(serverPlayer);
                            if (wasCarriedChestLocked) {
                                placementState = placementState.setValue(ChestBlock.TYPE, ChestType.SINGLE);
                            }
                        }
                    }
                }
            }
        }
        
        if (placementState == null || placementState.getBlock() != state.getBlock()) {
            placementState = state;
        }

        for(Property<?> prop : placementState.getProperties()) {
            if (prop instanceof DirectionProperty) {
                state = updateProperty(state, placementState, prop);
            }

            if (prop.getValueClass() == Direction.Axis.class) {
                state = updateProperty(state, placementState, prop);
            }

            if (ListHandler.isPropertyException(prop)) {
                state = updateProperty(state, placementState, prop);
            }
        }

        BlockState updatedState = Block.updateFromNeighbourShapes(state, player.level(), pos);
        if (updatedState.getBlock() == state.getBlock()) {
            state = updatedState;
        }

        if (placementState.hasProperty(BlockStateProperties.WATERLOGGED) && state.hasProperty(BlockStateProperties.WATERLOGGED)) {
            state = (BlockState)state.setValue(BlockStateProperties.WATERLOGGED, (Boolean)placementState.getValue(BlockStateProperties.WATERLOGGED));
        }

        return state;
    }
    
    /**
     * @author SeaOfChests
     * @reason Integration with Sea of Chests locked chest system - handle locked chest placement
     */
    @Overwrite
    public static boolean tryPlaceBlock(ServerPlayer player, BlockPos pos, Direction facing, @Nullable BiFunction<BlockPos, BlockState, Boolean> placementCallback) {
        CarryOnData carry = CarryOnDataManager.getCarryData(player);
        if (!carry.isCarrying(CarryOnData.CarryType.BLOCK)) {
            return false;
        } else if (player.tickCount == carry.getTick()) {
            return false;
        } else {
            Level level = player.level();
            BlockState state = carry.getBlock();
            BlockPlaceContext context = new BlockPlaceContext(player, InteractionHand.MAIN_HAND, ItemStack.EMPTY, BlockHitResult.miss(player.position(), facing, pos));
            if (!level.getBlockState(pos).canBeReplaced(context)) {
                pos = pos.relative(facing);
            }

            context = new BlockPlaceContext(player, InteractionHand.MAIN_HAND, ItemStack.EMPTY, BlockHitResult.miss(player.position(), facing, pos));
            BlockEntity blockEntity = carry.getBlockEntity(pos);
            state = getPlacementState(state, player, context, pos);
            boolean canPlace = state.canSurvive(level, pos) && level.mayInteract(player, pos) && level.getBlockState(pos).canBeReplaced(context) && level.isUnobstructed(state, pos, CollisionContext.of(player));
            if (!canPlace) {
                level.playSound((Player)null, player.getX(), player.getY(), player.getZ(), SoundEvents.LAVA_POP, SoundSource.PLAYERS, 0.5F, 0.5F);
                return false;
            } else {
                boolean doPlace = placementCallback == null || (Boolean)placementCallback.apply(pos, state);
                if (!doPlace) {
                    return false;
                } else {
                    if (carry.getActiveScript().isPresent()) {
                        CarryOnScript.ScriptEffects effects = ((CarryOnScript)carry.getActiveScript().get()).scriptEffects();
                        String cmd = effects.commandPlace();
                        if (!cmd.isEmpty()) {
                            Commands var10000 = player.getServer().getCommands();
                            CommandSourceStack var10001 = player.getServer().createCommandSourceStack();
                            String var10002 = player.getGameProfile().getName();
                            var10000.performPrefixedCommand(var10001, "/execute as " + var10002 + " run " + cmd);
                        }
                    }

                    level.setBlockAndUpdate(pos, state);
                    if (blockEntity != null) {
                        blockEntity.setBlockState(state);
                        level.setBlockEntity(blockEntity);
                    }

                    level.updateNeighborsAt(pos.relative(Direction.DOWN), level.getBlockState(pos.relative(Direction.DOWN)).getBlock());

                    CarryOnIntegration.onChestPlacement(player, pos, state);
                    
                    carry.clear();
                    CarryOnDataManager.setCarryData(player, carry);
                    player.playSound(state.getSoundType().getPlaceSound(), 1.0F, 0.5F);
                    level.playSound((Player)null, pos, state.getSoundType().getPlaceSound(), SoundSource.BLOCKS, 1.0F, 0.5F);
                    player.swing(InteractionHand.MAIN_HAND, true);
                    return true;
                }
            }
        }
    }
}