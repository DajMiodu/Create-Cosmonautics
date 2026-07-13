package dev.ryanhcode.sable.api.physics.constraint.free;

import org.joml.Quaterniondc;
import org.joml.Vector3dc;

public record FreeConstraintConfiguration(Vector3dc pos1, Vector3dc pos2, Quaterniondc orientation) {
}
