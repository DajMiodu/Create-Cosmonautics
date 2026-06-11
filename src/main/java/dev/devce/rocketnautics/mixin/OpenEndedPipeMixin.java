package dev.devce.rocketnautics.mixin;

import com.simibubi.create.content.fluids.OpenEndedPipe;
import dev.devce.rocketnautics.registry.RocketFluids;
import dev.devce.rocketnautics.registry.RocketParticles;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.fluids.FluidStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = OpenEndedPipe.class, remap = false)
public abstract class OpenEndedPipeMixin {
    @Shadow
    private Level world;
    @Shadow
    private BlockPos outputPos;

    @Inject(method = "provideFluidToSpace", at = @At("HEAD"), cancellable = true)
    private void onProvideFluidToSpace(FluidStack stack, boolean simulate, CallbackInfoReturnable<Boolean> cir) {
        if (!stack.isEmpty() && stack.getFluid().isSame(RocketFluids.ROCKET_EXHAUST.get())) {
            if (!simulate) {
                world.playSound(null, outputPos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.4F, 2.0F + (world.random.nextFloat() - world.random.nextFloat()) * 0.8F);
                
                if (world instanceof ServerLevel serverLevel) {
                    RandomSource random = world.getRandom();
                    double x = outputPos.getX() + 0.5;
                    double y = outputPos.getY() + 0.5;
                    double z = outputPos.getZ() + 0.5;
                    for (int i = 0; i < 6; i++) {
                        double speedX = (random.nextDouble() - 0.5) * 0.1;
                        double speedY = 0.15 + random.nextDouble() * 0.15;
                        double speedZ = (random.nextDouble() - 0.5) * 0.1;
                        
                        serverLevel.sendParticles(ParticleTypes.CLOUD,
                            x, y, z, 1,
                            speedX, speedY, speedZ, 0.05
                        );
                        
                        serverLevel.sendParticles(RocketParticles.RCS_GAS.get(),
                            x, y, z, 1,
                            speedX * 0.5, speedY * 1.5, speedZ * 0.5, 0.05
                        );
                    }
                }
            }
            cir.setReturnValue(true);
        }
    }
}
