package dev.devce.rocketnautics.content.blocks;

import com.mojang.serialization.MapCodec;
import com.simibubi.create.content.equipment.wrench.IWrenchable;
import com.simibubi.create.foundation.block.IBE;
import dev.devce.rocketnautics.registry.RocketBlockEntities;
import dev.devce.rocketnautics.registry.RocketBlocks;
import dev.devce.rocketnautics.registry.RocketItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.BlockHitResult;

public class ThrusterMountBlock extends DirectionalBlock implements IWrenchable, IBE<ThrusterMountBlockEntity> {
    public static final MapCodec<ThrusterMountBlock> CODEC = simpleCodec(ThrusterMountBlock::new);

    public ThrusterMountBlock(Properties properties) {
        super(properties);
        registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.UP));
    }

    @Override
    protected MapCodec<? extends DirectionalBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction facing = context.getNearestLookingDirection().getOpposite();
        if (context.getPlayer() != null && context.getPlayer().isShiftKeyDown()) {
            facing = facing.getOpposite();
        }
        return this.defaultBlockState().setValue(FACING, facing);
    }

    @Override
    public Class<ThrusterMountBlockEntity> getBlockEntityClass() {
        return ThrusterMountBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends ThrusterMountBlockEntity> getBlockEntityType() {
        return RocketBlockEntities.THRUSTER_MOUNT.get();
    }

    @org.jetbrains.annotations.Nullable
    @Override
    public <T extends BlockEntity> net.minecraft.world.level.block.entity.BlockEntityTicker<T> getTicker(Level level, BlockState state, net.minecraft.world.level.block.entity.BlockEntityType<T> type) {
        return type == getBlockEntityType() ? (level1, pos, state1, blockEntity) -> ThrusterMountBlockEntity.tick(level1, pos, state1, (ThrusterMountBlockEntity) blockEntity) : null;
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof ThrusterMountBlockEntity mountBe)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        ItemStack heldItem = player.getItemInHand(hand);
        Direction facing = state.getValue(FACING);
        Direction exhaustDir = facing.getOpposite();
        
        // 1. Fluid Pipe attachment
        if (heldItem.is(com.simibubi.create.AllBlocks.FLUID_PIPE.get().asItem())) {
            if (!mountBe.hasPipes) {
                BlockPos pipePos = pos.relative(exhaustDir);
                if (level.getBlockState(pipePos).canBeReplaced()) {
                    if (!level.isClientSide) {
                        level.setBlock(pipePos, RocketBlocks.ENGINE_PIPES.getDefaultState()
                            .setValue(EnginePipesBlock.FACING, exhaustDir)
                            .setValue(EnginePipesBlock.PIPE_TYPE, mountBe.pipeType), 3);
                        mountBe.hasPipes = true;
                        if (!player.isCreative()) {
                            heldItem.shrink(1);
                        }
                        level.playSound(null, pos, SoundEvents.COPPER_PLACE, SoundSource.BLOCKS, 1.0f, 1.0f);
                        mountBe.setChanged();
                        mountBe.sendData();
                    }
                    return ItemInteractionResult.sidedSuccess(level.isClientSide);
                }
            }
        }

        // 2. Copper Nozzle attachment
        if (heldItem.is(RocketItems.COPPER_NOZZLE.get())) {
            if (mountBe.nozzleType == 0) {
                BlockPos nozzlePos = pos.relative(exhaustDir, 2);
                if (level.getBlockState(nozzlePos).canBeReplaced()) {
                    if (!level.isClientSide) {
                        level.setBlock(nozzlePos, RocketBlocks.ENGINE_NOZZLE.getDefaultState()
                            .setValue(EngineNozzleBlock.FACING, exhaustDir)
                            .setValue(EngineNozzleBlock.NOZZLE_TYPE, 1), 3);
                        mountBe.nozzleType = 1;
                        if (!player.isCreative()) {
                            heldItem.shrink(1);
                        }
                        level.playSound(null, pos, SoundEvents.METAL_PLACE, SoundSource.BLOCKS, 1.0f, 1.0f);
                        mountBe.setChanged();
                        mountBe.sendData();
                    }
                    return ItemInteractionResult.sidedSuccess(level.isClientSide);
                }
            }
        }

        // 3. Titanium Nozzle attachment
        if (heldItem.is(RocketItems.TITANIUM_NOZZLE.get())) {
            if (mountBe.nozzleType == 0) {
                BlockPos nozzlePos = pos.relative(exhaustDir, 2);
                if (level.getBlockState(nozzlePos).canBeReplaced()) {
                    if (!level.isClientSide) {
                        level.setBlock(nozzlePos, RocketBlocks.ENGINE_NOZZLE.getDefaultState()
                            .setValue(EngineNozzleBlock.FACING, exhaustDir)
                            .setValue(EngineNozzleBlock.NOZZLE_TYPE, 2), 3);
                        mountBe.nozzleType = 2;
                        if (!player.isCreative()) {
                            heldItem.shrink(1);
                        }
                        level.playSound(null, pos, SoundEvents.METAL_PLACE, SoundSource.BLOCKS, 1.0f, 1.0f);
                        mountBe.setChanged();
                        mountBe.sendData();
                    }
                    return ItemInteractionResult.sidedSuccess(level.isClientSide);
                }
            }
        }

        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Override
    public InteractionResult onSneakWrenched(BlockState state, UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockEntity be = level.getBlockEntity(pos);
        Direction facing = state.getValue(FACING);
        Direction exhaustDir = facing.getOpposite();

        if (be instanceof ThrusterMountBlockEntity mountBe) {
            // Drop nozzle first
            if (mountBe.nozzleType > 0) {
                if (!level.isClientSide) {
                    BlockPos nozzlePos = pos.relative(exhaustDir, 2);
                    if (level.getBlockState(nozzlePos).is(RocketBlocks.ENGINE_NOZZLE.get())) {
                        level.destroyBlock(nozzlePos, true);
                    } else {
                        ItemStack dropStack = mountBe.nozzleType == 1 
                            ? new ItemStack(RocketItems.COPPER_NOZZLE.get()) 
                            : new ItemStack(RocketItems.TITANIUM_NOZZLE.get());
                        dropItem(level, pos, dropStack);
                    }
                    mountBe.nozzleType = 0;
                    level.playSound(null, pos, SoundEvents.METAL_BREAK, SoundSource.BLOCKS, 1.0f, 1.0f);
                    mountBe.setChanged();
                    mountBe.sendData();
                }
                return InteractionResult.sidedSuccess(level.isClientSide);
            }
            // Drop pipes second
            if (mountBe.hasPipes) {
                if (!level.isClientSide) {
                    BlockPos pipePos = pos.relative(exhaustDir);
                    if (level.getBlockState(pipePos).is(RocketBlocks.ENGINE_PIPES.get())) {
                        level.destroyBlock(pipePos, true);
                    } else {
                        dropItem(level, pos, new ItemStack(com.simibubi.create.AllBlocks.FLUID_PIPE.get().asItem()));
                    }
                    mountBe.hasPipes = false;
                    level.playSound(null, pos, SoundEvents.COPPER_BREAK, SoundSource.BLOCKS, 1.0f, 1.0f);
                    mountBe.setChanged();
                    mountBe.sendData();
                }
                return InteractionResult.sidedSuccess(level.isClientSide);
            }
        }

        return IWrenchable.super.onSneakWrenched(state, context);
    }

    private void dropItem(Level level, BlockPos pos, ItemStack stack) {
        if (!level.isClientSide && !stack.isEmpty()) {
            double x = pos.getX() + 0.5;
            double y = pos.getY() + 0.5;
            double z = pos.getZ() + 0.5;
            ItemEntity entity = new ItemEntity(level, x, y, z, stack);
            entity.setDefaultPickUpDelay();
            level.addFreshEntity(entity);
        }
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (state.getBlock() != newState.getBlock()) {
            Direction facing = state.getValue(FACING);
            Direction exhaustDir = facing.getOpposite();
            BlockPos pipePos = pos.relative(exhaustDir);
            BlockPos nozzlePos = pos.relative(exhaustDir, 2);

            if (level.getBlockState(pipePos).is(RocketBlocks.ENGINE_PIPES.get())) {
                level.destroyBlock(pipePos, true);
            }
            if (level.getBlockState(nozzlePos).is(RocketBlocks.ENGINE_NOZZLE.get())) {
                level.destroyBlock(nozzlePos, true);
            }

            IBE.onRemove(state, level, pos, newState);
        }
    }
}
