package com.beatcraft.animation.pointdefinition;

import java.util.function.Function;

public class Point<T> {
    T value;
    float time;
    Function<Float, Float> easing;
    boolean spline;
}
