package dev.devce.rocketnautics.api.radio;

import net.minecraft.nbt.CompoundTag;
import java.util.UUID;

public class VirtualRadioNode {
    private final UUID id;
    private double frequency;
    private String luaScript = "";
    private CompoundTag customData = new CompoundTag();

    public VirtualRadioNode(UUID id, double frequency) {
        this.id = id;
        this.frequency = frequency;
    }

    public UUID getId() {
        return id;
    }

    public double getFrequency() {
        return frequency;
    }

    public void setFrequency(double frequency) {
        this.frequency = frequency;
    }

    public String getLuaScript() {
        return luaScript;
    }

    public void setLuaScript(String luaScript) {
        this.luaScript = luaScript;
    }

    public CompoundTag getCustomData() {
        return customData;
    }

    public void setCustomData(CompoundTag customData) {
        this.customData = customData;
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putUUID("id", id);
        tag.putDouble("frequency", frequency);
        tag.putString("luaScript", luaScript);
        tag.put("customData", customData);
        return tag;
    }

    public static VirtualRadioNode load(CompoundTag tag) {
        UUID id = tag.getUUID("id");
        double freq = tag.getDouble("frequency");
        VirtualRadioNode node = new VirtualRadioNode(id, freq);
        node.luaScript = tag.getString("luaScript");
        if (tag.contains("customData")) {
            node.customData = tag.getCompound("customData");
        }
        return node;
    }
}
