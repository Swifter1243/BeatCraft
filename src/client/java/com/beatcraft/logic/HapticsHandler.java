package com.beatcraft.logic;

import net.minecraft.client.MinecraftClient;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.provider.ControllerType;

public class HapticsHandler {

    private static final int FREQUENCY = 160;

    private static boolean doRightVibration = false;
    private static boolean doLeftVibration = false;
    private static float leftAmplitude = 0.0f;
    private static float rightAmplitude = 0.0f;
    private static float leftTimeMod = 1.0f;
    private static float rightTimeMod = 1.0f;

    public static void vibrateRight(float power, float timeMod) {
        doRightVibration = true;
        rightAmplitude += power;
        rightTimeMod = timeMod;
        rightAmplitude = Math.clamp(rightAmplitude, 0, 1);
    }

    public static void vibrateLeft(float power, float timeMod) {
        doLeftVibration = true;
        leftAmplitude += power;
        leftTimeMod = timeMod;
        leftAmplitude = Math.clamp(leftAmplitude, 0, 1);
    }

    public static void endFrame() {
        if (ClientDataHolderVR.getInstance().vr == null) return;

        if (doRightVibration) {
            float time = rightTimeMod / (float) MinecraftClient.getInstance().getCurrentFps();
            ClientDataHolderVR.getInstance().vr.triggerHapticPulse(ControllerType.RIGHT, time, FREQUENCY, rightAmplitude);
            doRightVibration = false;
            rightAmplitude = 0;
            rightTimeMod = 1.0f;
        }

        if (doLeftVibration) {
            float time = leftTimeMod / (float) MinecraftClient.getInstance().getCurrentFps();
            ClientDataHolderVR.getInstance().vr.triggerHapticPulse(ControllerType.LEFT, time, FREQUENCY, leftAmplitude);
            doLeftVibration = false;
            leftAmplitude = 0;
            leftTimeMod = 1.0f;
        }
    }

}
