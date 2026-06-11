package dev.devce.rocketnautics.content.blocks.hose;

import com.simibubi.create.foundation.block.IBE;
import dev.devce.rocketnautics.registry.RocketBlockEntities;
import dev.simulated_team.simulated.content.blocks.rope.RopeHolderBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class HoseAnchorBlock extends Block implements IBE<HoseAnchorBlockEntity>, RopeHolderBlock<HoseAnchorBlockEntity> {
    public static final DirectionProperty FACING = DirectionalBlock.FACING;

    // Voxel shapes for each facing direction (where FACING is pointing AWAY from the connected pipe block)
    private static final VoxelShape UP_SHAPE = Shapes.or(
            Block.box(2.0, 0.0, 2.0, 14.0, 3.0, 14.0),
            Block.box(4.0, 3.0, 4.0, 12.0, 8.0, 12.0)
    );
    private static final VoxelShape DOWN_SHAPE = Shapes.or(
            Block.box(2.0, 13.0, 2.0, 14.0, 16.0, 14.0),
            Block.box(4.0, 8.0, 4.0, 12.0, 13.0, 12.0)
    );
    private static final VoxelShape NORTH_SHAPE = Shapes.or(
            Block.box(2.0, 2.0, 13.0, 14.0, 14.0, 16.0),
            Block.box(4.0, 4.0, 8.0, 12.0, 12.0, 13.0)
    );
    private static final VoxelShape SOUTH_SHAPE = Shapes.or(
            Block.box(2.0, 2.0, 0.0, 14.0, 14.0, 3.0),
            Block.box(4.0, 4.0, 3.0, 12.0, 12.0, 8.0)
    );
    private static final VoxelShape WEST_SHAPE = Shapes.or(
            Block.box(13.0, 2.0, 2.0, 16.0, 14.0, 14.0),
            Block.box(8.0, 4.0, 4.0, 13.0, 12.0, 12.0)
    );
    private static final VoxelShape EAST_SHAPE = Shapes.or(
            Block.box(0.0, 2.0, 2.0, 3.0, 14.0, 14.0),
            Block.box(3.0, 4.0, 4.0, 8.0, 12.0, 12.0)
    );

    public HoseAnchorBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.UP));
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getNearestLookingDirection().getOpposite());
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
        return switch (state.getValue(FACING)) {
            case UP -> UP_SHAPE;
            case DOWN -> DOWN_SHAPE;
            case NORTH -> NORTH_SHAPE;
            case SOUTH -> SOUTH_SHAPE;
            case WEST -> WEST_SHAPE;
            case EAST -> EAST_SHAPE;
        };
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
        return Shapes.empty();
    }

    @Override
    public Class<HoseAnchorBlockEntity> getBlockEntityClass() {
        return HoseAnchorBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends HoseAnchorBlockEntity> getBlockEntityType() {
        return RocketBlockEntities.HOSE_ANCHOR.get();
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        IBE.onRemove(state, level, pos, newState);
    }
}
