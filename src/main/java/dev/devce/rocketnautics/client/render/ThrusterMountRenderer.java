package dev.devce.rocketnautics.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.foundation.blockEntity.renderer.SafeBlockEntityRenderer;
import dev.devce.rocketnautics.content.blocks.ThrusterMountBlock;
import dev.devce.rocketnautics.content.blocks.ThrusterMountBlockEntity;
import dev.devce.rocketnautics.registry.RocketPartials;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ThrusterMountRenderer extends SafeBlockEntityRenderer<ThrusterMountBlockEntity> {

    public ThrusterMountRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    protected void renderSafe(ThrusterMountBlockEntity be, float partialTicks, PoseStack ms, MultiBufferSource buffer, int light, int overlay) {
    }
}
