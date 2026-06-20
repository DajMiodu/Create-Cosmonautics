package dev.devce.rocketnautics.mixin;

import dev.ryanhcode.sable.api.physics.PhysicsPipeline;
import dev.ryanhcode.sable.api.physics.PhysicsPipelineBody;
import dev.ryanhcode.sable.api.physics.constraint.PhysicsConstraintConfiguration;
import dev.ryanhcode.sable.api.physics.constraint.PhysicsConstraintHandle;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "dev.ryanhcode.sable.physics.impl.rapier.RapierPhysicsPipeline", remap = false)
public class RapierPhysicsPipelineMixin {

    @SuppressWarnings("unchecked")
    @Inject(method = "addConstraint", at = @At("HEAD"), cancellable = true)
    private <T extends PhysicsConstraintHandle> void onAddConstraint(
            PhysicsPipelineBody bodyA, 
            PhysicsPipelineBody bodyB, 
            PhysicsConstraintConfiguration<T> configuration, 
            CallbackInfoReturnable<T> cir) {
        
        Object configObj = (Object) configuration;
        
        if (configObj instanceof dev.ryanhcode.sable.api.physics.constraint.fixed.FixedConstraintConfiguration dummy) {
            T result = ((PhysicsPipeline) (Object) this).addConstraint(bodyA, bodyB, 
                (PhysicsConstraintConfiguration<T>) (Object) new dev.ryanhcode.sable.api.physics.constraint.FixedConstraintConfiguration(
                    dummy.pos1(), dummy.pos2(), dummy.orientation()
                )
            );
            cir.setReturnValue(result);
        } else if (configObj instanceof dev.ryanhcode.sable.api.physics.constraint.free.FreeConstraintConfiguration dummy) {
            T result = ((PhysicsPipeline) (Object) this).addConstraint(bodyA, bodyB, 
                (PhysicsConstraintConfiguration<T>) (Object) new dev.ryanhcode.sable.api.physics.constraint.FreeConstraintConfiguration(
                    dummy.pos1(), dummy.pos2(), dummy.orientation()
                )
            );
            cir.setReturnValue(result);
        } else if (configObj instanceof dev.ryanhcode.sable.api.physics.constraint.rotary.RotaryConstraintConfiguration dummy) {
            T result = ((PhysicsPipeline) (Object) this).addConstraint(bodyA, bodyB, 
                (PhysicsConstraintConfiguration<T>) (Object) new dev.ryanhcode.sable.api.physics.constraint.RotaryConstraintConfiguration(
                    dummy.pos1(), dummy.pos2(), dummy.normal1(), dummy.normal2()
                )
            );
            cir.setReturnValue(result);
        } else if (configObj instanceof dev.ryanhcode.sable.api.physics.constraint.generic.GenericConstraintConfiguration dummy) {
            T result = ((PhysicsPipeline) (Object) this).addConstraint(bodyA, bodyB, 
                (PhysicsConstraintConfiguration<T>) (Object) new dev.ryanhcode.sable.api.physics.constraint.GenericConstraintConfiguration(
                    dummy.pos1(), dummy.pos2(), dummy.orientation1(), dummy.orientation2(), dummy.lockedAxes()
                )
            );
            cir.setReturnValue(result);
        }
    }
}
