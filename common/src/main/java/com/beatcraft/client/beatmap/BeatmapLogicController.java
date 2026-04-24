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
import com.beatcraft.client.logic.PhysicsTransform;
import com.beatcraft.client.logic.Rank;
import com.beatcraft.client.menu.EndScreenData;
import com.beatcraft.client.render.DebugRenderer;
import com.beatcraft.client.render.HUDRenderer;
import com.beatcraft.client.render.particle.ScoreDisplay;
import com.beatcraft.common.items.ModItems;
import com.beatcraft.common.utils.MathUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.io.IOException;
import java.util.ArrayList;

public class BeatmapLogicController {

    // this is specifically NOT static, so that it's set based on when the logic controller
    // is made instead of when the class first loads
    private final double START_TIME = System.nanoTime() / 1_000_000_000d;

    public static final Vector3f SABER_TIP_OFFSET = new Vector3f(0, 1, 0);

    protected static class HitCalculation {
        private static final double TIMEOUT = 0.5;

        private static final ArrayList<HitCalculation> cache = new ArrayList<>();

        protected Matrix4f start;
        protected Matrix4f hit;
        protected double hitTime;
        protected float accuracyScore;

        protected ScoreDisplay.ScoreLink scoreLink;
        protected int score;
        protected int maxScore;

        protected float swingInMaxScore;
        protected float swingOutMaxScore;

        protected float swingInMaxAngle;
        protected float swingOutMaxAngle;
        protected NoteType noteType;

        protected static HitCalculation create(
            Matrix4f start, Matrix4f hit, double hitTime,
            float accuracy, float swingInMaxScore, float swingOutMaxScore,
            float swingInMaxAngle, float swingOutMaxAngle,
            NoteType noteType, ScoreDisplay.ScoreLink link, int maxScore
        ) {
            if (!cache.isEmpty()) {
                var x = cache.removeLast();
                x.start.set(start);
                x.hit.set(hit);
                x.hitTime = hitTime;
                x.accuracyScore = accuracy;
                x.swingInMaxScore = swingInMaxScore;
                x.swingOutMaxScore = swingOutMaxScore;
                x.swingInMaxAngle = swingInMaxAngle * Mth.DEG_TO_RAD;
                x.swingOutMaxAngle = swingOutMaxAngle * Mth.DEG_TO_RAD;
                x.noteType = noteType;
                x.scoreLink = link;
                x.maxScore = maxScore;
                x.updateScore(x.hit, x.hitTime);
                return x;
            } else {
                return new HitCalculation(
                    new Matrix4f(start), new Matrix4f(hit), hitTime,
                    accuracy, swingInMaxScore, swingOutMaxScore,
                    swingInMaxAngle * Mth.DEG_TO_RAD,
                    swingOutMaxAngle * Mth.DEG_TO_RAD,
                    noteType, link, maxScore
                );
            }
        }

        private HitCalculation(
            Matrix4f start, Matrix4f hit, double hitTime,
            float accuracy, float swingInMaxScore, float swingOutMaxScore,
            float swingInMaxAngle, float swingOutMaxAngle,
            NoteType noteType, ScoreDisplay.ScoreLink link,
            int maxScore
        ) {
            this.start = start;
            this.hit = hit;
            this.hitTime = hitTime;
            this.score = 0;
            this.accuracyScore = accuracy;
            this.swingInMaxScore = swingInMaxScore;
            this.swingOutMaxScore = swingOutMaxScore;
            this.swingInMaxAngle = swingInMaxAngle;
            this.swingOutMaxAngle = swingOutMaxAngle;
            this.noteType = noteType;
            this.scoreLink = link;
            this.maxScore = maxScore;

            updateScore(hit, hitTime);
        }

        protected void cleanup() {
            cache.add(this);
        }

        private final Vector3f startV3 = new Vector3f();
        private final Vector3f hitV3 = new Vector3f();
        private final Vector3f crossV3 = new Vector3f();
        private final Vector3f currentV3 = new Vector3f();
        protected boolean updateScore(Matrix4f current, double time) {
            double elapsed = time - hitTime;
            var exitDt = false;
            var outMod = 1f;
            if (elapsed > TIMEOUT) {
                exitDt = true;
                outMod = (float) (TIMEOUT/elapsed);
            }
            start.getColumn(1, startV3).normalize();
            hit.getColumn(1, hitV3).normalize();
            current.getColumn(1, currentV3).normalize();

            float dotSH = startV3.dot(hitV3);
            startV3.cross(hitV3, crossV3);
            float angleIn = (float) Math.atan2(crossV3.length(), dotSH);

            var dotHC = hitV3.dot(currentV3);
            hitV3.cross(currentV3, crossV3);
            float angleOut = (float) Math.atan2(crossV3.length(), dotHC) * outMod;

            var updatedScore = (int) (scoreFromAngle(angleIn, swingInMaxAngle, swingInMaxScore)
                + scoreFromAngle(angleOut, swingOutMaxAngle, swingOutMaxScore)
                + accuracyScore);

            if (updatedScore < score) {
                return true;
            }
            score = updatedScore;
            scoreLink.set(score);

            return exitDt;
        }

        private float scoreFromAngle(float angle, float maxAngle, float maxScore) {
            if (maxAngle <= 0) return 0;
            float t = Math.abs(angle) / maxAngle;
            return Math.min(maxScore, t * maxScore);
        }

    }

    private final BeatmapController controller;

    public Vector3f headPos = new Vector3f();
    public Quaternionf headRot = new Quaternionf();

    public PhysicsTransform leftSaber = new PhysicsTransform(0, 0, 0);
    public PhysicsTransform rightSaber = new PhysicsTransform(0, 0, 0);

    private final Matrix4f lastRight = new Matrix4f();
    private final Matrix4f currentRight = new Matrix4f();

    private final Matrix4f lastLeft = new Matrix4f();
    private final Matrix4f currentLeft = new Matrix4f();

    public Vector3f rightSaberPos = new Vector3f();
    public Quaternionf rightSaberRotation = new Quaternionf();

    public Vector3f leftSaberPos = new Vector3f();
    public Quaternionf leftSaberRotation = new Quaternionf();

    public Vector3f playerGlobalPosition = new Vector3f();
    public Quaternionf playerGlobalRotation = new Quaternionf();

    private final ArrayList<HitCalculation> hits = new ArrayList<>();

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

    private double failTime = 0;
    private static final double DISSOLVE_TIME = 2.5;


    public BeatmapLogicController(BeatmapController player) {
        controller = player;
    }

    public int getCombo() {
        return combo;
    }

    public int getMaxPossibleScore() {
        return maxPossibleScore;
    }

    public int getScore() {
        return score;
    }

    public float getAccuracy() {
        if (maxPossibleScore == 0) return 1;
        return score / (float) maxPossibleScore;
    }

    public float getBonusModifier() {
        return bonusModifier;
    }

    public float getHealthPercentage() {
        return health / maxHealth;
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

        leftSaber = left;
        rightSaber = right;

        var rightHanded = player.getMainArm() == HumanoidArm.RIGHT;
        var leftHand = rightHanded ? EquipmentSlot.OFFHAND : EquipmentSlot.MAINHAND;
        var rightHand = rightHanded ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND;
        var leftItem = player.getItemBySlot(leftHand).is(ModItems.SABER_ITEM);
        var rightItem = player.getItemBySlot(rightHand).is(ModItems.SABER_ITEM);
        if (leftItem) {
            left.copy(lastLeft, currentLeft);
            left.getPosition(leftSaberPos);
            left.getRotation(leftSaberRotation);
        } else {
            currentLeft.translation(1, 600, 0);
        }

        if (rightItem) {
            right.copy(lastRight, currentRight);
            right.getPosition(rightSaberPos);
            right.getRotation(rightSaberRotation);
        } else {
            currentRight.translation(-1, -600, 0);
        }

        head.getPosition(headPos);
        head.getRotation(headRot);

        return true;
    }

    private final ArrayList<HitCalculation> toRemove = new ArrayList<>();
    public void lateUpdate(double deltaTime) {
        var currentTime = (System.nanoTime() / 1_000_000_000d) - START_TIME;

        for (var hit : hits) {
            var pos = hit.noteType == NoteType.BLUE
                ? currentRight
                : currentLeft;

            if (hit.updateScore(pos, currentTime)) {
                toRemove.add(hit);
            }
        }
        for (var r : toRemove) {
            processGoodCut(r.score, r.maxScore);
            hits.remove(r);
        }
        toRemove.clear();

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

    private static final Vector3f scratch = new Vector3f();
    private static final Vector3f wp = new Vector3f();
    public void checkNote(PhysicalGameplayObject<? extends GameplayObject> obj) {

        obj.getWorldPos(wp.zero());
        var cd = 1.2 + obj.getCollisionDistance(); // 1.2 == distance that extends beyond the length of the saber

        if (rightSaber.getPosition(scratch).distance(wp) <= cd) {
            checkSaber(obj, rightSaber, NoteType.BLUE);
        }

        if (leftSaber.getPosition(scratch).distance(wp) <= cd) {
            checkSaber(obj, leftSaber, NoteType.RED);
        }

    }

    private final Vector3f localBase = new Vector3f();
    private final Vector3f localTip = new Vector3f();
    private final Vector3f vel = new Vector3f();
    private final Vector3f localVel = new Vector3f();
    private void processNote(PhysicalScorableObject obj, Hitbox gc, Hitbox bc, NoteType expectedNoteType, PhysicsTransform saber) {
        var cd = obj.score$getCutDirection();
        if (obj.score$getData().score$getNoteType() == expectedNoteType) {
            if (cd == CutDirection.DOT) {
                if (gc.checkCollision(localBase, localTip)) {
                    calculateScore(obj, saber, expectedNoteType);
                    obj.score$cutNote();
                    getPlaneNormal(localBase, localTip, vel, dir);
                    obj.score$spawnDebris(new Vector3f(localBase), dir);
                }
            } else {
                if (isPointingDown(localVel)) {
                    if (gc.checkCollision(localBase, localTip)) {
                        calculateScore(obj, saber, expectedNoteType);
                        obj.score$cutNote();
                        getPlaneNormal(localBase, localTip, vel, dir);
                        obj.score$spawnDebris(new Vector3f(localBase), dir);
                    }
                } else {
                    if (bc.checkCollision(localBase, localTip)) {
                        obj.score$setScoreState(ScoreState.badCut());
                        obj.score$cutNote();
                        getPlaneNormal(localBase, localTip, vel, dir);
                        obj.score$spawnDebris(new Vector3f(localBase), dir);
                        processBadCut(obj.score$getMaxFollowThroughScore() + obj.score$getMaxSwingInScore() + 15);
                        var vel = obj.score$getInverseVelocity().mul(4);
                        var pos = ((PhysicalGameplayObject<?>) obj).getWorldTransform().getTranslation(new Vector3f());
                        pos.y = controller.worldPosition.y;
                        var end = pos.add(vel, new Vector3f());
                        controller.hudRenderer.postBadcut(
                            pos,
                            end,
                            obj.score$getLaneRotation().rotateY(-controller.worldAngle, new Quaternionf())
                        );
                    }
                }
            }
        } else {
            if (bc.checkCollision(localBase, localTip)) {
                obj.score$setScoreState(ScoreState.badCut());
                obj.score$cutNote();
                getPlaneNormal(localBase, localTip, vel, dir);
                obj.score$spawnDebris(new Vector3f(localBase), dir);
                processBadCut(obj.score$getMaxFollowThroughScore() + obj.score$getMaxSwingInScore() + 15);
                var vel = obj.score$getInverseVelocity().mul(4);
                var pos = ((PhysicalGameplayObject<?>) obj).getWorldTransform().getTranslation(new Vector3f());
                pos.y = controller.worldPosition.y;
                var end = pos.add(vel, new Vector3f());
                controller.hudRenderer.postBadcut(
                    pos,
                    end,
                    obj.score$getLaneRotation().rotateY(-controller.worldAngle, new Quaternionf())
                );
            }
        }
    }

    private final Matrix4f invTransform = new Matrix4f();
    private final Quaternionf q0 = new Quaternionf();
    private final Vector3f pos = new Vector3f();
    private final Vector3f tipPos = new Vector3f();
    private void checkSaber(PhysicalGameplayObject<? extends GameplayObject> obj, PhysicsTransform saber, NoteType expectedNoteType) {

        var t = obj.getWorldTransform();
        saber.getPositionalVelocity(1, SABER_TIP_OFFSET, vel);
        t.invert(invTransform);

        saber.getPosition(pos);
        saber.getPosition(SABER_TIP_OFFSET, tipPos);

        invTransform.transformPosition(pos, localBase);
        invTransform.transformPosition(tipPos, localTip);
        vel.rotate(invTransform.getNormalizedRotation(q0), localVel);

        var gc = obj.getGoodCutBounds();
        var bc = obj.getBadCutBounds();
        var ac = obj.getAccurateHitbox();

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
            if (scorable.score$getScoreState().isUnchecked()) {
                processNote(scorable, gc, bc, expectedNoteType, saber);
            }
        } else if (obj instanceof PhysicalBombNote bomb) {
            if (bc.isPointInHitbox(localTip)) {
                obj.cutNote();
                processBombCut();
                var vel = bomb.getInverseVelocity().mul(4);
                controller.hudRenderer.postBadcut(
                    new Vector3f(controller.worldPosition),
                    controller.worldPosition.add(vel, new Vector3f()),
                    bomb.getLaneRotation().rotateY(-controller.worldAngle, new Quaternionf())
                );
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

        return dot >= 0.7071f; // sqrt(0.5)
    }

    private void calculateScore(PhysicalScorableObject colorNote, PhysicsTransform saber, NoteType noteType) {

        var base = saber.getPosition(new Vector3f()).sub(controller.worldPosition).rotateY(-controller.worldAngle);
        var tip = saber.getPosition(SABER_TIP_OFFSET, new Vector3f()).sub(controller.worldPosition).rotateY(-controller.worldAngle);
        var velocity = saber.getPositionalVelocity(0.01f, SABER_TIP_OFFSET, new Vector3f());

        var normal = getPlaneNormal(base, tip, velocity, new Vector3f());
        var dist = distanceToOrigin(tip, normal);
        int accPoints = (int) Math.clamp(
            15 * (1 - MathUtil.inverseLerp(0, 1f, dist)),
            0, 15
        );

        int maxScore = colorNote.score$getMaxCutPositionScore()
            + colorNote.score$getMaxSwingInScore()
            + colorNote.score$getMaxFollowThroughScore();

        colorNote.score$setScoreState(ScoreState.goodCut(accPoints));
        var vel = colorNote.score$getInverseVelocity().mul(4);
        var pos = ((PhysicalGameplayObject<?>) colorNote).getWorldTransform().getTranslation(new Vector3f());
        pos.y = controller.worldPosition.y;
        var end = pos.add(vel, new Vector3f());
        var link = controller.hudRenderer.postScore(
            accPoints,
            pos,
            end,
            colorNote.score$getLaneRotation().rotateY(-controller.worldAngle, new Quaternionf())
        );
        incrementCombo();
        hits.add(HitCalculation.create(
            saber.getTurnaround(new Matrix4f()),
            saber.copyCurrent(new Matrix4f()),
            System.nanoTime() / 1_000_000_000d,
            (float) accPoints,
            colorNote.score$getMaxSwingInScore(),
            colorNote.score$getMaxFollowThroughScore(),
            colorNote.score$getMaxSwingInAngle(),
            colorNote.score$getMaxFollowThroughAngle(),
            noteType,
            link, maxScore
        ));
    }

    private static final Vector3f localHeadPos = new Vector3f();
    public void checkObstacle(PhysicalObstacle obstacle, Vector3f position, Quaternionf rotation) {

        var hitbox = obstacle.getBounds();

        rotation.invert(q0);

        checkSaberAgainstObstacle(hitbox, position, q0, rightSaber, NoteType.BLUE);
        checkSaberAgainstObstacle(hitbox, position, q0, leftSaber, NoteType.RED);

        headPos.sub(position, localHeadPos).rotate(q0);

        if (!controller.isInWall) {
            controller.isInWall = hitbox.isPointInHitbox(localHeadPos);
        }

    }

    private static void checkSaberAgainstObstacle(Hitbox hitbox, Vector3f position, Quaternionf inv, PhysicsTransform transform, NoteType saber) {
        var saberPos = transform.getPosition(new Vector3f());
        var localTip = transform.getPosition(SABER_TIP_OFFSET, new Vector3f());

        saberPos.sub(position).rotate(inv);
        localTip.sub(position).rotate(inv);

        if (hitbox.checkCollision(saberPos, localTip)) {
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
        return Math.abs(planeNormal.dot(planeIncident));
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
        maxPossibleModifier = 1;
        maxModifierProgress = 0;
    }

    public void processGoodCut(int score, int maxScore) {
        addGoodCut();
        addScore(score, maxScore);

    }

    public void processBombCut() {
        addBombCut();
        breakCombo();
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

    public void addBombCut() {
        if (failed && noFail) return;
        processDamage(BOMB_HP);
        checkFail();
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

    private int maxPossibleModifier = 1;
    private int maxModifierProgress = 0;
    public void addScore(int earned, int possible) {
        score += earned * bonusModifier;
        maxPossibleScore += possible * maxPossibleModifier;
        if (maxPossibleModifier < 8) {
            maxModifierProgress++;
            if (maxModifierProgress == maxPossibleModifier) {
                maxPossibleModifier *= 2;
                maxModifierProgress = 0;
            }
        }
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
