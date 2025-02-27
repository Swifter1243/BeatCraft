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
import com.beatcraft.BeatmapPlayer;
import com.beatcraft.audio.BeatmapAudioPlayer;
import com.beatcraft.beatmap.data.CutDirection;
import com.beatcraft.beatmap.data.NoteType;
import com.beatcraft.beatmap.data.object.GameplayObject;
import com.beatcraft.menu.EndScreenData;
import com.beatcraft.render.DebugRenderer;
import com.beatcraft.render.HUDRenderer;
import com.beatcraft.render.particle.BeatcraftParticleRenderer;
import com.beatcraft.render.object.*;
import com.beatcraft.replay.PlayRecorder;
import com.beatcraft.replay.Replayer;
import com.beatcraft.utils.MathUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Pair;
import net.minecraft.util.math.MathHelper;
import org.joml.Quaternionf;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.vivecraft.client_vr.ClientDataHolderVR;

import java.io.IOException;
import java.util.Random;
import java.util.UUID;


public class GameLogicHandler {

    private static int goodCuts = 0;
    private static int badCuts = 0;
    private static int misses = 0;
    private static double loseFCTime = 0;
    private static int combo = 0;
    private static int maxCombo = 0;
    private static int bonusModifier = 1;
    private static int modifierProgress = 0;
    private static int maxPossibleScore = 0;
    private static int score = 0;
    private static boolean failed = false;
    public static boolean noFail = false;

    // health values:
    // start: 50
    // miss: -15
    // hit: +5
    // bad-cut: -10
    // bomb: -15
    // walls: -10/s
    public static int maxHealth = 100;
    public static float health = 50;
    private static final int MISS_HP = 15;
    private static final int BADCUT_HP = 10;
    private static final int BOMB_HP = 15;
    private static final int WALL_HP = 10;
    private static final int HEAL_HP = 5;
    private static boolean inWall = false;
    private static Vector3f headPos = new Vector3f();

    public static boolean FPFC = false;

    private static UUID trackedPlayerUuid = null;

    public static final Random random = new Random();

    public static Vector3f rightSaberPos = new Vector3f();
    public static Vector3f leftSaberPos = new Vector3f();

    public static Quaternionf leftSaberRotation = new Quaternionf();
    public static Quaternionf rightSaberRotation = new Quaternionf();

    private static Vector3f previousLeftSaberPos = new Vector3f();
    private static Vector3f previousRightSaberPos = new Vector3f();

    private static Quaternionf previousLeftSaberRotation = new Quaternionf();
    private static Quaternionf previousRightSaberRotation = new Quaternionf();

    private static final SwingState leftSwingState = new SwingState(NoteType.RED);
    private static final SwingState rightSwingState = new SwingState(NoteType.BLUE);

    public static void trackPlayer(UUID uuid) {
        trackedPlayerUuid = uuid;
    }

    public static void untrack(UUID uuid) {
        if (trackedPlayerUuid == uuid) {
            trackedPlayerUuid = null;
            unloadAll();
        }
    }

    public static boolean isTrackingClient() {
        return trackedPlayerUuid == null;
    }

    public static boolean isTracking(UUID uuid) {
        return trackedPlayerUuid == uuid;
    }

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

    public static void preUpdate(double deltaTime, float tickDelta) {
        inWall = false;
    }

    public static void update(double deltaTime, float tickDelta) {
        assert MinecraftClient.getInstance().player != null;
        headPos = MinecraftClient.getInstance().player.getLerpedPos(tickDelta).toVector3f().add(0, (float) (MinecraftClient.getInstance().player.getEyeY() - MinecraftClient.getInstance().player.getPos().y), 0);
        if (FPFC) {
            Quaternionf rot = new Quaternionf()
                    .rotateY(-MinecraftClient.getInstance().player.getYaw(tickDelta) * MathHelper.RADIANS_PER_DEGREE)
                    .normalize()
                    .rotateX((90 + MinecraftClient.getInstance().player.getPitch(tickDelta)) * MathHelper.RADIANS_PER_DEGREE)
                    .normalize();
            //Quaternionf rotation = new Quaternionf().rotateX(90 * MathHelper.RADIANS_PER_DEGREE).normalize().add(rot);
            rightSaberPos = new Vector3f(headPos);
            leftSaberPos = new Vector3f(headPos);
            rightSaberRotation = new Quaternionf(rot);
            leftSaberRotation = rot;
        }
        rightSwingState.updateSaber(rightSaberPos, rightSaberRotation, deltaTime);
        leftSwingState.updateSaber(leftSaberPos, leftSaberRotation, deltaTime);

        Vector3f leftEndpoint = new Vector3f(0, 1, 0).rotate(leftSaberRotation).add(leftSaberPos);
        Vector3f rightEndpoint = new Vector3f(0, 1, 0).rotate(rightSaberRotation).add(rightSaberPos);

        Pair<Float, Vector3f> res = MathUtil.getLineDistance(leftSaberPos, leftEndpoint, rightSaberPos, rightEndpoint);

        if (res.getLeft() <= 0.05) {
            HapticsHandler.vibrateLeft(0.2f, 1.0f);
            HapticsHandler.vibrateRight(0.2f, 1.0f);

            if (!FPFC) {
                BeatcraftParticleRenderer.spawnSparkParticles(res.getRight(), new Vector3f(0, 0f, 0), 0.2f, 0.03f, random.nextInt(3, 5), 0xFFFFFFFF, 0.02f);
            }
        }

        if (inWall) {
            processDamage(WALL_HP * (float) deltaTime);
            checkFail();
        }

    }

    public static class CutResult {

        private final int type;

        private static final int GOOD_CUT = 1;
        private static final int BAD_CUT = 2;
        private static final int NO_HIT = 3;

        private final int preSwingAngle;
        private int followThroughAngle = 0;
        private final int sliceScore;
        private Vector3f contactPosition;
        private boolean finalized = false;

        private PhysicalScorableObject note;

        private CutResult(PhysicalScorableObject note, int preSwing, int sliceScore, Vector3f pos, int type) {
            this.note = note;
            preSwingAngle = preSwing;
            this.sliceScore = sliceScore;
            contactPosition = new Vector3f(pos);
            this.type = type;
        }

        public static CutResult goodCut(PhysicalScorableObject note, int sliceScore, int preSwingAngle, Vector3f pos) {
            return new CutResult(note, preSwingAngle, sliceScore, pos, GOOD_CUT);
        }

        public static CutResult badCut(PhysicalScorableObject note, Vector3f pos) {
            return new CutResult(note, 0, 0, pos, BAD_CUT);
        }

        public static CutResult noHit(PhysicalScorableObject note) {
            return new CutResult(note, 0, 0, new Vector3f(), NO_HIT);
        }


        public void setFollowThroughAngle(int angle) {
            followThroughAngle = angle;
            finalizeScore();
        }

        public void setContactPosition(Vector3f pos) {
            this.contactPosition = new Vector3f(pos);
        }

        public int getPreSwingAngle() {
            return preSwingAngle;
        }

        public void finalizeScore() {
            if (finalized) return;
            finalized = true;
            GameLogicHandler.process(this);
        }

    }

    private static boolean matchAngle(float angle) {
        angle = angle % 360;
        if (angle < 0) angle += 360;
        return (225 < angle && angle < 315);
    }

    public static Vector3f getPlaneNormal(Vector3f start, Vector3f end, Vector3f velocity) {
        Vector3f s1 = end.sub(start, new Vector3f());
        Vector3f cross = s1.cross(velocity, new Vector3f());
        return cross.normalize();
    }

    public static float distanceToOrigin(Vector3f planeIncident, Vector3f planeNormal) {
        return Math.abs(planeNormal.dot(planeIncident)) / planeNormal.length();
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
        note.getWorldRot().conjugate(inverted);

        Vector3f endpoint = new Vector3f(0, 1, 0).rotate(saberRotation).add(saberPos);
        Vector3f oldEndpoint = new Vector3f(0, 1, 0).rotate(previousSaberRotation).add(previousSaberPos);

        Vector3f trueDiff = endpoint.sub(oldEndpoint, new Vector3f());
        Vector3f trueEndpoint = new Vector3f(endpoint);

        Vector3f local_hand = (new Vector3f(saberPos)).sub(notePos).rotate(inverted);
        endpoint.sub(notePos).rotate(inverted);
        oldEndpoint.sub(notePos).rotate(inverted);

        Vector3f diff = endpoint.sub(oldEndpoint, new Vector3f());

        float angle = MathUtil.getVectorAngleDegrees(new Vector2f(diff.x, diff.y).normalize());

        Hitbox goodCutHitbox = note.getGoodCutBounds();
        Hitbox badCutHitbox = note.getBadCutBounds();
        Hitbox accurateHitbox = note.getAccurateHitbox();

        if (DebugRenderer.doDebugRendering) {
            if (DebugRenderer.debugSaberRendering) {
                DebugRenderer.renderParticle(saberPos, DebugRenderer.GREEN_DUST);
                DebugRenderer.renderParticle(notePos, DebugRenderer.ORANGE_DUST);
                DebugRenderer.renderParticle(trueEndpoint, DebugRenderer.MAGENTA_DUST);
                DebugRenderer.renderLine(trueEndpoint, trueEndpoint.add(trueDiff, new Vector3f()), 0xFF00FF00, 0x7FFF0000);
                DebugRenderer.renderLine(endpoint, endpoint.add(diff, new Vector3f()), 0x7F00FF00, 0x27FF0000);
                int c = 0x7F000000 + (saberColor == NoteType.BLUE ? 0x0000FF : 0xFF0000);
                DebugRenderer.renderLine(local_hand, endpoint, c, 0x7FFFFFFF);
                DebugRenderer.renderHitbox(goodCutHitbox, new Vector3f(), new Quaternionf(), 0x00FF00);
                DebugRenderer.renderHitbox(badCutHitbox, new Vector3f(), new Quaternionf(), 0xFF0000);
            }
            if (DebugRenderer.renderHitboxes) {
                DebugRenderer.renderHitbox(goodCutHitbox, notePos, note.getWorldRot(), 0x00FF00);
                DebugRenderer.renderHitbox(badCutHitbox, notePos, note.getWorldRot(), 0xFF0000);
                DebugRenderer.renderHitbox(accurateHitbox, notePos, note.getWorldRot(), 0xFFFF00);
            }
        }

        assert MinecraftClient.getInstance().player != null;

        if (note instanceof PhysicalScorableObject scorable) {
            if (scorable.score$getData().score$getNoteType() == saberColor) {
                if (goodCutHitbox.checkCollision(local_hand, endpoint)) {
                    if (saberColor == NoteType.RED) {
                        leftSwingState.startSparkEffect();
                        HapticsHandler.vibrateLeft(1f, 0.075f * MinecraftClient.getInstance().getCurrentFps());
                    } else {
                        rightSwingState.startSparkEffect();
                        HapticsHandler.vibrateRight(1f, 0.075f * MinecraftClient.getInstance().getCurrentFps());
                    }

                    if (scorable.score$getData().score$getCutDirection() == CutDirection.DOT || matchAngle(angle)) {
                        if (saberColor == NoteType.BLUE) {
                            rightSwingState.followThrough(scorable);
                        } else {
                            leftSwingState.followThrough(scorable);
                        }

                        Vector3f planeNormal = getPlaneNormal(local_hand, endpoint, diff);
                        float distance = distanceToOrigin(local_hand, planeNormal);
                        int points = (int) Math.clamp(15 * (1-MathUtil.inverseLerp(0, 0.25f, distance)), 0, 15);
                        scorable.score$setCutResult(CutResult.goodCut(scorable, points, (int) (saberColor == NoteType.BLUE ? rightSwingState.getSwingAngle() : leftSwingState.getSwingAngle()), notePos));
                        note.spawnDebris(notePos.add(new Vector3f(-0.25f, -0.25f, -0.25f).rotate(note.getWorldRot())), note.getWorldRot(), scorable.score$getData().score$getNoteType(), local_hand.add(0.25f, 0.25f, 0.25f, new Vector3f()), planeNormal);
                        scorable.score$cutNote();
                    }
                }
            }
        }

        if (badCutHitbox.checkCollision(local_hand, endpoint) && note.getCutResult().type != CutResult.GOOD_CUT) {
            if (note instanceof PhysicalScorableObject scorable) {
                if (!matchAngle(angle)) {
                    scorable.score$setCutResult(CutResult.badCut(scorable, notePos));
                    scorable.score$getCutResult().finalizeScore();
                    Vector3f planeNormal = getPlaneNormal(local_hand, endpoint, diff);
                    note.spawnDebris(notePos.add(new Vector3f(-0.25f, -0.25f, -0.25f).rotate(note.getWorldRot())), note.getWorldRot(), scorable.score$getData().score$getNoteType(), local_hand.add(0.25f, 0.25f, 0.25f, new Vector3f()), planeNormal);
                }
            } else {
                hitBomb();
            }
            if (saberColor == NoteType.RED) {
                HapticsHandler.vibrateLeft(1f, 0.075f * MinecraftClient.getInstance().getCurrentFps());
            } else {
                HapticsHandler.vibrateRight(1f, 0.075f * MinecraftClient.getInstance().getCurrentFps());
            }
            note.cutNote();
            breakCombo();
            //process(CutResult.badCut(null, notePos));
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

    private static void checkSaberAgainstObstacle(Hitbox hitbox, Vector3f position, Quaternionf inverted, Vector3f saberPos, Quaternionf saberRotation, NoteType saber) {
        Vector3f endpoint = new Vector3f(0, 1, 0).rotate(saberRotation).add(saberPos);

        Vector3f local_hand = (new Vector3f(saberPos)).sub(position).rotate(inverted);
        endpoint.sub(position).rotate(inverted);

        if (hitbox.checkCollision(local_hand, endpoint)) {
            if (saber == NoteType.BLUE) {
                HapticsHandler.vibrateRight(0.25f, 1.0f);
            } else {
                HapticsHandler.vibrateLeft(0.25f, 1.0f);
            }
        }

    }

    public static void checkObstacle(PhysicalObstacle obstacle, Vector3f position, Quaternionf orientation) {
        Hitbox hitbox = obstacle.getBounds();

        Quaternionf inverted = new Quaternionf();
        orientation.invert(inverted);

        checkSaberAgainstObstacle(hitbox, position, inverted, rightSaberPos, rightSaberRotation, NoteType.BLUE);
        checkSaberAgainstObstacle(hitbox, position, inverted, leftSaberPos, leftSaberRotation, NoteType.RED);

        Vector3f localHeadPos = new Vector3f(headPos).sub(position).rotate(inverted);

        if (!inWall) {
            inWall = hitbox.isPointInHitbox(localHeadPos);
        }
    }

    public static void process(CutResult cut) {
        switch (cut.type) {
            case CutResult.NO_HIT -> {
                if (misses == 0 && badCuts == 0) {
                    loseFCTime = System.nanoTime() / 1_000_000_000d;
                }
                addMiss();
                breakCombo();
                addScore(0, 115);
                Vector3f startPos = cut.contactPosition.mul(1, 0, 1, new Vector3f());
                Vector3f endPos = startPos.add(new Vector3f(0, 0.5f, 5).rotate(cut.note.score$getLaneRotation().invert(new Quaternionf())), new Vector3f());

                HUDRenderer.postScore(-1, startPos, endPos, cut.note.score$getLaneRotation());
            }
            case CutResult.GOOD_CUT -> {
                int pre_swing = (int) (Math.clamp(MathUtil.inverseLerp(0, 100, cut.preSwingAngle), 0, 1) * 70);
                int post_swing = (int) (Math.clamp(MathUtil.inverseLerp(0, 60, cut.followThroughAngle), 0, 1) * 30);
                int finalScore = pre_swing + post_swing + cut.sliceScore;

                addGoodCut();
                incrementCombo();
                addScore(finalScore, 115);

                Vector3f startPos = cut.contactPosition.mul(1, 0, 1, new Vector3f());
                Vector3f endPos = startPos.add(new Vector3f(0, 0.5f, 5).rotate(cut.note.score$getLaneRotation().invert(new Quaternionf())), new Vector3f());

                HUDRenderer.postScore(finalScore, startPos, endPos, cut.note.score$getLaneRotation());
            }
            case CutResult.BAD_CUT -> {
                if (misses == 0 && badCuts == 0) {
                    loseFCTime = System.nanoTime() / 1_000_000_000d;
                }
                addBadcut();
                breakCombo();
                addScore(0, 115);
                Vector3f startPos = cut.contactPosition.mul(1, 0, 1, new Vector3f());
                Vector3f endPos = startPos.add(new Vector3f(0, 0.5f, 5).rotate(cut.note.score$getLaneRotation().invert(new Quaternionf())), new Vector3f());

                HUDRenderer.postScore(0, startPos, endPos, cut.note.score$getLaneRotation());
            }
        }
    }

    public static float getComboBarOpacity() {
        if (badCuts == 0 && misses == 0) {
            return 1;
        }
        return 1 - (float) Math.clamp(MathUtil.inverseLerp(loseFCTime, loseFCTime+0.3, System.nanoTime() / 1_000_000_000d), 0, 1);
    }

    public static int getGoodCuts() {
        return goodCuts;
    }

    public static int getCombo() {
        return combo;
    }

    public static int getMaxCombo() {
        return maxCombo;
    }

    public static int getBonusModifier() {
        return bonusModifier;
    }

    public static int getScore() {
        return score;
    }

    public static int getMaxPossibleScore() {
        return maxPossibleScore;
    }

    public static float getHealthPercentage() {
        return health / (float) maxHealth;
    }

    public static void addGoodCut() {
        goodCuts++;
        if (maxHealth == 100 && !(failed && noFail)) {
            health = Math.min(maxHealth, health+HEAL_HP);
        }
    }

    private static void processDamage(float damage) {
        if (maxHealth == 100) {
            health -= damage;
        } else if (maxHealth == 4) {
            health -= 1;
        } else {
            health = 0;
        }
    }

    private static void addMiss() {
        misses++;
        if (failed && noFail) return;
        processDamage(MISS_HP);
        checkFail();
    }

    private static void addBadcut() {
        badCuts++;
        if (failed && noFail) return;
        processDamage(BADCUT_HP);
        checkFail();
    }

    private static void hitBomb() {
        if (failed && noFail) return;
        processDamage(BOMB_HP);
        checkFail();
    }

    private static void checkFail() {
        if (health <= 0) {
            health = 0;
            failed = true;
            if (!noFail) {
                // trigger fail animation
                HUDRenderer.endScreenPanel.setFailed();
                HUDRenderer.scene = HUDRenderer.MenuScene.EndScreen;

                try {
                    PlayRecorder.save();
                } catch (IOException e) {
                    BeatCraft.LOGGER.error("Error saving recording", e);
                }

                unloadAll();
            }
        }
    }

    public static void incrementCombo() {
        combo++;
        maxCombo = Math.max(combo, maxCombo);
        if (bonusModifier < 8) {
            modifierProgress++;
            if (modifierProgress == bonusModifier) {
                bonusModifier *= 2;
                modifierProgress = 0;
            }
        }
    }

    public static float getModifierPercentage() {
        return (float) modifierProgress / (float) bonusModifier;
    }

    public static void breakCombo() {
        combo = 0;
        modifierProgress = 0;
        if (bonusModifier > 1) {
            bonusModifier /= 2;
        }
    }

    public static void addScore(int actual, int max) {
        score += actual * bonusModifier;
        maxPossibleScore += max * bonusModifier;
    }

    public static float getAccuracy() {
        float acc = (float) score / (float) maxPossibleScore;
        return Float.isNaN(acc) ? 1 : acc;
    }

    public static Rank getRank() {
        float acc = getAccuracy() * 100;

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

    public static void reset() {
        score = 0;
        maxPossibleScore = 0;
        combo = 0;
        maxCombo = 0;
        bonusModifier = 1;
        goodCuts = 0;
        loseFCTime = 0;
        badCuts = 0;
        misses = 0;
        failed = false;
        health = maxHealth == 100 ? 50 : maxHealth == 4 ? 4 : 1;
        inWall = false;
    }


    public static void triggerSongEnd() {
        HUDRenderer.scene = HUDRenderer.MenuScene.EndScreen;

        HUDRenderer.endScreenPanel.setData(new EndScreenData(
                getScore(), getRank(),
                getMaxCombo(), goodCuts, getAccuracy()*100,
                goodCuts + badCuts + misses
        ));

        //assert MinecraftClient.getInstance().player != null;
        //MinecraftClient.getInstance().player.sendMessage(
        //    Text.literal(String.format(
        //        "%s - %.1f%% - %s\nmax combo: %s\ngood cuts: %s\nbad cuts: %s\nmisses: %s",
        //        getRank(), getAccuracy()*100, getScore(), getMaxCombo(),
        //        goodCuts, badCuts, misses
        //    ))
        //);

        try {
            PlayRecorder.save();
        } catch (IOException e) {
            BeatCraft.LOGGER.error("Error saving recording", e);
        }

        unloadAll();
    }

    public static void unloadAll() {
        PlayRecorder.reset();
        Replayer.reset();
        BeatmapPlayer.reset();
        BeatmapAudioPlayer.unload();
        reset();
    }

}
