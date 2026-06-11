package dev.devce.rocketnautics.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.foundation.blockEntity.renderer.SafeBlockEntityRenderer;
import dev.devce.rocketnautics.content.blocks.EngineNozzleBlock;
import dev.devce.rocketnautics.content.blocks.EngineNozzleBlockEntity;
import dev.devce.rocketnautics.registry.RocketPartials;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class EngineNozzleRenderer extends SafeBlockEntityRenderer<EngineNozzleBlockEntity> {

    public EngineNozzleRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    protected void renderSafe(EngineNozzleBlockEntity be, float partialTicks, PoseStack ms,
                              MultiBufferSource buffer, int light, int overlay) {
        BlockState blockState = be.getBlockState();
        if (!(blockState.getBlock() instanceof EngineNozzleBlock)) return;

        Direction facing = blockState.getValue(EngineNozzleBlock.FACING);
        float heat = be.smoothedHeat;

        ms.pushPose();

        // Subtle high-heat vibration
        if (heat > 0.4f) {
            float shake = 0.005f * (heat - 0.4f);
            net.minecraft.util.RandomSource rand = be.getLevel().getRandom();
            ms.translate(
                (rand.nextFloat() - 0.5f) * shake,
                (rand.nextFloat() - 0.5f) * shake,
                (rand.nextFloat() - 0.5f) * shake
            );
        }

        ms.translate(0.5, 0.5, 0.5);
        ms.mulPose(facing.getRotation());
        ms.mulPose(com.mojang.math.Axis.XP.rotationDegrees(180));
        ms.translate(-0.5, -0.5, -0.5);

        BakedModel nozzleModel = RocketPartials.engineNozzle;
        if (nozzleModel == null) {
            ms.popPose();
            return;
        }

        // ── Normalise: fully red by heat = 0.8 ───────────────────────────────
        float t = Mth.clamp(heat / 0.8f, 0f, 1f);  // 0→1 in the first 0.8 units of heat

        // Cold = white(1,1,1), hot = pure red (1,0,0)
        float r = 1.0f;
        float g = Mth.clamp(1.0f - t * 3.0f, 0f, 1f);
        float b = Mth.clamp(1.0f - t * 4.0f, 0f, 1f);

        // ── Full-bright when hot ──────────────────────────────────────────────
        int renderLight = heat > 0.3f ? 0xF000F0 : light; // 0xF000F0 = FULL_BRIGHT

        // ── Pass 1: tinted model (gives white→yellow→orange→red on the texture) ─
        Minecraft.getInstance().getBlockRenderer().getModelRenderer().renderModel(
                ms.last(),
                buffer.getBuffer(RenderType.cutout()),
                blockState,
                nozzleModel,
                r, g, b,
                renderLight,
                overlay);

        // Pass 2 was removed because RenderType.cutout() doesn't support transparency/blending,
        // which caused the dark-light pass to overwrite the first pass, turning the nozzle black.

        ms.popPose();
    }

    /** Scales both light channels of a packed light int by [0,1]. */
    private static int scaleLight(int packed, float factor) {
        int bl = Math.min(240, (int) ((packed & 0xFF) * factor));
        int sl = Math.min(240, (int) (((packed >> 16) & 0xFF) * factor));
        return (sl << 16) | bl;
    }
}
