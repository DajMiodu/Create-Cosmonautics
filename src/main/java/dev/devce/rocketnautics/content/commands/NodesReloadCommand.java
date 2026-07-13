package dev.devce.rocketnautics.content.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import dev.devce.rocketnautics.registry.NodeDefinitionLoader;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

public class NodesReloadCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
            Commands.literal("nodes")
                .requires(src -> src.hasPermission(2))
                .then(Commands.literal("reload")
                    .executes(NodesReloadCommand::execute))
        );
    }

    private static int execute(CommandContext<CommandSourceStack> ctx) {
        ctx.getSource().sendSuccess(
            () -> Component.literal("§6[Sputnik] §eReloading custom nodes from disk..."), false);
        try {
            NodeDefinitionLoader.reload();
            ctx.getSource().sendSuccess(
                () -> Component.literal("§6[Sputnik] §aCustom nodes reloaded successfully."), true);
        } catch (Exception e) {
            ctx.getSource().sendFailure(
                Component.literal("§c[Sputnik] Failed to reload nodes: " + e.getMessage()));
        }
        return 1;
    }
}
