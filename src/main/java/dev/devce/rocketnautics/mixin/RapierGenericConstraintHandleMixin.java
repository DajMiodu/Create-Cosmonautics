package dev.devce.rocketnautics.mixin;

import org.spongepowered.asm.mixin.Mixin;

@Mixin(targets = "dev.ryanhcode.sable.physics.impl.rapier.constraint.generic.RapierGenericConstraintHandle", remap = false)
public interface RapierGenericConstraintHandleMixin extends dev.ryanhcode.sable.api.physics.constraint.generic.GenericConstraintHandle {
}
