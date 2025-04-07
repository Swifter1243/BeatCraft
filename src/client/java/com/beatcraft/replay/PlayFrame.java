package com.beatcraft.replay;

import org.apache.http.util.ByteArrayBuffer;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.nio.ByteBuffer;

public record PlayFrame(float beat, Vector3f leftSaberPosition, Quaternionf leftSaberRotation, Vector3f rightSaberPosition, Quaternionf rightSaberRotation, Vector3f headPos, Quaternionf headRotation) {
    public void write(ByteBuffer buf) {
        buf.putFloat(beat);
        putVector3f(buf, leftSaberPosition);
        putQuaternionf(buf, leftSaberRotation);
        putVector3f(buf, rightSaberPosition);
        putQuaternionf(buf, rightSaberRotation);
        putVector3f(buf, headPos);
        putQuaternionf(buf, headRotation);
    }

    private static void putVector3f(ByteBuffer buf, Vector3f vec) {
        buf.putFloat(vec.x);
        buf.putFloat(vec.y);
        buf.putFloat(vec.z);
    }

    private static void putQuaternionf(ByteBuffer buf, Quaternionf quat) {
        buf.putFloat(quat.x);
        buf.putFloat(quat.y);
        buf.putFloat(quat.z);
        buf.putFloat(quat.w);
    }

    public static PlayFrame load(ByteBuffer buf) {

        float b = buf.getFloat();

        var lp = new Vector3f(buf.getFloat(), buf.getFloat(), buf.getFloat());
        var lr = new Quaternionf(buf.getFloat(), buf.getFloat(), buf.getFloat(), buf.getFloat());
        var rp = new Vector3f(buf.getFloat(), buf.getFloat(), buf.getFloat());
        var rr = new Quaternionf(buf.getFloat(), buf.getFloat(), buf.getFloat(), buf.getFloat());
        var hp = new Vector3f(buf.getFloat(), buf.getFloat(), buf.getFloat());
        var hr = new Quaternionf(buf.getFloat(), buf.getFloat(), buf.getFloat(), buf.getFloat());

        return new PlayFrame(b, lp, lr, rp, rr, hp, hr);
    }


}
