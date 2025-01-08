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


import com.beatcraft.beatmap.data.NoteType;
import com.beatcraft.beatmap.data.object.GameplayObject;
import com.beatcraft.render.object.PhysicalColorNote;
import com.beatcraft.render.object.PhysicalGameplayObject;
import net.minecraft.client.MinecraftClient;
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

    private static void renderParticle(Vector3f point, int p) {
        MinecraftClient.getInstance().world.addParticle(
            p == 0 ? ParticleTypes.END_ROD : p == 1 ? ParticleTypes.ELECTRIC_SPARK : ParticleTypes.PORTAL,
            point.x, point.y, point.z, 0, 0, 0
        );
    }

    public static<T extends GameplayObject> void checkNote(PhysicalGameplayObject<T> note) {

        // Debug renderers:
        //renderParticle(note.getWorldPos(), 3);
        //
        //Vector3f pos = note.getWorldRot().transform(new Vector3f(0, 0.5f, 0));
        //
        //renderParticle(pos.add(note.getWorldPos()), 2);


        // right saber
        if (rightSaberPos.distance(note.getWorldPos()) <= 1.2 + note.getCollisionDistance()) {

            Vector3f notePos = note.getWorldPos();

            Quaternionf inverted = new Quaternionf();
            note.getWorldRot().invert(inverted);

            Vector3f endpoint = new Matrix4f().rotate(rightSaberRotation).translate(0, 1, 0).getTranslation(new Vector3f()).add(rightSaberPos);
            Vector3f oldEndpoint = new Matrix4f().rotate(previousRightSaberRotation).translate(0, 1, 0).getTranslation(new Vector3f()).add(previousRightSaberPos);

            Vector3f local_hand = (new Vector3f(rightSaberPos)).sub(notePos).rotate(inverted).add(notePos);
            endpoint.sub(notePos).rotate(inverted).add(notePos);
            oldEndpoint.sub(notePos).rotate(inverted).add(notePos);

            Vector3f diff = endpoint.sub(oldEndpoint, new Vector3f());

            Hitbox goodCutHitbox = note.getGoodCutBounds();

            if (goodCutHitbox.checkCollision(local_hand, endpoint)) {

                Hitbox badCutHitbox = note.getBadCutBounds();
                if (note instanceof PhysicalColorNote colorNote) {
                    if (colorNote.getData().getNoteType() == NoteType.BLUE) {

                        if (badCutHitbox.checkCollision(local_hand, endpoint)) {
                            // check slice direction
                        } else {
                            // good cut
                        }

                        note.cutNote();

                    } else {
                        if (badCutHitbox.checkCollision(local_hand, endpoint)) {
                            // bad cut
                            note.cutNote();
                        }
                        // no hit
                    }
                } else {
                    if (badCutHitbox.checkCollision(local_hand, endpoint)) {
                        // bad cut
                        note.cutNote();
                    }
                }



            } else {
                // not hit yet / miss
            }




        }

        // left saber
        if (leftSaberPos.distance(note.getWorldPos()) <= 1.2 + note.getCollisionDistance()) {

        }


    }


}
