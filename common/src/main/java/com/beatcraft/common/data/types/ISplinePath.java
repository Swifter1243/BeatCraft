package com.beatcraft.common.data.types;
import org.joml.Vector3f;

import java.util.List;

public interface ISplinePath {
    /// interpolate on a path from 0-1
    Vector3f evaluate(float t);

    /// gets the derivative of the path from 0-1
    Vector3f getTangent(float t);

    List<Vector3f> getControlPoints();
}
