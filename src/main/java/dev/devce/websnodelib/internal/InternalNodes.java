package dev.devce.websnodelib.internal;

import dev.devce.websnodelib.api.NodeRegistry;
import dev.devce.websnodelib.api.WNode;
import dev.devce.websnodelib.api.WPin;
import dev.devce.websnodelib.api.elements.WLabel;
import dev.devce.websnodelib.api.elements.WTextField;
import net.minecraft.resources.ResourceLocation;

public class InternalNodes {
    private static final int NUMBER_COLOR = 0xFF00FF88;
    private static final int STRING_COLOR = 0xFFFFCC33;

    public static void register() {
        registerStringNodes();
        registerConversionNodes();
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(dev.devce.websnodelib.WebsNodeLib.MOD_ID, path);
    }

    private static void registerStringNodes() {
        NodeRegistry.register(id("string_literal"), "Strings", (x, y) -> {
            WNode node = new WNode(id("string_literal"), "String", x, y);
            WTextField field = new WTextField(100);
            node.addElement(field);
            node.addOutput("Text", STRING_COLOR, WPin.ValueType.STRING);
            node.setEvaluator(n -> n.getOutputs().get(0).setStringValue(field.getValue()));
            return node;
        });

        NodeRegistry.register(id("string_display"), "Strings", (x, y) -> {
            WNode node = new WNode(id("string_display"), "String Display", x, y);
            node.addInput("Text", STRING_COLOR, WPin.ValueType.STRING);
            WLabel label = new WLabel("", STRING_COLOR);
            node.addElement(label);
            node.setEvaluator(n -> label.setText(n.getInputs().get(0).getStringValue()));
            return node;
        });

        NodeRegistry.register(id("string_concat"), "Strings", (x, y) -> {
            WNode node = new WNode(id("string_concat"), "Concat", x, y);
            node.addInput("A", STRING_COLOR, WPin.ValueType.STRING);
            node.addInput("B", STRING_COLOR, WPin.ValueType.STRING);
            node.addOutput("Text", STRING_COLOR, WPin.ValueType.STRING);
            node.setEvaluator(n -> n.getOutputs().get(0).setStringValue(
                n.getInputs().get(0).getStringValue() + n.getInputs().get(1).getStringValue()
            ));
            return node;
        });

        NodeRegistry.register(id("string_split"), "Strings", (x, y) -> {
            WNode node = new WNode(id("string_split"), "Split", x, y);
            node.addInput("Text", STRING_COLOR, WPin.ValueType.STRING);
            node.addInput("Separator", STRING_COLOR, WPin.ValueType.STRING);
            node.addInput("Index", NUMBER_COLOR, WPin.ValueType.NUMBER);
            node.addOutput("Part", STRING_COLOR, WPin.ValueType.STRING);
            node.addOutput("Count", NUMBER_COLOR, WPin.ValueType.NUMBER);
            node.setEvaluator(n -> {
                String text = n.getInputs().get(0).getStringValue();
                String separator = n.getInputs().get(1).getStringValue();
                String[] parts = separator.isEmpty() ? text.split("") : text.split(java.util.regex.Pattern.quote(separator), -1);
                int index = (int) n.getInputs().get(2).getValue();
                n.getOutputs().get(0).setStringValue(index >= 0 && index < parts.length ? parts[index] : "");
                n.getOutputs().get(1).setValue(parts.length);
            });
            return node;
        });

        NodeRegistry.register(id("string_char_at"), "Strings", (x, y) -> {
            WNode node = new WNode(id("string_char_at"), "Char At", x, y);
            node.addInput("Text", STRING_COLOR, WPin.ValueType.STRING);
            node.addInput("Index", NUMBER_COLOR, WPin.ValueType.NUMBER);
            node.addOutput("Char", STRING_COLOR, WPin.ValueType.STRING);
            node.setEvaluator(n -> {
                String text = n.getInputs().get(0).getStringValue();
                int index = (int) n.getInputs().get(1).getValue();
                n.getOutputs().get(0).setStringValue(index >= 0 && index < text.length() ? String.valueOf(text.charAt(index)) : "");
            });
            return node;
        });

        NodeRegistry.register(id("string_substring"), "Strings", (x, y) -> {
            WNode node = new WNode(id("string_substring"), "Substring", x, y);
            node.addInput("Text", STRING_COLOR, WPin.ValueType.STRING);
            node.addInput("Start", NUMBER_COLOR, WPin.ValueType.NUMBER);
            node.addInput("End", NUMBER_COLOR, WPin.ValueType.NUMBER);
            node.addOutput("Text", STRING_COLOR, WPin.ValueType.STRING);
            node.setEvaluator(n -> {
                String text = n.getInputs().get(0).getStringValue();
                int start = clamp((int) n.getInputs().get(1).getValue(), 0, text.length());
                int end = clamp((int) n.getInputs().get(2).getValue(), start, text.length());
                n.getOutputs().get(0).setStringValue(text.substring(start, end));
            });
            return node;
        });

        NodeRegistry.register(id("string_length"), "Strings", (x, y) -> {
            WNode node = new WNode(id("string_length"), "Length", x, y);
            node.addInput("Text", STRING_COLOR, WPin.ValueType.STRING);
            node.addOutput("Length", NUMBER_COLOR, WPin.ValueType.NUMBER);
            node.setEvaluator(n -> n.getOutputs().get(0).setValue(n.getInputs().get(0).getStringValue().length()));
            return node;
        });
    }

    private static void registerConversionNodes() {
        NodeRegistry.register(id("number_to_string"), "Conversion", (x, y) -> {
            WNode node = new WNode(id("number_to_string"), "Number To String", x, y);
            node.addInput("Number", NUMBER_COLOR, WPin.ValueType.NUMBER);
            node.addOutput("Text", STRING_COLOR, WPin.ValueType.STRING);
            node.setEvaluator(n -> n.getOutputs().get(0).setStringValue(Double.toString(n.getInputs().get(0).getValue())));
            return node;
        });

        NodeRegistry.register(id("string_to_number"), "Conversion", (x, y) -> {
            WNode node = new WNode(id("string_to_number"), "String To Number", x, y);
            node.addInput("Text", STRING_COLOR, WPin.ValueType.STRING);
            node.addOutput("Number", NUMBER_COLOR, WPin.ValueType.NUMBER);
            node.addOutput("Valid", NUMBER_COLOR, WPin.ValueType.NUMBER);
            node.setEvaluator(n -> {
                try {
                    n.getOutputs().get(0).setValue(Double.parseDouble(n.getInputs().get(0).getStringValue().trim()));
                    n.getOutputs().get(1).setValue(1);
                } catch (NumberFormatException e) {
                    n.getOutputs().get(0).setValue(0);
                    n.getOutputs().get(1).setValue(0);
                }
            });
            return node;
        });

        NodeRegistry.register(id("int_to_string"), "Conversion", (x, y) -> {
            WNode node = new WNode(id("int_to_string"), "Int To String", x, y);
            node.addInput("Int", NUMBER_COLOR, WPin.ValueType.NUMBER);
            node.addOutput("Text", STRING_COLOR, WPin.ValueType.STRING);
            node.setEvaluator(n -> n.getOutputs().get(0).setStringValue(Integer.toString((int) n.getInputs().get(0).getValue())));
            return node;
        });

        NodeRegistry.register(id("string_to_int"), "Conversion", (x, y) -> {
            WNode node = new WNode(id("string_to_int"), "String To Int", x, y);
            node.addInput("Text", STRING_COLOR, WPin.ValueType.STRING);
            node.addOutput("Int", NUMBER_COLOR, WPin.ValueType.NUMBER);
            node.addOutput("Valid", NUMBER_COLOR, WPin.ValueType.NUMBER);
            node.setEvaluator(n -> {
                try {
                    n.getOutputs().get(0).setValue(Integer.parseInt(n.getInputs().get(0).getStringValue().trim()));
                    n.getOutputs().get(1).setValue(1);
                } catch (NumberFormatException e) {
                    n.getOutputs().get(0).setValue(0);
                    n.getOutputs().get(1).setValue(0);
                }
            });
            return node;
        });
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }
}
