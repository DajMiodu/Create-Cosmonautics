package dev.devce.rocketnautics.registry;

import com.mojang.serialization.MapCodec;
import dev.devce.rocketnautics.RocketNautics;
import dev.devce.rocketnautics.content.world.StretchedNoise;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class RocketDensityFunctions {
    public static final DeferredRegister<MapCodec<? extends DensityFunction>> FUNCS = DeferredRegister.create(Registries.DENSITY_FUNCTION_TYPE, RocketNautics.MODID);

    public static final DeferredHolder<MapCodec<? extends DensityFunction>, MapCodec<StretchedNoise>> STRETCHED_NOISE = FUNCS.register("stretched_noise", () -> StretchedNoise.DATA_CODEC);

    public static void register(IEventBus bus) {
        FUNCS.register(bus);
    }

}
