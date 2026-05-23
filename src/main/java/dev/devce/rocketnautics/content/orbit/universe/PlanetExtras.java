package dev.devce.rocketnautics.content.orbit.universe;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public record PlanetExtras(boolean star, boolean clouds, int shadowLightSourceID) {
    public static final StreamCodec<FriendlyByteBuf, PlanetExtras> CODEC = StreamCodec.of(
            (buf, val) -> {
                buf.writeBoolean(val.star);
                buf.writeBoolean(val.clouds);
                buf.writeVarInt(val.shadowLightSourceID);
            },
            buf -> {
                boolean star = buf.readBoolean();
                boolean clouds = buf.readBoolean();
                int shadowLightSourceID = buf.readVarInt();
                return new PlanetExtras(star, clouds, shadowLightSourceID);
            }
    );

    public PlanetExtras(boolean star, boolean clouds) {
        this(star, clouds, -1);
    }

    public boolean diffuseLayers() {
        return star() || clouds();
    }

    public int diffuseLayerCount() {
        return star() ? 35 : 20;
    }

    public boolean renderShadow() {
        return shadowLightSourceID >= 0;
    }
}
