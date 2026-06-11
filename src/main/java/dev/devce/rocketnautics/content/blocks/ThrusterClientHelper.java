package dev.devce.rocketnautics.content.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.world.level.Level;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Common class to safely route client-side particle, sound, and camera shake calls from common code.
 * Callbacks are registered during client-side setup to prevent dedicated server classloading crashes.
 */
public class ThrusterClientHelper {
    public static BiConsumer<IThruster, Object> startSoundCallback = (t, container) -> {};
    public static Consumer<Object> stopSoundCallback = (container) -> {};
    public static ParticleAdder particleAdder = (p, x, y, z, xs, ys, zs) -> {};
    public static CameraShaker cameraShaker = (pos, throttle, radius, intensity) -> {};
    public static LevelGetter clientLevelGetter = () -> null;

    public static void startSound(IThruster thruster, Object container) {
        startSoundCallback.accept(thruster, container);
    }

    public static void stopSound(Object container) {
        stopSoundCallback.accept(container);
    }

    public static void addParticle(ParticleOptions particle, double x, double y, double z, double xs, double ys, double zs) {
        particleAdder.add(particle, x, y, z, xs, ys, zs);
    }

    public static void handleCameraShake(BlockPos pos, float throttle, double radius, float intensity) {
        cameraShaker.shake(pos, throttle, radius, intensity);
    }

    public static Level getClientLevel() {
        return clientLevelGetter.get();
    }

    @FunctionalInterface
    public interface ParticleAdder {
        void add(ParticleOptions particle, double x, double y, double z, double xs, double ys, double zs);
    }

    @FunctionalInterface
    public interface CameraShaker {
        void shake(BlockPos pos, float throttle, double radius, float intensity);
    }

    @FunctionalInterface
    public interface LevelGetter {
        Level get();
    }
}
