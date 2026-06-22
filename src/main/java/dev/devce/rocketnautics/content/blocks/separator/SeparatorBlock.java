package dev.devce.rocketnautics.content.blocks.separator;

import com.mojang.serialization.MapCodec;
import com.simibubi.create.content.equipment.wrench.IWrenchable;
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
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;

import java.util.EnumMap;
import java.util.Map;

public class SeparatorBlock extends DirectionalBlock implements IWrenchable, ISeparatorChaining {
    public static final EnumMap<Direction, BooleanProperty> LINKS = new EnumMap<>(Map.of(
            Direction.UP, BlockStateProperties.UP,
            Direction.DOWN, BlockStateProperties.DOWN,
            Direction.EAST, BlockStateProperties.EAST,
            Direction.WEST, BlockStateProperties.WEST,
            Direction.NORTH, BlockStateProperties.NORTH,
            Direction.SOUTH, BlockStateProperties.SOUTH
    ));

    public static final MapCodec<SeparatorBlock> CODEC = simpleCodec(SeparatorBlock::new);

    public SeparatorBlock(Properties properties) {
        super(properties);
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
        return true;
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
        level.playSound(null, pos, SoundEvents.GENERIC_EXPLODE.value(), SoundSource.BLOCKS, 5.0f, 1.5f + level.random.nextFloat());

        if (level instanceof ServerLevel serverLevel) {
            for (int i = 0; i < 5; i++) {
                double px = pos.getX() + 0.5 + (level.random.nextDouble() - 0.5);
                double py = pos.getY() + 0.5 + (level.random.nextDouble() - 0.5);
                double pz = pos.getZ() + 0.5 + (level.random.nextDouble() - 0.5);
                serverLevel.sendParticles(ParticleTypes.LARGE_SMOKE, px, py, pz, 1, 0, 0, 0, 0.05);
                serverLevel.sendParticles(ParticleTypes.FLAME, px, py, pz, 1, 0, 0, 0, 0.02);
            }
        }

        for (Direction direction : Direction.values()) {
            if (state.getValue(FACING).getAxis().test(direction) || state.getValue(LINKS.get(direction))) {
                BlockPos neighborPos = pos.relative(direction);
                BlockState neighborState = level.getBlockState(neighborPos);
                if (neighborState.getBlock() instanceof ISeparatorChaining sep) {
                    sep.triggerChainReaction(level, neighborPos);
                }
            }
        }
    }

    @Override
    public @NotNull VoxelShape getShape(BlockState state, net.minecraft.world.level.BlockGetter level, BlockPos pos, net.minecraft.world.phys.shapes.CollisionContext context) {
        Direction direction = state.getValue(FACING);
        return switch (direction) {
            case UP -> Block.box(2.0, 0.0, 2.0, 14.0, 16.0, 14.0);
            case DOWN -> Block.box(2.0, 0.0, 2.0, 14.0, 16.0, 14.0);
            case NORTH -> Block.box(2.0, 2.0, 0.0, 14.0, 14.0, 16.0);
            case SOUTH -> Block.box(2.0, 2.0, 0.0, 14.0, 14.0, 16.0);
            case EAST -> Block.box(0.0, 2.0, 2.0, 16.0, 14.0, 14.0);
            case WEST -> Block.box(0.0, 2.0, 2.0, 16.0, 14.0, 14.0);
        };
    }

    @Override
    public @NotNull VoxelShape getCollisionShape(BlockState state, net.minecraft.world.level.BlockGetter level, BlockPos pos, net.minecraft.world.phys.shapes.CollisionContext context) {
        return this.getShape(state, level, pos, context);
    }
}
