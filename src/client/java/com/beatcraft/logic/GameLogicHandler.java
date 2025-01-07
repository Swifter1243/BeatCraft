package com.beatcraft.logic;

/*
Needed info:
Bad-cut & bomb hitbox:
pos: 0, 0, 0
size: 0.35, 0.35, 0.35
extents: 0.175, 0.175, 0.175 (vector to the furthest corner)

Good-cut hitbox:
pos: 0, 0, -0.25
size: 0.8, 0.5, 1.0
extents: 0.4, 0.25, 0.5


chain note link hitbox size/position
large hitbox:
pos: 0, 0, -0.2542
size: 0.8, 0.2, 1.0083
extents: 0.4, 0.1, 0.5042

small hitbox:
pos: 0, 0, 0
size: 0.35, 0.1, 0.35
extents: 0.175, 0.05, 0.175


saber:
1m long from controller position
width: not much if any

wall dimensions and positioning

 */


import com.beatcraft.BeatmapPlayer;
import com.beatcraft.beatmap.data.object.ColorNote;
import com.beatcraft.beatmap.data.object.GameplayObject;
import com.beatcraft.render.object.PhysicalGameplayObject;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.particle.ParticleTypes;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class GameLogicHandler {

    private static Vector3f rightSaberPos = new Vector3f();
    private static Vector3f leftSaberPos = new Vector3f();

    private static Quaternionf leftSaberRotation = new Quaternionf();
    private static Quaternionf rightSaberRotation = new Quaternionf();

    private static Vector3f previousLeftSaberPos = new Vector3f();
    private static Vector3f previousRightSaberPos = new Vector3f();

    private static Quaternionf previousLeftSaberRotation = new Quaternionf();
    private static Quaternionf previousRightSaberRotation = new Quaternionf();

    public static void updateLeftSaber(Vector3f position, Quaternionf rotation) {
        previousLeftSaberPos = leftSaberPos;
        previousLeftSaberRotation = leftSaberRotation;
        leftSaberPos = position;
        leftSaberRotation = rotation;
    }


    public static void updateRightSaber(Vector3f position, Quaternionf rotation) {
        previousRightSaberPos = rightSaberPos;
        previousRightSaberRotation = rightSaberRotation;
        rightSaberPos = position;
        rightSaberRotation = rotation;
    }

    public static void update() {

    }

    private static Vector3f toLocalSpace(Vector3f point, Vector3f notePos, Quaternionf noteOrientation) {
        Vector3f translatedPoint = point.sub(notePos, new Vector3f());
        Quaternionf inverted = noteOrientation.invert(new Quaternionf());
        Vector3f local = new Vector3f();
        inverted.transform(translatedPoint, local);
        return local;
    }

    private static void renderParticle(Vector3f point) {
        MinecraftClient.getInstance().world.addParticle(
            ParticleTypes.END_ROD,
            point.x, point.y, point.z, 0, 0, 0
        );
    }

    public static<T extends GameplayObject> void checkNote(PhysicalGameplayObject<T> note) {

        // right saber
        if (rightSaberPos.distance(note.getWorldPos()) <= 1.5) {

            Vector3f localSaberPos = toLocalSpace(rightSaberPos, note.getWorldPos(), note.getWorldRot());

            renderParticle(localSaberPos);
            renderParticle(rightSaberPos);
            renderParticle(note.getWorldPos());

        }

        // left saber
        if (leftSaberPos.distance(note.getWorldPos()) <= 1.5) {

        }


    }


}
