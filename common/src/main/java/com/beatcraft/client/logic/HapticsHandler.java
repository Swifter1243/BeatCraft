package com.beatcraft.client.logic;

import java.util.ServiceLoader;

public class HapticsHandler {

    private static final IHapticsHandler inner = ServiceLoader.load(IHapticsHandler.class).findFirst().orElseThrow(() -> new RuntimeException("Could not load HapticsHandler"));

    public static void vibrateRight(float power, float timeMod) {
        inner.vibrateRight(power, timeMod);
    }

    public static void vibrateLeft(float power, float timeMod) {
        inner.vibrateLeft(power, timeMod);
    }

    public static void endFrame() {
        inner.endFrame();
    }
}
