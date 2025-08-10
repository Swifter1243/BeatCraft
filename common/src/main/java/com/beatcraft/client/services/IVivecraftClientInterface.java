package com.beatcraft.client.services;

import org.joml.Matrix4f;

public interface IVivecraftClientInterface {

    boolean isVRNonNull();
    Matrix4f getVRModelView();
    boolean isVRActive();

}
