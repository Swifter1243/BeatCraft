package com.beatcraft.render.instancing.lightshow.kaleidoscope;

import com.beatcraft.render.instancing.InstancedMesh;
import org.joml.Matrix4f;

import java.nio.FloatBuffer;

public class SpikeInstanceData implements InstancedMesh.InstanceData {
    private Matrix4f transform;

    private int TRANSFORM_LOCATION = 3;
    private int LEFT_COLOR_0 = 7;
    private int LEFT_COLOR_1 = 8;
    private int LEFT_COLOR_2 = 9;
    private int LEFT_COLOR_3 = 10;

    @Override
    public Matrix4f getTransform() {
        return transform;
    }

    @Override
    public void putData(FloatBuffer buffer) {

    }

    @Override
    public int getFrameSize() {
        return 16;
    }

    @Override
    public void init() {

    }

    @Override
    public int[] getLocations() {
        return new int[0];
    }

    @Override
    public void setup(int program) {

    }

    @Override
    public void cleanup() {

    }

    @Override
    public void free() {

    }

    @Override
    public InstancedMesh.InstanceData copy() {
        return null;
    }
}
