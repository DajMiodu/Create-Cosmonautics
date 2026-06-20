package dev.devce.rocketnautics.mixin;

import org.spongepowered.asm.mixin.Mixin;

@Mixin(targets = "dev.ryanhcode.sable.physics.impl.rapier.constraint.free.RapierFreeConstraintHandle", remap = false)
public interface RapierFreeConstraintHandleMixin extends dev.ryanhcode.sable.api.physics.constraint.free.FreeConstraintHandle {
}
