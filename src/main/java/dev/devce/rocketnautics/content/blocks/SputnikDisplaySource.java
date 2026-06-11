package dev.devce.rocketnautics.content.blocks;

import com.simibubi.create.content.redstone.displayLink.DisplayLinkContext;
import com.simibubi.create.content.redstone.displayLink.source.SingleLineDisplaySource;
import com.simibubi.create.content.redstone.displayLink.target.DisplayTargetStats;
import com.simibubi.create.foundation.gui.ModularGuiLineBuilder;
import dev.devce.websnodelib.api.WNode;
import dev.devce.websnodelib.api.NodeRegistry;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Connects Sputnik block entities to Create Mod display boards.
 * Supports displaying values from both standard Display graph nodes and the Lua display bridge.
 */
public class SputnikDisplaySource extends SingleLineDisplaySource {

    @Override
    protected MutableComponent provideLine(DisplayLinkContext context, DisplayTargetStats stats) {
        if (!(context.getSourceBlockEntity() instanceof SputnikBlockEntity sputnik))
            return EMPTY_LINE;

        List<WNode> displayNodes = sputnik.graph.getNodes().stream()
                .filter(n -> "Display".equals(NodeRegistry.getCategory(n.getTypeId())))
                .sorted(Comparator.comparing(WNode::getTitle))
                .toList();

        List<String> bridgeKeys = sputnik.getDisplayBridge().keySet().stream()
                .sorted()
                .toList();

        int index = context.sourceConfig().getInt("NodeIndex");
        if (index < 0) return EMPTY_LINE;

        if (index < displayNodes.size()) {
            WNode targetNode = displayNodes.get(index);
            if (targetNode.getInputs().isEmpty())
                return EMPTY_LINE;

            if (targetNode.getInputs().size() == 1) {
                double val = targetNode.getInputs().get(0).getValue();
                return Component.literal(String.format("%.2f", val));
            } else {
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < targetNode.getInputs().size(); i++) {
                    if (i > 0) sb.append(" ");
                    sb.append(targetNode.getInputs().get(i).getName()).append(": ")
                      .append(String.format("%.2f", targetNode.getInputs().get(i).getValue()));
                }
                return Component.literal(sb.toString());
            }
        } else {
            int bridgeIndex = index - displayNodes.size();
            if (bridgeIndex >= 0 && bridgeIndex < bridgeKeys.size()) {
                String key = bridgeKeys.get(bridgeIndex);
                Double val = sputnik.getDisplayBridge().get(key);
                return Component.literal(key + ": " + (val != null ? String.format("%.2f", val) : "0.00"));
            }
        }
        return EMPTY_LINE;
    }

    @Override
    protected boolean allowsLabeling(DisplayLinkContext context) {
        return true;
    }

    @Override
    protected String getTranslationKey() {
        return "sputnik";
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void initConfigurationWidgets(DisplayLinkContext context, ModularGuiLineBuilder builder, boolean isFirstLine) {
        super.initConfigurationWidgets(context, builder, isFirstLine);
        if (isFirstLine) return;

        if (!(context.getSourceBlockEntity() instanceof SputnikBlockEntity sputnik)) return;

        List<Component> options = new ArrayList<>();
        List<WNode> displayNodes = sputnik.graph.getNodes().stream()
                .filter(n -> "Display".equals(NodeRegistry.getCategory(n.getTypeId())))
                .sorted(Comparator.comparing(WNode::getTitle))
                .toList();

        for (WNode n : displayNodes) {
            options.add(Component.literal("Node: " + n.getTitle()));
        }

        List<String> bridgeKeys = sputnik.getDisplayBridge().keySet().stream()
                .sorted()
                .toList();

        for (String k : bridgeKeys) {
            options.add(Component.literal("Lua: " + k));
        }

        if (options.isEmpty()) {
            options.add(Component.literal("No Display Targets"));
        }

        builder.addSelectionScrollInput(0, 120, (si, l) -> si
                .forOptions(options)
                .titled(Component.literal("Select Display Target")),
                "NodeIndex");
    }
}
