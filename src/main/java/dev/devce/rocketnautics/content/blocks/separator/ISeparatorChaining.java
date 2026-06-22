package dev.devce.rocketnautics.content.blocks.separator;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public interface ISeparatorChaining {

    boolean shouldConnectTo(Level level, BlockPos pos, BlockState state, Direction connectionDirection);

    void connectTo(Level level, BlockPos pos, BlockState state, Direction connectionDirection);

    void triggerChainReaction(Level level, BlockPos pos);
}
