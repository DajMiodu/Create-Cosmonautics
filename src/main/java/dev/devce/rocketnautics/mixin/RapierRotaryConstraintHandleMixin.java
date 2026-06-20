package dev.devce.rocketnautics.mixin;

import org.spongepowered.asm.mixin.Mixin;

@Mixin(targets = "dev.ryanhcode.sable.physics.impl.rapier.constraint.rotary.RapierRotaryConstraintHandle", remap = false)
public interface RapierRotaryConstraintHandleMixin extends dev.ryanhcode.sable.api.physics.constraint.rotary.RotaryConstraintHandle {
}
