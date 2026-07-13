package dev.devce.rocketnautics.content.blocks.hose.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.foundation.blockEntity.renderer.SafeBlockEntityRenderer;
import dev.devce.rocketnautics.content.blocks.hose.HoseAnchorBlockEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;

public class HoseAnchorRenderer extends SafeBlockEntityRenderer<HoseAnchorBlockEntity> {
    public HoseAnchorRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    protected void renderSafe(HoseAnchorBlockEntity be, float partialTicks, PoseStack ms, MultiBufferSource buffer, int light, int overlay) {
        HoseStrandRenderer.render(be, be.getRopeHolder(), partialTicks, ms, buffer);
    }
}
