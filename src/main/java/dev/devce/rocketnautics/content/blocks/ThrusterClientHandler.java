package dev.devce.rocketnautics.content.blocks;

import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ThrusterClientHandler {
    
    public static void init() {
        ThrusterClientHelper.startSoundCallback = (t, container) -> {
            Minecraft mc = Minecraft.getInstance();
            ThrusterSoundInstance instance = new ThrusterSoundInstance(t);
            mc.getSoundManager().play(instance);
            if (container instanceof SoundContainer sc) {
                sc.soundInstance = instance;
            } else if (container instanceof ThrustBehaviour behaviour) {
                behaviour.soundInstance = instance;
            } else if (container instanceof AbstractThrusterBlockEntity be) {
                be.soundInstance = instance;
            }
        };

        ThrusterClientHelper.stopSoundCallback = (container) -> {
            if (container instanceof SoundContainer sc) {
                if (sc.soundInstance instanceof ThrusterSoundInstance instance) {
                    instance.stopSound();
                    sc.soundInstance = null;
                }
            } else if (container instanceof ThrustBehaviour behaviour) {
                if (behaviour.soundInstance instanceof ThrusterSoundInstance instance) {
                    instance.stopSound();
                    behaviour.soundInstance = null;
                }
            } else if (container instanceof AbstractThrusterBlockEntity be) {
                if (be.soundInstance instanceof ThrusterSoundInstance instance) {
                    instance.stopSound();
                    be.soundInstance = null;
                }
            }
        };

        ThrusterClientHelper.particleAdder = (p, x, y, z, xs, ys, zs) -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc.level != null) {
                mc.level.addParticle(p, x, y, z, xs, ys, zs);
            }
        };

        ThrusterClientHelper.cameraShaker = (pos, throttle, radius, intensity) -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player != null) {
                double distSq = mc.player.distanceToSqr(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
                if (distSq < (radius * radius)) {
                    float distanceFactor = 1.0f - (float) (Math.sqrt(distSq) / radius);
                    float powerFactor = throttle;
                    dev.devce.rocketnautics.client.CameraShakeHandler.addShake(distanceFactor * powerFactor * intensity);
                }
            }
        };

        ThrusterClientHelper.clientLevelGetter = () -> Minecraft.getInstance().level;
    }

    public static void startSound(IThruster thruster, SoundContainer container) {
        Minecraft mc = Minecraft.getInstance();
        ThrusterSoundInstance instance = new ThrusterSoundInstance(thruster);
        mc.getSoundManager().play(instance);
        container.soundInstance = instance;
    }

    public static void stopSound(SoundContainer container) {
        if (container.soundInstance instanceof ThrusterSoundInstance instance) {
            instance.stopSound();
            container.soundInstance = null;
        }
    }

    
    public static class SoundContainer {
        public Object soundInstance;
    }
}
