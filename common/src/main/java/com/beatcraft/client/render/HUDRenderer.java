package com.beatcraft.client.render;

import com.beatcraft.Beatcraft;
import com.beatcraft.client.BeatcraftClient;
import com.beatcraft.client.animation.Easing;
import com.beatcraft.client.beatmap.BeatmapController;
import com.beatcraft.client.beatmap.data.NoteType;
import com.beatcraft.client.logic.Rank;
import com.beatcraft.client.menu.*;
import com.beatcraft.client.render.menu.*;
import com.beatcraft.client.render.particle.BeatcraftParticleRenderer;
import com.beatcraft.client.render.particle.MenuPointerParticle;
import com.beatcraft.client.render.particle.ScoreDisplay;
import com.beatcraft.common.memory.MemoryPool;
import com.beatcraft.common.utils.MathUtil;
import com.beatcraft.mixin_utils.BufferBuilderAccessor;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector2f;
import org.joml.Vector3f;

public class HUDRenderer {

    public enum MenuScene {
        InGame,
        SongSelect,
        MainMenu,
        Settings,
        Downloader,
        EndScreen,
        ConfirmSongDelete,
        Paused,
        SaberPreview,
    }

    public static MultiBufferSource.BufferSource buffers;
    private static final Font.DisplayMode TEXT_LAYER = Font.DisplayMode.NORMAL;

    public static final int TEXT_COLOR = 0xFFFFFFFF;
    public static final int TEXT_LIGHT = 255;

    public final BeatmapController controller;

    public final Matrix4f leftPanelTransform = new Matrix4f().translate(3, 1, 0).rotateZ((float) Math.PI);
    public final Matrix4f rightPanelTransform = new Matrix4f().translate(-3, 1, 0).rotateZ((float) Math.PI);
    public final Matrix4f healthBarTransform = new Matrix4f().translate(0, -1.5f, 0).rotateZ((float) Math.PI);

    public NoteType pointerSaber = NoteType.BLUE;

    public boolean triggerPressed = false;
    public boolean triggerWasPressed = false;

    public boolean showHUD = true;
    public boolean advancedHUD = true;

    // Menu Panels
    public final SongSelectMenu songSelectMenu;
    public SongSelectMenuPanel songSelectMenuPanel = null;
    public ErrorMessagePanel errorMessagePanel;

    public final PauseScreenPanel pauseScreenPanel;

    public final ModifierMenu modifierMenu;
    public final ModifierMenuPanel modifierMenuPanel;

    public final EndScreenPanel endScreenPanel;

    public ConfirmSongDeleteMenuPanel confirmSongDeleteMenuPanel = null;

    private final SongDownloaderMenuPanel songDownloaderMenuPanel;

    private final SettingsMenuPanel settingsMenuPanel;

    private final CreditsPanel creditsPanel;

    public boolean showKeyboard = false;
    private final KeyboardMenu keyboardData;
    public final KeyboardPanel keyboard;

    public HUDRenderer(BeatmapController controller) {
        this.controller = controller;
        pauseScreenPanel = new PauseScreenPanel(this);
        modifierMenu = new ModifierMenu(this);
        modifierMenuPanel = new ModifierMenuPanel(modifierMenu);
        songSelectMenu = new SongSelectMenu(this);
        errorMessagePanel = new ErrorMessagePanel(new ErrorMessageMenu(this));
        endScreenPanel = new EndScreenPanel(new EndScreenData(this, 0, Rank.A, 0, 0, 0, 0));
        songDownloaderMenuPanel = new SongDownloaderMenuPanel(this);
        settingsMenuPanel = new SettingsMenuPanel(this);
        creditsPanel = new CreditsPanel(this);
        keyboardData = new KeyboardMenu(this, null);
        keyboard = new KeyboardPanel(keyboardData);
    }


    public void hookToKeyboard(TextInput input) {
        keyboardData.input = input;
        showKeyboard = true;
        errorMessagePanel.close();
    }

    public void hideKeyboard() {
        showKeyboard = false;
    }

    public void initSongSelectMenuPanel() {
        songSelectMenuPanel = new SongSelectMenuPanel(songSelectMenu);
    }

    public void postScore(int score, Vector3f position, Vector3f endpoint, Quaternionf orientation) {
        if (!showHUD) return;
        BeatcraftParticleRenderer.addParticle(new ScoreDisplay(score, position, endpoint, orientation));
    }

    public void render(MultiBufferSource imm) {
        buffers = (MultiBufferSource.BufferSource) imm;

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(true);



        switch (controller.scene) {
            case InGame -> {
                renderGameHud();
            }
            case SongSelect -> {
                renderSongSelectHud();
            }
            case Settings -> {
                renderSettings();
            }
            case MainMenu -> {
            }
            case Downloader -> {
                renderDownloader();
            }
            case EndScreen -> {
                renderEndScreen();
            }
            case ConfirmSongDelete -> {
                renderConfirmSongDelete();
            }
            case Paused -> {
                renderPauseScreen();
            }
            case SaberPreview -> {
                renderSaberPreviewScreen();
            }
        }


        RenderSystem.disableBlend();
        RenderSystem.enableCull();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(true);


    }

    public boolean isTriggerPressed() {
        boolean res = triggerPressed && !triggerWasPressed;
        triggerWasPressed = triggerPressed;
        return res;
    }


    public void renderGameHud() {

        if ((!showHUD) || controller.isModifierActive("Zen Mode")) {
            renderTime(null, null, null, null);
            return;
        }


        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder buffer = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

        Font textRenderer = Minecraft.getInstance().font;
        Vector3f cameraPos = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition().toVector3f();
        PoseStack matrices = new PoseStack();

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(false);

        var wp = controller.worldPosition;
        var wr = controller.worldAngle;
        matrices.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);
        matrices.translate(wp.x, wp.y, wp.z);
        matrices.mulPose(new Quaternionf().rotationY(wr));

        matrices.translate(0, 0, 7.99);

        matrices.pushPose();
        matrices.mulPose(leftPanelTransform);
        matrices.scale(1f/32f, 1f/32f, 1f/32f);
        renderRank(matrices, textRenderer, buffer, cameraPos);
        renderCombo(matrices, textRenderer, buffer, cameraPos);
        renderScore(matrices, textRenderer, buffer, cameraPos);
        renderAccuracy(matrices, textRenderer, buffer, cameraPos);
        matrices.popPose();

        matrices.pushPose();
        matrices.mulPose(rightPanelTransform);
        matrices.scale(1f/32f, 1f/32f, 1f/32f);
        renderModifier(matrices, textRenderer, buffer, cameraPos);
        renderTime(matrices, textRenderer, buffer, cameraPos);
        matrices.popPose();

        var buff = buffer.build();
        if (buff == null) return;

        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
        buff.sortQuads(((BufferBuilderAccessor) buffer).beatcraft$getAllocator(), VertexSorting.DISTANCE_TO_ORIGIN);
        BufferUploader.drawWithShader(buff);
        RenderSystem.disableDepthTest();
        RenderSystem.enableCull();
        RenderSystem.disableBlend();


        matrices.pushPose();
        matrices.mulPose(healthBarTransform);
        matrices.scale(1f/32f, 1f/32f, 1f/32f);
        renderPlayerHealth(matrices, textRenderer, cameraPos);
        matrices.popPose();

    }

    private void renderSongSelectHud() {
        if (songSelectMenuPanel == null) return;

        var saberPos = pointerSaber == NoteType.BLUE ? controller.logic.rightSaberPos : controller.logic.leftSaberPos;
        var saberRot = pointerSaber == NoteType.BLUE ? controller.logic.rightSaberRotation : controller.logic.leftSaberRotation;

        var tp = isTriggerPressed();

        var pair = songSelectMenuPanel.raycast(saberPos, saberRot);

        Vector2f local = null;

        if (pair != null) {
            spawnMenuPointerParticle(pair.getA(), songSelectMenuPanel.getNormal());

            local = pair.getB();
        }

        songSelectMenuPanel.render(buffers, local, tp);

        pair = modifierMenuPanel.raycast(saberPos, saberRot);

        if (pair == null) {
            local = null;
        } else {
            spawnMenuPointerParticle(pair.getA(), modifierMenuPanel.getNormal());
            local = pair.getB();
        }

        modifierMenuPanel.render(buffers, local, tp);


        pair = creditsPanel.raycast(saberPos, saberRot);

        if (pair == null) {
            local = null;
        } else {
            spawnMenuPointerParticle(pair.getA(), creditsPanel.getNormal());
            local = pair.getB();
        }

        creditsPanel.render(buffers, local, tp);

        if (errorMessagePanel.shouldDisplay()) {
            pair = errorMessagePanel.raycast(saberPos, saberRot);

            local = null;

            if (pair != null) {
                spawnMenuPointerParticle(pair.getA(), errorMessagePanel.getNormal());

                local = pair.getB();
            }

            errorMessagePanel.render(buffers, local, tp);
        }

    }

    private void renderSettings() {
        var saberPos = pointerSaber == NoteType.BLUE ? controller.logic.rightSaberPos : controller.logic.leftSaberPos;
        var saberRot = pointerSaber == NoteType.BLUE ? controller.logic.rightSaberRotation : controller.logic.leftSaberRotation;

        var tp = isTriggerPressed();

        var pair = settingsMenuPanel.raycast(saberPos, saberRot);

        Vector2f local = null;

        if (pair != null) {
            spawnMenuPointerParticle(pair.getA(), settingsMenuPanel.getNormal());

            local = pair.getB();
        }

        settingsMenuPanel.render(buffers, local, tp);

        if (errorMessagePanel.shouldDisplay()) {
            pair = errorMessagePanel.raycast(saberPos, saberRot);

            local = null;

            if (pair != null) {
                spawnMenuPointerParticle(pair.getA(), errorMessagePanel.getNormal());

                local = pair.getB();
            }

            errorMessagePanel.render(buffers, local, tp);
        }

        pair = modifierMenuPanel.raycast(saberPos, saberRot);

        if (pair == null) {
            local = null;
        } else {
            spawnMenuPointerParticle(pair.getA(), modifierMenuPanel.getNormal());
            local = pair.getB();
        }

        modifierMenuPanel.render(buffers, local, tp);

        pair = creditsPanel.raycast(saberPos, saberRot);

        if (pair == null) {
            local = null;
        } else {
            spawnMenuPointerParticle(pair.getA(), creditsPanel.getNormal());
            local = pair.getB();
        }

        creditsPanel.render(buffers, local, tp);


    }

    private void renderDownloader() {
        var saberPos = pointerSaber == NoteType.BLUE ? controller.logic.rightSaberPos : controller.logic.leftSaberPos;
        var saberRot = pointerSaber == NoteType.BLUE ? controller.logic.rightSaberRotation : controller.logic.leftSaberRotation;

        var tp = isTriggerPressed();

        var pair = songDownloaderMenuPanel.raycast(saberPos, saberRot);
        Vector2f local = null;
        if (pair != null) {
            spawnMenuPointerParticle(pair.getA(), songDownloaderMenuPanel.getNormal());
            local = pair.getB();
        }
        songDownloaderMenuPanel.render(buffers, local, tp);

        pair = modifierMenuPanel.raycast(saberPos, saberRot);
        if (pair == null) {
            local = null;
        } else {
            spawnMenuPointerParticle(pair.getA(), modifierMenuPanel.getNormal());
            local = pair.getB();
        }

        modifierMenuPanel.render(buffers, local, tp);
        pair = creditsPanel.raycast(saberPos, saberRot);
        if (pair == null) {
            local = null;
        } else {
            spawnMenuPointerParticle(pair.getA(), creditsPanel.getNormal());
            local = pair.getB();
        }
        creditsPanel.render(buffers, local, tp);

        if (errorMessagePanel.shouldDisplay()) {
            pair = errorMessagePanel.raycast(saberPos, saberRot);
            local = null;
            if (pair != null) {
                spawnMenuPointerParticle(pair.getA(), errorMessagePanel.getNormal());
                local = pair.getB();
            }
            errorMessagePanel.render(buffers, local, tp);
        } else if (showKeyboard) {
            pair = keyboard.raycast(saberPos, saberRot);
            local = null;
            if (pair != null) {
                spawnMenuPointerParticle(pair.getA(), keyboard.getNormal());
                local = pair.getB();
            }
            keyboard.render(buffers, local, tp);
        }

    }

    private void renderEndScreen() {

        var saberPos = pointerSaber == NoteType.BLUE ? controller.logic.rightSaberPos : controller.logic.leftSaberPos;
        var saberRot = pointerSaber == NoteType.BLUE ? controller.logic.rightSaberRotation : controller.logic.leftSaberRotation;

        var pair = endScreenPanel.raycast(saberPos, saberRot);

        Vector2f local = null;

        if (pair != null) {
            spawnMenuPointerParticle(pair.getA(), endScreenPanel.getNormal());
            local = pair.getB();
        }

        endScreenPanel.render(buffers, local, isTriggerPressed());
    }

    private void renderConfirmSongDelete() {
        if (confirmSongDeleteMenuPanel == null) return;
        var saberPos = pointerSaber == NoteType.BLUE ? controller.logic.rightSaberPos : controller.logic.leftSaberPos;
        var saberRot = pointerSaber == NoteType.BLUE ? controller.logic.rightSaberRotation : controller.logic.leftSaberRotation;

        var pair = confirmSongDeleteMenuPanel.raycast(saberPos, saberRot);

        Vector2f local = null;

        if (pair != null) {
            spawnMenuPointerParticle(pair.getA(), confirmSongDeleteMenuPanel.getNormal());
            local = pair.getB();
        }

        confirmSongDeleteMenuPanel.render(buffers, local, isTriggerPressed());
    }

    private void renderPauseScreen() {
        var saberPos = pointerSaber == NoteType.BLUE ? controller.logic.rightSaberPos : controller.logic.leftSaberPos;
        var saberRot = pointerSaber == NoteType.BLUE ? controller.logic.rightSaberRotation : controller.logic.leftSaberRotation;

        var pair = pauseScreenPanel.raycast(saberPos, saberRot);

        Vector2f local = null;

        if (pair != null) {
            spawnMenuPointerParticle(pair.getA(), pauseScreenPanel.getNormal());
            local = pair.getB();
        }

        pauseScreenPanel.render(buffers, local, isTriggerPressed());
    }

    private void renderSaberPreviewScreen() {
        var saberPos = pointerSaber == NoteType.BLUE ? controller.logic.rightSaberPos : controller.logic.leftSaberPos;
        var saberRot = pointerSaber == NoteType.BLUE ? controller.logic.rightSaberRotation : controller.logic.leftSaberRotation;

        var pair = modifierMenuPanel.raycast(saberPos, saberRot);

        Vector2f local;

        if (pair == null) {
            local = null;
        } else {
            spawnMenuPointerParticle(pair.getA(), modifierMenuPanel.getNormal());
            local = pair.getB();
        }

        modifierMenuPanel.render(buffers, local, isTriggerPressed());



    }

    private void spawnMenuPointerParticle(Vector3f position, Vector3f normal) {
        BeatcraftParticleRenderer.addParticle(new MenuPointerParticle(position.add(normal.mul(0.01f, new Vector3f()), new Vector3f()), normal));
    }

    private void renderRank(PoseStack matrices, Font textRenderer, BufferBuilder buffer, Vector3f cameraPos) {

        String rank = controller.logic.getRank().toString();

        int w = textRenderer.width(rank);

        matrices.pushPose();
        matrices.scale(2, 2, 2);

        textRenderer.drawInBatch(
            Component.literal(rank),
            -w/2f, 12, TEXT_COLOR, false,
            matrices.last().pose(), buffers,
            TEXT_LAYER, 0, TEXT_LIGHT
        );

        matrices.popPose();

    }

    private void renderCombo(PoseStack matrices, Font textRenderer, BufferBuilder buffer, Vector3f cameraPos) {
        Matrix4f pose = new Matrix4f(matrices.last().pose());
        pose.scale(-32f);

        var txt = Component.translatable("hud.beatcraft.combo");
        int w = textRenderer.width(txt.getString());

        textRenderer.drawInBatch(
            txt,
            -w / 2f, -28f, TEXT_COLOR, false,
            matrices.last().pose(), buffers,
            TEXT_LAYER, 0, TEXT_LIGHT
        );

        Vector3f topLineLocal = new Vector3f(0f, -31f, 0f);
        Vector3f bottomLineLocal = new Vector3f(0f, -4f, 0f);

        Vector3f topLine = topLineLocal.mulPosition(pose, new Vector3f());
        Vector3f bottomLine = bottomLineLocal.mulPosition(pose, new Vector3f());

        float op = Easing.easeInOutExpo(controller.logic.getComboBarOpacity());

        if (op > 0) {
            float width = 0.5f * (1f / op);
            int color = 0xFFFFFF + (((int) (255 * op)) << 24);

            // Draw top line
            Vector3f tl0 = new Vector3f(width, 0, 0).add(topLine);
            Vector3f tl1 = new Vector3f(width, 0.05f, 0).add(topLine);
            Vector3f tl2 = new Vector3f(-width, 0.05f, 0).add(topLine);
            Vector3f tl3 = new Vector3f(-width, 0, 0).add(topLine);

            buffer.addVertex(tl0.x, tl0.y, tl0.z).setColor(color);
            buffer.addVertex(tl1.x, tl1.y, tl1.z).setColor(color);
            buffer.addVertex(tl2.x, tl2.y, tl2.z).setColor(color);
            buffer.addVertex(tl3.x, tl3.y, tl3.z).setColor(color);

            // Draw bottom line
            Vector3f bl0 = new Vector3f(width, 0, 0).add(bottomLine);
            Vector3f bl1 = new Vector3f(width, 0.05f, 0).add(bottomLine);
            Vector3f bl2 = new Vector3f(-width, 0.05f, 0).add(bottomLine);
            Vector3f bl3 = new Vector3f(-width, 0, 0).add(bottomLine);

            buffer.addVertex(bl0.x, bl0.y, bl0.z).setColor(color);
            buffer.addVertex(bl1.x, bl1.y, bl1.z).setColor(color);
            buffer.addVertex(bl2.x, bl2.y, bl2.z).setColor(color);
            buffer.addVertex(bl3.x, bl3.y, bl3.z).setColor(color);
        }

        Matrix4f comboPose = new Matrix4f(matrices.last().pose());
        comboPose.scale(1.5f, 1.5f, 1.5f);

        String combo = String.valueOf(controller.logic.getCombo());
        w = textRenderer.width(combo);

        textRenderer.drawInBatch(
            Component.literal(combo),
            -w / 2f, -12f, TEXT_COLOR, false,
            comboPose, buffers,
            TEXT_LAYER, 0, TEXT_LIGHT
        );
    }


    private void renderScore(PoseStack matrices, Font textRenderer, BufferBuilder buffer, Vector3f cameraPos) {

        String score = String.valueOf(controller.logic.getScore());

        int w = textRenderer.width(score);

        matrices.pushPose();

        matrices.scale(1.2f, 1.2f, 1.2f);
        textRenderer.drawInBatch(
            Component.literal(score),
            -w/2f, 2, TEXT_COLOR, false,
            matrices.last().pose(), buffers,
            TEXT_LAYER, 0, TEXT_LIGHT
        );


        matrices.popPose();

    }

    private void renderAccuracy(PoseStack matrices, Font textRenderer, BufferBuilder buffer, Vector3f cameraPos) {

        String accuracy = String.format("%.1f", controller.logic.getAccuracy()*100) + "%";

        int w = textRenderer.width(accuracy);

        matrices.pushPose();

        matrices.scale(0.8f, 0.8f, 0.8f);
        textRenderer.drawInBatch(
            Component.literal(accuracy),
            -w/2f, 18, TEXT_COLOR, false,
            matrices.last().pose(), buffers,
            TEXT_LAYER, 0, TEXT_LIGHT
        );

        matrices.popPose();

    }

    public void drawArc(Matrix4f pose, Vector3f center, Vector3f[] innerArc, BufferBuilder buffer, int color) {
        for (int i = 0; i < innerArc.length - 1; i++) {
            Vector3f a = new Vector3f(innerArc[i]);
            Vector3f b = new Vector3f(innerArc[i + 1]);
            Vector3f c = a.sub(center, new Vector3f()).normalize().mul(0.05f).add(a);
            Vector3f d = b.sub(center, new Vector3f()).normalize().mul(0.05f).add(b);

            a.mulPosition(pose);
            b.mulPosition(pose);
            c.mulPosition(pose);
            d.mulPosition(pose);

            buffer.addVertex(a.x, a.y, a.z).setColor(color);
            buffer.addVertex(b.x, b.y, b.z).setColor(color);
            buffer.addVertex(d.x, d.y, d.z).setColor(color);
            buffer.addVertex(c.x, c.y, c.z).setColor(color);
        }
    }


    private void renderModifier(PoseStack matrices, Font textRenderer, BufferBuilder buffer, Vector3f cameraPos) {
        String mod = String.valueOf((int) controller.logic.getBonusModifier());

        textRenderer.drawInBatch(
            Component.literal("x"),
            -8.5f, -20, TEXT_COLOR, false,
            matrices.last().pose(), buffers,
            TEXT_LAYER, 0, TEXT_LIGHT
        );

        matrices.pushPose();
        matrices.scale(2.5f, 2.5f, 2.5f);
        textRenderer.drawInBatch(
            Component.literal(mod),
            -1, -8, TEXT_COLOR, false,
            matrices.last().pose(), buffers,
            TEXT_LAYER, 0, TEXT_LIGHT
        );
        matrices.popPose();

        Matrix4f pose = new Matrix4f(matrices.last().pose());
        pose.scale(-32);

        Vector3f centerLocal = new Vector3f(0f, 0.375f, 0f);
        Vector3f normalLocal = new Vector3f(0, 0, 1);
        float radius = 0.5f;
        float circleProgress = controller.logic.getModifierPercentage();

        if (circleProgress > 0) {
            Vector3f[] arcPoints = MathUtil.generateCircle(
                normalLocal, radius, 2 + (int) (circleProgress * 20),
                centerLocal, 360 * circleProgress, 180
            );
            drawArc(pose, centerLocal, arcPoints, buffer, 0xFFFFFFFF);
        }

        if (circleProgress < 1) {
            Vector3f[] arcPoints = MathUtil.generateCircle(
                normalLocal, radius, 2 + (int) ((1 - circleProgress) * 20),
                centerLocal, 360 * (1 - circleProgress), 180 + 360 * circleProgress
            );
            drawArc(pose, centerLocal, arcPoints, buffer, 0x7F7F7F7F);
        }
    }


    private void renderTime(PoseStack matrices, Font textRenderer, BufferBuilder buffer, Vector3f cameraPos) {
        float songDuration = 0;
        if (controller.info != null) {
            songDuration = controller.info.getSongDuration();
        }
        float t = controller.currentSeconds;

        String currentTime = MathUtil.timeToString((int) t);
        String length = MathUtil.timeToString((int) songDuration);

        float progress = MathUtil.inverseLerp(0, songDuration, t);

        if (progress > 1 && controller.info != null) {
            controller.logic.triggerSongEnd();
        }

        if ((!showHUD) || controller.isModifierActive("Zen Mode")) return;

        String display = currentTime + " | " + length;

        int w = textRenderer.width(display);

        Vector3f leftPos = matrices.last().pose().getTranslation(new Vector3f()).add(0.65f, -0.4f, 0);

        Vector3f rightPos = leftPos.add(-1.3f, 0, 0, new Vector3f());

        Vector3f midPos = MathUtil.lerpVector3(leftPos, rightPos, progress);

        buffer.addVertex(leftPos.x, leftPos.y, leftPos.z).setColor(0xFFFFFFFF);
        buffer.addVertex(leftPos.x, leftPos.y+0.05f, leftPos.z).setColor(0xFFFFFFFF);
        buffer.addVertex(midPos.x, midPos.y+0.05f, midPos.z).setColor(0xFFFFFFFF);
        buffer.addVertex(midPos.x, midPos.y, midPos.z).setColor(0xFFFFFFFF);

        buffer.addVertex(midPos.x, midPos.y, midPos.z).setColor(0x7F7F7F7F);
        buffer.addVertex(midPos.x, midPos.y+0.05f, midPos.z).setColor(0x7F7F7F7F);
        buffer.addVertex(rightPos.x, rightPos.y+0.05f, rightPos.z).setColor(0x7F7F7F7F);
        buffer.addVertex(rightPos.x, rightPos.y, rightPos.z).setColor(0x7F7F7F7F);

        matrices.pushPose();
        matrices.scale(0.5f, 0.5f, 0.5f);
        textRenderer.drawInBatch(
            Component.literal(display),
            -w/2f, 32, TEXT_COLOR, false,
            matrices.last().pose(), buffers,
            TEXT_LAYER,
            0, TEXT_LIGHT
        );

        matrices.popPose();
    }

    private void renderPlayerHealth(PoseStack matrices, Font textRenderer, Vector3f cameraPos) {
        float progress = controller.logic.getHealthPercentage();

        switch (BeatcraftClient.playerConfig.preferences.healthStyle()) {
            case Classic -> {
                Tesselator tesselator = Tesselator.getInstance();
                BufferBuilder buffer = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

                Matrix4f pose = new Matrix4f(matrices.last().pose());
                pose.scale(-32);

                Vector3f leftLocal = new Vector3f(1.35f, 0f, 0f);
                Vector3f rightLocal = new Vector3f(-1.35f, 0f, 0f);

                Vector3f leftPos = leftLocal.mulPosition(pose, new Vector3f());
                Vector3f rightPos = rightLocal.mulPosition(pose, new Vector3f());
                Vector3f midPos = MathUtil.lerpVector3(leftPos, rightPos, progress);

                buffer.addVertex(leftPos.x, leftPos.y, leftPos.z).setColor(0xFFFFFFFF);
                buffer.addVertex(leftPos.x, leftPos.y + 0.05f, leftPos.z).setColor(0xFFFFFFFF);
                buffer.addVertex(midPos.x, midPos.y + 0.05f, midPos.z).setColor(0xFFFFFFFF);
                buffer.addVertex(midPos.x, midPos.y, midPos.z).setColor(0xFFFFFFFF);

                buffer.addVertex(midPos.x, midPos.y, midPos.z).setColor(0x7F7F7F7F);
                buffer.addVertex(midPos.x, midPos.y + 0.05f, midPos.z).setColor(0x7F7F7F7F);
                buffer.addVertex(rightPos.x, rightPos.y + 0.05f, rightPos.z).setColor(0x7F7F7F7F);
                buffer.addVertex(rightPos.x, rightPos.y, rightPos.z).setColor(0x7F7F7F7F);

                var buff = buffer.build();
                if (buff == null) return;

                RenderSystem.setShader(GameRenderer::getPositionColorShader);
                RenderSystem.enableBlend();
                RenderSystem.defaultBlendFunc();
                RenderSystem.disableCull();
                RenderSystem.enableDepthTest();
                buff.sortQuads(((BufferBuilderAccessor) buffer).beatcraft$getAllocator(), VertexSorting.DISTANCE_TO_ORIGIN);
                BufferUploader.drawWithShader(buff);
                RenderSystem.disableDepthTest();
                RenderSystem.enableCull();
                RenderSystem.disableBlend();
                RenderSystem.depthMask(true);
            }
            case Hearts -> {
                var maxHp = controller.logic.maxHealth;
                var hp = controller.logic.health;

                Matrix4f pose = new Matrix4f(matrices.last().pose());
                pose.scale(-32);
                Vector3f pos = new Vector3f(0, 0, 0).mulPosition(pose, new Vector3f());

                var heart_buffer = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);

                boolean doShaking = false;
                boolean inWall = controller.isInWall;

                switch (maxHp) {
                    case 1 -> {
                        doShaking = true;
                        if (hp == 1) drawHeart(heart_buffer, pose, 0, 1, 1, inWall);
                    }
                    case 4 -> {
                        if (hp <= 2) doShaking = true;
                        for (int i = 0; i < 4; i++)
                            drawHeart(heart_buffer, pose, i, 4, progress, inWall);
                    }
                    default -> {
                        if (progress <= 0.275f) doShaking = true;
                        for (int i = 0; i < 10; i++)
                            drawHeart(heart_buffer, pose, i, 10, progress, inWall);
                    }
                }

                var buff = heart_buffer.build();
                if (buff != null) {
                    RenderSystem.setShader(() -> BeatcraftRenderer.heartHealthShader);
                    RenderSystem.setShaderTexture(0, heartsTexture);
                    BeatcraftRenderer.heartHealthShader.safeGetUniform("DoShaking").set(doShaking ? 1f : 0f);
                    BeatcraftRenderer.heartHealthShader.safeGetUniform("GameTime").set(System.nanoTime() / 1_000_000_000f);

                    RenderSystem.enableBlend();
                    RenderSystem.defaultBlendFunc();
                    RenderSystem.disableCull();
                    RenderSystem.enableDepthTest();
                    BufferUploader.drawWithShader(buff);
                    RenderSystem.disableDepthTest();
                    RenderSystem.enableCull();
                    RenderSystem.disableBlend();
                }
            }
        }
    }


    private final Vector2f HEART_SIZE = new Vector2f(0.25f);

    private final ResourceLocation heartsTexture = Beatcraft.id("textures/gui/hearts.png");

    private final Vector2f emptyHeartUV = new Vector2f(18f/36f, 0);

    private Vector2f getHeartUV(boolean hardcore, boolean halfHeart, boolean poisoned, boolean blinking) {
        float x = hardcore ? 9f : 0f;
        x += blinking ? 18f : 0f;

        float y = halfHeart ? 27f : 9f;
        y += poisoned ? 9f : 0f;

        return new Vector2f(x/36f, y/45f);
    }

    private void drawHeart(BufferBuilder buffer, Matrix4f pose, int heartId, int heartCount, float hpPercent, boolean poisoned) {
        float x = ((float) heartId) * HEART_SIZE.x - (((float) heartCount) * HEART_SIZE.x * 0.5f);

        Vector3f v0 = new Vector3f(-x, HEART_SIZE.y / 2f, 0);
        Vector3f v1 = new Vector3f(-x, -HEART_SIZE.y / 2f, 0);
        Vector3f v2 = new Vector3f(-x - HEART_SIZE.x, -HEART_SIZE.y / 2f, 0);
        Vector3f v3 = new Vector3f(-x - HEART_SIZE.x, HEART_SIZE.y / 2f, 0);

        v0.mulPosition(pose);
        v1.mulPosition(pose);
        v2.mulPosition(pose);
        v3.mulPosition(pose);

        float filled = hpPercent * heartCount * 2f - heartId * 2f;

        Vector2f uv = (filled <= 0)
            ? emptyHeartUV
            : getHeartUV(heartCount <= 4, filled <= 1, poisoned, false);

        buffer.addVertex(v0).setUv(uv.x, uv.y).setColor(heartId, 0, 0, 0);
        buffer.addVertex(v1).setUv(uv.x, uv.y + 9f / 45f).setColor(heartId, 0, 0, 0);
        buffer.addVertex(v2).setUv(uv.x + 9f / 36f, uv.y + 9f / 45f).setColor(heartId, 0, 0, 0);
        buffer.addVertex(v3).setUv(uv.x + 9f / 36f, uv.y).setColor(heartId, 0, 0, 0);
    }



}
