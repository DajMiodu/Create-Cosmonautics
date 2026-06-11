package foundry.veil.api.quasar.data.module.init;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import foundry.veil.api.quasar.data.ParticleModuleTypeRegistry;
import foundry.veil.api.quasar.data.module.ModuleType;
import foundry.veil.api.quasar.data.module.ParticleModuleData;
import foundry.veil.api.quasar.emitters.module.InitParticleModule;
import foundry.veil.api.quasar.particle.ParticleModuleSet;
import net.minecraft.client.renderer.LightTexture;

public record LightmapParticleModuleData(int packedLight) implements ParticleModuleData {

    public static final MapCodec<LightmapParticleModuleData> CODEC = Codec.mapEither(
            Codec.BOOL.optionalFieldOf("fullbright", false)
                    .xmap(bright -> bright ? LightTexture.FULL_BRIGHT : -1, packedLight -> packedLight == LightTexture.FULL_BRIGHT),
            RecordCodecBuilder.<Integer>mapCodec(instance -> instance.group(
                    Codec.intRange(0, 15)
                            .fieldOf("block")
                            .forGetter(LightTexture::block),
                    Codec.intRange(0, 15)
                            .fieldOf("sky")
                            .forGetter(LightTexture::sky)
            ).apply(instance, LightTexture::pack))
    ).xmap(either -> either.map(LightmapParticleModuleData::new, LightmapParticleModuleData::new), module -> {
        int packedLight = module.packedLight();
        if (packedLight == -1 || packedLight == LightTexture.FULL_BRIGHT) {
            return Either.left(packedLight);
        }
        return Either.right(packedLight);
    });

    @Override
    public void addModules(ParticleModuleSet.Builder builder) {
        if (this.packedLight != -1) {
            builder.addModule((InitParticleModule) particle -> particle.getRenderData().setFixedPackedLight(this.packedLight));
        }
    }

    @Override
    public ModuleType<?> getType() {
        return ParticleModuleTypeRegistry.LIGHTMAP;
    }
}
