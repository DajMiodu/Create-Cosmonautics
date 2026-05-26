package dev.devce.rocketnautics.content.world;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.world.level.levelgen.DensityFunction;

public record StretchedNoise(NoiseHolder noise, double xScale, double yScale, double zScale) implements DensityFunction {
    public static final MapCodec<StretchedNoise> DATA_CODEC = RecordCodecBuilder.mapCodec(
            p_208798_ -> p_208798_.group(
                            NoiseHolder.CODEC.fieldOf("noise").forGetter(StretchedNoise::noise),
                            Codec.DOUBLE.fieldOf("x_scale").forGetter(StretchedNoise::xScale),
                            Codec.DOUBLE.fieldOf("y_scale").forGetter(StretchedNoise::yScale),
                            Codec.DOUBLE.fieldOf("z_scale").forGetter(StretchedNoise::zScale)
                    )
                    .apply(p_208798_, StretchedNoise::new)
    );
    public static final KeyDispatchDataCodec<StretchedNoise> CODEC = KeyDispatchDataCodec.of(DATA_CODEC);

    @Override
    public double compute(FunctionContext p_208800_) {
        return this.noise
                .getValue((double) p_208800_.blockX() * this.xScale, (double) p_208800_.blockY() * this.yScale, (double) p_208800_.blockZ() * this.zScale);
    }

    @Override
    public void fillArray(double[] p_224079_, ContextProvider p_224080_) {
        p_224080_.fillAllDirectly(p_224079_, this);
    }

    @Override
    public DensityFunction mapAll(Visitor p_224077_) {
        return p_224077_.apply(new StretchedNoise(p_224077_.visitNoise(this.noise), this.xScale, this.yScale, this.zScale));
    }

    @Override
    public double minValue() {
        return -this.maxValue();
    }

    @Override
    public double maxValue() {
        return this.noise.maxValue();
    }

    @Override
    public KeyDispatchDataCodec<? extends DensityFunction> codec() {
        return CODEC;
    }
}
