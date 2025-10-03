package com.beatcraft.client.beatmap;

import com.beatcraft.client.BeatcraftClient;
import com.beatcraft.client.logic.PhysicsTransform;
import net.minecraft.client.Minecraft;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class BeatmapLogicController {
    private final BeatmapController controller;

    public Vector3f headPos = new Vector3f();
    public Quaternionf headRot = new Quaternionf();

    public Vector3f leftSaberPos = new Vector3f();
    public Quaternionf leftSaberRotation = new Quaternionf();

    public Vector3f rightSaberPos = new Vector3f();
    public Quaternionf rightSaberRotation = new Quaternionf();

    public Vector3f playerGlobalPosition = new Vector3f();
    public Quaternionf playerGlobalRotation = new Quaternionf();


    public BeatmapLogicController(BeatmapController player) {
        controller = player;
    }

    public int getCombo() {
        return 0;
    }

    public int getMaxPossibleScore() {
        return 0;
    }

    public int getScore() {
        return 0;
    }

    public float getAccuracy() {
        return 0;
    }

    public float getBonusModifier() {
        return 1;
    }

    public float getHealthPercentage() {
        return 1;
    }

    public void update(double deltaTime) {

        if (controller.trackedPlayer != null && BeatcraftClient.saberTransforms.containsKey(controller.trackedPlayer)) {
            var transforms = BeatcraftClient.saberTransforms.get(controller.trackedPlayer);
            PhysicsTransform leftSaber = transforms.getA();
            PhysicsTransform rightSaber = transforms.getB();



        }

    }


}
