package dev.devce.rocketnautics.content.blocks.hose;

import com.simibubi.create.content.fluids.FluidPropagator;
import com.simibubi.create.content.fluids.FluidTransportBehaviour;
import com.simibubi.create.content.fluids.pump.PumpBlock;
import com.simibubi.create.content.fluids.pump.PumpBlockEntity;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import dev.simulated_team.simulated.content.blocks.rope.RopeStrandHolderBehavior;
import dev.simulated_team.simulated.content.blocks.rope.RopeStrandHolderBlockEntity;
import dev.simulated_team.simulated.content.blocks.rope.strand.client.ClientRopeStrand;
import dev.simulated_team.simulated.content.blocks.rope.strand.server.RopeAttachment;
import dev.simulated_team.simulated.content.blocks.rope.strand.server.RopeAttachmentPoint;
import dev.simulated_team.simulated.content.blocks.rope.strand.server.ServerRopeStrand;
import net.createmod.catnip.data.Iterate;
import net.createmod.catnip.data.Pair;
import net.createmod.catnip.math.BlockFace;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;

import java.util.*;

public class HoseAnchorBlockEntity extends SmartBlockEntity implements RopeStrandHolderBlockEntity,
        com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation {
    public static final double RENDER_BOUNDING_BOX_INFLATION = 3.0;
    private static final int TRANSFER_RATE = 250;

    private RopeStrandHolderBehavior ropeHolder;
    public final FluidTank fuelTank = new FluidTank(1000, fluid -> true);

    /** Tracks whether we were outputting pressure last tick (for proper reset on idle). */
    private boolean wasOutputting = false;

    public HoseAnchorBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public RopeStrandHolderBehavior getRopeHolder() { return this.ropeHolder; }

    @Override
    public RopeStrandHolderBehavior getBehavior() { return this.ropeHolder; }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        behaviours.add(this.ropeHolder = new RopeStrandHolderBehavior(this));
    }

    @Override
    public void tick() {
        super.tick();
        if (level == null) return;

        if (level.isClientSide) {
            invalidateRenderBoundingBox();
            return;
        }

        ServerRopeStrand strand = ropeHolder.getAttachedStrand();
        if (strand == null) {
            if (wasOutputting) {
                Direction pipeDir = getBlockState().getValue(HoseAnchorBlock.FACING).getOpposite();
                FluidPropagator.resetAffectedFluidNetworks(level, worldPosition, pipeDir);
                wasOutputting = false;
            }
            return;
        }

        if (ropeHolder.ownsRope()) {
            RopeAttachment startAttach = strand.getAttachment(RopeAttachmentPoint.START);
            RopeAttachment endAttach   = strand.getAttachment(RopeAttachmentPoint.END);
            if (startAttach != null && endAttach != null) {
                BlockPos startPos = startAttach.blockAttachment();
                BlockPos endPos   = endAttach.blockAttachment();

                BlockEntity beStart = getBlockEntityAcrossLevels(level, startPos);
                BlockEntity beEnd   = getBlockEntityAcrossLevels(level, endPos);

                if (beStart instanceof HoseAnchorBlockEntity startAnchor && beEnd instanceof HoseAnchorBlockEntity endAnchor) {
                    equalizeFluid(startAnchor, endAnchor);

                    int startAmt = startAnchor.fuelTank.getFluidAmount();
                    int endAmt   = endAnchor.fuelTank.getFluidAmount();

                    if (startAmt > endAmt) {
                        // Flow is Start -> End. End anchor is the output and must push pressure.
                        tickAnchorPressure(endAnchor, true);
                        tickAnchorPressure(startAnchor, false);
                    } else if (endAmt > startAmt) {
                        // Flow is End -> Start. Start anchor is the output and must push pressure.
                        tickAnchorPressure(startAnchor, true);
                        tickAnchorPressure(endAnchor, false);
                    } else if (startAmt > 0) {
                        // Equal levels and not empty: sustain active output pressure, or default to End anchor.
                        if (startAnchor.wasOutputting) {
                            tickAnchorPressure(startAnchor, true);
                            tickAnchorPressure(endAnchor, false);
                        } else {
                            tickAnchorPressure(endAnchor, true);
                            tickAnchorPressure(startAnchor, false);
                        }
                    } else {
                        // Both empty.
                        tickAnchorPressure(startAnchor, false);
                        tickAnchorPressure(endAnchor, false);
                    }
                }
            }
        }
    }

    private static BlockEntity getBlockEntityAcrossLevels(Level currentLevel, BlockPos pos) {
        BlockEntity localBe = currentLevel.getBlockEntity(pos);
        if (localBe instanceof HoseAnchorBlockEntity) {
            return localBe;
        }
        if (currentLevel.getServer() != null) {
            for (net.minecraft.server.level.ServerLevel serverLevel : currentLevel.getServer().getAllLevels()) {
                if (serverLevel == currentLevel) continue;
                BlockEntity be = serverLevel.getBlockEntity(pos);
                if (be instanceof HoseAnchorBlockEntity) {
                    return be;
                }
            }
        }
        return null;
    }

    private static void equalizeFluid(HoseAnchorBlockEntity a, HoseAnchorBlockEntity b) {
        int aAmt = a.fuelTank.getFluidAmount();
        int bAmt = b.fuelTank.getFluidAmount();

        if (aAmt == bAmt) return;

        if (aAmt > bAmt) {
            FluidStack available = a.fuelTank.drain(TRANSFER_RATE, IFluidHandler.FluidAction.SIMULATE);
            if (!available.isEmpty()) {
                int accepted = b.fuelTank.fill(available, IFluidHandler.FluidAction.SIMULATE);
                if (accepted > 0) {
                    int toMove = Math.min(available.getAmount(), accepted);
                    FluidStack moving = available.copy();
                    moving.setAmount(toMove);
                    a.fuelTank.drain(toMove, IFluidHandler.FluidAction.EXECUTE);
                    b.fuelTank.fill(moving, IFluidHandler.FluidAction.EXECUTE);
                    a.setChanged();
                    b.setChanged();
                }
            }
        } else {
            FluidStack available = b.fuelTank.drain(TRANSFER_RATE, IFluidHandler.FluidAction.SIMULATE);
            if (!available.isEmpty()) {
                int accepted = a.fuelTank.fill(available, IFluidHandler.FluidAction.SIMULATE);
                if (accepted > 0) {
                    int toMove = Math.min(available.getAmount(), accepted);
                    FluidStack moving = available.copy();
                    moving.setAmount(toMove);
                    b.fuelTank.drain(toMove, IFluidHandler.FluidAction.EXECUTE);
                    a.fuelTank.fill(moving, IFluidHandler.FluidAction.EXECUTE);
                    a.setChanged();
                    b.setChanged();
                }
            }
        }
    }

    private static void tickAnchorPressure(HoseAnchorBlockEntity anchor, boolean active) {
        if (active && !anchor.fuelTank.isEmpty()) {
            Direction facing = anchor.getBlockState().getValue(HoseAnchorBlock.FACING);
            Direction pipeDir = facing.getOpposite();
            BlockPos connectedPos = anchor.worldPosition.relative(pipeDir);
            IFluidHandler targetHandler = anchor.level.getCapability(Capabilities.FluidHandler.BLOCK, connectedPos, facing);

            if (targetHandler != null) {
                FluidStack available = anchor.fuelTank.drain(TRANSFER_RATE, IFluidHandler.FluidAction.SIMULATE);
                if (!available.isEmpty()) {
                    int accepted = targetHandler.fill(available, IFluidHandler.FluidAction.SIMULATE);
                    if (accepted > 0) {
                        int toMove = Math.min(available.getAmount(), accepted);
                        FluidStack moving = available.copy();
                        moving.setAmount(toMove);
                        anchor.fuelTank.drain(toMove, IFluidHandler.FluidAction.EXECUTE);
                        targetHandler.fill(moving, IFluidHandler.FluidAction.EXECUTE);
                        anchor.setChanged();
                    }
                }
            } else {
                anchor.distributeOutputPressure();
                anchor.wasOutputting = true;
            }
        } else if (anchor.wasOutputting) {
            Direction pipeDir = anchor.getBlockState().getValue(HoseAnchorBlock.FACING).getOpposite();
            FluidPropagator.resetAffectedFluidNetworks(anchor.level, anchor.worldPosition, pipeDir);
            anchor.wasOutputting = false;
        }
    }

    private void distributeOutputPressure() {
        if (level == null || level.isClientSide) return;

        Direction facing  = getBlockState().getValue(HoseAnchorBlock.FACING);
        Direction pipeDir = facing.getOpposite();
        BlockPos  targetPos = worldPosition.relative(pipeDir);

        BlockFace start = new BlockFace(worldPosition, pipeDir);
        boolean pull = false; // we are PUSHING to destination

        buildAndApplyPressure(start, targetPos, pipeDir, pull);
    }

    private void buildAndApplyPressure(BlockFace start, BlockPos targetPos, Direction pipeDir, boolean pull) {

        Set<BlockFace> targets = new HashSet<>();
        Map<BlockPos, Pair<Integer, Map<Direction, Boolean>>> pipeGraph = new HashMap<>();

        pipeGraph.computeIfAbsent(worldPosition, $ -> Pair.of(0, new IdentityHashMap<>()))
                .getSecond().put(pipeDir, pull);
        pipeGraph.computeIfAbsent(targetPos, $ -> Pair.of(1, new IdentityHashMap<>()))
                .getSecond().put(pipeDir.getOpposite(), !pull);

        List<Pair<Integer, BlockPos>> frontier = new ArrayList<>();
        Set<BlockPos> visited = new HashSet<>();
        int maxDistance = FluidPropagator.getPumpRange();
        frontier.add(Pair.of(1, targetPos));

        while (!frontier.isEmpty()) {
            Pair<Integer, BlockPos> entry = frontier.remove(0);
            int distance = entry.getFirst();
            BlockPos currentPos = entry.getSecond();

            if (!level.isLoaded(currentPos)) continue;
            if (visited.contains(currentPos)) continue;
            visited.add(currentPos);

            BlockState currentState = level.getBlockState(currentPos);
            FluidTransportBehaviour pipe = FluidPropagator.getPipe(level, currentPos);
            if (pipe == null) continue;

            for (Direction face : FluidPropagator.getPipeConnections(currentState, pipe)) {
                BlockFace blockFace = new BlockFace(currentPos, face);
                BlockPos connectedPos = blockFace.getConnectedPos();

                if (!level.isLoaded(connectedPos)) continue;
                if (blockFace.isEquivalent(start)) continue;

                if (hasReachedValidEndpoint(blockFace, pull)) {
                    pipeGraph.computeIfAbsent(currentPos, $ -> Pair.of(distance, new IdentityHashMap<>()))
                            .getSecond().put(face, pull);
                    targets.add(blockFace);
                    continue;
                }

                FluidTransportBehaviour pipeBehaviour = FluidPropagator.getPipe(level, connectedPos);
                if (pipeBehaviour == null) continue;
                if (pipeBehaviour.getClass().getSimpleName().equals("PumpFluidTransferBehaviour")) continue;
                if (visited.contains(connectedPos)) continue;
                if (distance + 1 >= maxDistance) {
                    pipeGraph.computeIfAbsent(currentPos, $ -> Pair.of(distance, new IdentityHashMap<>()))
                            .getSecond().put(face, pull);
                    targets.add(blockFace);
                    continue;
                }

                pipeGraph.computeIfAbsent(currentPos, $ -> Pair.of(distance, new IdentityHashMap<>()))
                        .getSecond().put(face, pull);
                pipeGraph.computeIfAbsent(connectedPos, $ -> Pair.of(distance + 1, new IdentityHashMap<>()))
                        .getSecond().put(face.getOpposite(), !pull);
                frontier.add(Pair.of(distance + 1, connectedPos));
            }
        }

        // Build valid faces via recursive search, then add pressure
        Map<Integer, Set<BlockFace>> validFaces = new HashMap<>();
        searchForEndpointRecursively(pipeGraph, targets, validFaces,
                new BlockFace(start.getPos(), start.getOppositeFace()), pull);

        float pressure = 128.0f;
        for (Set<BlockFace> set : validFaces.values()) {
            int parallelBranches = Math.max(1, set.size() - 1);
            for (BlockFace face : set) {
                BlockPos pipePos = face.getPos();
                Direction pipeSide = face.getFace();

                if (pipePos.equals(worldPosition)) continue;

                Boolean inbound = pipeGraph.get(pipePos).getSecond().get(pipeSide);
                if (inbound == null) continue;

                FluidTransportBehaviour pipeBehaviour = FluidPropagator.getPipe(level, pipePos);
                if (pipeBehaviour == null) continue;

                pipeBehaviour.addPressure(pipeSide, inbound, pressure / parallelBranches);
            }
        }
    }

    private boolean hasReachedValidEndpoint(BlockFace blockFace, boolean pull) {
        BlockPos connectedPos = blockFace.getConnectedPos();
        BlockState connectedState = level.getBlockState(connectedPos);
        BlockEntity blockEntity = level.getBlockEntity(connectedPos);
        Direction face = blockFace.getFace();

        // Don't route back to ourselves
        if (connectedPos.equals(worldPosition)) return false;

        // Facing a pump — respect pump direction
        if (PumpBlock.isPump(connectedState)
                && connectedState.getValue(PumpBlock.FACING).getAxis() == face.getAxis()
                && blockEntity instanceof PumpBlockEntity pumpBE) {
            Direction pumpFacing = connectedState.getValue(PumpBlock.FACING);
            boolean isFront = blockFace.getOppositeFace() == pumpFacing;
            boolean isPulling = !isFront;
            return isPulling != pull;
        }

        // Another pipe — not an endpoint
        FluidTransportBehaviour pipe = FluidPropagator.getPipe(level, connectedPos);
        if (pipe != null && pipe.canHaveFlowToward(connectedState, blockFace.getOppositeFace()))
            return false;

        // IFluidHandler endpoint (thruster, tank, etc.) — but not another hose anchor
        if (blockEntity != null && !(blockEntity instanceof HoseAnchorBlockEntity)) {
            IFluidHandler cap = level.getCapability(Capabilities.FluidHandler.BLOCK,
                    blockEntity.getBlockPos(), face.getOpposite());
            if (cap != null) return true;
        }

        return FluidPropagator.isOpenEnd(level, blockFace.getPos(), face);
    }

    private boolean searchForEndpointRecursively(
            Map<BlockPos, Pair<Integer, Map<Direction, Boolean>>> pipeGraph,
            Set<BlockFace> targets,
            Map<Integer, Set<BlockFace>> validFaces,
            BlockFace currentFace,
            boolean pull) {

        BlockPos currentPos = currentFace.getPos();
        if (!pipeGraph.containsKey(currentPos)) return false;

        Pair<Integer, Map<Direction, Boolean>> pair = pipeGraph.get(currentPos);
        int distance = pair.getFirst();

        boolean atLeastOne = false;
        for (Direction nextFacing : Iterate.directions) {
            if (nextFacing == currentFace.getFace()) continue;
            Map<Direction, Boolean> map = pair.getSecond();
            if (!map.containsKey(nextFacing)) continue;

            BlockFace localTarget = new BlockFace(currentPos, nextFacing);
            if (targets.contains(localTarget)) {
                validFaces.computeIfAbsent(distance, $ -> new HashSet<>()).add(localTarget);
                atLeastOne = true;
                continue;
            }

            if (map.get(nextFacing) != pull) continue;
            if (!searchForEndpointRecursively(pipeGraph, targets, validFaces,
                    new BlockFace(currentPos.relative(nextFacing), nextFacing.getOpposite()), pull))
                continue;

            validFaces.computeIfAbsent(distance, $ -> new HashSet<>()).add(localTarget);
            atLeastOne = true;
        }

        if (atLeastOne)
            validFaces.computeIfAbsent(distance, $ -> new HashSet<>()).add(currentFace);

        return atLeastOne;
    }

    // -------------------------------------------------------------------------
    //  Rendering / attachment
    // -------------------------------------------------------------------------

    @Override
    public AABB getRenderBoundingBox() {
        ClientRopeStrand rope = this.ropeHolder.getClientStrand();
        if (rope != null && this.ropeHolder.ownsRope()) {
            AABB bounds = rope.getBounds();
            if (bounds == null) return super.getRenderBoundingBox();
            return bounds.inflate(RENDER_BOUNDING_BOX_INFLATION);
        }
        return super.getRenderBoundingBox();
    }

    @Override
    public Vec3 getAttachmentPoint(BlockPos pos, BlockState state) {
        Direction facing = state.getValue(HoseAnchorBlock.FACING);
        double offset = 0.0;
        return pos.getCenter().add(facing.getStepX() * offset, facing.getStepY() * offset, facing.getStepZ() * offset);
    }

    @Override
    public Vec3 getVisualAttachmentPoint(BlockPos pos, BlockState state) {
        Direction facing = state.getValue(HoseAnchorBlock.FACING);
        double offset = 0.0;
        return pos.getCenter().add(facing.getStepX() * offset, facing.getStepY() * offset, facing.getStepZ() * offset);
    }

    @Override
    public boolean addToGoggleTooltip(List<net.minecraft.network.chat.Component> tooltip, boolean isPlayerSneaking) {
        tooltip.add(net.minecraft.network.chat.Component.literal("    ")
                .append(net.minecraft.network.chat.Component.translatable(getBlockState().getBlock().getDescriptionId())
                        .withStyle(net.minecraft.ChatFormatting.GOLD)));

        boolean isConnected = false;
        if (level != null) {
            if (level.isClientSide) {
                isConnected = ropeHolder.getClientStrand() != null;
            } else {
                isConnected = ropeHolder.getAttachedStrand() != null;
            }
        }

        tooltip.add(net.minecraft.network.chat.Component.literal("  ")
                .append(net.minecraft.network.chat.Component.translatable("rocketnautics.goggles.hose.status").withStyle(net.minecraft.ChatFormatting.GRAY))
                .append(": ")
                .append(isConnected 
                        ? net.minecraft.network.chat.Component.translatable("rocketnautics.goggles.hose.connected").withStyle(net.minecraft.ChatFormatting.GREEN)
                        : net.minecraft.network.chat.Component.translatable("rocketnautics.goggles.hose.disconnected").withStyle(net.minecraft.ChatFormatting.RED)));

        if (isConnected) {
            tooltip.add(net.minecraft.network.chat.Component.literal("  ")
                    .append(net.minecraft.network.chat.Component.translatable("rocketnautics.goggles.hose.role").withStyle(net.minecraft.ChatFormatting.GRAY))
                    .append(": ")
                    .append(wasOutputting 
                            ? net.minecraft.network.chat.Component.translatable("rocketnautics.goggles.hose.role.output").withStyle(net.minecraft.ChatFormatting.GOLD)
                            : net.minecraft.network.chat.Component.translatable("rocketnautics.goggles.hose.role.input").withStyle(net.minecraft.ChatFormatting.BLUE)));
        }

        if (!fuelTank.isEmpty()) {
            String fluidName = fuelTank.getFluid().getHoverName().getString();
            int amount = fuelTank.getFluidAmount();
            tooltip.add(net.minecraft.network.chat.Component.literal("  ")
                    .append(net.minecraft.network.chat.Component.translatable("rocketnautics.goggles.hose.contents").withStyle(net.minecraft.ChatFormatting.GRAY))
                    .append(": ")
                    .append(net.minecraft.network.chat.Component.literal(fluidName + " (" + amount + " / " + fuelTank.getCapacity() + " mB)")
                            .withStyle(net.minecraft.ChatFormatting.AQUA)));
        } else {
            tooltip.add(net.minecraft.network.chat.Component.literal("  ")
                    .append(net.minecraft.network.chat.Component.translatable("rocketnautics.goggles.hose.contents").withStyle(net.minecraft.ChatFormatting.GRAY))
                    .append(": ")
                    .append(net.minecraft.network.chat.Component.translatable("gui.rocketnautics.nozzle.missing").withStyle(net.minecraft.ChatFormatting.DARK_GRAY)));
        }

        return true;
    }

    // -------------------------------------------------------------------------
    //  NBT
    // -------------------------------------------------------------------------

    @Override
    protected void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.write(tag, registries, clientPacket);
        CompoundTag tankTag = new CompoundTag();
        fuelTank.writeToNBT(registries, tankTag);
        tag.put("FluidTank", tankTag);
    }

    @Override
    protected void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(tag, registries, clientPacket);
        if (tag.contains("FluidTank")) {
            fuelTank.readFromNBT(registries, tag.getCompound("FluidTank"));
        }
    }
}
