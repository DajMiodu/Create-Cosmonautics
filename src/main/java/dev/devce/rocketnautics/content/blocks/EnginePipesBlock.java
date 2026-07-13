package dev.devce.rocketnautics.content.blocks;

import com.mojang.serialization.MapCodec;
import com.simibubi.create.content.equipment.wrench.IWrenchable;
import com.simibubi.create.foundation.block.IBE;
import dev.devce.rocketnautics.registry.RocketBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

public class EnginePipesBlock extends DirectionalBlock implements IWrenchable, IBE<EnginePipesBlockEntity> {
    public static final MapCodec<EnginePipesBlock> CODEC = simpleCodec(EnginePipesBlock::new);
    public static final IntegerProperty PIPE_TYPE = IntegerProperty.create("pipe_type", 0, 3);

    public EnginePipesBlock(Properties properties) {
        super(properties);
        registerDefaultState(this.stateDefinition.any()
            .setValue(FACING, Direction.UP)
            .setValue(PIPE_TYPE, 0));
    }

    @Override
    public InteractionResult onWrenched(BlockState state, UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        if (level.isClientSide) return InteractionResult.SUCCESS;

        int currentType = state.getValue(PIPE_TYPE);
        int nextType = (currentType + 1) % 4;
        BlockState newState = state.setValue(PIPE_TYPE, nextType);
        level.setBlock(pos, newState, 3);

        // Update parent ThrusterMountBlockEntity if present
        Direction facing = state.getValue(FACING);
        BlockPos mountPos = pos.relative(facing.getOpposite());
        if (level.getBlockEntity(mountPos) instanceof ThrusterMountBlockEntity mountBe) {
            mountBe.pipeType = nextType;
            mountBe.setChanged();
            mountBe.sendData();
        }

        return InteractionResult.SUCCESS;
    }

    @Override
    protected MapCodec<? extends DirectionalBlock> codec() {
        return CODEC;
    }

    @Override
    public Class<EnginePipesBlockEntity> getBlockEntityClass() {
        return EnginePipesBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends EnginePipesBlockEntity> getBlockEntityType() {
        return RocketBlockEntities.ENGINE_PIPES.get();
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getNearestLookingDirection().getOpposite());
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, PIPE_TYPE);
    }

    @Override
    public net.minecraft.world.level.block.RenderShape getRenderShape(BlockState state) {
        return net.minecraft.world.level.block.RenderShape.INVISIBLE;
    }
}
