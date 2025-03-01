package com.beatcraft.data;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.util.math.MathHelper;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class ControllerProfile {

    private Vector3f rightTranslation = new Vector3f(0, 0, 0);
    private Vector3f rightRotation = new Vector3f(0, 0, 0);

    private Vector3f leftTranslation = new Vector3f(0, 0, 0);
    private Vector3f leftRotation = new Vector3f(0, 0, 0);

    private Vector3f parseVec3f(JsonObject json) {
        float x = 0;
        float y = 0;
        float z = 0;

        if (json.has("x")) {
            x = json.get("x").getAsFloat();
        }
        if (json.has("y")) {
            y = json.get("y").getAsFloat();
        }
        if (json.has("z")) {
            z = json.get("z").getAsFloat();
        }

        return new Vector3f(x, y, z);
    }


    public ControllerProfile() {

    }

    public ControllerProfile(JsonObject json) {
        this();

        if (json.has("leftController")) {
            JsonObject leftData = json.getAsJsonObject("leftController");
            if (leftData.has("position")) {
                leftTranslation = parseVec3f(leftData.getAsJsonObject("position"));
            }
            if (leftData.has("rotation")) {
                leftRotation = parseVec3f(leftData.getAsJsonObject("rotation"));
            }

        }

        if (json.has("rightController")) {
            JsonObject rightData = json.getAsJsonObject("rightController");
            if (rightData.has("position")) {
                rightTranslation = parseVec3f(rightData.getAsJsonObject("position"));
            }
            if (rightData.has("rotation")) {
                rightRotation = parseVec3f(rightData.getAsJsonObject("rotation"));
            }

        }

    }

    public void writeJson(JsonArray array) {
        JsonObject json = new JsonObject();

        var lc = new JsonObject();
        lc.add("position", writeVector3f(leftTranslation));
        lc.add("rotation", writeVector3f(leftRotation));

        var rc = new JsonObject();
        rc.add("position", writeVector3f(rightTranslation));
        rc.add("rotation", writeVector3f(rightRotation));

        json.add("leftController", lc);
        json.add("rightController", rc);

        array.add(json);

    }

    private JsonObject writeVector3f(Vector3f vector3f) {
        JsonObject json = new JsonObject();
        if (vector3f.x != 0) {
            json.addProperty("x", vector3f.x);
        }
        if (vector3f.y != 0) {
            json.addProperty("y", vector3f.y);
        }
        if (vector3f.z != 0) {
            json.addProperty("z", vector3f.z);
        }
        return json;
    }


    public Vector3f getLeftTranslation() {
        return this.leftTranslation;
    }

    public Vector3f getRightTranslation() {
        return this.rightTranslation;
    }

    public Vector3f getLeftRotationEuler() {
        return this.leftRotation;
    }

    public Vector3f getRightRotationEuler() {
        return this.rightRotation;
    }

    public Quaternionf getLeftRotation() {
        return (new Quaternionf()).rotationYXZ(leftRotation.y * MathHelper.RADIANS_PER_DEGREE, leftRotation.x * MathHelper.RADIANS_PER_DEGREE, leftRotation.z * MathHelper.RADIANS_PER_DEGREE);
    }

    public Quaternionf getRightRotation() {
        return (new Quaternionf()).rotationYXZ(rightRotation.y * MathHelper.RADIANS_PER_DEGREE, rightRotation.x * MathHelper.RADIANS_PER_DEGREE, rightRotation.z * MathHelper.RADIANS_PER_DEGREE);
    }

    public void setLeftTranslation(Vector3f pos) {
        leftTranslation = pos;
    }

    public void setRightTranslation(Vector3f pos) {
        rightTranslation = pos;
    }

    public void setLeftRotation(Vector3f euler) {
        leftRotation = euler;
    }

    public void setRightRotation(Vector3f euler) {
        rightRotation = euler;
    }

}
