package dev.devce.rocketnautics.api.orbit;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.EnumSet;
import java.util.Locale;

public enum AtmosphereFlags implements StringRepresentable {
    LOW_DENSITY, DROWNING;
    public static final Codec<AtmosphereFlags> CODEC = StringRepresentable.fromEnum(AtmosphereFlags::values);

    public static EnumSet<AtmosphereFlags> empty() {
        return EnumSet.noneOf(AtmosphereFlags.class);
    }

    public static EnumSet<AtmosphereFlags> properCopy(Collection<AtmosphereFlags> collection) {
        if (collection.isEmpty()) return empty();
        return EnumSet.copyOf(collection);
    }

    @Override
    public @NotNull String getSerializedName() {
        return name().toLowerCase(Locale.ROOT);
    }
}
