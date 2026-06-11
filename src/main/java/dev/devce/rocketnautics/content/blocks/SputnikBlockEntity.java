package dev.devce.rocketnautics.content.blocks;

import dev.devce.rocketnautics.api.peripherals.IPeripheral;
import dev.ryanhcode.sable.sublevel.SubLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Vector3d;
import dev.devce.websnodelib.api.WGraph;

/**
 * Sputnik Block Entity acts as the core central computer of a spacecraft.
 * It ticks the Lua script graph and provides vector telemetry (velocity, rotation, position).
 */
public class SputnikBlockEntity extends BlockEntity {
    public final WGraph graph = new WGraph();
    private final java.util.Map<String, Double> displayBridge = new java.util.concurrent.ConcurrentHashMap<>();

    public java.util.Map<String, Double> getDisplayBridge() {
        return displayBridge;
    }

    public SputnikBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        graph.setContext(this);
        if (level != null) graph.setRegistries(level.registryAccess());
    }

    @Override
    public void setLevel(Level level) {
        super.setLevel(level);
        if (level != null) {
            graph.setRegistries(level.registryAccess());
            graph.setContext(this);
        }
    }

    private SubLevel getSubLevel() {
        if (level == null) return null;
        Object lvlObj = level;
        if (lvlObj instanceof SubLevel sl) return sl;
        Object obj = dev.ryanhcode.sable.Sable.HELPER.getContaining(level, worldPosition);
        if (obj instanceof SubLevel sl) return sl;
        return null;
    }

    public static void tick(Level level, BlockPos pos, BlockState state, SputnikBlockEntity blockEntity) {
        if (!level.isClientSide) {
            blockEntity.tickNodes();
        }
    }

    private void tickNodes() {
        dev.devce.rocketnautics.content.blocks.LinkedSignalHandler.tick(level);
        graph.tick();
    }

    public double getX() { return getGlobalPos().x; }
    public double getY() { return getGlobalPos().y; }
    public double getZ() { return getGlobalPos().z; }

    public double getAltitude() {
        return getGlobalPos().y;
    }

    public double getVelocity() {
        SubLevel subLevel = getSubLevel();
        if (subLevel != null) {
            var pose = subLevel.logicalPose();
            var lastPose = subLevel.lastPose();
            return new Vector3d(pose.position()).distance(lastPose.position()) * 20.0;
        }
        return 0;
    }

    public Vector3d getVelocityVector() {
        SubLevel subLevel = getSubLevel();
        if (subLevel != null) {
            var pose = subLevel.logicalPose();
            var lastPose = subLevel.lastPose();
            return new Vector3d(pose.position()).sub(lastPose.position()).mul(20.0);
        }
        return new Vector3d(0, 0, 0);
    }

    public Vector3d getAngularVelocity() {
        SubLevel subLevel = getSubLevel();
        if (subLevel != null) {
            Vector3d currentEuler = subLevel.logicalPose().orientation().getEulerAnglesYXZ(new Vector3d());
            Vector3d lastEuler = subLevel.lastPose().orientation().getEulerAnglesYXZ(new Vector3d());

            double dx = Math.toDegrees(currentEuler.x - lastEuler.x);
            double dy = Math.toDegrees(currentEuler.y - lastEuler.y);
            double dz = Math.toDegrees(currentEuler.z - lastEuler.z);

            dx = normalizeAngleDifference(dx);
            dy = normalizeAngleDifference(dy);
            dz = normalizeAngleDifference(dz);

            return new Vector3d(dx * 20.0, dy * 20.0, dz * 20.0);
        }
        return new Vector3d(0, 0, 0);
    }

    private double normalizeAngleDifference(double angle) {
        while (angle < -180.0) angle += 360.0;
        while (angle > 180.0) angle -= 360.0;
        return angle;
    }

    public double getPitch() {
        SubLevel subLevel = getSubLevel();
        if (subLevel != null) {
            Vector3d euler = subLevel.logicalPose().orientation().getEulerAnglesYXZ(new Vector3d());
            return Math.toDegrees(euler.x);
        }
        return 0;
    }

    public double getYaw() {
        SubLevel subLevel = getSubLevel();
        if (subLevel != null) {
            Vector3d euler = subLevel.logicalPose().orientation().getEulerAnglesYXZ(new Vector3d());
            return Math.toDegrees(euler.y);
        }
        return 0;
    }

    public double getRoll() {
        SubLevel subLevel = getSubLevel();
        if (subLevel != null) {
            Vector3d euler = subLevel.logicalPose().orientation().getEulerAnglesYXZ(new Vector3d());
            return Math.toDegrees(euler.z);
        }
        return 0;
    }

    public int getBiomeColor() {
        if (level == null) return 0;
        Biome biome = level.getBiome(worldPosition).value();
        return biome.getFoliageColor();
    }

    public String getBiomeName() {
        if (level == null) return "Unknown";
        return level.getBiome(worldPosition).getRegisteredName();
    }

    public Vector3d getGlobalPos() {
        SubLevel subLevel = getSubLevel();
        if (subLevel != null) {
            return subLevel.logicalPose().position();
        }
        return new Vector3d(worldPosition.getX() + 0.5, worldPosition.getY() + 0.5, worldPosition.getZ() + 0.5);
    }

    public Biome getGlobalBiome() {
        if (level == null) return null;
        var subLevel = dev.ryanhcode.sable.Sable.HELPER.getContaining(level, worldPosition);

        if (subLevel != null && !level.isClientSide) {
            Vector3d global = getGlobalPos();
            if (this.level.getServer() != null) {
                for (var sl : this.level.getServer().getAllLevels()) {
                    var container = dev.ryanhcode.sable.api.sublevel.SubLevelContainer.getContainer(sl);
                    if (container != null) {
                        for (var slItem : container.getAllSubLevels()) {
                            if (slItem.getUniqueId().equals(subLevel.getUniqueId())) {
                                return sl.getBiome(BlockPos.containing(global.x, 64, global.z)).value();
                            }
                        }
                    }
                }
            }
        }

        return level.getBiome(worldPosition).value();
    }

    public int getGlobalBiomeColor() {
        Biome b = getGlobalBiome();
        return b != null ? b.getFoliageColor() : 0;
    }

    public String getGlobalBiomeName() {
        var subLevel = dev.ryanhcode.sable.Sable.HELPER.getContaining(level, worldPosition);
        if (subLevel != null && !level.isClientSide) {
            if (this.level.getServer() != null) {
                for (var sl : this.level.getServer().getAllLevels()) {
                    var container = dev.ryanhcode.sable.api.sublevel.SubLevelContainer.getContainer(sl);
                    if (container != null) {
                        for (var slItem : container.getAllSubLevels()) {
                            if (slItem.getUniqueId().equals(subLevel.getUniqueId())) {
                                Vector3d global = getGlobalPos();
                                return sl.getBiome(BlockPos.containing(global.x, 64, global.z)).getRegisteredName();
                            }
                        }
                    }
                }
            }
        }
        return getBiomeName();
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = super.getUpdateTag(registries);
        saveAdditional(tag, registries);
        return tag;
    }

    @Override
    public net.minecraft.network.protocol.Packet<net.minecraft.network.protocol.game.ClientGamePacketListener> getUpdatePacket() {
        return net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("NodeGraph", graph.save());
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("NodeGraph")) {
            CompoundTag graphTag = tag.getCompound("NodeGraph");
            graph.setRegistries(registries);
            graph.load(graphTag);
            graph.setContext(this);
        }
    }
}
