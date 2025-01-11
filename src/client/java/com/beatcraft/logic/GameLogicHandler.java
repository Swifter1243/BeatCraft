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


import com.beatcraft.BeatCraft;
import com.beatcraft.beatmap.data.CutDirection;
import com.beatcraft.beatmap.data.NoteType;
import com.beatcraft.beatmap.data.object.GameplayObject;
import com.beatcraft.render.DebugRenderer;
import com.beatcraft.render.object.PhysicalColorNote;
import com.beatcraft.render.object.PhysicalGameplayObject;
import com.beatcraft.utils.MathUtil;
import net.minecraft.block.enums.NoteBlockInstrument;
import net.minecraft.client.MinecraftClient;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector2f;
import org.joml.Vector3f;


public class GameLogicHandler {

    private static int goodCuts = 0;
    private static int combo = 0;
    private static int maxCombo = 0;
    private static int bonusModifier = 1;
    private static int maxPossibleScore = 0;
    private static int score = 0;
    private static int maxHealth;
    private static int health;

    private static Vector3f rightSaberPos = new Vector3f();
    private static Vector3f leftSaberPos = new Vector3f();

    private static Quaternionf leftSaberRotation = new Quaternionf();
    private static Quaternionf rightSaberRotation = new Quaternionf();

    private static Vector3f previousLeftSaberPos = new Vector3f();
    private static Vector3f previousRightSaberPos = new Vector3f();

    private static Quaternionf previousLeftSaberRotation = new Quaternionf();
    private static Quaternionf previousRightSaberRotation = new Quaternionf();

    private static final SwingState leftSwingState = new SwingState();
    private static final SwingState rightSwingState = new SwingState();

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

    public static void update(double deltaTime) {
        rightSwingState.updateSaber(rightSaberPos, rightSaberRotation, deltaTime);
        leftSwingState.updateSaber(leftSaberPos, leftSaberRotation, deltaTime);
    }

    public enum CutResult {
        NO_HIT,   // note was never hit
        HIT,      // note was hit but not with precise calculations
        GOOD_CUT, // note was cut in the correct direction
        BAD_CUT;  // note was hit with wrong color or in wrong direction

        private int points = 0;
        private int preSwingAngle = 0;
        private int followThroughAngle = 0;
        CutResult() {

        }

        public static CutResult goodCut(int points, int preSwingAngle) {
            CutResult cut = CutResult.GOOD_CUT;
            cut.points = points;
            cut.preSwingAngle = preSwingAngle;
            cut.followThroughAngle = 0;
            return cut;
        }

        public void setFollowThroughAngle(int angle) {
            followThroughAngle = angle;
        }

        public int getPreSwingAngle() {
            return preSwingAngle;
        }

    }

    private static boolean matchAngle(float angle, CutDirection direction) {
        angle = angle % 360;
        if (angle < 0) angle += 360;
        BeatCraft.LOGGER.info("check angle/dir: {} {}", angle, direction);
        switch (direction) {
            case RIGHT -> {
                return 135 < angle && angle <= 225;
            }
            case UP_RIGHT -> {
                return 90 < angle && angle <= 180;
            }
            case UP -> {
                return 45 < angle && angle <= 135;
            }
            case UP_LEFT -> {
                return 0 < angle && angle <= 90;
            }
            case LEFT -> {
                return angle <= 45 || 315 < angle;
            }
            case DOWN_LEFT -> {
                return 270 < angle && angle <= 360;
            }
            case DOWN -> {
                return 225 < angle && angle <= 315;
            }
            case DOWN_RIGHT -> {
                return 180 < angle && angle <= 270;
            }
            case DOT -> {
                return true;
            }
        }

        return false;
    }

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
        Hitbox badCutHitbox = note.getBadCutBounds();


        if (DebugRenderer.doDebugRendering) {
            if (DebugRenderer.debugSaberRendering) {
                DebugRenderer.renderParticle(saberPos, DebugRenderer.GREEN_DUST);
                DebugRenderer.renderParticle(local_hand, DebugRenderer.RED_DUST);
                DebugRenderer.renderParticle(endpoint, DebugRenderer.YELLOW_DUST);
                DebugRenderer.renderParticle(notePos, DebugRenderer.ORANGE_DUST);
                var ep = new Matrix4f().rotate(saberRotation).translate(0, 1, 0).getTranslation(new Vector3f()).add(saberPos);
                DebugRenderer.renderParticle(ep, DebugRenderer.MAGENTA_DUST);
            }
            if (DebugRenderer.renderHitboxes) {
                DebugRenderer.renderHitbox(goodCutHitbox, note.getWorldPos(), note.getWorldRot(), 0x00FF00);
                DebugRenderer.renderHitbox(badCutHitbox, note.getWorldPos(), note.getWorldRot(), 0xFF0000);
            }
        }

        assert MinecraftClient.getInstance().player != null;
        if (goodCutHitbox.checkCollision(local_hand, endpoint)) {

            if (note instanceof PhysicalColorNote colorNote) {
                if (colorNote.getData().getNoteType() == saberColor) {
                    colorNote.setContactColor(saberColor);
                    if (badCutHitbox.checkCollision(local_hand, endpoint)) {
                        // check slice direction
                        if (colorNote.getData().getCutDirection() == CutDirection.DOT) {
                            colorNote.setCutResult(CutResult.goodCut(1, (int) (saberColor == NoteType.BLUE ? rightSwingState.getSwingAngle() : leftSwingState.getSwingAngle())));
                            MinecraftClient.getInstance().player.playSound(
                                NoteBlockInstrument.PLING.getSound().value(),
                                1, 1
                            );
                        } else {
                            if (matchAngle(angle, colorNote.getData().getCutDirection())) {
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
                        MinecraftClient.getInstance().player.playSound(
                            NoteBlockInstrument.CHIME.getSound().value(),
                            1, 1
                        );
                        // can't trigger a cut yet because saber needs a chance to potentially hit the bad-cut hitbox
                        note.setCutResult(CutResult.HIT);
                    }

                } else {
                    if (badCutHitbox.checkCollision(local_hand, endpoint)) {
                        note.setContactColor(saberColor.opposite());
                        // bad cut
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

    public int getGoodCuts() {
        return goodCuts;
    }

    public int getCombo() {
        return combo;
    }

    public int getMaxCombo() {
        return maxCombo;
    }

    public int getBonusModifier() {
        return bonusModifier;
    }

    public int getScore() {
        return score;
    }

    public int getMaxPossibleScore() {
        return maxPossibleScore;
    }

    public void addGoodCut() {
        goodCuts++;
    }

    public void incrementCombo() {
        combo++;
        maxCombo = Math.max(combo, maxCombo);
        if (bonusModifier < 8) {
            bonusModifier *= 2;
        }
    }

    public void breakCombo() {
        combo = 0;
        if (bonusModifier > 1) {
            bonusModifier /= 2;
        }
    }

    public void addScore(int actual, int max) {
        score += actual;
        maxPossibleScore += max;
    }

    public Rank getRank() {
        if (maxPossibleScore == 0) {
            return Rank.SS;
        }
        float acc = (float) score / (float) maxPossibleScore;

        if (acc < 20) {
            return Rank.E;
        } else if (acc < 35) {
            return Rank.D;
        } else if (acc < 50) {
            return Rank.C;
        } else if (acc < 65) {
            return Rank.B;
        } else if (acc < 80) {
            return Rank.A;
        } else if (acc < 90) {
            return Rank.S;
        } else {
            return Rank.SS;
        }

    }

    public void reset() {
        score = 0;
        maxPossibleScore = 0;
        combo = 0;
        maxCombo = 0;
        bonusModifier = 1;
        goodCuts = 0;
    }

}
