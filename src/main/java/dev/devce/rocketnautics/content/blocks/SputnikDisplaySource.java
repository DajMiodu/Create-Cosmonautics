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

import java.util.Comparator;
import java.util.List;

public class SputnikDisplaySource extends SingleLineDisplaySource {

    @Override
    protected MutableComponent provideLine(DisplayLinkContext context, DisplayTargetStats stats) {
        if (!(context.getSourceBlockEntity() instanceof SputnikBlockEntity sputnik))
            return EMPTY_LINE;

        List<WNode> displayNodes = sputnik.graph.getNodes().stream()
                .filter(n -> "Display".equals(NodeRegistry.getCategory(n.getTypeId())))
                .sorted(Comparator.comparing(WNode::getTitle))
                .toList();

        int index = context.sourceConfig().getInt("NodeIndex");
        if (index < 0 || index >= displayNodes.size())
            return EMPTY_LINE;

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

        List<Component> options = sputnik.graph.getNodes().stream()
                .filter(n -> "Display".equals(NodeRegistry.getCategory(n.getTypeId())))
                .sorted(Comparator.comparing(WNode::getTitle))
                .map(n -> (Component) Component.literal(n.getTitle()))
                .toList();

        if (options.isEmpty()) {
            options = List.of(Component.literal("No Display Nodes"));
        }

        List<Component> finalOptions = options;
        builder.addSelectionScrollInput(0, 120, (si, l) -> si
                .forOptions(finalOptions)
                .titled(Component.literal("Select Display Node")),
                "NodeIndex");
    }
}
