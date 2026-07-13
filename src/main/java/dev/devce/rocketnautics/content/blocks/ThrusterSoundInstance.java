package dev.devce.rocketnautics.content.blocks;

import dev.devce.rocketnautics.registry.RocketSounds;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ThrusterSoundInstance extends AbstractTickableSoundInstance {
    private final IThruster blockEntity;

    public ThrusterSoundInstance(IThruster blockEntity) {
        super(RocketSounds.ROCKET_THRUST.get(), SoundSource.BLOCKS, blockEntity.getLevel().getRandom());
        this.blockEntity = blockEntity;
        this.looping = true;
        this.delay = 0;
        this.volume = 1.0f;
        this.pitch = 1.0f;
        this.x = blockEntity.getBlockPos().getX() + 0.5f;
        this.y = blockEntity.getBlockPos().getY() + 0.5f;
        this.z = blockEntity.getBlockPos().getZ() + 0.5f;
    }

    @Override
    public void tick() {
        if (blockEntity.isRemoved() || !blockEntity.isActive()) {
            this.stop();
            return;
        }

        com.simibubi.create.foundation.blockEntity.behaviour.scrollValue.ScrollValueBehaviour behaviour = blockEntity.getThrustPower();
        int power = (behaviour != null) ? behaviour.getValue() : 5; // Default for RCS/Booster
        
        float targetVolume = 0.4f + (power / 20.0f) * 0.6f;
        float targetPitch = 0.7f + (power / 20.0f) * 0.5f;

        if (blockEntity instanceof RCSThrusterBlockEntity) {
            targetVolume *= 0.3f;
            targetPitch *= 1.5f;
        }

        this.volume = Mth.lerp(0.05f, this.volume, targetVolume);
        this.pitch = Mth.lerp(0.05f, this.pitch, targetPitch);
        
        
        this.x = blockEntity.getBlockPos().getX() + 0.5f;
        this.y = blockEntity.getBlockPos().getY() + 0.5f;
        this.z = blockEntity.getBlockPos().getZ() + 0.5f;
    }

    public void stopSound() {
        this.stop();
    }
}
