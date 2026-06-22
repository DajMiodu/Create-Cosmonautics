package dev.devce.rocketnautics.content.blocks.separator;

import com.mojang.serialization.MapCodec;
import com.simibubi.create.content.equipment.wrench.IWrenchable;
import com.simibubi.create.content.kinetics.base.BlockBreakingKineticBlockEntity;
import com.simibubi.create.foundation.utility.BlockHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

import java.util.EnumMap;

public class SeparatorChargeBlock extends DirectionalBlock implements IWrenchable, ISeparatorChaining {
    public static final EnumMap<Direction, BooleanProperty> LINKS = SeparatorBlock.LINKS;

    public static final MapCodec<SeparatorChargeBlock> CODEC = simpleCodec(SeparatorChargeBlock::new);

    public SeparatorChargeBlock(Properties p_52591_) {
        super(p_52591_);
        BlockState state = this.stateDefinition.any().setValue(FACING, Direction.UP);
        for (BooleanProperty p : LINKS.values()) {
            state = state.setValue(p, false);
        }
        registerDefaultState(state);
    }

    @Override
    protected @NotNull MapCodec<? extends DirectionalBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
        LINKS.values().forEach(builder::add);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction facing = context.getClickedFace();
        BlockPos clickedPos = context.getClickedPos().relative(context.getClickedFace().getOpposite());
        BlockState clickedState = context.getLevel().getBlockState(clickedPos);

        if (clickedState.getBlock() == this && clickedState.getValue(FACING).getAxis().test(facing)) {
            facing = clickedState.getValue(FACING).getOpposite();
        }

        if (context.getPlayer() != null && context.getPlayer().isShiftKeyDown())
            facing = context.getNearestLookingDirection().getOpposite();

        BlockState result = this.defaultBlockState().setValue(FACING, facing);

        for (Direction dir : Direction.values()) {
            BlockPos n = context.getClickedPos().relative(dir);
            BlockState neighbor = context.getLevel().getBlockState(n);
            if (neighbor.getBlock() instanceof ISeparatorChaining chaining) {
                if (chaining.shouldConnectTo(context.getLevel(), n, neighbor, dir.getOpposite())) {
                    result = result.setValue(LINKS.get(dir), true);
                }
            }
        }

        return result;
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean p_60570_) {
        for (Direction dir : Direction.values()) {
            if (state.getValue(LINKS.get(dir))) {
                BlockPos n = pos.relative(dir);
                BlockState neighbor = level.getBlockState(n);
                if (neighbor.getBlock() instanceof ISeparatorChaining chaining) {
                    chaining.connectTo(level, n, neighbor, dir.getOpposite());
                }
            }
        }
    }

    @Override
    public boolean shouldConnectTo(Level level, BlockPos pos, BlockState state, Direction connectionDirection) {
        return state.getValue(FACING) != connectionDirection;
    }

    @Override
    public void connectTo(Level level, BlockPos pos, BlockState state, Direction connectionDirection) {
        BooleanProperty prop = LINKS.get(connectionDirection);
        if (!state.getValue(prop)) {
            level.setBlock(pos, state.setValue(prop, true), 2);
        }
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
        if (!level.isClientSide) {
            if (level.hasNeighborSignal(pos)) {
                triggerChainReaction(level, pos);
            }
            Direction incomingDir = Direction.getNearest(pos.getX() - fromPos.getX(), pos.getY() - fromPos.getY(), pos.getZ() - fromPos.getZ());
            BooleanProperty prop = LINKS.get(incomingDir.getOpposite());
            if (state.getValue(prop)) {
                if (!(level.getBlockState(fromPos).getBlock() instanceof ISeparatorChaining)) {
                    level.setBlock(pos, state.setValue(prop, false), 2);
                }
            }
        }
    }

    public void triggerChainReaction(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (!(state.getBlock() instanceof ISeparatorChaining)) return;

        level.removeBlock(pos, false);
        level.playSound(null, pos, SoundEvents.GENERIC_EXPLODE.value(), SoundSource.BLOCKS, 5.0f, 0.8f + level.random.nextFloat() * 0.5f);

        if (level instanceof ServerLevel serverLevel) {
            for (int i = 0; i < 5; i++) {
                double px = pos.getX() + 0.5 + (level.random.nextDouble() - 0.5);
                double py = pos.getY() + 0.5 + (level.random.nextDouble() - 0.5);
                double pz = pos.getZ() + 0.5 + (level.random.nextDouble() - 0.5);
                serverLevel.sendParticles(ParticleTypes.LARGE_SMOKE, px, py, pz, 1, 0, 0, 0, 0.05);
                serverLevel.sendParticles(ParticleTypes.FLAME, px, py, pz, 1, 0, 0, 0, 0.02);
            }
            for (int i = 0; i < 5; i++) {
                Vector3f step = state.getValue(FACING).getOpposite().step().mul(level.random.nextFloat() + 0.5f);
                double px = step.x() + pos.getX() + 0.5 + (level.random.nextDouble() - 0.5) / 2;
                double py = step.y() + pos.getY() + 0.5 + (level.random.nextDouble() - 0.5) / 2;
                double pz = step.z() + pos.getZ() + 0.5 + (level.random.nextDouble() - 0.5) / 2;
                serverLevel.sendParticles(ParticleTypes.EXPLOSION, px, py, pz, 1, 0, 0, 0, 0.05);
            }
        }
        BlockPos destroy = pos.relative(state.getValue(FACING).getOpposite());
        BlockState destroyState = level.getBlockState(destroy);
        if (BlockBreakingKineticBlockEntity.isBreakable(destroyState, destroyState.getDestroySpeed(level, destroy))) {
            BlockHelper.destroyBlock(level, destroy, 0f);
        }

        for (Direction direction : Direction.values()) {
            if (state.getValue(LINKS.get(direction))) {
                BlockPos neighborPos = pos.relative(direction);
                BlockState neighborState = level.getBlockState(neighborPos);
                if (neighborState.getBlock() instanceof SeparatorBlock) {
                    triggerChainReaction(level, neighborPos);
                }
            }
        }
    }
}
