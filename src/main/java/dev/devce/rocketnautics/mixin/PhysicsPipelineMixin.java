package dev.devce.rocketnautics.mixin;

import dev.ryanhcode.sable.api.physics.PhysicsPipeline;
import dev.ryanhcode.sable.api.physics.PhysicsPipelineBody;
import dev.ryanhcode.sable.api.physics.constraint.PhysicsConstraintConfiguration;
import dev.ryanhcode.sable.api.physics.constraint.PhysicsConstraintHandle;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(value = PhysicsPipeline.class, remap = false)
public interface PhysicsPipelineMixin extends PhysicsPipeline {

    // Default compatibility method added directly to the PhysicsPipeline interface
    default <T extends PhysicsConstraintHandle> T addConstraint(
            dev.ryanhcode.sable.sublevel.ServerSubLevel bodyA, 
            dev.ryanhcode.sable.sublevel.ServerSubLevel bodyB, 
            PhysicsConstraintConfiguration<T> configuration) {
        
        return this.addConstraint(
                (PhysicsPipelineBody) bodyA, 
                (PhysicsPipelineBody) bodyB, 
                configuration
        );
    }
}
