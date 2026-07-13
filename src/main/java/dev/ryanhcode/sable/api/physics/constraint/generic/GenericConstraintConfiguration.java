package dev.ryanhcode.sable.api.physics.constraint.generic;

import dev.ryanhcode.sable.api.physics.constraint.ConstraintJointAxis;
import org.joml.Quaterniondc;
import org.joml.Vector3dc;
import java.util.Set;
import java.util.EnumSet;

public record GenericConstraintConfiguration(
        Vector3dc pos1,
        Vector3dc pos2,
        Quaterniondc orientation1,
        Quaterniondc orientation2,
        Set<ConstraintJointAxis> lockedAxes
) {
    public GenericConstraintConfiguration(Vector3dc pos1, Vector3dc pos2, Quaterniondc orientation1, Quaterniondc orientation2) {
        this(pos1, pos2, orientation1, orientation2, EnumSet.noneOf(ConstraintJointAxis.class));
    }
}
