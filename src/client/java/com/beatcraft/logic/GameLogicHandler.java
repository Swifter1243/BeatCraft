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


import com.beatcraft.beatmap.data.CutDirection;
import com.beatcraft.beatmap.data.NoteType;
import com.beatcraft.beatmap.data.object.GameplayObject;
import com.beatcraft.render.object.PhysicalColorNote;
import com.beatcraft.render.object.PhysicalGameplayObject;
import com.beatcraft.utils.MathUtil;
import net.minecraft.block.enums.NoteBlockInstrument;
import net.minecraft.client.MinecraftClient;
import net.minecraft.particle.ParticleEffect;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector2f;
import org.joml.Vector3f;

public class GameLogicHandler {

    private static int good_cuts;
    private static int combo;
    private static int max_combo;
    private static int bonus_modifier;
    private static int max_possible_score;
    private static int score;


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

    private static void renderParticle(Vector3f point, ParticleEffect particle) {
        MinecraftClient.getInstance().world.addParticle(
            particle,
            point.x, point.y, point.z, 0, 0, 0
        );
    }

    public enum CutResult {
        NO_HIT,   // note was never hit
        HIT,      // note was hit but not with precise calculations
        GOOD_CUT, // note was cut in the correct direction
        BAD_CUT;  // note was hit with wrong color or in wrong direction

        private int points = 0;
        private int preSwingAngle = 0;
        CutResult() {

        }

        public static CutResult goodCut(int points, int preSwingAngle) {
            CutResult cut = CutResult.GOOD_CUT;
            cut.points = points;
            cut.preSwingAngle = preSwingAngle;
            return cut;
        }

    }


    // Useful for debugging:
    //private static final ParticleEffect RED_DUST = new DustParticleEffect(new Vector3f(1, 0, 0), 1);
    //private static final ParticleEffect GREEN_DUST = new DustParticleEffect(new Vector3f(0, 1, 0), 1);
    //private static final ParticleEffect BLUE_DUST = new DustParticleEffect(new Vector3f(0, 0, 1), 1);
    //private static final ParticleEffect ORANGE_DUST = new DustParticleEffect(new Vector3f(1, 0.5f, 0), 1);
    //private static final ParticleEffect YELLOW_DUST = new DustParticleEffect(new Vector3f(1, 1, 0), 1);
    //private static final ParticleEffect MAGENTA_DUST = new DustParticleEffect(new Vector3f(1, 0, 1), 1);


    private static<T extends GameplayObject> void checkSaber(
        PhysicalGameplayObject<T> note,
        Quaternionf saberRotation, Quaternionf previousSaberRotation,
        Vector3f saberPos, Vector3f previousSaberPos,
        NoteType saberColor
    ) {
        if (note.getContactColor() == saberColor.opposite()) return;
        Vector3f notePos = note.getWorldPos();

        Quaternionf inverted = new Quaternionf();
        note.getWorldRot().invert(inverted);

        Vector3f endpoint = new Matrix4f().rotate(saberRotation).translate(0, 1, 0).getTranslation(new Vector3f()).add(saberPos);
        Vector3f oldEndpoint = new Matrix4f().rotate(previousSaberRotation).translate(0, 1, 0).getTranslation(new Vector3f()).add(previousSaberPos);

        Vector3f local_hand = (new Vector3f(saberPos)).sub(notePos).rotate(inverted);
        endpoint.sub(notePos).rotate(inverted);
        oldEndpoint.sub(notePos).rotate(inverted);

        Vector3f diff = endpoint.sub(oldEndpoint, new Vector3f());

        float angle = MathUtil.getVectorAngleDegrees(new Vector2f(diff.x, diff.y));

        Hitbox goodCutHitbox = note.getGoodCutBounds();

        // Useful for debugging:
        //renderParticle(saberPos, GREEN_DUST);
        //renderParticle(local_hand, RED_DUST);
        //renderParticle(endpoint, YELLOW_DUST);
        //renderParticle(notePos, ORANGE_DUST);


        if (goodCutHitbox.checkCollision(local_hand, endpoint)) {

            Hitbox badCutHitbox = note.getBadCutBounds();
            if (note instanceof PhysicalColorNote colorNote) {
                if (colorNote.getData().getNoteType() == saberColor) {
                    colorNote.setContactColor(saberColor);
                    if (badCutHitbox.checkCollision(local_hand, endpoint)) {
                        // check slice direction
                        //BeatCraft.LOGGER.info("SLICE DIRECTION: {}", angle);
                        //MinecraftClient.getInstance().player.sendMessage(Text.literal("SLICE DIRECTION: " + angle));

                        if (colorNote.getData().getCutDirection() == CutDirection.DOT) {
                            colorNote.setCutResult(CutResult.goodCut(1, 0));
                            MinecraftClient.getInstance().player.playSound(
                                NoteBlockInstrument.PLING.getSound().value(),
                                1, 1
                            );
                        } else {
                            if (-45 >= angle && angle >= -135) {
                                colorNote.setCutResult(CutResult.goodCut(2, 0));
                                MinecraftClient.getInstance().player.playSound(
                                    NoteBlockInstrument.PLING.getSound().value(),
                                    1, 1
                                );
                            } else {
                                colorNote.setCutResult(CutResult.BAD_CUT);
                                MinecraftClient.getInstance().player.playSound(
                                    NoteBlockInstrument.SNARE.getSound().value(),
                                    1, 1
                                );
                            }
                        }

                        colorNote.cutNote();

                    } else {
                        // good cut
                        //BeatCraft.LOGGER.info("directionless good cut");
                        //MinecraftClient.getInstance().player.sendMessage(Text.literal("Directionless good cut"));
                        // can't trigger a cut yet because saber needs a chance to potentially hit the bad-cut hitbox
                        note.setCutResult(CutResult.HIT);
                    }

                } else {
                    if (badCutHitbox.checkCollision(local_hand, endpoint)) {
                        note.setContactColor(saberColor.opposite());
                        // bad cut
                        //BeatCraft.LOGGER.info("wrong color; bad cut");
                        //MinecraftClient.getInstance().player.sendMessage(Text.literal("wrong color; bad cut"));
                        note.setCutResult(CutResult.BAD_CUT);
                        note.cutNote();
                        MinecraftClient.getInstance().player.playSound(
                            NoteBlockInstrument.SNARE.getSound().value(),
                            1, 1
                        );
                    }
                    // no hit
                }
            } else {
                if (badCutHitbox.checkCollision(local_hand, endpoint)) {
                    // bad cut
                    //BeatCraft.LOGGER.info("bomb hit");
                    //MinecraftClient.getInstance().player.sendMessage(Text.literal("Bomb hit"));
                    MinecraftClient.getInstance().player.playSound(
                        NoteBlockInstrument.SNARE.getSound().value(),
                        1, 1
                    );
                    note.cutNote();
                }
            }
        } else if (note.getCutResult() != CutResult.NO_HIT) {
            note.cutNote();
        }

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
            checkSaber(
                note,
                rightSaberRotation, previousRightSaberRotation,
                rightSaberPos, previousRightSaberPos,
                NoteType.BLUE
            );

        }

        // left saber
        if (leftSaberPos.distance(note.getWorldPos()) <= 1.2 + note.getCollisionDistance()) {
            checkSaber(
                note,
                leftSaberRotation, previousLeftSaberRotation,
                leftSaberPos, previousLeftSaberPos,
                NoteType.RED
            );
        }


    }


}
