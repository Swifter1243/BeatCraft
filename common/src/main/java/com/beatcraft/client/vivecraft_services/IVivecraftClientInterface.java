package com.beatcraft.client.vivecraft_services;

import org.joml.Matrix4f;

public interface IVivecraftClientInterface {

    boolean isVRNonNull();
    Matrix4f getVRModelView();
    boolean isVRActive();

}
