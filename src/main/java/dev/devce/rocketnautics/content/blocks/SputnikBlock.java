package dev.devce.rocketnautics.content.blocks;

import com.simibubi.create.foundation.block.IBE;
import dev.devce.rocketnautics.registry.RocketBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class SputnikBlock extends BaseEntityBlock implements IBE<SputnikBlockEntity> {
    public static final com.mojang.serialization.MapCodec<SputnikBlock> CODEC = simpleCodec(SputnikBlock::new);

    public SputnikBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected com.mojang.serialization.MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        tooltip.add(Component.literal("someone said this \"black pokeball block\"").withStyle(net.minecraft.ChatFormatting.GRAY));
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public Class<SputnikBlockEntity> getBlockEntityClass() {
        return SputnikBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends SputnikBlockEntity> getBlockEntityType() {
        return RocketBlockEntities.SPUTNIK.get();
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return createTickerHelper(type, RocketBlockEntities.SPUTNIK.get(), SputnikBlockEntity::tick);
    }
    
    @Override
    public net.minecraft.world.InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, net.minecraft.world.entity.player.Player player, net.minecraft.world.phys.BlockHitResult hitResult) {
        if (level.isClientSide) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof SputnikBlockEntity sputnik) {
                dev.devce.rocketnautics.client.SputnikClientUI.openNodeScreen(sputnik);
            }
        }
        return net.minecraft.world.InteractionResult.SUCCESS;
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof SputnikBlockEntity sputnik) {
                if (!level.isClientSide() && level.getServer() != null) {
                    var radioMgr = dev.devce.rocketnautics.api.radio.RadioNetworkManager.getInstance(level.getServer());
                    for (var node : sputnik.graph.getNodes()) {
                        if (node.getTypeId().getPath().equals("radio")) {
                            radioMgr.removeNode(node.getId());
                        }
                    }
                }
            }
            super.onRemove(state, level, pos, newState, isMoving);
        }
    }
}
