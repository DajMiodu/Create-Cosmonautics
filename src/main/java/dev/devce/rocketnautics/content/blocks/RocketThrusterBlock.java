package dev.devce.rocketnautics.content.blocks;

import dev.devce.rocketnautics.registry.RocketBlockEntities;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class RocketThrusterBlock extends AbstractRocketThrusterBlock<RocketThrusterBlockEntity> {
    public static final com.mojang.serialization.MapCodec<RocketThrusterBlock> CODEC = simpleCodec(RocketThrusterBlock::new);

    public RocketThrusterBlock(Properties properties) {
        super(properties);
        registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.UP));
    }

    @Override
    protected com.mojang.serialization.MapCodec<? extends DirectionalBlock> codec() {
        return CODEC;
    }



    @Override
    public Class<RocketThrusterBlockEntity> getBlockEntityClass() {
        return RocketThrusterBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends RocketThrusterBlockEntity> getBlockEntityType() {
        return RocketBlockEntities.ROCKET_THRUSTER.get();
    }
}
