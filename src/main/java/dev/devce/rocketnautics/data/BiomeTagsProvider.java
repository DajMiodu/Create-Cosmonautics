package dev.devce.rocketnautics.data;

import dev.devce.rocketnautics.RocketNautics;
import dev.devce.rocketnautics.data.worldgen.BiomeData;
import dev.devce.rocketnautics.registry.RocketTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.world.level.biome.Biome;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.util.concurrent.CompletableFuture;

public class BiomeTagsProvider extends TagsProvider<Biome> {
    protected BiomeTagsProvider(PackOutput p_256596_, CompletableFuture<HolderLookup.Provider> p_256513_, @Nullable ExistingFileHelper existingFileHelper) {
        super(p_256596_, Registries.BIOME, p_256513_, RocketNautics.MODID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.@NonNull Provider provider) {
        tag(RocketTags.BiomeTags.LUNAR_HIGHLANDS.tag)
                .addOptional(BiomeData.LUNAR_HIGHLANDS.location())
                .addOptional(BiomeData.LUNAR_AGED_CHASM.location())
                .addOptional(BiomeData.LUNAR_AGED_SPIKES.location());
        tag(RocketTags.BiomeTags.LUNAR_MARIA.tag)
                .addOptional(BiomeData.LUNAR_MARIA.location())
                .addOptional(BiomeData.LUNAR_BASALT_CHASM.location())
                .addOptional(BiomeData.LUNAR_BASALT_SPIKES.location());
    }
}
