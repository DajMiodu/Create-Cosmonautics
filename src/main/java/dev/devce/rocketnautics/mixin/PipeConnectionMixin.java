package dev.devce.rocketnautics.mixin;

import com.simibubi.create.content.fluids.PipeConnection;
import dev.devce.rocketnautics.registry.RocketFluids;
import dev.devce.rocketnautics.registry.RocketParticles;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.fluids.FluidStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = PipeConnection.class, remap = false)
public abstract class PipeConnectionMixin {
    @Shadow
    public Direction side;

    @Shadow
    private java.util.Optional<com.simibubi.create.content.fluids.PipeConnection.Flow> flow;

    @Shadow
    private boolean hasOpenEnd() {
        return false;
    }

    @Inject(method = "spawnParticlesInner", at = @At("HEAD"), cancellable = true)
    @org.spongepowered.asm.mixin.Unique
    private void onSpawnParticlesInner(Level level, BlockPos pos, FluidStack fluid, CallbackInfo ci) {
        if (!fluid.isEmpty() && fluid.getFluid().isSame(RocketFluids.ROCKET_EXHAUST.get())) {
            if (level == Minecraft.getInstance().level) {
                if (!PipeConnection.isRenderEntityWithinDistance(pos))
                    return;

                RandomSource random = level.getRandom();

                if (hasOpenEnd()) {
                    double x = pos.getX() + 0.5 + side.getStepX() * 0.45 + (random.nextDouble() - 0.5) * 0.1;
                    double y = pos.getY() + 0.5 + side.getStepY() * 0.45 + (random.nextDouble() - 0.5) * 0.1;
                    double z = pos.getZ() + 0.5 + side.getStepZ() * 0.45 + (random.nextDouble() - 0.5) * 0.1;

                    double speedX = side.getStepX() * 0.2 + (random.nextDouble() - 0.5) * 0.05;
                    double speedY = side.getStepY() * 0.2 + (random.nextDouble() - 0.5) * 0.05;
                    double speedZ = side.getStepZ() * 0.2 + (random.nextDouble() - 0.5) * 0.05;

                    level.addParticle(ParticleTypes.CLOUD, x, y, z, speedX, speedY, speedZ);
                    level.addParticle(RocketParticles.RCS_GAS.get(), x, y, z, speedX * 0.8, speedY * 0.8, speedZ * 0.8);
                } else if (flow != null && flow.isPresent()) {
                    com.simibubi.create.content.fluids.PipeConnection.Flow pipeFlow = flow.get();
                    boolean inbound = pipeFlow.inbound;

                    if (random.nextFloat() < 0.25F) {
                        double centerX = pos.getX() + 0.5;
                        double centerY = pos.getY() + 0.5;
                        double centerZ = pos.getZ() + 0.5;

                        int dirX = side.getStepX();
                        int dirY = side.getStepY();
                        int dirZ = side.getStepZ();

                        double flowSpeed = 0.04 + random.nextDouble() * 0.02;

                        double offU = (random.nextDouble() - 0.5) * 0.12;
                        double offV = (random.nextDouble() - 0.5) * 0.12;

                        double perpX1 = 0, perpY1 = 0, perpZ1 = 0;
                        double perpX2 = 0, perpY2 = 0, perpZ2 = 0;
                        if (side.getAxis() == Direction.Axis.Y) {
                            perpX1 = 1; perpZ2 = 1;
                        } else if (side.getAxis() == Direction.Axis.X) {
                            perpY1 = 1; perpZ2 = 1;
                        } else {
                            perpX1 = 1; perpY2 = 1;
                        }

                        double crossX = perpX1 * offU + perpX2 * offV;
                        double crossY = perpY1 * offU + perpY2 * offV;
                        double crossZ = perpZ1 * offU + perpZ2 * offV;

                        double startX, startY, startZ;
                        double vx, vy, vz;

                        if (inbound) {
                            startX = centerX + dirX * 0.5 + crossX;
                            startY = centerY + dirY * 0.5 + crossY;
                            startZ = centerZ + dirZ * 0.5 + crossZ;

                            vx = -dirX * flowSpeed;
                            vy = -dirY * flowSpeed;
                            vz = -dirZ * flowSpeed;
                        } else {
                            startX = centerX + crossX;
                            startY = centerY + crossY;
                            startZ = centerZ + crossZ;

                            vx = dirX * flowSpeed;
                            vy = dirY * flowSpeed;
                            vz = dirZ * flowSpeed;
                        }

                        level.addParticle(RocketParticles.RCS_GAS.get(), startX, startY, startZ, vx, vy, vz);
                    }
                }
            }
            ci.cancel();
        }
    }
}
