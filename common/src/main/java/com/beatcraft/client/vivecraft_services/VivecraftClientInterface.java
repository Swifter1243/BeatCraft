package com.beatcraft.client.vivecraft_services;

import org.joml.Matrix4f;

import java.util.ServiceLoader;

public class VivecraftClientInterface {

    private static final IVivecraftClientInterface inner = ServiceLoader.load(IVivecraftClientInterface.class).findFirst().orElseThrow(() -> new RuntimeException("Could not load Vivecraft interface"));


    public static boolean isVRNonNull() {
        return inner.isVRNonNull();
    }

    public static Matrix4f getVRModelView() {
        return inner.getVRModelView();
    }

}
