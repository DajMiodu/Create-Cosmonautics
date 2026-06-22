package dev.devce.rocketnautics.registry;

import dev.devce.rocketnautics.RocketNautics;
import dev.devce.rocketnautics.content.blocks.SputnikBlockEntity;
import dev.devce.rocketnautics.api.peripherals.IPeripheral;
import dev.devce.rocketnautics.api.peripherals.PeripheralRegistry;
import dev.devce.rocketnautics.lua.LuaUIBridge;
import dev.devce.websnodelib.api.NodeRegistry;
import dev.devce.websnodelib.api.WNode;
import dev.devce.websnodelib.api.WGraph;
import dev.devce.websnodelib.api.WPin;
import net.minecraft.resources.ResourceLocation;
import java.util.List;
import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.luaj.vm2.*;
import org.luaj.vm2.lib.jse.JsePlatform;
import org.luaj.vm2.lib.*;

/**
 * Registers all websnodelib node types for Rocketnautics.
 * Standard sensors and actuators are registered with pre-defined default Lua code scripts,
 * allowing players to open and edit them just like a standard Lua script node.
 */
public class RocketNodes {
    public static void register() {
        // --- Input Port (for nested functions) ---
        NodeRegistry.register(ResourceLocation.fromNamespaceAndPath(RocketNautics.MODID, "input"), "Functions", (x, y) -> {
            WNode node = new WNode(ResourceLocation.fromNamespaceAndPath(RocketNautics.MODID, "input"), "Input Port", x, y);
            node.addOutput("Out", 0xFFFFFFFF);
            dev.devce.websnodelib.api.elements.WTextField nameField = new dev.devce.websnodelib.api.elements.WTextField(60);
            nameField.setValue("in_1");
            node.addElement(nameField);
            return node;
        });

        // --- Output Port (for nested functions) ---
        NodeRegistry.register(ResourceLocation.fromNamespaceAndPath(RocketNautics.MODID, "output"), "Functions", (x, y) -> {
            WNode node = new WNode(ResourceLocation.fromNamespaceAndPath(RocketNautics.MODID, "output"), "Output Port", x, y);
            node.addInput("In", 0xFFFFFFFF);
            dev.devce.websnodelib.api.elements.WTextField nameField = new dev.devce.websnodelib.api.elements.WTextField(60);
            nameField.setValue("out_1");
            node.addElement(nameField);
            return node;
        });

        // --- Dynamic Function Node ---
        NodeRegistry.register(ResourceLocation.fromNamespaceAndPath(RocketNautics.MODID, "function"), "Functions", (x, y) -> {
            WNode node = new WNode(ResourceLocation.fromNamespaceAndPath(RocketNautics.MODID, "function"), "Function", x, y);
            node.setInternalGraph(new WGraph());
            
            node.setEvaluator(n -> {
                WGraph sub = n.getInternalGraph();
                if (sub == null) return;

                syncFunctionPins(n);

                List<WNode> internalNodes = sub.getNodes();
                int inIdx = 0;
                for (WNode inNode : internalNodes) {
                    if (inNode.getTypeId().getPath().equals("input")) {
                        if (inIdx < n.getInputs().size()) {
                            double val = n.getInputs().get(inIdx).getValue();
                            inNode.getOutputs().get(0).setValue(val);
                        }
                        inIdx++;
                    }
                }

                sub.tick();

                int outIdx = 0;
                for (WNode outNode : internalNodes) {
                    if (outNode.getTypeId().getPath().equals("output")) {
                        if (outIdx < n.getOutputs().size()) {
                            double val = outNode.getInputs().get(0).getValue();
                            n.getOutputs().get(outIdx).setValue(val);
                        }
                        outIdx++;
                    }
                }
            });
            return node;
        });

        // --- Comment Frame Node ---
        NodeRegistry.register(ResourceLocation.fromNamespaceAndPath(RocketNautics.MODID, "frame"), "System", (x, y) -> {
            WNode node = new WNode(ResourceLocation.fromNamespaceAndPath(RocketNautics.MODID, "frame"), "Comment Frame", x, y);
            node.setWidth(200);
            node.setHeight(150);
            return node;
        });

        // --- Lua Script Node (Turing Complete Logic & Control) ---
        NodeRegistry.register(ResourceLocation.fromNamespaceAndPath(RocketNautics.MODID, "lua_script"), "Programming", (x, y) -> {
            WNode node = new WNode(ResourceLocation.fromNamespaceAndPath(RocketNautics.MODID, "lua_script"), "Lua Script", x, y);
            setupLuaEvaluator(node);
            return node;
        });


        NodeRegistry.register(ResourceLocation.fromNamespaceAndPath(RocketNautics.MODID, "display"), "Display", (x, y) -> {
            WNode node = new WNode(ResourceLocation.fromNamespaceAndPath(RocketNautics.MODID, "display"), "Display", x, y);
            node.getCustomData().putString("code", "-- Display input value\nlocal val = input(\"In\")\noutput(\"Out\", val)");
            node.addInput("In", 0xFFFFFFFF);
            node.addOutput("Out", 0xFF00FF88);
            
            dev.devce.websnodelib.api.elements.WLabel label = new dev.devce.websnodelib.api.elements.WLabel("0.00", 0xFF00FF00);
            node.addElement(label);
            
            setupLuaEvaluator(node);
            
            WNode.Evaluator parentEvaluator = node.getEvaluator();
            node.setEvaluator(n -> {
                parentEvaluator.evaluate(n);
                double val = n.getInputs().get(0).getValue();
                label.setText(String.format("%.2f", val));
            });
            
            return node;
        });

        NodeRegistry.register(ResourceLocation.fromNamespaceAndPath(RocketNautics.MODID, "attitude_display"), "Display", (x, y) -> {
            WNode node = new WNode(ResourceLocation.fromNamespaceAndPath(RocketNautics.MODID, "attitude_display"), "Attitude Display", x, y);
            node.getCustomData().putString("code", "-- Display orientation\nlocal pitch = input(\"Pitch\")\nlocal yaw = input(\"Yaw\")\nlocal roll = input(\"Roll\")");
            node.addInput("Pitch", 0xFFFFAA00);
            node.addInput("Yaw", 0xFFFFAA00);
            node.addInput("Roll", 0xFFFFAA00);
            
            dev.devce.websnodelib.api.elements.WViewport3D viewport = new dev.devce.websnodelib.api.elements.WViewport3D(120, 120);
            viewport.setZoom(0.8f);
            viewport.addModel(new net.minecraft.world.item.ItemStack(RocketBlocks.ROCKET_THRUSTER.get()), new org.joml.Vector3f(0, -0.75f, 0), new org.joml.Vector3f(180, 0, 0), 1.0f);
            viewport.addModel(new net.minecraft.world.item.ItemStack(net.minecraft.world.level.block.Blocks.IRON_BLOCK), new org.joml.Vector3f(0, -0.25f, 0), new org.joml.Vector3f(0, 0, 0), 1.0f);
            viewport.addModel(new net.minecraft.world.item.ItemStack(net.minecraft.world.level.block.Blocks.IRON_BLOCK), new org.joml.Vector3f(0, 0.25f, 0), new org.joml.Vector3f(0, 0, 0), 1.0f);
            viewport.addModel(new net.minecraft.world.item.ItemStack(net.minecraft.world.level.block.Blocks.LIGHTNING_ROD), new org.joml.Vector3f(0, 0.75f, 0), new org.joml.Vector3f(0, 0, 0), 1.0f);
            node.addElement(viewport);
            
            setupLuaEvaluator(node);
            
            WNode.Evaluator parentEvaluator = node.getEvaluator();
            node.setEvaluator(n -> {
                parentEvaluator.evaluate(n);
                float pitch = (float) n.getInputs().get(0).getValue();
                float yaw = (float) n.getInputs().get(1).getValue();
                float roll = (float) n.getInputs().get(2).getValue();
                viewport.setGlobalRotation(pitch, yaw, roll);
            });
            
            return node;
        });


    }

    private static void registerLuaNode(String id, String name, String category, String defaultCode, String[] inputs, String[] outputs) {
        NodeRegistry.register(ResourceLocation.fromNamespaceAndPath(RocketNautics.MODID, id), category, (x, y) -> {
            WNode node = new WNode(ResourceLocation.fromNamespaceAndPath(RocketNautics.MODID, id), name, x, y);
            node.getCustomData().putString("code", defaultCode);
            for (String input : inputs) {
                node.addInput(input, 0xFFFFFFFF);
            }
            for (String output : outputs) {
                node.addOutput(output, 0xFF00FF88);
            }
            setupLuaEvaluatorPublic(node);
            return node;
        });
    }

    /** Public alias used by NodeDefinitionLoader. */
    public static void setupLuaEvaluatorPublic(WNode node) {
        setupLuaEvaluator(node);
    }

    private static void setupLuaEvaluator(WNode node) {
        final Globals[]  globals  = {null};
        final LuaValue[] chunk    = {null};
        final String[]   lastCode = {null};
        // Per-node UI element registry: persists across ticks so elements survive recompiles
        @SuppressWarnings("unchecked")
        final ConcurrentHashMap<LuaTable, LuaUIBridge.LuaElement>[] uiRegistry =
            new ConcurrentHashMap[]{ new ConcurrentHashMap<>() };

        node.setEvaluator(n -> {
            String code = n.getCustomData().getString("code");
            if (code == null || code.isBlank()) return;

            SputnikBlockEntity sputnik = null;
            if (n.getParentGraph().getContext() instanceof SputnikBlockEntity sbe) {
                sputnik = sbe;
            }

            final SputnikBlockEntity finalSputnik = sputnik;

            if (!code.equals(lastCode[0]) || globals[0] == null) {
                n.getCustomData().putBoolean("failed", false);
                
                // Parse pins to stay in sync
                n.clearInputs();
                n.clearOutputs();
                Pattern pinPat = Pattern.compile("(input|output)\\(\\s*[\"']([^\"']+)[\"']");
                for (String line : code.split("\n")) {
                    String codeOnly = line;
                    int idx = line.indexOf("--");
                    if (idx != -1) codeOnly = line.substring(0, idx);
                    Matcher m = pinPat.matcher(codeOnly);
                    while (m.find()) {
                        String kind = m.group(1), name = m.group(2);
                        if (kind.equals("input")) n.addInput(name, 0xFFFFFFFF);
                        if (kind.equals("output")) n.addOutput(name, 0xFF00FF88);
                    }
                }

                globals[0] = JsePlatform.standardGlobals();
                globals[0].set("os",      LuaValue.NIL);
                globals[0].set("io",      LuaValue.NIL);
                globals[0].set("luajava", LuaValue.NIL);
                globals[0].set("debug",   LuaValue.NIL);
                globals[0].set("require", LuaValue.NIL);
                // Inject the UI bridge so scripts can build custom node UIs
                LuaUIBridge.inject(globals[0], n, uiRegistry[0]);
                try {
                    chunk[0]    = globals[0].load(code);
                    lastCode[0] = code;
                    n.getCustomData().putBoolean("err", false);
                    n.getCustomData().putString("errMsg", "");
                } catch (Throwable e) {
                    n.getCustomData().putBoolean("err", true);
                    n.getCustomData().putString("errMsg", e.getMessage() != null ? e.getMessage() : e.toString());
                    chunk[0] = null;
                    return;
                }
            }
            if (chunk[0] == null) {
                n.getCustomData().putBoolean("err", true);
                return;
            }

            // Bridge: input(name) → read static pin value
            globals[0].set("input", new OneArgFunction() {
                @Override public LuaValue call(LuaValue arg) {
                    String name = arg.tojstring();
                    for (int i = 0; i < n.getInputs().size(); i++)
                        if (n.getInputs().get(i).getName().equals(name))
                            return LuaValue.valueOf(n.getInputs().get(i).getValue());
                    return LuaValue.ZERO;
                }
            });

            // Bridge: output(name, value) → write static pin value
            globals[0].set("output", new TwoArgFunction() {
                @Override public LuaValue call(LuaValue arg1, LuaValue arg2) {
                    String name  = arg1.tojstring();
                    double value = arg2.todouble();
                    for (int i = 0; i < n.getOutputs().size(); i++)
                        if (n.getOutputs().get(i).getName().equals(name)) {
                            n.getOutputs().get(i).setValue(value);
                            return LuaValue.NIL;
                        }
                    return LuaValue.NIL;
                }
            });

            // getDeepSpacePosition() -> returns x, y, z
            globals[0].set("getDeepSpacePosition", new VarArgFunction() {
                @Override public Varargs invoke(Varargs args) {
                    if (finalSputnik != null && finalSputnik.getLevel() != null && !finalSputnik.getLevel().isClientSide() && finalSputnik.getLevel().getServer() != null) {
                        var dsData = dev.devce.rocketnautics.content.orbit.DeepSpaceData.getInstance(finalSputnik.getLevel().getServer());
                        var inst = dsData.getInstanceForPos((int) finalSputnik.getGlobalPos().x, (int) finalSputnik.getGlobalPos().z);
                        if (inst != null && !inst.isCorrupted()) {
                            var pos = inst.getPosition().getCurrentPosition();
                            return varargsOf(new LuaValue[]{
                                LuaValue.valueOf(pos.getX()),
                                LuaValue.valueOf(pos.getY()),
                                LuaValue.valueOf(pos.getZ())
                            });
                        }
                    }
                    if (finalSputnik != null) {
                        var pos = finalSputnik.getGlobalPos();
                        return varargsOf(new LuaValue[]{
                            LuaValue.valueOf(pos.x),
                            LuaValue.valueOf(pos.y),
                            LuaValue.valueOf(pos.z)
                        });
                    }
                    return varargsOf(new LuaValue[]{ LuaValue.ZERO, LuaValue.ZERO, LuaValue.ZERO });
                }
            });

            // getDeepSpaceVelocity() -> returns vx, vy, vz
            globals[0].set("getDeepSpaceVelocity", new VarArgFunction() {
                @Override public Varargs invoke(Varargs args) {
                    if (finalSputnik != null && finalSputnik.getLevel() != null && !finalSputnik.getLevel().isClientSide() && finalSputnik.getLevel().getServer() != null) {
                        var dsData = dev.devce.rocketnautics.content.orbit.DeepSpaceData.getInstance(finalSputnik.getLevel().getServer());
                        var inst = dsData.getInstanceForPos((int) finalSputnik.getGlobalPos().x, (int) finalSputnik.getGlobalPos().z);
                        if (inst != null && !inst.isCorrupted()) {
                            var vel = inst.getPosition().getCurrentPVCoords().getVelocity();
                            return varargsOf(new LuaValue[]{
                                LuaValue.valueOf(vel.getX()),
                                LuaValue.valueOf(vel.getY()),
                                LuaValue.valueOf(vel.getZ())
                            });
                        }
                    }
                    return varargsOf(new LuaValue[]{ LuaValue.ZERO, LuaValue.ZERO, LuaValue.ZERO });
                }
            });

            // getDeepSpaceFrame() -> returns string
            globals[0].set("getDeepSpaceFrame", new ZeroArgFunction() {
                @Override public LuaValue call() {
                    if (finalSputnik != null && finalSputnik.getLevel() != null && !finalSputnik.getLevel().isClientSide() && finalSputnik.getLevel().getServer() != null) {
                        var dsData = dev.devce.rocketnautics.content.orbit.DeepSpaceData.getInstance(finalSputnik.getLevel().getServer());
                        var inst = dsData.getInstanceForPos((int) finalSputnik.getGlobalPos().x, (int) finalSputnik.getGlobalPos().z);
                        if (inst != null && !inst.isCorrupted()) {
                            return LuaValue.valueOf(inst.getPosition().getFrame().getName());
                        }
                    }
                    if (finalSputnik != null && finalSputnik.getLevel() != null) {
                        return LuaValue.valueOf(finalSputnik.getLevel().dimension().location().toString());
                    }
                    return LuaValue.NIL;
                }
            });

            // getPosition()
            globals[0].set("getPosition", new VarArgFunction() {
                @Override public Varargs invoke(Varargs args) {
                    if (finalSputnik != null) {
                        org.joml.Vector3d pos = finalSputnik.getGlobalPos();
                        return varargsOf(new LuaValue[] {
                            LuaValue.valueOf(pos.x),
                            LuaValue.valueOf(pos.y),
                            LuaValue.valueOf(pos.z)
                        });
                    }
                    return varargsOf(new LuaValue[] { LuaValue.ZERO, LuaValue.ZERO, LuaValue.ZERO });
                }
            });

            // getVelocity()
            globals[0].set("getVelocity", new VarArgFunction() {
                @Override public Varargs invoke(Varargs args) {
                    if (finalSputnik != null) {
                        org.joml.Vector3d vel = finalSputnik.getVelocityVector();
                        return varargsOf(new LuaValue[] {
                            LuaValue.valueOf(vel.x),
                            LuaValue.valueOf(vel.y),
                            LuaValue.valueOf(vel.z)
                        });
                    }
                    return varargsOf(new LuaValue[] { LuaValue.ZERO, LuaValue.ZERO, LuaValue.ZERO });
                }
            });

            // getOrientation()
            globals[0].set("getOrientation", new VarArgFunction() {
                @Override public Varargs invoke(Varargs args) {
                    if (finalSputnik != null) {
                        return varargsOf(new LuaValue[] {
                            LuaValue.valueOf(finalSputnik.getPitch()),
                            LuaValue.valueOf(finalSputnik.getYaw()),
                            LuaValue.valueOf(finalSputnik.getRoll())
                        });
                    }
                    return varargsOf(new LuaValue[] { LuaValue.ZERO, LuaValue.ZERO, LuaValue.ZERO });
                }
            });

            // getAngularVelocity()
            globals[0].set("getAngularVelocity", new VarArgFunction() {
                @Override public Varargs invoke(Varargs args) {
                    if (finalSputnik != null) {
                        org.joml.Vector3d angVel = finalSputnik.getAngularVelocity();
                        return varargsOf(new LuaValue[] {
                            LuaValue.valueOf(angVel.x),
                            LuaValue.valueOf(angVel.y),
                            LuaValue.valueOf(angVel.z)
                        });
                    }
                    return varargsOf(new LuaValue[] { LuaValue.ZERO, LuaValue.ZERO, LuaValue.ZERO });
                }
            });

            // getAltitude()
            globals[0].set("getAltitude", new ZeroArgFunction() {
                @Override public LuaValue call() {
                    return finalSputnik != null ? LuaValue.valueOf(finalSputnik.getAltitude()) : LuaValue.ZERO;
                }
            });

            // getPeripheralIds()
            globals[0].set("getPeripheralIds", new VarArgFunction() {
                @Override public Varargs invoke(Varargs args) {
                    LuaTable table = new LuaTable();
                    if (finalSputnik != null && finalSputnik.getLevel() != null) {
                        int index = 1;
                        for (IPeripheral p : PeripheralRegistry.getPeripherals(finalSputnik.getLevel())) {
                            table.set(index++, LuaValue.valueOf(p.getUniqueId().toString()));
                        }
                    }
                    return table;
                }
            });

            // getPeripheralType(id)
            globals[0].set("getPeripheralType", new OneArgFunction() {
                @Override public LuaValue call(LuaValue arg) {
                    if (finalSputnik != null && finalSputnik.getLevel() != null) {
                        try {
                            UUID id = UUID.fromString(arg.tojstring());
                            IPeripheral p = PeripheralRegistry.getPeripheral(finalSputnik.getLevel(), id);
                            if (p != null) {
                                return LuaValue.valueOf(p.getPeripheralType());
                            }
                        } catch (Exception e) {}
                    }
                    return LuaValue.NIL;
                }
            });

            // readPeripheral(id, key)
            globals[0].set("readPeripheral", new TwoArgFunction() {
                @Override public LuaValue call(LuaValue arg1, LuaValue arg2) {
                    if (finalSputnik != null && finalSputnik.getLevel() != null) {
                        try {
                            UUID id = UUID.fromString(arg1.tojstring());
                            String key = arg2.tojstring();
                            IPeripheral p = PeripheralRegistry.getPeripheral(finalSputnik.getLevel(), id);
                            if (p != null) {
                                return LuaValue.valueOf(p.readValue(key));
                            }
                        } catch (Exception e) {}
                    }
                    return LuaValue.ZERO;
                }
            });

            // writePeripheral(id, key, value)
            globals[0].set("writePeripheral", new ThreeArgFunction() {
                @Override public LuaValue call(LuaValue arg1, LuaValue arg2, LuaValue arg3) {
                    if (finalSputnik != null && finalSputnik.getLevel() != null) {
                        try {
                            UUID id = UUID.fromString(arg1.tojstring());
                            String key = arg2.tojstring();
                            double value = arg3.todouble();
                            IPeripheral p = PeripheralRegistry.getPeripheral(finalSputnik.getLevel(), id);
                            if (p != null) {
                                p.writeValue(key, value);
                            }
                        } catch (Exception e) {}
                    }
                    return LuaValue.NIL;
                }
            });

            // writePeripheralValues(id, key, ...)
            globals[0].set("writePeripheralValues", new VarArgFunction() {
                @Override public Varargs invoke(Varargs args) {
                    if (finalSputnik != null && finalSputnik.getLevel() != null && args.narg() >= 2) {
                        try {
                            UUID id = UUID.fromString(args.arg(1).tojstring());
                            String key = args.arg(2).tojstring();
                            int valCount = args.narg() - 2;
                            double[] values = new double[valCount];
                            for (int i = 0; i < valCount; i++) {
                                values[i] = args.arg(i + 3).todouble();
                            }
                            IPeripheral p = PeripheralRegistry.getPeripheral(finalSputnik.getLevel(), id);
                            if (p != null) {
                                p.writeValues(key, values);
                            }
                        } catch (Exception e) {}
                    }
                    return LuaValue.NIL;
                }
            });

            // display(name, value) -> writes to Sputnik's display bridge
            globals[0].set("display", new TwoArgFunction() {
                @Override public LuaValue call(LuaValue arg1, LuaValue arg2) {
                    if (finalSputnik != null) {
                        String name = arg1.tojstring();
                        double val = arg2.todouble();
                        finalSputnik.getDisplayBridge().put(name, val);
                    }
                    return LuaValue.NIL;
                }
            });

            // ---------------------------------------------------------------
            // Radio API
            // ---------------------------------------------------------------

            // registerFrequency(freq) — register this node as a radio node on a frequency
            globals[0].set("registerFrequency", new OneArgFunction() {
                @Override public LuaValue call(LuaValue arg) {
                    if (finalSputnik != null && finalSputnik.getLevel() != null &&
                        !finalSputnik.getLevel().isClientSide() && finalSputnik.getLevel().getServer() != null) {
                        double freq = arg.todouble();
                        Long dsInstanceId = null;
                        if (dev.devce.rocketnautics.api.orbit.DeepSpaceHelper.isDeepSpace(finalSputnik.getLevel())) {
                            var dsData = dev.devce.rocketnautics.content.orbit.DeepSpaceData.getInstance(finalSputnik.getLevel().getServer());
                            var inst = dsData.getInstanceForPos((int) finalSputnik.getGlobalPos().x, (int) finalSputnik.getGlobalPos().z);
                            if (inst != null) dsInstanceId = inst.getId();
                        }
                        String code = n.getCustomData().getString("code");
                        var radioMgr = dev.devce.rocketnautics.api.radio.RadioNetworkManager.getInstance(finalSputnik.getLevel().getServer());
                        org.joml.Vector3d pos = finalSputnik.getGlobalPos();
                        String dim = finalSputnik.getLevel().dimension().location().toString();
                        radioMgr.registerOrUpdateNode(n.getId(), freq, code, dsInstanceId, pos.x, pos.y, pos.z, dim);
                    }
                    return LuaValue.NIL;
                }
            });

            // sendPacket(channel, code, value) — broadcast a radio packet
            globals[0].set("sendPacket", new ThreeArgFunction() {
                @Override public LuaValue call(LuaValue arg1, LuaValue arg2, LuaValue arg3) {
                    if (finalSputnik != null && finalSputnik.getLevel() != null &&
                        !finalSputnik.getLevel().isClientSide() && finalSputnik.getLevel().getServer() != null) {
                        double channel = arg1.todouble();
                        double code    = arg2.todouble();
                        double value   = arg3.todouble();
                        if (channel != 0) {
                            var radioMgr = dev.devce.rocketnautics.api.radio.RadioNetworkManager.getInstance(finalSputnik.getLevel().getServer());
                            radioMgr.sendPacket(channel, code, value);
                        }
                    }
                    return LuaValue.NIL;
                }
            });

            // receivePacket(channel, code) -> value (last received)
            globals[0].set("receivePacket", new TwoArgFunction() {
                @Override public LuaValue call(LuaValue arg1, LuaValue arg2) {
                    if (finalSputnik != null && finalSputnik.getLevel() != null &&
                        !finalSputnik.getLevel().isClientSide() && finalSputnik.getLevel().getServer() != null) {
                        double channel = arg1.todouble();
                        double code    = arg2.todouble();
                        var radioMgr = dev.devce.rocketnautics.api.radio.RadioNetworkManager.getInstance(finalSputnik.getLevel().getServer());
                        return LuaValue.valueOf(radioMgr.getLastValue(channel, code));
                    }
                    return LuaValue.ZERO;
                }
            });

            // ---------------------------------------------------------------
            // World API
            // ---------------------------------------------------------------

            globals[0].set("getDimension", new ZeroArgFunction() {
                @Override public LuaValue call() {
                    return finalSputnik != null ? LuaValue.valueOf(finalSputnik.getDimensionId()) : LuaValue.valueOf("unknown");
                }
            });

            globals[0].set("getWorldTime", new ZeroArgFunction() {
                @Override public LuaValue call() {
                    return finalSputnik != null ? LuaValue.valueOf((double) finalSputnik.getWorldTime()) : LuaValue.ZERO;
                }
            });

            globals[0].set("getLightLevel", new ZeroArgFunction() {
                @Override public LuaValue call() {
                    return finalSputnik != null ? LuaValue.valueOf(finalSputnik.getLightLevel()) : LuaValue.ZERO;
                }
            });

            globals[0].set("getTemperature", new ZeroArgFunction() {
                @Override public LuaValue call() {
                    return finalSputnik != null ? LuaValue.valueOf(finalSputnik.getTemperature()) : LuaValue.ZERO;
                }
            });

            globals[0].set("getBiome", new ZeroArgFunction() {
                @Override public LuaValue call() {
                    return finalSputnik != null ? LuaValue.valueOf(finalSputnik.getGlobalBiomeName()) : LuaValue.valueOf("unknown");
                }
            });

            globals[0].set("getSpeed", new ZeroArgFunction() {
                @Override public LuaValue call() {
                    return finalSputnik != null ? LuaValue.valueOf(finalSputnik.getSpeed()) : LuaValue.ZERO;
                }
            });

            // ---------------------------------------------------------------
            // DeepSpace API
            // ---------------------------------------------------------------

            // getDeepSpaceOrbitalData() -> sma, ecc, inc, period, speed
            globals[0].set("getDeepSpaceOrbitalData", new VarArgFunction() {
                @Override public Varargs invoke(Varargs args) {
                    if (finalSputnik != null) {
                        return varargsOf(new LuaValue[] {
                            LuaValue.valueOf(finalSputnik.getOrbitalSemiMajorAxis()),
                            LuaValue.valueOf(finalSputnik.getOrbitalEccentricity()),
                            LuaValue.valueOf(finalSputnik.getOrbitalInclination()),
                            LuaValue.valueOf(finalSputnik.getOrbitalPeriod()),
                            LuaValue.valueOf(finalSputnik.getOrbitalSpeed())
                        });
                    }
                    return varargsOf(new LuaValue[] { LuaValue.ZERO, LuaValue.ZERO, LuaValue.ZERO, LuaValue.ZERO, LuaValue.ZERO });
                }
            });

            globals[0].set("getDeepSpaceGravity", new ZeroArgFunction() {
                @Override public LuaValue call() {
                    return finalSputnik != null ? LuaValue.valueOf(finalSputnik.getGravityAcceleration()) : LuaValue.ZERO;
                }
            });

            globals[0].set("getDeepSpaceParentBody", new ZeroArgFunction() {
                @Override public LuaValue call() {
                    return finalSputnik != null ? LuaValue.valueOf(finalSputnik.getParentBodyName()) : LuaValue.valueOf("unknown");
                }
            });

            globals[0].set("getDeepSpaceParentRadius", new ZeroArgFunction() {
                @Override public LuaValue call() {
                    return finalSputnik != null ? LuaValue.valueOf(finalSputnik.getParentBodyRadius()) : LuaValue.ZERO;
                }
            });

            globals[0].set("getDeepSpaceDistanceToPlanet", new ZeroArgFunction() {
                @Override public LuaValue call() {
                    return finalSputnik != null ? LuaValue.valueOf(finalSputnik.getDistanceToPlanet()) : LuaValue.ZERO;
                }
            });

            globals[0].set("isInAtmosphere", new ZeroArgFunction() {
                @Override public LuaValue call() {
                    return finalSputnik != null ? LuaValue.valueOf(finalSputnik.isInAtmosphere() ? 1 : 0) : LuaValue.ZERO;
                }
            });

            globals[0].set("getAtmosphereFlags", new ZeroArgFunction() {
                @Override public LuaValue call() {
                    return finalSputnik != null ? LuaValue.valueOf(finalSputnik.getAtmosphereFlags()) : LuaValue.valueOf("");
                }
            });

            globals[0].set("getUniverseTime", new ZeroArgFunction() {
                @Override public LuaValue call() {
                    return finalSputnik != null ? LuaValue.valueOf((double) finalSputnik.getUniverseTime()) : LuaValue.ZERO;
                }
            });

            try {
                chunk[0].call();
                n.getCustomData().putBoolean("err", false);
                n.getCustomData().putString("errMsg", "");
            } catch (Throwable e) {
                n.getCustomData().putBoolean("err", true);
                n.getCustomData().putBoolean("failed", true);
                n.getCustomData().putString("errMsg", e.getMessage() != null ? e.getMessage() : e.toString());
            }
        });
    }

    private static void syncFunctionPins(WNode node) {
        WGraph sub = node.getInternalGraph();
        if (sub == null) return;

        List<WNode> inputs = sub.getNodes().stream().filter(n -> n.getTypeId().getPath().equals("input")).toList();
        List<WNode> outputs = sub.getNodes().stream().filter(n -> n.getTypeId().getPath().equals("output")).toList();

        if (node.getInputs().size() != inputs.size() || node.getOutputs().size() != outputs.size()) {
            node.clearInputs();
            for (WNode in : inputs) {
                String name = "in";
                try { name = ((dev.devce.websnodelib.api.elements.WTextField)in.getElements().get(0)).getValue(); } catch(Exception e){}
                node.addInput(name, 0xFFFFFFFF);
            }

            node.clearOutputs();
            for (WNode out : outputs) {
                String name = "out";
                try { name = ((dev.devce.websnodelib.api.elements.WTextField)out.getElements().get(0)).getValue(); } catch(Exception e){}
                node.addOutput(name, 0xFFFFFFFF);
            }
        }
    }
}
