package dev.devce.rocketnautics.registry;

import dev.devce.rocketnautics.RocketNautics;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.neoforged.bus.api.IEventBus;

import java.util.function.Consumer;

import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.Shapes;

public class RocketFluids {
    public static final DeferredRegister<FluidType> FLUID_TYPES = DeferredRegister.create(NeoForgeRegistries.FLUID_TYPES, RocketNautics.MODID);
    public static final DeferredRegister<Fluid> FLUIDS = DeferredRegister.create(BuiltInRegistries.FLUID, RocketNautics.MODID);

    public static final DeferredHolder<FluidType, FluidType> ROCKET_EXHAUST_TYPE = FLUID_TYPES.register("rocket_exhaust",
            () -> new FluidType(FluidType.Properties.create()
                    .density(-1000)
                    .temperature(1000)
                    .viscosity(100)
                    .descriptionId("fluid.rocketnautics.rocket_exhaust")
                    .motionScale(0.007)
                    .canSwim(false)
                    .canDrown(false)
            ) {
                @Override
                public void initializeClient(Consumer<IClientFluidTypeExtensions> consumer) {
                    consumer.accept(new IClientFluidTypeExtensions() {
                        private static final ResourceLocation STILL = ResourceLocation.withDefaultNamespace("block/water_still");
                        private static final ResourceLocation FLOW = ResourceLocation.withDefaultNamespace("block/water_flow");

                        @Override
                        public ResourceLocation getStillTexture() {
                            return STILL;
                        }

                        @Override
                        public ResourceLocation getFlowingTexture() {
                            return FLOW;
                        }

                        @Override
                        public int getTintColor() {
                            return 0xCCFFFFFF;
                        }
                    });
                }

                @Override
                public boolean isVaporizedOnPlacement(net.minecraft.world.level.Level level, net.minecraft.core.BlockPos pos, net.neoforged.neoforge.fluids.FluidStack stack) {
                    return true;
                }

                @Override
                public void onVaporize(net.minecraft.world.entity.player.Player player, net.minecraft.world.level.Level level, net.minecraft.core.BlockPos pos, net.neoforged.neoforge.fluids.FluidStack stack) {
                    level.playSound(player, pos, net.minecraft.sounds.SoundEvents.FIRE_EXTINGUISH, net.minecraft.sounds.SoundSource.BLOCKS, 0.5F, 2.6F + (level.random.nextFloat() - level.random.nextFloat()) * 0.8F);
                    if (level.isClientSide) {
                        net.minecraft.util.RandomSource random = level.getRandom();
                        for (int i = 0; i < 8; i++) {
                            double x = pos.getX() + 0.5 + (random.nextDouble() - 0.5) * 0.2;
                            double y = pos.getY() + 0.5 + (random.nextDouble() - 0.5) * 0.2;
                            double z = pos.getZ() + 0.5 + (random.nextDouble() - 0.5) * 0.2;

                            double speedX = (random.nextDouble() - 0.5) * 0.1;
                            double speedY = 0.1 + random.nextDouble() * 0.15;
                            double speedZ = (random.nextDouble() - 0.5) * 0.1;

                            level.addParticle(net.minecraft.core.particles.ParticleTypes.CLOUD, x, y, z, speedX, speedY, speedZ);
                        }
                    }
                }
            }
    );

    public static final DeferredHolder<Fluid, Fluid> ROCKET_EXHAUST = FLUIDS.register("rocket_exhaust",
            () -> new Fluid() {
                @Override
                public VoxelShape getShape(FluidState state, BlockGetter level, BlockPos pos) {
                    return Shapes.empty();
                }

                @Override
                public float getOwnHeight(FluidState state) {
                    return 1.0f;
                }

                @Override
                public float getHeight(FluidState state, BlockGetter level, BlockPos pos) {
                    return 1.0f;
                }

                @Override
                public FluidType getFluidType() {
                    return ROCKET_EXHAUST_TYPE.get();
                }

                @Override
                protected boolean canBeReplacedWith(FluidState state, BlockGetter level, BlockPos pos, Fluid fluid, Direction direction) {
                    return true;
                }

                @Override
                protected Vec3 getFlow(BlockGetter level, BlockPos pos, FluidState state) {
                    return Vec3.ZERO;
                }

                @Override
                public int getTickDelay(LevelReader level) {
                    return 5;
                }

                @Override
                protected float getExplosionResistance() {
                    return 100.0f;
                }

                @Override
                protected BlockState createLegacyBlock(FluidState state) {
                    return Blocks.AIR.defaultBlockState();
                }

                @Override
                public boolean isSource(FluidState state) {
                    return true;
                }

                @Override
                public int getAmount(FluidState state) {
                    return 8;
                }

                @Override
                public Item getBucket() {
                    return Items.AIR;
                }
            }
    );

    public static void register(IEventBus eventBus) {
        FLUID_TYPES.register(eventBus);
        FLUIDS.register(eventBus);
    }
}
