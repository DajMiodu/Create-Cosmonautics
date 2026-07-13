package dev.devce.websnodelib.api;

import net.minecraft.client.gui.GuiGraphics;

/**
 * Represents a connection point on a node.
 * Pins can be of type INPUT or OUTPUT and carry a typed value.
 */
public class WPin extends WElement {
    /**
     * Defines the direction of data flow for the pin.
     */
    public enum Type { INPUT, OUTPUT }

    public enum ValueType {
        NUMBER,
        STRING,
        ANY;

        public boolean accepts(ValueType other) {
            return this == ANY || other == ANY || this == other;
        }

        public static ValueType byName(String name) {
            for (ValueType type : values()) {
                if (type.name().equalsIgnoreCase(name)) return type;
            }
            return NUMBER;
        }
    }

    private final String name;
    private final Type type;
    private final ValueType valueType;
    private final int color;
    private boolean connected;
    private double value;
    private String stringValue = "";

    /**
     * Creates a new pin.
     * @param name Display name of the pin.
     * @param type INPUT or OUTPUT.
     * @param color The accent color for the pin's icon.
     */
    public WPin(String name, Type type, int color) {
        this(name, type, color, ValueType.NUMBER);
    }

    public WPin(String name, Type type, int color, ValueType valueType) {
        this.name = name;
        this.type = type;
        this.color = color;
        this.valueType = valueType;
    }

    /**
     * Note: Pin rendering is currently managed by the parent WNode to ensure 
     * correct alignment with the node body and headers.
     */
    @Override
    public void render(GuiGraphics graphics, int x, int y, int mouseX, int mouseY, float partialTick) {
        // Rendering handled by WNode
    }

    public String getName() { return name; }
    public Type getType() { return type; }
    public ValueType getValueType() { return valueType; }
    public int getColor() { return color; }

    /**
     * @return True if this pin is part of an active WConnection.
     */
    public boolean isConnected() { return connected; }

    /**
     * Updates the connection status of this pin.
     */
    public void setConnected(boolean connected) { this.connected = connected; }

    public net.minecraft.nbt.CompoundTag save() {
        net.minecraft.nbt.CompoundTag tag = new net.minecraft.nbt.CompoundTag();
        tag.putString("name", name);
        tag.putString("pinType", type.name());
        tag.putString("valueType", valueType.name());
        tag.putInt("color", color);
        tag.putDouble("value", value);
        tag.putString("stringValue", stringValue);
        return tag;
    }

    public void load(net.minecraft.nbt.CompoundTag tag) {
        this.value = tag.getDouble("value");
        if (tag.contains("stringValue")) this.stringValue = tag.getString("stringValue");
    }

    /**
     * @return The current numeric value stored in this pin.
     */
    public double getValue() { return value; }

    /**
     * Sets the numeric value for this pin.
     * For INPUT pins, this is usually set by a WConnection.
     * For OUTPUT pins, this is set by the node's Evaluator.
     */
    public void setValue(double value) { this.value = value; }

    public String getStringValue() { return stringValue; }

    public void setStringValue(String value) { this.stringValue = value == null ? "" : value; }

    public String getValueAsString() {
        return valueType == ValueType.STRING || (valueType == ValueType.ANY && !stringValue.isEmpty()) ? stringValue : Double.toString(value);
    }

    public void copyValueFrom(WPin source) {
        if (valueType == ValueType.STRING) {
            setStringValue(source.getValueAsString());
        } else if (source.getValueType() == ValueType.STRING) {
            setStringValue(source.getStringValue());
            try {
                setValue(Double.parseDouble(source.getStringValue().trim()));
            } catch (NumberFormatException ignored) {
                setValue(0);
            }
        } else {
            setValue(source.getValue());
            setStringValue(source.getValueAsString());
        }
    }
}
