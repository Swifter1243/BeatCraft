package com.beatcraft.client.logic;

public interface IHapticsHandler {
    void vibrateRight(float power, float timeMod);
    void vibrateLeft(float power, float timeMod);
    void endFrame();
}