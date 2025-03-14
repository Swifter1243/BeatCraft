package com.beatcraft.logic;

public class InputSystem {

    private static boolean hotbarLocked = false;
    private static boolean motionLocked = false;


    public static void lockHotbar() {
        hotbarLocked = true;
    }

    public static void unlockHotbar() {
        hotbarLocked = false;
    }

    public static boolean isHotbarLocked() {
        return hotbarLocked;
    }

    public static void lockMovement() {
        motionLocked = true;
    }

    public static void unlockMovement() {
        motionLocked = false;
    }

    public static boolean isMovementLocked() {
        return motionLocked;
    }

}
