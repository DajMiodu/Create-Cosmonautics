package dev.devce.rocketnautics.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.foundation.blockEntity.renderer.SafeBlockEntityRenderer;
import dev.devce.rocketnautics.content.blocks.EnginePipesBlock;
import dev.devce.rocketnautics.content.blocks.EnginePipesBlockEntity;
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
public class EnginePipesRenderer extends SafeBlockEntityRenderer<EnginePipesBlockEntity> {

    public EnginePipesRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    protected void renderSafe(EnginePipesBlockEntity be, float partialTicks, PoseStack ms, MultiBufferSource buffer, int light, int overlay) {
        BlockState blockState = be.getBlockState();
        if (!(blockState.getBlock() instanceof EnginePipesBlock)) return;

        Direction facing = blockState.getValue(EnginePipesBlock.FACING);
        int pipeType = blockState.getValue(EnginePipesBlock.PIPE_TYPE);

        ms.pushPose();
        ms.translate(0.5, 0.5, 0.5);
        ms.mulPose(facing.getRotation());
        ms.mulPose(com.mojang.math.Axis.XP.rotationDegrees(180));
        ms.translate(-0.5, -0.5, -0.5);

        BakedModel baseModel = RocketPartials.enginePipesBase;
        if (baseModel != null) {
            Minecraft.getInstance().getBlockRenderer().getModelRenderer().renderModel(
                    ms.last(), buffer.getBuffer(RenderType.cutout()), blockState, baseModel,
                    1.0f, 1.0f, 1.0f, light, overlay);
        }

        BakedModel cycleModel = switch (pipeType) {
            case 0 -> RocketPartials.enginePipesClosed;
            case 1 -> RocketPartials.enginePipesOpen;
            case 2 -> RocketPartials.enginePipesFullflow;
            default -> RocketPartials.enginePipesExpander;
        };
        if (cycleModel != null) {
            Minecraft.getInstance().getBlockRenderer().getModelRenderer().renderModel(
                    ms.last(), buffer.getBuffer(RenderType.cutout()), blockState, cycleModel,
                    1.0f, 1.0f, 1.0f, light, overlay);
        }

        ms.popPose();
    }
}
