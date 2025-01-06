package com.beatcraft.data;

import com.google.gson.JsonObject;
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


    public Vector3f getLeftTranslation() {
        return this.leftTranslation;
    }

    public Vector3f getRightTranslation() {
        return this.rightTranslation;
    }

    public Quaternionf getLeftRotation() {
        return (new Quaternionf()).rotationXYZ(leftRotation.x, leftRotation.y, leftRotation.z);
    }

    public Quaternionf getRightRotation() {
        return (new Quaternionf()).rotationXYZ(rightRotation.x, rightRotation.y, rightRotation.z);
    }


}
