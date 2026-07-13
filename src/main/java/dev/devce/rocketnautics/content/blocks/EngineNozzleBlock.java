package dev.devce.rocketnautics.content.blocks;

import com.mojang.serialization.MapCodec;
import com.simibubi.create.foundation.block.IBE;
import dev.devce.rocketnautics.registry.RocketBlockEntities;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

public class EngineNozzleBlock extends DirectionalBlock implements IBE<EngineNozzleBlockEntity> {
    public static final MapCodec<EngineNozzleBlock> CODEC = simpleCodec(EngineNozzleBlock::new);
    public static final IntegerProperty NOZZLE_TYPE = IntegerProperty.create("nozzle_type", 1, 2);

    public EngineNozzleBlock(Properties properties) {
        super(properties);
        registerDefaultState(this.stateDefinition.any()
            .setValue(FACING, Direction.UP)
            .setValue(NOZZLE_TYPE, 1));
    }

    @Override
    protected MapCodec<? extends DirectionalBlock> codec() {
        return CODEC;
    }

    @Override
    public Class<EngineNozzleBlockEntity> getBlockEntityClass() {
        return EngineNozzleBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends EngineNozzleBlockEntity> getBlockEntityType() {
        return RocketBlockEntities.ENGINE_NOZZLE.get();
    }

    @org.jetbrains.annotations.Nullable
    @Override
    public <T extends net.minecraft.world.level.block.entity.BlockEntity> net.minecraft.world.level.block.entity.BlockEntityTicker<T> getTicker(net.minecraft.world.level.Level level, BlockState state, BlockEntityType<T> type) {
        return type == getBlockEntityType() ? (level1, pos, state1, blockEntity) -> EngineNozzleBlockEntity.tick(level1, pos, state1, (EngineNozzleBlockEntity) blockEntity) : null;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getNearestLookingDirection().getOpposite());
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, NOZZLE_TYPE);
    }

    @Override
    public net.minecraft.world.level.block.RenderShape getRenderShape(BlockState state) {
        return net.minecraft.world.level.block.RenderShape.INVISIBLE;
    }
}
