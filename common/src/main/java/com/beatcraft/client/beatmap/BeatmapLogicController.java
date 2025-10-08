package com.beatcraft.client.beatmap;

import com.beatcraft.Beatcraft;
import com.beatcraft.client.BeatcraftClient;
import com.beatcraft.client.beatmap.data.CutDirection;
import com.beatcraft.client.beatmap.data.NoteType;
import com.beatcraft.client.beatmap.object.data.GameplayObject;
import com.beatcraft.client.beatmap.object.data.ScoreState;
import com.beatcraft.client.beatmap.object.physical.*;
import com.beatcraft.client.logic.HapticsHandler;
import com.beatcraft.client.logic.Hitbox;
import com.beatcraft.client.logic.Rank;
import com.beatcraft.client.menu.EndScreenData;
import com.beatcraft.client.render.DebugRenderer;
import com.beatcraft.client.render.HUDRenderer;
import com.beatcraft.common.items.ModItems;
import com.beatcraft.common.utils.MathUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.io.IOException;

public class BeatmapLogicController {
    private final BeatmapController controller;

    public Vector3f headPos = new Vector3f();
    public Quaternionf headRot = new Quaternionf();

    public Vector3f leftSaberPos = new Vector3f();
    public Quaternionf leftSaberRotation = new Quaternionf();
    public Vector3f leftSaberTipVelocity = new Vector3f();

    public Vector3f rightSaberPos = new Vector3f();
    public Quaternionf rightSaberRotation = new Quaternionf();
    public Vector3f rightSaberTipVelocity = new Vector3f();

    public Vector3f playerGlobalPosition = new Vector3f();
    public Quaternionf playerGlobalRotation = new Quaternionf();

    private static final Vector3f SABER_TIP_OFFSET = new Vector3f(0, 1, 0);

    private static final int MISS_HP = 15;
    private static final int BADCUT_HP = 10;
    private static final int BOMB_HP = 15;
    private static final int WALL_HP = 10;
    private static final int HEAL_HP = 5;

    public int goodCuts = 0;
    public int badCuts = 0;
    public int misses = 0;
    public double loseFCTime = 0;
    public int combo = 0;
    public int maxCombo = 0;
    public int bonusModifier = 1;
    public int modifierProgress = 0;
    public int maxPossibleScore = 0;
    public int score = 0;
    public boolean failed = false;
    public boolean noFail = false;
    public int maxHealth = 100;
    public float health = 50;

    public float mapSpeed = 1;

    private boolean failAnim = false;
    public float globalDissolve = 0;
    public float globalArrowDissolve = 0;
    public float ghostNoteDissolve = 0;

    private double failTime = 0;
    private static final double DISSOLVE_TIME = 2.5;


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

    public boolean update(double deltaTime) {

        if (controller.trackedPlayer == null) return true;
        assert Minecraft.getInstance().level != null;
        var player = Minecraft.getInstance().level.getPlayerByUUID(controller.trackedPlayer);
        if (player == null) return false;

        if (!player.level().equals(controller.level)) return false;

        var sabers = BeatcraftClient.controllerTransforms.get(controller.trackedPlayer);
        if (sabers == null) return false;

        controller.isInWall = false;

        var left = sabers.getA();
        var head = sabers.getB();
        var right = sabers.getC();

        var rightHanded = player.getMainArm() == HumanoidArm.RIGHT;
        var leftHand = rightHanded ? EquipmentSlot.OFFHAND : EquipmentSlot.MAINHAND;
        var rightHand = rightHanded ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND;
        var leftItem = player.getItemBySlot(leftHand).is(ModItems.SABER_ITEM);
        var rightItem = player.getItemBySlot(rightHand).is(ModItems.SABER_ITEM);
        if (leftItem) {
            left.getPosition(leftSaberPos);
            left.getRotation(leftSaberRotation);
            left.getPositionalVelocity((float) deltaTime, SABER_TIP_OFFSET, leftSaberTipVelocity);
        } else {
            leftSaberPos.set(0, -600, 0); // just stick it below the void I guess?
        }

        if (rightItem) {
            right.getPosition(rightSaberPos);
            right.getRotation(rightSaberRotation);
            right.getPositionalVelocity((float) deltaTime, SABER_TIP_OFFSET, rightSaberTipVelocity);
        } else {
            rightSaberPos.set(0, -600, 0);
        }

        if (player == Minecraft.getInstance().player) {
            BeatcraftClient.wearingHeadset = player.getItemBySlot(EquipmentSlot.HEAD).is(ModItems.HEADSET_ITEM);
        }

        head.getPosition(headPos);
        head.getRotation(headRot);

        return true;
    }

    public void lateUpdate(double deltaTime) {
        if (controller.isInWall) {
            processDamage(WALL_HP * (float) deltaTime);
            checkFail();
            breakCombo();
        }

        if (failAnim) {
            var t = System.nanoTime() / 1_000_000_000d;
            var normalized = MathUtil.inverseLerp(failTime, failTime+DISSOLVE_TIME, t);

            var n = (float) Math.clamp(normalized, 0, 1);

            controller.setSpeed((0.1f * mapSpeed) + ((mapSpeed-n) * (0.9f * mapSpeed)));

            if (normalized <= 1.0) {
                globalDissolve = Math.max(globalDissolve, (float) normalized);
                globalArrowDissolve = Math.max(globalArrowDissolve, (float) normalized);
            } else if (normalized >= 1.1) {
                failAnim = false;
                failTime = 0;
                resetToMenu();
                controller.setSpeed(1);
            }

        }

    }

    private static final Vector3f wp = new Vector3f();
    public void checkNote(PhysicalGameplayObject<? extends GameplayObject> obj) {

        obj.getWorldPos(wp);
        var cd = 1.2 + obj.getCollisionDistance();

        if (rightSaberPos.distance(wp) <= cd) {
            checkSaber(obj, rightSaberPos, rightSaberRotation, rightSaberTipVelocity, NoteType.BLUE);
        }

        if (leftSaberPos.distance(wp) <= cd) {
            checkSaber(obj, leftSaberPos, leftSaberRotation, leftSaberTipVelocity, NoteType.RED);
        }

    }

    private static final Matrix4f invTransform = new Matrix4f();
    private static final Vector3f tipPos = new Vector3f();
    private static final Vector3f localTip = new Vector3f();
    private static final Vector3f localBase = new Vector3f();
    private static final Vector3f localVel = new Vector3f();
    private static final Quaternionf q0 = new Quaternionf();

    private void processNote(PhysicalScorableObject obj, Hitbox gc, Hitbox bc, NoteType expectedNoteType, Vector3f vel) {
        var cd = obj.score$getCutDirection();
        if (obj.score$getData().score$getNoteType() == expectedNoteType) {
            if (cd == CutDirection.DOT) {
                if (gc.checkCollision(localBase, localTip)) {
                    calculateScore(obj, localVel, localBase, localTip);
                    obj.score$cutNote();
                    getPlaneNormal(localBase, localTip, vel, dir);
                    obj.score$spawnDebris(localBase.add(0.25f, 0.25f, 0.25f, new Vector3f()), dir);
                }
            } else {
                if (isPointingDown(localVel)) {
                    if (gc.checkCollision(localBase, localTip)) {
                        calculateScore(obj, localVel, localBase, localTip);
                        obj.score$cutNote();
                        getPlaneNormal(localBase, localTip, vel, dir);
                        obj.score$spawnDebris(localBase.add(0.25f, 0.25f, 0.25f, new Vector3f()), dir);
                    }
                } else {
                    if (bc.checkCollision(localBase, localTip)) {
                        obj.score$setScoreState(ScoreState.badCut());
                        obj.score$cutNote();
                        getPlaneNormal(localBase, localTip, vel, dir);
                        obj.score$spawnDebris(localBase.add(0.25f, 0.25f, 0.25f, new Vector3f()), dir);
                        processBadCut(obj.score$getMaxFollowThroughScore() + obj.score$getMaxSwingInScore() + 15);
                    }
                }
            }
        } else {
            if (bc.checkCollision(localBase, localTip)) {
                obj.score$setScoreState(ScoreState.badCut());
                obj.score$cutNote();
                getPlaneNormal(localBase, localTip, vel, dir);
                obj.score$spawnDebris(localBase.add(0.25f, 0.25f, 0.25f, new Vector3f()), dir);
                processBadCut(obj.score$getMaxFollowThroughScore() + obj.score$getMaxSwingInScore() + 15);
            }
        }
    }

    private void checkSaber(PhysicalGameplayObject<? extends GameplayObject> obj, Vector3f pos, Quaternionf rot, Vector3f vel, NoteType expectedNoteType) {

        var t = obj.getWorldTransform();
        t.invert(invTransform);
        SABER_TIP_OFFSET.rotate(rot, tipPos).add(pos);

        invTransform.transformPosition(pos, localBase);
        invTransform.transformPosition(tipPos, localTip);
        vel.rotate(invTransform.getNormalizedRotation(q0), localVel);

        var gc = obj.getGoodCutBounds();
        var bc = obj.getBadCutBounds();
        var ac = obj.getAccurateHitbox();

        if (BeatcraftClient.playerConfig.debug.beatmap.renderSaberColliders()) {
            DebugRenderer.renderParticle(new Vector3f(localBase), DebugRenderer.GREEN_DUST);
            DebugRenderer.renderParticle(obj.getWorldPos(new Vector3f()), DebugRenderer.ORANGE_DUST);
            DebugRenderer.renderParticle(new Vector3f(localTip), DebugRenderer.MAGENTA_DUST);
            DebugRenderer.renderLine(new Vector3f(tipPos), tipPos.add(vel, new Vector3f()), 0xFF00FF00, 0x7FFF0000);
            DebugRenderer.renderLine(new Vector3f(localTip), localTip.add(localVel, new Vector3f()), 0x7F007F00, 0x27007F00);
            int c = 0x7F000000 + (expectedNoteType == NoteType.BLUE ? 0x0000FF : 0xFF0000);
            DebugRenderer.renderLine(new Vector3f(localBase), new Vector3f(localTip), c, 0x7FFFFFFF);
            DebugRenderer.renderHitbox(gc, new Vector3f(), new Quaternionf(), 0x00FF00);
            DebugRenderer.renderHitbox(bc, new Vector3f(), new Quaternionf(), 0xFF0000);
        }
        if (BeatcraftClient.playerConfig.debug.beatmap.renderHitboxes()) {
            var wt = obj.getWorldTransform();
            var wp = wt.getTranslation(new Vector3f());
            var wr = wt.getNormalizedRotation(new Quaternionf());
            DebugRenderer.renderHitbox(gc, wp, wr, 0x00FF00);
            DebugRenderer.renderHitbox(bc, wp, wr, 0xFF0000);
            DebugRenderer.renderHitbox(ac, wp, wr, 0x7F7F7F);
        }

        if (failAnim) return;

        if (obj instanceof PhysicalScorableObject scorable) {
            processNote(scorable, gc, bc, expectedNoteType, vel);
        } else if (obj instanceof PhysicalBombNote bombNote) {
            if (bc.isPointInHitbox(localTip)) {
                obj.cutNote();
                processBadCut(0);
            }
        }


    }

    private static final Vector3f DOWN = new Vector3f(0, -1, 0);
    private static final Vector3f dir = new Vector3f();
    private static boolean isPointingDown(Vector3f velocity) {
        if (velocity.lengthSquared() == 0f) {
            return false;
        }
        dir.set(velocity).normalize();

        float dot = dir.dot(DOWN);

        return dot >= 0.7071f;
    }


    private void calculateScore(PhysicalScorableObject colorNote, Vector3f velocity, Vector3f base, Vector3f tip) {

        var normal = getPlaneNormal(base, tip, velocity, new Vector3f());
        var dist = distanceToOrigin(tip, normal);

        var angle = (colorNote.score$getMaxSwingInAngle() + colorNote.score$getMaxFollowThroughAngle()) * Mth.DEG_TO_RAD;
        var angleScore = colorNote.score$getMaxSwingInScore() + colorNote.score$getMaxFollowThroughScore();

        int accPoints = (int) Math.clamp(15 * (1 - MathUtil.inverseLerp(0, 0.25f, dist)), 0, 15);

        int swingPoints = (int) Math.clamp(angleScore * MathUtil.inverseLerp(0, Math.PI * angle, velocity.length()), 0, 100);

        colorNote.score$setScoreState(ScoreState.goodCut(accPoints + swingPoints));

        processGoodCut(accPoints + swingPoints, angleScore);

    }

    private static final Vector3f localHeadPos = new Vector3f();
    public void checkObstacle(PhysicalObstacle obstacle, Vector3f position, Quaternionf rotation) {

        var hitbox = obstacle.getBounds();

        rotation.invert(q0);

        checkSaberAgainstObstacle(hitbox, position, q0, rightSaberPos, rightSaberRotation, NoteType.BLUE);
        checkSaberAgainstObstacle(hitbox, position, q0, leftSaberPos, leftSaberRotation, NoteType.RED);

        headPos.sub(position, localHeadPos).rotate(q0);

        if (!controller.isInWall) {
            controller.isInWall = hitbox.isPointInHitbox(localHeadPos);
        }

    }

    private static void checkSaberAgainstObstacle(Hitbox hitbox, Vector3f position, Quaternionf inv, Vector3f saberPos, Quaternionf saberRot, NoteType saber) {
        SABER_TIP_OFFSET.rotate(saberRot, localTip).add(saberPos);

        saberPos.sub(position, localBase).rotate(inv);
        localTip.sub(position).rotate(inv);

        if (hitbox.checkCollision(localBase, localTip)) {
            if (saber.equals(NoteType.BLUE)) {
                HapticsHandler.vibrateRight(0.25f, 1.0f);
            } else {
                HapticsHandler.vibrateLeft(0.25f, 1.0f);
            }
        }

    }


    public static Vector3f getPlaneNormal(Vector3f start, Vector3f end, Vector3f velocity, Vector3f dest) {
        Vector3f s1 = end.sub(start, dest);
        Vector3f cross = s1.cross(velocity);
        return cross.normalize();
    }

    public static float distanceToOrigin(Vector3f planeIncident, Vector3f planeNormal) {
        return Math.abs(planeNormal.dot(planeIncident)) / planeNormal.length();
    }


    private void processDamage(float damage) {
        if (maxHealth == 100) {
            health -= damage;
        } else if (maxHealth == 4) {
            health -= 1;
        } else {
            health = 0;
        }
    }

    private void checkFail() {
        if (health <= 0 && !failed) {
            health = 0;
            failed = true;
            if (!noFail) {
                failAnim = true;
                failTime = System.nanoTime() / 1_000_000_000d;
            }
        }
    }

    private void resetToMenu() {
        // reset HUD

        // save recording

        unloadAll();

    }

    public float getModifierPercentage() {
        return (float) modifierProgress / (float) bonusModifier;
    }

    public float getComboBarOpacity() {
        if (badCuts == 0 && misses == 0) {
            return 1;
        }
        return 1 - (float) Math.clamp(MathUtil.inverseLerp(loseFCTime, loseFCTime+0.3, System.nanoTime() / 1_000_000_000d), 0, 1);
    }

    public Rank getRank() {
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


    private void breakCombo() {
        combo = 0;
        modifierProgress = 0;
        if (bonusModifier > 1) {
            bonusModifier /= 2;
        }
    }

    private void unloadAll() {
        // reset play recorder
        // reset replay
        controller.reset();
        reset();
    }

    public void reset() {
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
        controller.isInWall = false;
        failTime = 0;
        failAnim = false;
        globalDissolve = 0;
        globalArrowDissolve = 0;
    }

    public void processGoodCut(int score, int maxScore) {
        addGoodCut();
        incrementCombo();
        addScore(score, maxScore);

        // push score display to HUD
    }

    public void processBadCut(int maxScore) {
        if (misses == 0 && badCuts == 0) {
            loseFCTime = System.nanoTime() / 1_000_000_000d;
        }
        addBadCut();
        breakCombo();
        addScore(0, maxScore);
        // push X to HUD
    }

    public void processNoCut(int maxScore) {
        if (misses == 0 && badCuts == 0) {
            loseFCTime = System.nanoTime() / 1_000_000_000d;
        }
        addMiss();
        breakCombo();
        addScore(0, maxScore);
        // push MISS to HUD
    }


    public void addGoodCut() {
        goodCuts++;
        if (maxHealth == 100 && !(failed && noFail)) {
            health = Math.min(maxHealth, health + HEAL_HP);
        }
    }

    public void addMiss() {
        misses++;
        if (failed && noFail) return;
        processDamage(MISS_HP);
        checkFail();
    }

    public void addBadCut() {
        badCuts++;
        if (failed && noFail) return;
        processDamage(BADCUT_HP);
        checkFail();
    }

    public void hitBomb() {
        badCuts++;
        if (failed && noFail) return;
        processDamage(BADCUT_HP);
        checkFail();
    }

    public void incrementCombo() {
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

    public void addScore(int earned, int possible) {
        score += earned * bonusModifier;
        maxPossibleScore += possible * bonusModifier;
    }


    public void triggerSongEnd() {
        controller.scene = HUDRenderer.MenuScene.EndScreen;

        controller.hudRenderer.endScreenPanel.setData(new EndScreenData(controller.hudRenderer,
            getScore(), getRank(),
            maxCombo, goodCuts, getAccuracy()*100,
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
            controller.playRecorder.save();
        } catch (IOException e) {
            Beatcraft.LOGGER.error("Error saving recording", e);
        }

        unloadAll();
    }

}
