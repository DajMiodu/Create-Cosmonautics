package dev.devce.rocketnautics.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.level.LevelAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import javax.annotation.Nullable;

@Mixin(BucketItem.class)
public interface BucketItemAccessor {
    @Invoker("playEmptySound")
    void rocketnautics$playEmptySound(@Nullable Player p_40696_, LevelAccessor p_40697_, BlockPos p_40698_);
}
