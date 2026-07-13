package dev.devce.rocketnautics.mixin;

import org.spongepowered.asm.mixin.Mixin;

@Mixin(targets = "dev.ryanhcode.sable.physics.impl.rapier.constraint.fixed.RapierFixedConstraintHandle", remap = false)
public interface RapierFixedConstraintHandleMixin extends dev.ryanhcode.sable.api.physics.constraint.fixed.FixedConstraintHandle {
}
